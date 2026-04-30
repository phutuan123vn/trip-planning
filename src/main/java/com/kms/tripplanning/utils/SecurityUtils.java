package com.kms.tripplanning.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kms.tripplanning.config.AuthUserDetails;

public class SecurityUtils {

    public static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    public static AuthUserDetails getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthUserDetails)) {
            return null;
        }
        return (AuthUserDetails) authentication.getPrincipal();
    }
}
