package com.parttime.enterprise.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void shouldHaveFourRoles() {
        assertThat(Role.values()).hasSize(4);
    }

    @Test
    void adminNameShouldBeADMIN() {
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void hrNameShouldBeHR() {
        assertThat(Role.HR.name()).isEqualTo("HR");
    }

    @Test
    void managerNameShouldBeMANAGER() {
        assertThat(Role.MANAGER.name()).isEqualTo("MANAGER");
    }

    @Test
    void financeNameShouldBeFINANCE() {
        assertThat(Role.FINANCE.name()).isEqualTo("FINANCE");
    }
}
