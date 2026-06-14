package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.ReferralConfigUpdateCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralPosterVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/worker/referral")
public class ReferralController {

    @Resource
    private ReferralService referralService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取邀请链接")
    @GetMapping("/link")
    public ReferralLinkVO getReferralLink() {
        Long workerId = getCurrentWorkerId();
        return referralService.getReferralLink(workerId);
    }

    @Operation(summary = "获取邀请海报")
    @GetMapping("/poster")
    public ApiResponse<ReferralPosterVO> getReferralPoster() {
        Long workerId = getCurrentWorkerId();
        String posterUrl = referralService.getReferralPoster(workerId);
        return ApiResponse.success(new ReferralPosterVO(posterUrl));
    }

    @Operation(summary = "邀请统计概览")
    @GetMapping("/stats")
    public ReferralStatsVO getReferralStats() {
        Long workerId = getCurrentWorkerId();
        return referralService.getReferralStats(workerId);
    }

    @Operation(summary = "被邀请人列表")
    @GetMapping("/referees")
    public PageVO<RefereeVO> getReferees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = getCurrentWorkerId();
        return referralService.getReferees(workerId, page, pageSize);
    }

    @Operation(summary = "我的邀请奖励列表")
    @GetMapping("/rewards")
    public PageVO<ReferralRewardVO> getReferralRewards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = getCurrentWorkerId();
        return referralService.getReferralRewards(workerId, page, pageSize);
    }

    @Operation(summary = "获取奖励规则配置")
    @GetMapping("/config")
    public List<ReferralConfig> getConfig() {
        return referralService.getConfig();
    }

    @Operation(summary = "修改奖励规则配置")
    @PostMapping("/config")
    public void updateConfig(@RequestBody ReferralConfigUpdateCmd cmd) {
        referralService.updateConfig(cmd.getConfigs());
    }
}
