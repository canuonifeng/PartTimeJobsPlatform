package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.CompanyLocationMapper;
import com.parttime.enterprise.pojo.entity.CompanyLocation;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.impl.CompanyLocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyLocationServiceTest {

    @Mock
    private CompanyLocationMapper companyLocationMapper;

    @InjectMocks
    private CompanyLocationServiceImpl companyLocationService;

    @Test
    void list_shouldReturnPagedLocations() {
        CompanyLocation location = new CompanyLocation();
        location.setId(1L);
        location.setCompanyId(2L);
        location.setName("总部");
        location.setStatus("ENABLED");
        when(companyLocationMapper.countByCompanyId(2L)).thenReturn(1L);
        when(companyLocationMapper.findByCompanyIdPage(2L, 0, 20)).thenReturn(List.of(location));

        PageVO<?> result = companyLocationService.list(2L, 1, 20);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
        verify(companyLocationMapper).findByCompanyIdPage(2L, 0, 20);
    }
}
