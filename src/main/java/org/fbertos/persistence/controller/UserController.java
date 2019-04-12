package org.fbertos.persistence.controller;

import java.util.List;

import org.fbertos.persistence.model.User;
import org.fbertos.persistence.search.Filter;
import org.fbertos.persistence.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	 @Autowired
	 private UserService userService;
	 
	 @GetMapping(value="/users/{username}")
	 public @ResponseBody User get(@PathVariable String username) {
		 String q = "username like " + username;
		 List<User> l = userService.find(new Filter(q, null, null, null));
		 if (l.size() > 0) return l.get(0);
		 return null;
	 }
	 
	 @DeleteMapping(value="/users/{username}")
	 public void delete(@PathVariable String username) {
		 String q = "username like " + username;
		 List<User> l = userService.find(new Filter(q, null, null, null));
		 if (l.size() > 0) userService.delete(l.get(0).getId().toHexString());
     }

	 @PutMapping(value="/users")
     public void update(@RequestBody User user) {
    	 userService.update(user);
     }
	    
	 @PostMapping(value="/users")
     public @ResponseBody User create(@RequestBody User user) {
    	 userService.save(user);
    	 return user;
     }

	 @GetMapping(value = "/users")
	 public @ResponseBody List<User> get(@RequestParam(value="q", required=false) String q,
			 @RequestParam(value="page", required=false) String page,
			 @RequestParam(value="order", required=false) String order,
			 @RequestParam(value="itemsperpage", required=false) String itemsperpage) {
		 
		 return userService.find(new Filter(q, order, page, itemsperpage));
	 }
}
