package com.minhanh.coreg;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoRegApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void connectToNeo4j() throws Exception {
        mvc.perform(get("/graph?genes=TP53\nMUC16\nKMT2D\nKDM6A\nARID1A\nPIK3CA\nKMT2C\nRB1\nFAT4\nCSMD3&coregulation=2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").exists());
    }

    private final String test_uuid = "63db09ad-6dd7-4952-bcae-f8853aba6427";
    private final String test_document_id = "7841e41a-b43a-4a1c-a1fc-954d964b963b";

    @Test
    void getPatientInfo() throws Exception {
        mvc.perform(get("/patient-info?uuid=" + test_uuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient_gender").value("M"))
                .andExpect(jsonPath("$.patient_group").value("heart_attack_2020"));
    }

    @Test 
    void getTrackRecords() throws Exception {
        mvc.perform(get("/track-records?uuid=" + test_uuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id").value("8c2a8b34-1615-45a5-bc21-f0514461f140"))
                .andExpect(jsonPath("$[1].id").value("3998d547-5b12-47b3-ace1-b5c24bc7e335"));
    }

    @Test
    void getDocumentFromServer() throws Exception {
        mvc.perform(get("/document?document_type=laboratory_result&id=" + test_document_id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Результат_исследования").exists());
    }
}

