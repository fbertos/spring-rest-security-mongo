package org.fbertos.persistence.model;

import org.springframework.security.core.GrantedAuthority;

public class Authority implements GrantedAuthority {
	private static final long serialVersionUID = 105209821198085900L;

    private String name;

    @Override
    public String getAuthority() {
        return name;
    }
    
    public void setAuthority(String name) {
        this.name = name;
    }
}