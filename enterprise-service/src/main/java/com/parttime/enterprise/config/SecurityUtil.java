package com.parttime.enterprise.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityUtil {

    @SuppressWarnings("unchecked")
    public static Long getCurrentCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return 1L;
        }
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object companyId = details.get("companyId");
            if (companyId instanceof Number num) {
                return num.longValue();
            }
        }
        return 1L;
    }

    @SuppressWarnings("unchecked")
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return 0L;
        }
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object userId = details.get("userId");
            if (userId instanceof Number num) {
                return num.longValue();
            }
        }
        return 0L;
    }
}
