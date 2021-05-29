package com.vignesh.springsecuritypg.model;

import org.springframework.security.core.GrantedAuthority;

public enum Authorities implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
