package com.parttime.enterprise.service;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import com.parttime.enterprise.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private EnterpriseAccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void delete_shouldRejectDeletion() {
        assertThatThrownBy(() -> accountService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号不能删除，请禁用账号");
        verify(accountMapper, never()).deleteById(1L);
    }

    @Test
    void update_shouldAllowDisablingAccount() {
        EnterpriseAccount account = new EnterpriseAccount();
        account.setId(1L);
        account.setEnterpriseId(2L);
        account.setUsername("admin");
        account.setDisplayName("管理员");
        account.setRole("ADMIN");
        account.setStatus("ACTIVE");
        when(accountMapper.findById(1L)).thenReturn(Optional.of(account));

        AccountUpdateCmd cmd = new AccountUpdateCmd();
        cmd.setId(1L);
        cmd.setStatus("DISABLED");

        var result = accountService.update(cmd);

        assertThat(result.getStatus()).isEqualTo("DISABLED");
        verify(accountMapper).update(account);
    }
}
