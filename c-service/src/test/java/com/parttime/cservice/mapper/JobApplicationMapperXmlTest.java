package com.parttime.cservice.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationMapperXmlTest {

    @Test
    void balanceTransactionMapperXml_shouldPreferLinkedAttendanceAndFallbackToDescriptionShiftDate() throws IOException {
        String xml = new String(
                getClass().getClassLoader().getResourceAsStream("mapper/BalanceTransactionMapper.xml").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(xml).contains("COALESCE(ar.shift_id, legacy_ar.shift_id)");
        assertThat(xml).contains("REGEXP_SUBSTR(bt.description");
        assertThat(xml).contains("COALESCE(linked_j.title, legacy_j.title) AS job_title");
        assertThat(xml).contains("ORDER BY COALESCE(ss.shift_date, bt.created_at) DESC, COALESCE(ss.start_time, TIME(bt.created_at)) DESC, bt.created_at DESC");
    }

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
