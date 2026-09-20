package com.parttime.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.PlatformOperatorMapper;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.OperatorCreateCmd;
import com.parttime.platform.pojo.cmd.OperatorUpdateCmd;
import com.parttime.platform.pojo.entity.PlatformOperator;
import com.parttime.platform.pojo.vo.OperatorVO;
import com.parttime.platform.pojo.vo.RoleVO;
import com.parttime.platform.service.OperatorService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperatorServiceImpl implements OperatorService {

    private static final Map<String, RoleDef> ROLE_DEFS = new LinkedHashMap<>();

    static {
        ROLE_DEFS.put("SUPER_ADMIN", new RoleDef("超级管理员", "拥有全部权限",
                Arrays.asList("DASHBOARD", "REPORTS", "JOB", "ENTERPRISE", "WORKER", "APPLICATION",
                        "SCHEDULE", "ATTENDANCE", "SETTLEMENT", "FINANCE", "WITHDRAWAL", "CONTENT",
                        "ACTIVITY", "PUSH", "COMPLAINT", "RISK", "REVIEW", "OPERATOR_MANAGE",
                        "SYSTEM", "OPERATION_LOG")));
        ROLE_DEFS.put("OPERATOR_LEAD", new RoleDef("运营主管", "运营模块全权管理",
                Arrays.asList("DASHBOARD", "REPORTS", "JOB", "ENTERPRISE", "WORKER", "APPLICATION",
                        "SCHEDULE", "CONTENT", "ACTIVITY", "PUSH", "REVIEW")));
        ROLE_DEFS.put("OPERATOR", new RoleDef("运营专员", "日常运营操作",
                Arrays.asList("DASHBOARD", "REPORTS", "JOB", "CONTENT", "ACTIVITY")));
        ROLE_DEFS.put("FINANCE", new RoleDef("财务专员", "财务结算与提现",
                Arrays.asList("DASHBOARD", "REPORTS", "SETTLEMENT", "FINANCE", "WITHDRAWAL")));
        ROLE_DEFS.put("RISK_CONTROL", new RoleDef("风控专员", "风控与审核",
                Arrays.asList("DASHBOARD", "REPORTS", "RISK", "REVIEW", "COMPLAINT")));
        ROLE_DEFS.put("CUSTOMER_SERVICE", new RoleDef("客服专员", "客服与投诉处理",
                Arrays.asList("DASHBOARD", "COMPLAINT", "WORKER", "ENTERPRISE")));
    }

    private static class RoleDef {
        final String name;
        final String description;
        final List<String> permissions;

        RoleDef(String name, String description, List<String> permissions) {
            this.name = name;
            this.description = description;
            this.permissions = permissions;
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private PlatformOperatorMapper operatorMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public List<OperatorVO> list() {
        return operatorMapper.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void create(OperatorCreateCmd cmd) {
        operatorMapper.findByUsername(cmd.getUsername()).ifPresent(o -> {
            throw new BusinessException("用户名已存在: " + cmd.getUsername());
        });
        if (!ROLE_DEFS.containsKey(cmd.getRole())) {
            throw new BusinessException("非法角色: " + cmd.getRole());
        }
        PlatformOperator operator = new PlatformOperator();
        operator.setUsername(cmd.getUsername());
        operator.setPassword(passwordEncoder.encode(cmd.getPassword() == null || cmd.getPassword().isBlank()
                ? "123456" : cmd.getPassword()));
        operator.setRealName(cmd.getRealName());
        operator.setPhone(cmd.getPhone());
        operator.setEmail(cmd.getEmail());
        operator.setRole(cmd.getRole());
        operator.setPermissions(defaultPermissionsJson(cmd.getRole()));
        operatorMapper.insert(operator);
    }

    @Override
    public void update(OperatorUpdateCmd cmd) {
        PlatformOperator operator = operatorMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("账号不存在: " + cmd.getId()));
        if (cmd.getRole() != null && !ROLE_DEFS.containsKey(cmd.getRole())) {
            throw new BusinessException("非法角色: " + cmd.getRole());
        }
        operator.setRealName(cmd.getRealName());
        operator.setPhone(cmd.getPhone());
        operator.setEmail(cmd.getEmail());
        if (cmd.getRole() != null) {
            operator.setRole(cmd.getRole());
        }
        List<String> perms = cmd.getPermissions() != null ? cmd.getPermissions()
                : defaultPermissions(operator.getRole());
        operator.setPermissions(toJson(perms));
        operatorMapper.update(operator);
    }

    @Override
    public void resetPassword(IdCmd cmd) {
        PlatformOperator operator = operatorMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("账号不存在: " + cmd.getId()));
        operatorMapper.updatePassword(operator.getId(), passwordEncoder.encode("123456"));
    }

    @Override
    public void toggleStatus(IdCmd cmd) {
        PlatformOperator operator = operatorMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("账号不存在: " + cmd.getId()));
        String newStatus = "ACTIVE".equals(operator.getStatus()) ? "INACTIVE" : "ACTIVE";
        operatorMapper.updateStatus(operator.getId(), newStatus);
    }

    @Override
    public List<RoleVO> roles() {
        List<RoleVO> list = new ArrayList<>();
        for (Map.Entry<String, RoleDef> entry : ROLE_DEFS.entrySet()) {
            RoleVO vo = new RoleVO();
            vo.setRoleCode(entry.getKey());
            vo.setRoleName(entry.getValue().name);
            vo.setDescription(entry.getValue().description);
            vo.setPermissions(entry.getValue().permissions);
            list.add(vo);
        }
        return list;
    }

    private OperatorVO toVO(PlatformOperator o) {
        OperatorVO vo = new OperatorVO();
        vo.setId(o.getId());
        vo.setUsername(o.getUsername());
        vo.setRealName(o.getRealName());
        vo.setPhone(o.getPhone());
        vo.setEmail(o.getEmail());
        vo.setRole(o.getRole());
        RoleDef def = ROLE_DEFS.get(o.getRole());
        vo.setRoleName(def == null ? o.getRole() : def.name);
        vo.setPermissions(parsePermissions(o.getPermissions(), o.getRole()));
        vo.setStatus(o.getStatus());
        vo.setLastLoginAt(o.getLastLoginAt());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private List<String> defaultPermissions(String role) {
        RoleDef def = ROLE_DEFS.get(role);
        return def == null ? new ArrayList<>() : def.permissions;
    }

    private String defaultPermissionsJson(String role) {
        return toJson(defaultPermissions(role));
    }

    private String toJson(List<String> perms) {
        try {
            return objectMapper.writeValueAsString(perms);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parsePermissions(String json, String role) {
        if (json == null || json.isBlank()) {
            return defaultPermissions(role);
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return defaultPermissions(role);
        }
    }
}
