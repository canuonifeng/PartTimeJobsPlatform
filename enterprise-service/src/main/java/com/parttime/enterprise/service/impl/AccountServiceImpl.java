package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.pojo.cmd.AccountCreateCmd;
import com.parttime.enterprise.pojo.cmd.AccountPasswordUpdateCmd;
import com.parttime.enterprise.pojo.cmd.AccountResetPasswordCmd;
import com.parttime.enterprise.pojo.cmd.AccountSecurityUpdateCmd;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import com.parttime.enterprise.pojo.vo.AccountVO;
import com.parttime.enterprise.pojo.vo.PageVO;
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
    public PageVO<AccountVO> list(Long enterpriseId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (currentPage - 1) * currentPageSize;
        long total = accountMapper.countByEnterpriseId(enterpriseId);
        List<AccountVO> records = accountMapper.findByEnterpriseIdPage(enterpriseId, offset, currentPageSize).stream()
                .map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, total);
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
        if (cmd.getStatus() != null) account.setStatus(cmd.getStatus());
        accountMapper.update(account);
        return toVO(account);
    }

    @Override
    public AccountVO getCurrent(Long accountId, Long enterpriseId) {
        EnterpriseAccount account = findOwnAccount(accountId, enterpriseId);
        return toVO(account);
    }

    @Override
    public AccountVO updateCurrent(Long accountId, Long enterpriseId, AccountSecurityUpdateCmd cmd) {
        EnterpriseAccount account = findOwnAccount(accountId, enterpriseId);
        String displayName = cmd.getDisplayName() != null ? cmd.getDisplayName().trim() : account.getDisplayName();
        String phone = cmd.getPhone() != null ? cmd.getPhone().trim() : account.getPhone();
        accountMapper.updateProfile(account.getId(), displayName, phone);
        account.setDisplayName(displayName);
        account.setPhone(phone);
        return toVO(account);
    }

    @Override
    public void updateCurrentPassword(Long accountId, Long enterpriseId, AccountPasswordUpdateCmd cmd) {
        EnterpriseAccount account = findOwnAccount(accountId, enterpriseId);
        if (cmd.getNewPassword() == null || cmd.getNewPassword().isBlank()) {
            throw new BusinessException("新密码不能为空");
        }
        accountMapper.updatePassword(account.getId(), passwordEncoder.encode(cmd.getNewPassword()));
    }

    @Override
    public void resetPassword(AccountResetPasswordCmd cmd) {
        EnterpriseAccount account = accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        accountMapper.updatePassword(cmd.getId(), passwordEncoder.encode(cmd.getNewPassword()));
    }

    @Override
    public void delete(Long id) {
        throw new BusinessException("账号不能删除，请禁用账号");
    }

    private AccountVO toVO(EnterpriseAccount account) {
        AccountVO vo = new AccountVO();
        vo.setId(account.getId());
        vo.setUsername(account.getUsername());
        vo.setDisplayName(account.getDisplayName());
        vo.setPhone(account.getPhone());
        vo.setRole(account.getRole());
        vo.setStatus(account.getStatus());
        vo.setCreatedAt(account.getCreatedAt());
        return vo;
    }

    private EnterpriseAccount findOwnAccount(Long accountId, Long enterpriseId) {
        EnterpriseAccount account = accountMapper.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found: " + accountId));
        if (!account.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException("不能操作其他企业账号");
        }
        return account;
    }
}
