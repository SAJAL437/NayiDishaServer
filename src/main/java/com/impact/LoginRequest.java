package com.impact;

import java.util.Set;

import com.impact.entity.Role;

public class LoginRequest {

    private String email;
	private String password;
	
	public LoginRequest() {
		// TODO Auto-generated constructor stub
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public Set<Role> getRoles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRoles'");
    }

	

}
