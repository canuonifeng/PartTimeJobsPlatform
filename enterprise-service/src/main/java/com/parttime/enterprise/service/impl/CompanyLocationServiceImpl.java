package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyLocationMapper;
import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.entity.CompanyLocation;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.service.CompanyLocationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyLocationServiceImpl implements CompanyLocationService {

    @Resource
    private CompanyLocationMapper companyLocationMapper;

    @Override
    public List<CompanyLocationVO> list(Long companyId) {
        return companyLocationMapper.findByCompanyId(companyId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public CompanyLocationVO create(LocationCreateCmd cmd, Long companyId) {
        CompanyLocation entity = new CompanyLocation();
        entity.setCompanyId(companyId);
        entity.setName(cmd.getName());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        companyLocationMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public CompanyLocationVO update(LocationUpdateCmd cmd) {
        CompanyLocation entity = companyLocationMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("地点不存在"));
        entity.setName(cmd.getName());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        companyLocationMapper.update(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        CompanyLocation entity = companyLocationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("地点不存在"));
        companyLocationMapper.deleteById(id);
    }

    @Override
    public void enable(Long id) {
        companyLocationMapper.updateStatus(id, "ENABLED");
    }

    @Override
    public void disable(Long id) {
        companyLocationMapper.updateStatus(id, "DISABLED");
    }

    private CompanyLocationVO toVO(CompanyLocation entity) {
        CompanyLocationVO vo = new CompanyLocationVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
