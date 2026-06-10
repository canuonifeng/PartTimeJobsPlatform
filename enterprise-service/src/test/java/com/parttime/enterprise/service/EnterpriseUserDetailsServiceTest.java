package com.parttime.enterprise.service;

import com.parttime.enterprise.config.CompanyUserDetails;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseUserDetailsServiceTest {

    @Mock
    private EnterpriseAccountMapper accountMapper;

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @InjectMocks
    private EnterpriseUserDetailsService userDetailsService;

    private EnterpriseAccount activeAccount;

    @BeforeEach
    void setUp() {
        activeAccount = new EnterpriseAccount();
        activeAccount.setId(1L);
        activeAccount.setUsername("admin");
        activeAccount.setPassword("$2a$10$encodedPassword");
        activeAccount.setRole("ADMIN");
        activeAccount.setStatus("ACTIVE");
        activeAccount.setEnterpriseId(100L);
    }

    @Test
    void loadUserByUsername_withValidSuffixAndAccount_shouldWork() {
        when(enterpriseMapper.findIdByEmailSuffix("acme.com")).thenReturn(100L);
        when(accountMapper.findByUsername("admin")).thenReturn(Optional.of(activeAccount));

        UserDetails details = userDetailsService.loadUserByUsername("admin@acme.com");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_withoutAtSign_shouldFail() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("前缀@后缀");
    }

    @Test
    void loadUserByUsername_withEmptyPrefix_shouldFail() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("@acme.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("前缀@后缀");
    }

    @Test
    void loadUserByUsername_withEmptySuffix_shouldFail() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin@"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("前缀@后缀");
    }

    @Test
    void loadUserByUsername_withUnknownSuffix_shouldFail() {
        when(enterpriseMapper.findIdByEmailSuffix("unknown.com")).thenReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin@unknown.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("后缀不正确");
    }

    @Test
    void loadUserByUsername_withWrongEnterprise_shouldFail() {
        when(enterpriseMapper.findIdByEmailSuffix("other.com")).thenReturn(200L);
        when(accountMapper.findByUsername("admin")).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin@other.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("不属于该企业");
    }

    @Test
    void loadUserByUsername_withInactiveAccount_shouldFail() {
        EnterpriseAccount inactiveAccount = new EnterpriseAccount();
        inactiveAccount.setUsername("user");
        inactiveAccount.setStatus("INACTIVE");
        inactiveAccount.setEnterpriseId(100L);

        when(enterpriseMapper.findIdByEmailSuffix("acme.com")).thenReturn(100L);
        when(accountMapper.findByUsername("user")).thenReturn(Optional.of(inactiveAccount));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("user@acme.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("已被禁用");
    }
}
