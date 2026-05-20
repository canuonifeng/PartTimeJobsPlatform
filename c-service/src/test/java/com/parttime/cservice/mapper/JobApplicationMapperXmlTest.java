package com.parttime.cservice.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationMapperXmlTest {

    @Test
    void mapperXml_shouldUseSharedJobApplicationsTable() throws IOException {
        String xml = new String(
                getClass().getClassLoader().getResourceAsStream("mapper/JobApplicationMapper.xml").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(xml).contains("INSERT INTO job_applications");
        assertThat(xml).doesNotContain("c_job_application");
    }
}
