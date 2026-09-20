package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.BannerCmd;
import com.parttime.platform.pojo.cmd.HotRecommendationCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ServiceFeeRateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.BannerVO;
import com.parttime.platform.pojo.vo.HotRecommendationVO;
import com.parttime.platform.pojo.vo.ServiceFeeRateVO;
import com.parttime.platform.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content")
public class ContentController {

    @Resource
    private ContentService contentService;

    @Operation(summary = "获取轮播图列表")
    @PostMapping("/banners")
    public ApiResponse<List<BannerVO>> banners() {
        return ApiResponse.success(contentService.banners());
    }

    @Operation(summary = "创建轮播图")
    @PostMapping("/banners/create")
    public ApiResponse<Void> createBanner(@RequestBody BannerCmd body) {
        contentService.createBanner(body);
        return ApiResponse.success();
    }

    @Operation(summary = "更新轮播图")
    @PostMapping("/banners/update")
    public ApiResponse<Void> updateBanner(@RequestBody BannerCmd body) {
        contentService.updateBanner(body);
        return ApiResponse.success();
    }

    @Operation(summary = "删除轮播图")
    @PostMapping("/banners/delete")
    public ApiResponse<Void> deleteBanner(@RequestBody IdCmd body) {
        contentService.deleteBanner(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取热门推荐列表")
    @PostMapping("/hot-recommendations")
    public ApiResponse<List<HotRecommendationVO>> hotRecommendations() {
        return ApiResponse.success(contentService.hotRecommendations());
    }

    @Operation(summary = "添加热门推荐")
    @PostMapping("/hot-recommendations/add")
    public ApiResponse<Void> addHotRecommendation(@RequestBody HotRecommendationCmd body) {
        contentService.addHotRecommendation(body);
        return ApiResponse.success();
    }

    @Operation(summary = "移除热门推荐")
    @PostMapping("/hot-recommendations/remove")
    public ApiResponse<Void> removeHotRecommendation(@RequestBody IdCmd body) {
        contentService.removeHotRecommendation(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取服务费率配置")
    @PostMapping("/service-fee-rates")
    public ApiResponse<List<ServiceFeeRateVO>> serviceFeeRates() {
        return ApiResponse.success(contentService.serviceFeeRates());
    }

    @Operation(summary = "更新服务费率")
    @PostMapping("/service-fee-rates/update")
    public ApiResponse<Void> updateServiceFeeRate(@RequestBody ServiceFeeRateCmd body) {
        contentService.updateServiceFeeRate(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取地区列表")
    @PostMapping("/regions")
    public ApiResponse<List<String>> regions() {
        return ApiResponse.success(contentService.regions());
    }
}
