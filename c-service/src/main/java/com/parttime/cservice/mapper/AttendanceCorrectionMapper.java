package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceCorrectionMapper {

    int insert(AttendanceCorrectionEntity correction);

    Optional<AttendanceCorrectionEntity> findById(Long id);

    Optional<AttendanceCorrectionEntity> findByShiftId(Long shiftId);

    List<AttendanceCorrectionEntity> findByShiftIds(@Param("shiftIds") List<Long> shiftIds);
}
