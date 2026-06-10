package com.parttime.enterprise.service;

import com.parttime.enterprise.config.CompanyUserDetails;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import jakarta.annotation.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnterpriseUserDetailsService implements UserDetailsService {

    @Resource
    private EnterpriseAccountMapper accountMapper;

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || !username.contains("@")) {
            throw new UsernameNotFoundException("用户名格式不正确，须使用 前缀@后缀 格式");
        }
        int atIndex = username.indexOf("@");
        String localPart = username.substring(0, atIndex);
        String suffix = username.substring(atIndex + 1);
        if (localPart.isEmpty() || suffix.isEmpty()) {
            throw new UsernameNotFoundException("用户名格式不正确，须使用 前缀@后缀 格式");
        }
        Long enterpriseId = enterpriseMapper.findIdByEmailSuffix(suffix);
        if (enterpriseId == null) {
            throw new UsernameNotFoundException("账号后缀不正确");
        }
        EnterpriseAccount account = accountMapper.findByUsername(localPart)
                .orElseThrow(() -> new UsernameNotFoundException("账号或密码错误"));
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new UsernameNotFoundException("账号已被禁用");
        }
        if (!account.getEnterpriseId().equals(enterpriseId)) {
            throw new UsernameNotFoundException("账号不属于该企业");
        }
        return new CompanyUserDetails(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())),
                account.getEnterpriseId()
        );
    }
}
