package org.fbertos.persistence.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fbertos.persistence.dao.UserRepository;
import org.fbertos.persistence.model.User;
import org.fbertos.persistence.search.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
    private UserRepository userRepository;

	//@PreAuthorize("hasAuthority('USER_CREATE')")
	public void save(User user) {
		userRepository.save(user);
	}

	//@PreAuthorize("hasAuthority('USER_READ')")
	public User get(String id) {
		return userRepository.findById(id).get();
	}

    //@PreAuthorize("hasAuthority('USER_READ')")
    @PostFilter("filterObject.username == authentication.name or hasPermission(filterObject, 3)")
	public List<User> find(Filter filter) {
    	Pageable page = PageRequest.of(0, 50);
    	List<User> list_before = userRepository.findAll(page).getContent();
    	ArrayList<User> list_after = new ArrayList<User>(list_before);
		return list_after;
	}

    @PostFilter("filterObject.username == authentication.name or hasPermission(filterObject, 3)")
	public List<User> find() {
		return userRepository.findAll();
	}
    
    //@PreAuthorize("hasAuthority('USER_UPDATE')")
	public void update(User user) {
		userRepository.save(user);
	}

    //@PreAuthorize("hasAuthority('USER_DELETE')")
	public void delete(String id) {
		userRepository.deleteById(id);
	}
	
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	Filter filter = new Filter("username like " + username, null, null, null);
		List<User> list = userRepository.findByUsername(username);
		
		if (list != null && list.size() > 0)
			return list.get(0);
    	
        throw new UsernameNotFoundException(username);
    }	
}
