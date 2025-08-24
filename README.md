# Software component placement diagrams
## _Co-regulating graph RNA-binding proteins_
The main task for this subsystem is to obtain data on the RSB and genes from the database and the structure of the graph. To optimize the maximum capabilities of the Cypher language when working with the graph model of the database, instead of implementing logical processes on the client side with a programming language, the Cypher language commands were encapsulated and sent to the Neo4j DBMS core to request a record. After receiving the results, only the final processing of the data is done to display them to the user. It is necessary to use the Neo4j driver corresponding to the programming language. In this case, the program and the DBMS will exchange data via the bolt protocol.
An additional function for obtaining data on the RSB was added. When observing the graph picture, the user can view the data on the RSB by clicking on its node. The websocket interface was used for communication between the client and server parts. On the server, a microservice was created to exchange data on RSB objects. Neo4j also has the ability to create a microservice with automatic links between class objects in the programming language and records in the database. This approach allows for quick and immediate response to requests made on the client's website.


## _Analyze patient's electronic heath records_
The initial data set consists of two parts, as described above. Each part is suitable for a different management model, which depends on the data structure. First, the patient profile has several field tables with certain data types, and there are relationships between the tables. For such patient profile tables, a classic relational database can be created. For standard and simple queries, such as "search for a patient by identification" or "select all documents for a given patient", a relational DBMS works with high speed and accuracy.


# Request from graph DB
The Neo4j DBMS organizes data in a graph model and the Cypher query language is optimal for working with the model graph and representing relationships between objects. Therefore, when analyzing this subject area, where objects are difficult to connect with each other, it was concluded that it is better and faster to develop with the received results of records from Cypher queries in the database core than to create entity models in the service program. This process is implemented by importing the query code into the Spring Boot controller. The results of the query execution are an object of the Result class. It is read as a sequence of record lines (Record). Having received the results, they are processed to obtain the desired format.

```java
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
```

What's nice about working with Neo4j via Java is that Neo4j was written (mostly) in Java, so all parameters and data types in Java are fully compatible with the Cypher query itself, also when processing the results.

# Interfaces
For a web application written in Java with Spring Framework, the user interface forms for communication with the user are created as HTML markup text documents using the bootstrap library, and interactive actions are implemented in JavaScript. In accordance with the three use cases, three pages were created, including 1. the home page where the task is selected and the initial data is entered, 2. a page for analyzing the RBP co-regulating graph, and 3. a page for analyzing the patient's electronic medical records history.


The d3 library provides a set of tools for visualizing data in the form of a graph, as well as in others. Thanks to the svg base, it can be used to create an interactive graph drawing. This allows even more opportunities for communication with information for the user when observing and analyzing biochemical elements or documents - medical records. For the developed system, we will consider two tools of the d3 library class - force simulation for graph visualization and a tree layout for visualizing a hierarchical structure.

# Results
Using the software system, we can create graph models and find the most important RBPs in different cases of disease by looking at their effects on others in the graph. Once we know the list of genes, we create links between pairs of RBPs that affect the same genes. We can limit the links by the number of genes they jointly regulate (more than 2, 3, or 4 instead of one).