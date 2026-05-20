package com.parttime.platform.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JobSyncMapperXmlTest {

    @Test
    void updateCompanyLogoByCompanyId_shouldBindNamedParams() throws Exception {
        Configuration configuration = new Configuration();
        try (Reader reader = Resources.getResourceAsReader("mapper/JobSyncMapper.xml")) {
            XMLMapperBuilder parser = new XMLMapperBuilder(reader, configuration, "mapper/JobSyncMapper.xml", configuration.getSqlFragments());
            parser.parse();
        }

        MappedStatement statement = configuration.getMappedStatement(
                "com.parttime.platform.mapper.JobSyncMapper.updateCompanyLogoByCompanyId");
        BoundSql boundSql = statement.getBoundSql(Map.of(
                "companyId", 7L,
                "companyLogo", "https://cdn.example.com/logo.png"
        ));

        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
        assertThat(sql).contains("UPDATE c_job");
        assertThat(sql).contains("SET company_logo = ?");
        assertThat(sql).contains("WHERE company_id = ?");
    }
}
