package com.parttime.enterprise.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void shouldHaveFourRoles() {
        assertThat(Role.values()).hasSize(4);
    }

    @Test
    void adminShouldHaveCorrectNameAndCode() {
        assertThat(Role.ADMIN.getName()).isEqualTo("管理员");
        assertThat(Role.ADMIN.getCode()).isEqualTo(0);
    }

    @Test
    void hrShouldHaveCorrectNameAndCode() {
        assertThat(Role.HR.getName()).isEqualTo("人事");
        assertThat(Role.HR.getCode()).isEqualTo(1);
    }

    @Test
    void managerShouldHaveCorrectNameAndCode() {
        assertThat(Role.MANAGER.getName()).isEqualTo("经理");
        assertThat(Role.MANAGER.getCode()).isEqualTo(2);
    }

    @Test
    void financeShouldHaveCorrectNameAndCode() {
        assertThat(Role.FINANCE.getName()).isEqualTo("财务");
        assertThat(Role.FINANCE.getCode()).isEqualTo(3);
    }

    @Test
    void fromCodeShouldReturnCorrectEnum() {
        assertThat(BaseEnum.fromCode(Role.class, 0)).isEqualTo(Role.ADMIN);
        assertThat(BaseEnum.fromCode(Role.class, 3)).isEqualTo(Role.FINANCE);
    }

    @Test
    void fromNameShouldReturnCorrectEnum() {
        assertThat(BaseEnum.fromName(Role.class, "管理员")).isEqualTo(Role.ADMIN);
        assertThat(BaseEnum.fromName(Role.class, "财务")).isEqualTo(Role.FINANCE);
    }
}
