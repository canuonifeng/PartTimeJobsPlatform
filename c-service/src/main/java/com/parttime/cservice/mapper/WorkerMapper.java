package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.Worker;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerMapper {
    int insert(Worker worker);
    Optional<Worker> findById(Long id);
    Optional<Worker> findByPhone(String phone);
    Optional<Worker> findByWechatCode(String wechatCode);
    Optional<Worker> findByOpenId(String openId);
    List<Worker> findAll();
    int update(Worker worker);
}
