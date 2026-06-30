package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/content")
public class ContentController {

    @Operation(summary = "获取轮播图列表")
    @PostMapping("/banners")
    public ApiResponse<List<Map<String, Object>>> banners() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("title", "轮播图" + i);
            item.put("imageUrl", "https://example.com/banner" + i + ".jpg");
            item.put("linkUrl", "/pages/jobs/" + i);
            item.put("sortOrder", i);
            item.put("enabled", true);
            item.put("createdAt", LocalDateTime.now().minusDays(i * 10).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "创建轮播图")
    @PostMapping("/banners/create")
    public ApiResponse<Void> createBanner(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "更新轮播图")
    @PostMapping("/banners/update")
    public ApiResponse<Void> updateBanner(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "删除轮播图")
    @PostMapping("/banners/delete")
    public ApiResponse<Void> deleteBanner(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取热门推荐列表")
    @PostMapping("/hot-recommendations")
    public ApiResponse<List<Map<String, Object>>> hotRecommendations() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("jobId", 100 + i);
            item.put("jobTitle", "热门职位" + i);
            item.put("companyName", "公司名称" + i);
            item.put("sortOrder", i);
            item.put("enabled", true);
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "添加热门推荐")
    @PostMapping("/hot-recommendations/add")
    public ApiResponse<Void> addHotRecommendation(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "移除热门推荐")
    @PostMapping("/hot-recommendations/remove")
    public ApiResponse<Void> removeHotRecommendation(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取服务费率配置")
    @PostMapping("/service-fee-rates")
    public ApiResponse<List<Map<String, Object>>> serviceFeeRates() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> defaultRate = new HashMap<>();
        defaultRate.put("id", 0);
        defaultRate.put("categoryId", null);
        defaultRate.put("categoryName", "默认费率");
        defaultRate.put("rate", new BigDecimal("0.05"));
        list.add(defaultRate);
        
        String[] cats = {"餐饮服务", "物流配送", "家政保洁", "商超零售"};
        for (int i = 0; i < cats.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("categoryId", i + 1);
            item.put("categoryName", cats[i]);
            item.put("rate", new BigDecimal("0.0" + (4 + i)));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "更新服务费率")
    @PostMapping("/service-fee-rates/update")
    public ApiResponse<Void> updateServiceFeeRate(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }
}
