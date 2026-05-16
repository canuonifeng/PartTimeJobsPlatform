package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.pojo.cmd.AccountCreateCmd;
import com.parttime.enterprise.pojo.cmd.AccountResetPasswordCmd;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import com.parttime.enterprise.pojo.vo.AccountVO;
import com.parttime.enterprise.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private EnterpriseAccountMapper accountMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<AccountVO> list(Long enterpriseId) {
        return accountMapper.findByEnterpriseId(enterpriseId).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AccountVO create(AccountCreateCmd cmd, Long enterpriseId) {
        EnterpriseAccount account = new EnterpriseAccount();
        account.setEnterpriseId(enterpriseId);
        account.setUsername(cmd.getUsername());
        account.setPassword(passwordEncoder.encode(cmd.getPassword()));
        account.setDisplayName(cmd.getDisplayName());
        account.setRole(cmd.getRole() != null ? cmd.getRole() : "ADMIN");
        account.setStatus("ACTIVE");
        accountMapper.insert(account);
        return toVO(account);
    }

    @Override
    public AccountVO update(AccountUpdateCmd cmd) {
        EnterpriseAccount account = accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        if (cmd.getDisplayName() != null) account.setDisplayName(cmd.getDisplayName());
        if (cmd.getRole() != null) account.setRole(cmd.getRole());
        accountMapper.update(account);
        return toVO(account);
    }

    @Override
    public void resetPassword(AccountResetPasswordCmd cmd) {
        EnterpriseAccount account = accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        accountMapper.updatePassword(cmd.getId(), passwordEncoder.encode(cmd.getNewPassword()));
    }

    @Override
    public void delete(Long id) {
        EnterpriseAccount account = accountMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found: " + id));
        accountMapper.deleteById(id);
    }

    private AccountVO toVO(EnterpriseAccount account) {
        AccountVO vo = new AccountVO();
        vo.setId(account.getId());
        vo.setUsername(account.getUsername());
        vo.setDisplayName(account.getDisplayName());
        vo.setRole(account.getRole());
        vo.setStatus(account.getStatus());
        vo.setCreatedAt(account.getCreatedAt());
        return vo;
    }
}
