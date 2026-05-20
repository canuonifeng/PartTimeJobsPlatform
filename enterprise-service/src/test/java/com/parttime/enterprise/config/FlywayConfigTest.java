package com.parttime.enterprise.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayConfigTest {

    @Test
    void applicationYaml_shouldEnableFlywayWithBaselineMigration() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(yaml).contains("spring:");
        assertThat(yaml).contains("flyway:");
        assertThat(yaml).contains("enabled: true");
        assertThat(yaml).contains("baseline-on-migrate: true");
        assertThat(yaml).contains("baseline-version: 8");
    }
}
