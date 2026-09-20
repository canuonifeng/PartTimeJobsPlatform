package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ReviewMapper {

    List<Review> findByFilters(@Param("reviewType") String reviewType,
                               @Param("isViolation") Boolean isViolation,
                               @Param("rating") Integer rating,
                               @Param("keyword") String keyword);

    Optional<Review> findById(@Param("id") Long id);

    int markViolation(@Param("id") Long id,
                      @Param("reason") String reason,
                      @Param("operatorName") String operatorName);

    int softDelete(@Param("id") Long id);

    int updateWorkerCreditScore(@Param("targetId") Long targetId, @Param("delta") Integer delta);

    int updateEnterpriseCreditScore(@Param("targetId") Long targetId, @Param("delta") Integer delta);

    int insertCreditScoreLog(@Param("targetType") String targetType,
                             @Param("targetId") Long targetId,
                             @Param("delta") Integer delta,
                             @Param("reason") String reason,
                             @Param("operatorName") String operatorName);
}
