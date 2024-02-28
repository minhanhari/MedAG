package com.minhanh.coreg.transfactor;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TransfactorRepository extends MongoRepository<Transfactor, String>{
	@Query("{'Target Gene': { $in: ?0}}")
	List<Transfactor> findByGenes(String[] genes);

	@Query("{'Regulatory factor': ?0}")
	List<Transfactor> findByName(String name);
	
	/*
	 * @Query("SELECT * FROM Transfactor WHERE name LIKE :x") public Page<Transfactor>
	 * findProduct(@Param("x") String keyWord, Pageable pageable);
	 */
}
