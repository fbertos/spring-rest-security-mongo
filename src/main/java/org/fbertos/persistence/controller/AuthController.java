package org.fbertos.persistence.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fbertos.authorization.config.JwtTokenProvider;
import org.fbertos.persistence.model.Authority;
import org.fbertos.persistence.model.User;
import org.fbertos.persistence.search.Filter;
import org.fbertos.persistence.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
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
public class AuthController {
	 @Autowired
	 UserService userService;
	
	 @Autowired
	 JwtTokenProvider jwtTokenProvider;
	 
	 @Autowired
	 AuthenticationManager authenticationManager;
	 
	 @PostMapping(value="/auth")
	 public @ResponseBody ResponseEntity<AuthInfo> auth(@RequestParam String username, @RequestParam String password) {
		 try {
			 authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			 UserDetails user = userService.loadUserByUsername(username);
			 String token = jwtTokenProvider.createToken(username, new ArrayList());
			 AuthInfo info = new AuthInfo(username, token);
			 Map<Object, Object> model = new HashMap<>();
			 model.put(username, username);
			 model.put("token", token);
			 return ResponseEntity.status(HttpStatus.OK).body(info);
		 }
		 catch (AuthenticationException e) {
			 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);			 
		 }
	 }
}
