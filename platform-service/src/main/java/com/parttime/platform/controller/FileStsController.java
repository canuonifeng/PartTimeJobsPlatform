package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.OssStsVO;
import com.parttime.platform.service.OssStsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/files")
public class FileStsController {

    @Resource
    private OssStsService ossStsService;

    private Long resolveAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Operation(summary = "获取 OSS 直传 STS 凭证")
    @PostMapping("/sts")
    public ApiResponse<OssStsVO> issueSts(@RequestParam("biz") String biz) {
        Long adminId = resolveAdminId();
        if (adminId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            OssStsVO vo = ossStsService.issueSts(adminId, biz);
            return ApiResponse.success(vo);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(503, e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(500, "服务器内部错误");
        }
    }
}
