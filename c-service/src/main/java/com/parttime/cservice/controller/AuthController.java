package com.parttime.cservice.controller;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.pojo.cmd.PhoneLoginCmd;
import com.parttime.cservice.pojo.cmd.WeChatLoginCmd;
import com.parttime.cservice.pojo.cmd.WeChatPhoneLoginCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.cmd.LoginCmd;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.ReferralService;
import com.parttime.cservice.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/worker/auth")
public class AuthController {

    @Resource
    private WorkerService workerService;
    @Resource
    private ReferralService referralService;
    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "工人注册", description = "工人注册账号并返回JWT令牌")
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Parameter(description = "注册请求") @RequestBody RegisterCmd request) {
        WorkerVO worker = workerService.register(request);

        if (request.referralCode() != null && !request.referralCode().isEmpty()) {
            try {
                referralService.bindReferral(worker.getId(), request.referralCode());
            } catch (Exception e) {
                // Log error but don't fail registration
            }
        }

        String token = jwtTokenProvider.generateToken(String.valueOf(worker.getId()), List.of("ROLE_WORKER"));
        return ApiResponse.success(new LoginVO(token, worker.getId()));
    }

    @Operation(summary = "工人登录", description = "工人通过微信授权码登录")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Parameter(description = "登录请求") @RequestBody LoginCmd request) {
        String token = workerService.login(request.wechatCode());
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        return ApiResponse.success(new LoginVO(token, Long.valueOf(userId)));
    }

    @Operation(summary = "微信登录", description = "工人通过微信登录获取完整信息")
    @PostMapping("/wechat-login")
    public ApiResponse<LoginVO> wechatLogin(@Parameter(description = "微信登录请求") @RequestBody WeChatLoginCmd request) {
        LoginVO response = workerService.loginWithWechat(request.code(), request.referralCode());
        return ApiResponse.success(response);
    }

    @Operation(summary = "微信手机号登录", description = "工人通过微信授权获取手机号并登录")
    @PostMapping("/wechat-phone-login")
    public ApiResponse<LoginVO> wechatPhoneLogin(@Parameter(description = "微信手机号登录请求") @RequestBody WeChatPhoneLoginCmd request) {
        LoginVO response = workerService.loginWithWechatPhone(request, request.referralCode());
        return ApiResponse.success(response);
    }

    @Operation(summary = "发送短信验证码", description = "向手机号发送登录验证码")
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestBody PhoneLoginCmd request) {
        workerService.sendSmsCode(request.phone());
        return ApiResponse.success();
    }

    @Operation(summary = "手机号登录", description = "通过手机号+验证码登录")
    @PostMapping("/phone-login")
    public ApiResponse<LoginVO> phoneLogin(@RequestBody PhoneLoginCmd request) {
        LoginVO response = workerService.loginByPhone(request, request.referralCode());
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取当前工人信息", description = "获取当前登录工人的基本信息")
    @GetMapping("/profile")
    public ApiResponse<WorkerVO> profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(authentication.getName());
        WorkerVO worker = workerService.getWorkerById(workerId);
        return ApiResponse.success(worker);
    }

    @Operation(summary = "更新工人信息", description = "更新当前登录工人的基本信息")
    @PostMapping("/profile")
    public ApiResponse<WorkerVO> updateProfile(@Parameter(description = "更新信息") @RequestBody RegisterCmd request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(authentication.getName());
        WorkerVO worker = workerService.updateProfile(workerId, request);
        return ApiResponse.success(worker);
    }

}
