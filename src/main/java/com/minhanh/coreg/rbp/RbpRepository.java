package com.minhanh.coreg.rbp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface RbpRepository extends MongoRepository<Rbp, String> {
    @Query("{'Gene Symbol': ?0}")
    Rbp findByName(String name);
}
