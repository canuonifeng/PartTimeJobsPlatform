package com.parttime.enterprise.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CompanyUserDetails extends User {

    private final Long companyId;

    public CompanyUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long companyId) {
        super(username, password, authorities);
        this.companyId = companyId;
    }

    public Long getCompanyId() {
        return companyId;
    }
}
