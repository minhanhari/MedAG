package com.minhanh.coreg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import static org.neo4j.driver.Values.parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@RestController
public class ApplicationController implements AutoCloseable {

    private Driver driver;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    @GetMapping("/")
    public ModelAndView rootView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("index");
        return mav;
    }

    @GetMapping("/search")
    public ModelAndView coRegulationView(@RequestParam Map<String, String> allParams) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(!"".equals(allParams.get("genes")) ? "co-regulation" : "medical-records");
        return mav;
    }

    @GetMapping(value = "/graph", produces = "application/json")
    public Map<String, ?> getCoregulation(@RequestParam Map<String, String> allParams) {
        String[] genes_list = allParams.get("genes").split("\\R");
        byte min = Byte.parseByte(allParams.get("coregulation"));

        return coregulatingGraph(genes_list, min);
    }

    private Map<String, ?> coregulatingGraph(String[] genes_list, byte min) {
        String uri = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "co-regulation";

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password)); // 1
        driver.verifyConnectivity(); // 2

        try (var session = driver.session(SessionConfig.builder().withDatabase("neo4j").build())) {
            return session.executeRead(tx -> {
                LocalTime start = LocalTime.now();
                var query = new Query(
                        """
                                MATCH (t1:TransFactor)-[:REGULATES]->(a:Gene)<-[:REGULATES]-(t2:TransFactor)
                                WHERE a.symbol IN $list AND t1 <> t2
                                WITH t1, t2, count(DISTINCT a) AS n
                                WHERE n >= $min
                                RETURN t1.symbol AS u, t2.symbol AS v, n""",
                        parameters("list", genes_list, "min", min));
                var result = tx.run(query);

                LocalTime stop = LocalTime.now();
                System.out.println("Time to get data from neo4j: " + start.until(stop, ChronoUnit.MILLIS));

                List<Map<String, String>> nodes_list = new ArrayList<>();
                List<Map<String, Object>> links_list = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    String u = record.get("u").asString();
                    String v = record.get("v").asString();
                    Integer n = record.get("n").asInt();
                    Map<String, String> map_node_1 = Map.of("id", u);
                    Map<String, String> map_node_2 = Map.of("id", v);
                    Map<String, Object> map_link_1 = Map.of("source", u, "target", v, "value", n);
                    Map<String, Object> map_link_2 = Map.of("source", v, "target", u, "value", n);
                    if (!nodes_list.contains(map_node_1)) {
                        nodes_list.add(map_node_1);
                    }
                    if (!nodes_list.contains(map_node_2)) {
                        nodes_list.add(map_node_2);
                    }
                    if (!links_list.contains(map_link_1) & !links_list.contains(map_link_2)) {
                        links_list.add(map_link_1);
                    }
                }
                System.out.println("Time to process data: " + stop.until(LocalTime.now(), ChronoUnit.MILLIS));

                if (nodes_list.isEmpty() || links_list.isEmpty()) {
                    return Map.of("error", "Empty graph.");
                } else {
                    return Map.of("nodes", nodes_list, "links", links_list);
                }
            });
        } catch (Exception e) {
            System.out.print("Cannot make co-regulating graph: ");
            System.out.println(e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping(value = "/patient-info", produces = "application/json")
    public Map<String, Object> getPatientInfo(@RequestParam Map<String, String> allParams) {
        String patient_id = allParams.get("uuid");
        String query = "SELECT * FROM patient WHERE patient_uuid = '%s'".formatted(patient_id);
        try {
            return jdbcTemplate.queryForMap(query);
        } catch (DataAccessException e) {
            System.out.print("Cannot obtain patient info: ");
            System.out.println(e.getMessage());
            return Map.of();
        }
    }

    @GetMapping(value = "/track-records", produces = "application/json")
    public List<Map<String, Object>> getTrackRecords(@RequestParam Map<String, String> allParams) {
        String patient_id = allParams.get("uuid");
        String query = "SELECT * FROM patient_documents WHERE patient_uuid = '%s' ORDER BY event_date"
                .formatted(patient_id);
        try {
            return jdbcTemplate.queryForList(query);
        } catch (DataAccessException e) {
            System.out.print("Cannot obtain medical records: ");
            System.out.println(e.getMessage());
            return List.of();
        }
    }

    @GetMapping(value = "/document", produces = "application/json")
    public Map<String, Object> document(@RequestParam Map<String, String> allParams) {
        String document_type = allParams.get("document_type");
        String id = allParams.get("id");

        return getMedicalDocument(document_type, id);
    }

    @GetMapping(value = "/document-tree", produces = "application/json")
    public Map<String, Object> documentTree(@RequestParam Map<String, String> allParams) {
        String document_type = allParams.get("document_type");
        String id = allParams.get("id");
        String document_class_name = allParams.get("document_class_name");

        try {
            Map<String, Object> document = getMedicalDocument(document_type, id);
            return documentToTree(document, document_class_name);
        } catch (Exception e) {
            System.out.println("Cannot open document " + id + ": " + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private Map<String, Object> documentToTree(Map<String, Object> document, String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name.replace('_', ' '));

        List<Map<String, Object>> children = new ArrayList<>();
        for (var entry : document.entrySet()) {
            var value = entry.getValue();
            String key = entry.getKey();
            if (key.equals("encoding")) continue;

            if (value instanceof Map) {
                children.add(documentToTree((Map<String, Object>) value, key));
            } else if (value instanceof List) {
                for (Object element : (List<Map<String, Object>>) value) {
                    children.add(documentToTree((Map<String, Object>) element, key));
                }
            } else {
                result.put(key.replace('_', ' '), value);
            }
        }
        if (!children.isEmpty()) {result.put("children", children);}
        return result;
    }

    private Map<String, Object> getMedicalDocument(String document_type, String id) {
        Session session = JSchSession("minhanh", "dn2rMnQNJzewSw", "195.19.43.24", 2622);
        String json = readFileFromSession(session, document_type, id);
        if (session != null) {
            session.disconnect();
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Session JSchSession(String username, String password,
            String host, int port) {
        Session session = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            System.err.println("Cannot connect to server " + host + ": " + e.getMessage());
        }
        return session;
    }

    private String readFileFromSession(Session session, String document_type, String name) {
        ChannelSftp channel = null;
        String resulttext = "";
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            for (String folder : availableFolders(document_type)) {
                String filePath = "data/common/documents/%s/%s.json".formatted(folder, name);
                try {
                    InputStream stream = channel.get(filePath);
                    resulttext = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                    //System.out.println(resulttext.substring(0, 15));
                    break;
                } catch (SftpException | IOException e) {
                    //System.err.println("Cannot read file " + filePath + ": " + e.getMessage());
                }
            }
        } catch (JSchException e) {
            System.err.println("Cannot create sftp channel: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return resulttext;
    }

    private List<String> availableFolders(String document_type) {
        switch (document_type) {
            case "questionary" -> {
                return List.of("questionaries");
            }
            case "instrumental_result" -> {
                return List.of("instrumental_results", "instrumental_results_full_length");
            }
            case "laboratory_result" -> {
                List<String> l = new ArrayList<>();
                for (int i = 1; i <= 8; i++) {
                    l.add("laboratory_results_part_" + i);
                }
                return l;
            }
            case "examination" -> {
                List<String> l = new ArrayList<>();
                for (int i = 1; i <= 8; i++) {
                    l.add("examination_part_" + i);
                    l.add("examination_part_" + i + "_full_length");
                }
                l.add("examination_part_9");
                return l;
            }
            case "epicrisis" -> {
                return List.of("epicrisis");
            }
            case "ambulance" -> {
                return List.of("ambulance");
            }
            case "drugs_prescription" -> {
                return List.of("drugs_prescriptions");
            }
            default -> {
                return null;
            }
        }
    }
}
