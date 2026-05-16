package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseAccountMapper;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.entity.EnterpriseAccount;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import com.parttime.platform.service.EnterpriseAccountService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseAccountServiceImpl implements EnterpriseAccountService {

    @Resource
    private EnterpriseAccountMapper accountMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public List<EnterpriseAccountVO> listByEnterprise(Long enterpriseId) {
        enterpriseMapper.findById(enterpriseId)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + enterpriseId));
        return accountMapper.findByEnterpriseId(enterpriseId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public EnterpriseAccountVO create(EnterpriseAccountCreateCmd cmd) {
        enterpriseMapper.findById(cmd.getEnterpriseId())
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + cmd.getEnterpriseId()));
        if (accountMapper.findByUsername(cmd.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists: " + cmd.getUsername());
        }
        EnterpriseAccount account = new EnterpriseAccount();
        account.setEnterpriseId(cmd.getEnterpriseId());
        account.setUsername(cmd.getUsername());
        account.setPassword(passwordEncoder.encode(cmd.getPassword()));
        account.setDisplayName(cmd.getDisplayName());
        account.setRole(cmd.getRole());
        account.setStatus("ACTIVE");
        accountMapper.insert(account);
        return toVO(account);
    }

    @Override
    public EnterpriseAccountVO update(EnterpriseAccountUpdateCmd cmd) {
        EnterpriseAccount account = accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        if (cmd.getDisplayName() != null) account.setDisplayName(cmd.getDisplayName());
        if (cmd.getRole() != null) account.setRole(cmd.getRole());
        accountMapper.update(account);
        return toVO(account);
    }

    @Override
    public void resetPassword(ResetPasswordCmd cmd) {
        accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        accountMapper.updatePassword(cmd.getId(), passwordEncoder.encode(cmd.getNewPassword()));
    }

    @Override
    public void delete(Long id) {
        accountMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found: " + id));
        accountMapper.deleteById(id);
    }

    private EnterpriseAccountVO toVO(EnterpriseAccount a) {
        EnterpriseAccountVO vo = new EnterpriseAccountVO();
        vo.setId(a.getId());
        vo.setEnterpriseId(a.getEnterpriseId());
        vo.setUsername(a.getUsername());
        vo.setDisplayName(a.getDisplayName());
        vo.setRole(a.getRole());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }
}
