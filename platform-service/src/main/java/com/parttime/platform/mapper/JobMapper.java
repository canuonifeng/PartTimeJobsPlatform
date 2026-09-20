package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobMapper {

    Optional<Job> findById(Long id);

    List<Job> findAll();

    List<Job> findByFilters(@Param("status") String status,
                             @Param("companyId") Long companyId,
                             @Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateTop(@Param("id") Long id, @Param("isTop") Boolean isTop);

    int updateRecommended(@Param("id") Long id, @Param("isRecommended") Boolean isRecommended);

    List<Job> findRecommended();
}
