package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobMapper {

    int insert(Job job);

    Optional<Job> findById(Long id);

    List<Job> findAll();

    List<Job> findByCompanyId(Long companyId);

    List<Job> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    List<Job> findByCategoryId(Long categoryId);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int update(Job job);

    int delete(Long id);

    List<Job> findByIds(@Param("ids") List<Long> ids);
}
