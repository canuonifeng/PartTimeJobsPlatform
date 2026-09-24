package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.OssStsVO;
import com.parttime.enterprise.service.OssStsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/files")
public class FileStsController {

    @Resource
    private OssStsService ossStsService;

    private Long resolveCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object companyId = details.get("companyId");
            if (companyId instanceof Number num) {
                return num.longValue();
            }
        }
        return null;
    }

    @Operation(summary = "获取 OSS 直传 STS 凭证")
    @PostMapping("/sts")
    public ApiResponse<OssStsVO> issueSts(@RequestParam("biz") String biz) {
        Long companyId = resolveCompanyId();
        if (companyId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            OssStsVO vo = ossStsService.issueSts(companyId, biz);
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
