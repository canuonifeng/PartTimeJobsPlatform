package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BannerMapper {

    List<Banner> findAll();

    Optional<Banner> findById(@Param("id") Long id);

    int insert(Banner banner);

    int update(Banner banner);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteById(@Param("id") Long id);
}
