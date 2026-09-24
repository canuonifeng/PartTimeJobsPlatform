package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
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

@RestController
@RequestMapping("/api/enterprise/files")
public class FileStsController {

    @Resource
    private OssStsService ossStsService;

    private boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @Operation(summary = "获取 OSS 直传 STS 凭证")
    @PostMapping("/sts")
    public ApiResponse<OssStsVO> issueSts(@RequestParam("biz") String biz) {
        if (!isLoggedIn()) {
            return ApiResponse.error(401, "未登录");
        }
        Long companyId = SecurityUtil.getCurrentCompanyId();
        try {
            OssStsVO vo = ossStsService.issueSts(companyId, biz);
            return ApiResponse.success(vo);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(503, e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}
