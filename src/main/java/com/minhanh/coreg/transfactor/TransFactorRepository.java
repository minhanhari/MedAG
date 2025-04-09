package com.minhanh.coreg.transfactor;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
//import reactor.core.publisher.Mono;

public interface TransFactorRepository extends Neo4jRepository<TransFactorEntity, String> {
    @Query("MATCH (r:TransFactor {symbol: $name}) RETURN r")
    TransFactorEntity findByName(String name);
}
