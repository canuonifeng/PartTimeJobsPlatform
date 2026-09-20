package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Faq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FaqMapper {

    Optional<Faq> findById(@Param("id") Long id);

    List<Faq> findByFilters(@Param("keyword") String keyword,
                            @Param("category") String category,
                            @Param("status") String status);

    int insert(Faq faq);

    int update(Faq faq);

    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    int deleteById(@Param("id") Long id);
}
