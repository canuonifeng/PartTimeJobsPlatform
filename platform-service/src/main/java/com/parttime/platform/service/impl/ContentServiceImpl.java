package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.BannerMapper;
import com.parttime.platform.mapper.JobCategoryMapper;
import com.parttime.platform.mapper.JobMapper;
import com.parttime.platform.mapper.SystemConfigMapper;
import com.parttime.platform.pojo.cmd.BannerCmd;
import com.parttime.platform.pojo.cmd.HotRecommendationCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ServiceFeeRateCmd;
import com.parttime.platform.pojo.entity.Banner;
import com.parttime.platform.pojo.entity.Job;
import com.parttime.platform.pojo.entity.JobCategory;
import com.parttime.platform.pojo.vo.BannerVO;
import com.parttime.platform.pojo.vo.HotRecommendationVO;
import com.parttime.platform.pojo.vo.ServiceFeeRateVO;
import com.parttime.platform.service.ContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentServiceImpl implements ContentService {

    private static final String DEFAULT_RATE_KEY = "platform_fee_rate";
    private static final String RATE_KEY_PREFIX = "service_fee_rate_cat_";
    private static final String REGION_KEY = "platform_regions";

    @Resource
    private BannerMapper bannerMapper;

    @Resource
    private JobMapper jobMapper;

    @Resource
    private JobCategoryMapper jobCategoryMapper;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public List<BannerVO> banners() {
        return bannerMapper.findAll().stream().map(this::toBannerVO).collect(Collectors.toList());
    }

    @Override
    public void createBanner(BannerCmd cmd) {
        Banner banner = new Banner();
        applyCmd(banner, cmd);
        banner.setStatus(cmd.getStatus() == null ? "ACTIVE" : cmd.getStatus());
        banner.setPosition(cmd.getPosition() == null ? "HOME" : cmd.getPosition());
        bannerMapper.insert(banner);
    }

    @Override
    public void updateBanner(BannerCmd cmd) {
        Banner banner = bannerMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("轮播图不存在: " + cmd.getId()));
        applyCmd(banner, cmd);
        if (cmd.getStatus() != null) {
            banner.setStatus(cmd.getStatus());
        }
        bannerMapper.update(banner);
    }

    @Override
    public void deleteBanner(IdCmd cmd) {
        bannerMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("轮播图不存在: " + cmd.getId()));
        bannerMapper.deleteById(cmd.getId());
    }

    @Override
    public List<HotRecommendationVO> hotRecommendations() {
        return jobMapper.findRecommended().stream().map(this::toHotVO).collect(Collectors.toList());
    }

    @Override
    public void addHotRecommendation(HotRecommendationCmd cmd) {
        jobMapper.findById(cmd.getJobId())
                .orElseThrow(() -> new BusinessException("职位不存在: " + cmd.getJobId()));
        jobMapper.updateRecommended(cmd.getJobId(), true);
    }

    @Override
    public void removeHotRecommendation(IdCmd cmd) {
        jobMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("职位不存在: " + cmd.getId()));
        jobMapper.updateRecommended(cmd.getId(), false);
    }

    @Override
    public List<ServiceFeeRateVO> serviceFeeRates() {
        BigDecimal defaultRate = systemConfigMapper.findByKey(DEFAULT_RATE_KEY)
                .map(c -> new BigDecimal(c.getConfigValue()))
                .orElse(new BigDecimal("0.10"));

        ServiceFeeRateVO defaultVo = new ServiceFeeRateVO();
        defaultVo.setCategoryId(null);
        defaultVo.setCategoryName("默认费率");
        defaultVo.setRate(defaultRate);

        List<ServiceFeeRateVO> list = new java.util.ArrayList<>();
        list.add(defaultVo);

        for (JobCategory cat : jobCategoryMapper.findAll()) {
            if (!"ACTIVE".equals(cat.getStatus())) {
                continue;
            }
            ServiceFeeRateVO vo = new ServiceFeeRateVO();
            vo.setCategoryId(cat.getId());
            vo.setCategoryName(cat.getName());
            vo.setRate(systemConfigMapper.findByKey(RATE_KEY_PREFIX + cat.getId())
                    .map(c -> new BigDecimal(c.getConfigValue()))
                    .orElse(defaultRate));
            list.add(vo);
        }
        return list;
    }

    @Override
    public void updateServiceFeeRate(ServiceFeeRateCmd cmd) {
        String key = cmd.getCategoryId() == null
                ? DEFAULT_RATE_KEY
                : RATE_KEY_PREFIX + cmd.getCategoryId();
        systemConfigMapper.updateByKey(key, cmd.getRate().toPlainString());
    }

    @Override
    public List<String> regions() {
        String value = systemConfigMapper.findByKey(REGION_KEY)
                .map(c -> c.getConfigValue())
                .orElse("杭州,宁波,温州,金华,上海,南京,苏州,北京,深圳,广州");
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void applyCmd(Banner banner, BannerCmd cmd) {
        banner.setTitle(cmd.getTitle());
        banner.setImageUrl(cmd.getImageUrl());
        banner.setLinkUrl(cmd.getLinkUrl());
        banner.setPosition(cmd.getPosition());
        banner.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
        banner.setStartTime(cmd.getStartTime());
        banner.setEndTime(cmd.getEndTime());
    }

    private BannerVO toBannerVO(Banner b) {
        BannerVO vo = new BannerVO();
        vo.setId(b.getId());
        vo.setTitle(b.getTitle());
        vo.setImageUrl(b.getImageUrl());
        vo.setLinkUrl(b.getLinkUrl());
        vo.setPosition(b.getPosition());
        vo.setSortOrder(b.getSortOrder());
        vo.setStatus(b.getStatus());
        vo.setStartTime(b.getStartTime());
        vo.setEndTime(b.getEndTime());
        vo.setClickCount(b.getClickCount());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }

    private HotRecommendationVO toHotVO(Job j) {
        HotRecommendationVO vo = new HotRecommendationVO();
        vo.setJobId(j.getId());
        vo.setJobTitle(j.getTitle());
        vo.setCompanyName(j.getCompanyName());
        vo.setCategoryName(j.getCategoryName());
        vo.setImageUrl(j.getImageUrl());
        return vo;
    }
}
