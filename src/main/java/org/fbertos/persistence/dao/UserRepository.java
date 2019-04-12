package org.fbertos.persistence.dao;


import java.util.List;

import org.fbertos.persistence.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	@Query("{ 'username' : ?0 }")
	List<User> findByUsername(String usernamename);	
}
