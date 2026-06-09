package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobMapper {
    int insert(Job job);
    Optional<Job> findById(Long id);
    Optional<Job> findByJobId(Long jobId);
    List<Job> findAll();
    List<Job> search(@Param("keyword") String keyword, @Param("location") String location, @Param("categoryId") Long categoryId);
    List<Job> findByCompanyId(Long companyId);
    int update(Job job);
    int batchCloseJobs(@Param("ids") List<Long> ids, @Param("closeReason") String closeReason);
}
