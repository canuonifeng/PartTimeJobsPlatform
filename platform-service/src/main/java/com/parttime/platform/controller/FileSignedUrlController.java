package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.SignedUrlVO;
import com.parttime.platform.service.OssSignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/files")
public class FileSignedUrlController {

    @Resource
    private OssSignedUrlService ossSignedUrlService;

    private String resolveAdminName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            return null;
        }
        return name;
    }

    @Operation(summary = "获取 OSS 私有文件签名 URL")
    @GetMapping("/signed-url")
    public ApiResponse<SignedUrlVO> getSignedUrl(@RequestParam("key") String key) {
        String adminName = resolveAdminName();
        if (adminName == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            SignedUrlVO vo = ossSignedUrlService.getSignedUrl(adminName, key);
            return ApiResponse.success(vo);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (SecurityException e) {
            return ApiResponse.error(403, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(503, e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(500, "服务器内部错误");
        }
    }
}
