package com.parttime.cservice.controller;

import com.parttime.cservice.config.JwtUtil;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Resource
    private ReferralService referralService;

    @Resource
    private JwtUtil jwtUtil;

    @Operation(summary = "获取邀请链接")
    @GetMapping("/link")
    public ReferralLinkVO getReferralLink(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralLink(workerId);
    }

    @Operation(summary = "获取邀请海报")
    @GetMapping("/poster")
    public Map<String, String> getReferralPoster(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        String posterUrl = referralService.getReferralPoster(workerId);
        return Map.of("posterUrl", posterUrl);
    }

    @Operation(summary = "邀请统计概览")
    @GetMapping("/stats")
    public ReferralStatsVO getReferralStats(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralStats(workerId);
    }

    @Operation(summary = "被邀请人列表")
    @GetMapping("/referees")
    public PageVO<RefereeVO> getReferees(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferees(workerId, page, pageSize);
    }

    @Operation(summary = "我的邀请奖励列表")
    @GetMapping("/rewards")
    public PageVO<ReferralRewardVO> getReferralRewards(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralRewards(workerId, page, pageSize);
    }

    @Operation(summary = "获取奖励规则配置")
    @GetMapping("/config")
    public List<ReferralConfig> getConfig() {
        return referralService.getConfig();
    }

    @Operation(summary = "修改奖励规则配置")
    @PutMapping("/config")
    public void updateConfig(@RequestBody List<ReferralConfig> configs) {
        referralService.updateConfig(configs);
    }
}
