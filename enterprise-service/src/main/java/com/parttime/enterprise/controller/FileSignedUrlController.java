package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.SignedUrlVO;
import com.parttime.enterprise.service.OssSignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/files")
public class FileSignedUrlController {

    @Resource
    private OssSignedUrlService ossSignedUrlService;

    private boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @Operation(summary = "获取 OSS 私有文件签名 URL")
    @GetMapping("/signed-url")
    public ApiResponse<SignedUrlVO> getSignedUrl(@RequestParam("key") String key) {
        if (!isLoggedIn()) {
            return ApiResponse.error(401, "未登录");
        }
        Long companyId = SecurityUtil.getCurrentCompanyId();
        try {
            SignedUrlVO vo = ossSignedUrlService.getSignedUrl(companyId, key);
            return ApiResponse.success(vo);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (SecurityException e) {
            return ApiResponse.error(403, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(503, e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}
