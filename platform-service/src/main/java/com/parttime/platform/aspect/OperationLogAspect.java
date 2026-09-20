package com.parttime.platform.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.mapper.OperationLogMapper;
import com.parttime.platform.pojo.entity.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class OperationLogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OperationLogMapper operationLogMapper;

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("execution(* com.parttime.platform.controller..*.*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                recordLog(pjp, result, error);
            } catch (Exception ignored) {
            }
        }
    }

    private void recordLog(ProceedingJoinPoint pjp, Object result, Throwable error) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/admin")) {
            return;
        }
        if (uri.contains("/operation-logs") || uri.contains("/auth/")) {
            return;
        }

        OperationLog log = new OperationLog();
        log.setOperatorName(currentOperatorName());
        log.setModule(resolveModule(uri));
        log.setOperationType(resolveOperationType(uri, pjp));
        log.setRequestMethod(request.getMethod());
        log.setRequestUrl(uri);
        log.setIpAddress(resolveIp(request));
        String ua = request.getHeader("User-Agent");
        log.setUserAgent(ua == null ? null : ua.length() > 500 ? ua.substring(0, 500) : ua);
        log.setRequestParams(serializeArgs(pjp.getArgs()));
        log.setResult(error == null ? "SUCCESS" : "FAIL");
        if (error != null) {
            String msg = error.getMessage();
            log.setErrorMessage(msg == null ? error.toString() : msg.length() > 500 ? msg.substring(0, 500) : msg);
        }
        try {
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
        }
    }

    private String resolveModule(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 4) {
            String seg = parts[3].toUpperCase();
            switch (seg) {
                case "JOBS": return "JOB";
                case "ENTERPRISES": case "ENTERPRISE-ACCOUNTS": case "ENTERPRISE-BALANCE": case "ENTERPRISE-TOP-UP":
                case "ENTERPRISE-REAL-NAME-AUTH-REVIEWS": return "ENTERPRISE";
                case "WORKERS": case "WORKER-REAL-NAME-AUTH-REVIEWS": return "WORKER";
                case "APPLICATIONS": return "APPLICATION";
                case "SCHEDULES": case "JOB-SCHEDULES": return "SCHEDULE";
                case "ATTENDANCE": case "ATTENDANCE-RECORDS": return "ATTENDANCE";
                case "SETTLEMENTS": case "FINANCE-REPORTS": case "TRANSACTIONS": return "SETTLEMENT";
                case "WITHDRAWAL-RECORDS": return "WITHDRAWAL";
                case "CONTENT": return "CONTENT";
                case "ACTIVITIES": return "ACTIVITY";
                case "OPERATORS": return "OPERATOR_MANAGE";
                case "REPORTS": return "REPORT";
                case "RISK": return "RISK";
                case "REVIEWS": return "REVIEW";
                case "CUSTOMER-SERVICE": case "CS": return "CS";
                case "SYSTEM-CONFIG": case "SYSTEM-CONFIGS": return "SYSTEM";
                case "JOB-CATEGORIES": case "JOB-TAGS": return "JOB";
                case "LEADS": return "LEAD";
                case "REFERRAL": return "REFERRAL";
                case "TRAINING-COURSES": case "TRAINING-CERTIFICATIONS": return "TRAINING";
                default: return seg;
            }
        }
        return "SYSTEM";
    }

    private String resolveOperationType(String uri, ProceedingJoinPoint pjp) {
        String lower = uri.toLowerCase();
        if (lower.contains("/create") || lower.contains("/add") || lower.contains("/upload")) {
            return "CREATE";
        }
        if (lower.contains("/update") || lower.contains("/edit") || lower.contains("/set-") || lower.contains("/toggle")) {
            return "UPDATE";
        }
        if (lower.contains("/delete") || lower.contains("/remove")) {
            return "DELETE";
        }
        if (lower.contains("/audit") || lower.contains("/review") || lower.contains("/approve") || lower.contains("/reject")) {
            return "AUDIT";
        }
        if (lower.contains("/export")) {
            return "EXPORT";
        }
        if (lower.contains("/list") || lower.contains("/detail") || lower.contains("/overview")
                || lower.contains("/stats") || lower.contains("/roles") || lower.contains("/banners")
                || lower.contains("/rates") || lower.contains("/reports") || lower.contains("/recommendations")
                || lower.contains("/trend") || lower.contains("/ranking") || lower.contains("/regions")) {
            return "QUERY";
        }
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod().getName().toUpperCase();
    }

    private String currentOperatorName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        String name = auth.getName();
        return name == null || name.isBlank() ? "anonymous" : name;
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private String serializeArgs(Object[] args) {
        try {
            List<Object> serializable = new ArrayList<>();
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                        || arg instanceof MultipartFile) {
                    continue;
                }
                serializable.add(arg);
            }
            String json = OBJECT_MAPPER.writeValueAsString(serializable);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            return null;
        }
    }
}
