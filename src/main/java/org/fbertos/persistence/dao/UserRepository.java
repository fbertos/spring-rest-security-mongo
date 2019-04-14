package org.fbertos.persistence.dao;


import java.util.List;

import org.fbertos.persistence.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	//@Query("{'$or':[ {'type':?0}, {'username':?1} ]}")
	//@Query(value = "{'username': {$regex : ?0, $options: 'i'}}")
	//db.user.find({"username": {$regex : /ADM.*/, $options: 'i'}})
	@Query("{ 'username' : ?0 }")
	List<User> findByUsername(String username);	
}
