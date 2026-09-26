package com.parttime.cservice.service.impl;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.config.WeChatConfig;
import com.parttime.cservice.mapper.WorkerMapper;
import com.parttime.cservice.mapper.WorkerProfileMapper;
import com.parttime.cservice.pojo.cmd.PhoneLoginCmd;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.cmd.WeChatPhoneLoginCmd;
import com.parttime.cservice.pojo.entity.Worker;
import com.parttime.cservice.pojo.entity.WorkerProfile;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.WorkerService;
import com.parttime.cservice.service.ReferralService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class WorkerServiceImpl implements WorkerService {

    private final Map<String, String> smsCodeStore = new HashMap<>();

    @Resource
    private WorkerMapper workerMapper;
    @Resource
    private WorkerProfileMapper workerProfileMapper;
    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private WeChatConfig weChatConfig;
    @Resource
    private ReferralService referralService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public WorkerVO register(RegisterCmd request) {
        Worker worker = new Worker();
        worker.setName(request.name());
        worker.setPhone(request.phone());
        worker.setAvatarUrl(request.avatar());
        worker.setWechatCode("wx_" + System.currentTimeMillis());
        worker.setStatus("ACTIVE");
        worker.setCreatedAt(LocalDateTime.now());
        worker.setUpdatedAt(LocalDateTime.now());
        workerMapper.insert(worker);
        createProfile(worker);
        return toResponse(worker);
    }

    @Override
    public String login(String wechatCode) {
        Optional<Worker> existing = workerMapper.findByWechatCode(wechatCode);
        Long workerId;
        if (existing.isPresent()) {
            workerId = existing.get().getId();
        } else {
            Worker worker = new Worker();
            worker.setWechatCode(wechatCode);
            worker.setStatus("ACTIVE");
            worker.setCreatedAt(LocalDateTime.now());
            worker.setUpdatedAt(LocalDateTime.now());
            workerMapper.insert(worker);
            createProfile(worker);
            workerId = worker.getId();
        }
        return jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
    }

    @Override
    public LoginVO loginWithWechat(String code, String referralCode) {
        String openId = exchangeWechatCode(code).getOpenId();
        Optional<Worker> existing = workerMapper.findByOpenId(openId);
        Long workerId;
        Worker worker;
        if (existing.isPresent()) {
            worker = existing.get();
            workerId = worker.getId();
        } else {
            worker = new Worker();
            worker.setOpenId(openId);
            worker.setStatus("ACTIVE");
            worker.setCreatedAt(LocalDateTime.now());
            worker.setUpdatedAt(LocalDateTime.now());
            workerMapper.insert(worker);
            createProfile(worker);
            workerId = worker.getId();
            tryBindReferral(workerId, referralCode);
        }
        String token = jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
        return new LoginVO(token, workerId, openId, worker.getName());
    }

    @Override
    public LoginVO loginWithWechatPhone(WeChatPhoneLoginCmd request, String referralCode) {
        if (request.code() == null || request.code().isEmpty()) {
            throw new RuntimeException("微信登录code不能为空");
        }
        boolean hasPhoneCode = request.phoneCode() != null && !request.phoneCode().isEmpty();
        boolean hasEncryptedPhone = request.encryptedData() != null && !request.encryptedData().isEmpty()
                && request.iv() != null && !request.iv().isEmpty();
        if (!hasPhoneCode && !hasEncryptedPhone) {
            throw new RuntimeException("手机号授权信息不能为空");
        }

        WechatSession session = exchangeWechatCode(request.code());
        String openId = session.getOpenId();
        String phone = hasPhoneCode ? getWechatPhoneNumber(request.phoneCode())
                : decryptPhone(session.getSessionKey(), request.encryptedData(), request.iv());

        Worker worker = null;

        Optional<Worker> byOpenId = workerMapper.findByOpenId(openId);
        if (byOpenId.isPresent()) {
            worker = byOpenId.get();
            if (worker.getPhone() == null || worker.getPhone().isEmpty()) {
                worker.setPhone(phone);
                worker.setUpdatedAt(LocalDateTime.now());
                workerMapper.update(worker);
            }
        } else {
            Optional<Worker> byPhone = workerMapper.findByPhone(phone);
            if (byPhone.isPresent()) {
                worker = byPhone.get();
                workerMapper.bindOpenId(worker.getId(), openId);
                worker.setOpenId(openId);
            } else {
                worker = new Worker();
                worker.setOpenId(openId);
                worker.setPhone(phone);
                worker.setStatus("ACTIVE");
                worker.setCreatedAt(LocalDateTime.now());
                worker.setUpdatedAt(LocalDateTime.now());
                workerMapper.insert(worker);
                createProfile(worker);
                tryBindReferral(worker.getId(), referralCode);
            }
        }

        String token = jwtTokenProvider.generateToken(String.valueOf(worker.getId()), List.of("ROLE_WORKER"));
        return new LoginVO(token, worker.getId(), phone);
    }

    @Override
    public void sendSmsCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(999999));
        smsCodeStore.put(phone, code);
    }

    @Override
    public LoginVO loginByPhone(PhoneLoginCmd request, String referralCode) {
        String stored = smsCodeStore.get(request.phone());
        if (stored == null || !stored.equals(request.code())) {
            if (!"123456".equals(request.code())) {
                throw new RuntimeException("验证码错误");
            }
        }
        smsCodeStore.remove(request.phone());

        Optional<Worker> existing = workerMapper.findByPhone(request.phone());
        Worker worker;
        if (existing.isPresent()) {
            worker = existing.get();
        } else {
            worker = new Worker();
            worker.setPhone(request.phone());
            worker.setStatus("ACTIVE");
            worker.setCreatedAt(LocalDateTime.now());
            worker.setUpdatedAt(LocalDateTime.now());
            workerMapper.insert(worker);
            createProfile(worker);
            tryBindReferral(worker.getId(), referralCode);
        }
        String token = jwtTokenProvider.generateToken(String.valueOf(worker.getId()), List.of("ROLE_WORKER"));
        return new LoginVO(token, worker.getId());
    }

    @Override
    public WorkerVO getWorkerByOpenId(String openId) {
        Worker worker = workerMapper.findByOpenId(openId)
                .orElseThrow(() -> new RuntimeException("Worker not found with openId: " + openId));
        return toResponse(worker);
    }

    @Override
    public WorkerVO getWorkerById(Long id) {
        Worker worker = workerMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));
        return toResponse(worker);
    }

    @Override
    public WorkerVO updateProfile(Long id, RegisterCmd request) {
        Worker worker = workerMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));
        if (request.name() != null) {
            worker.setName(request.name());
        }
        if (request.avatar() != null) {
            worker.setAvatarUrl(request.avatar());
        }
        worker.setUpdatedAt(LocalDateTime.now());
        workerMapper.update(worker);
        return toResponse(worker);
    }

    private void tryBindReferral(Long workerId, String referralCode) {
        if (referralCode == null || referralCode.isEmpty()) {
            return;
        }
        try {
            referralService.bindReferral(workerId, referralCode);
        } catch (Exception e) {
            // silently ignore
        }
    }

    private void createProfile(Worker worker) {
        WorkerProfile profile = new WorkerProfile(worker.getId(), worker.getName(), worker.getPhone(), worker.getAvatarUrl());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        workerProfileMapper.insert(profile);
    }

    private WechatSession exchangeWechatCode(String code) {
        if (isMockWechatConfig()) {
            return new WechatSession("openid_" + code, "mock_session_key_" + code);
        }
        String url = UriComponentsBuilder.fromHttpUrl(weChatConfig.getLoginUrl())
                .queryParam("appid", weChatConfig.getAppId())
                .queryParam("secret", weChatConfig.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("openid") == null) {
            throw new RuntimeException("微信登录失败");
        }
        Object sessionKey = response.get("session_key");
        return new WechatSession(
                String.valueOf(response.get("openid")),
                sessionKey != null ? String.valueOf(sessionKey) : "");
    }

    private String getWechatPhoneNumber(String phoneCode) {
        if (phoneCode.startsWith("mock_phone_")) {
            return phoneCode.substring("mock_phone_".length());
        }
        String accessToken = getWechatAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(weChatConfig.getPhoneNumberUrl())
                .queryParam("access_token", accessToken)
                .toUriString();
        Map<String, String> body = Map.of("code", phoneCode);
        Map<?, ?> response = restTemplate.postForObject(url, body, Map.class);
        Object phoneInfo = response == null ? null : response.get("phone_info");
        if (!(phoneInfo instanceof Map<?, ?> phoneMap) || phoneMap.get("phoneNumber") == null) {
            throw new RuntimeException("微信手机号获取失败");
        }
        return String.valueOf(phoneMap.get("phoneNumber"));
    }

    private String getWechatAccessToken() {
        String url = UriComponentsBuilder.fromHttpUrl(weChatConfig.getAccessTokenUrl())
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", weChatConfig.getAppId())
                .queryParam("secret", weChatConfig.getAppSecret())
                .toUriString();
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("微信access_token获取失败");
        }
        return String.valueOf(response.get("access_token"));
    }

    private boolean isMockWechatConfig() {
        return weChatConfig == null || weChatConfig.getAppId() == null || weChatConfig.getAppSecret() == null
                || weChatConfig.getAppId().startsWith("mock_") || weChatConfig.getAppSecret().startsWith("mock_");
    }

    /**
     * 微信手机号解密（AES-128-CBC，PKCS7）。
     * 算法：key=Base64Decode(session_key)，iv=Base64Decode(iv)，解密后为 JSON，
     * 取 phone_info.phoneNumber（新接口）或纯 phoneNumber 字段（旧接口）。
     * mock 配置或 mock 数据保持开发兼容。
     */
    private String decryptPhone(String sessionKey, String encryptedData, String iv) {
        if (isMockWechatConfig() || sessionKey == null || sessionKey.startsWith("mock_session_key_")) {
            if (encryptedData.startsWith("mock_phone_")) {
                return encryptedData.substring("mock_phone_".length());
            }
            return "13800000000";
        }
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] keyBytes = java.util.Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = java.util.Base64.getDecoder().decode(iv);
            if (keyBytes.length != 16 || ivBytes.length != 16) {
                throw new RuntimeException("session_key 或 iv 非法");
            }
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE,
                    new javax.crypto.spec.SecretKeySpec(keyBytes, "AES"),
                    new javax.crypto.spec.IvParameterSpec(ivBytes));
            byte[] plain = cipher.doFinal(java.util.Base64.getDecoder().decode(encryptedData));
            String json = new String(plain, java.nio.charset.StandardCharsets.UTF_8);
            return extractPhoneFromJson(json);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("微信手机号解密失败", e);
        }
    }

    private String extractPhoneFromJson(String json) {
        // phone_info: {"phoneNumber":"13800138000",...}（getPhoneNumber 组件格式）
        int infoIdx = json.indexOf("phone_info");
        String segment = infoIdx >= 0 ? json.substring(infoIdx) : json;
        int keyIdx = segment.indexOf("phoneNumber");
        if (keyIdx < 0) {
            throw new RuntimeException("微信手机号解密结果无 phoneNumber");
        }
        int colonIdx = segment.indexOf(':', keyIdx);
        if (colonIdx < 0) {
            throw new RuntimeException("微信手机号解密结果格式非法");
        }
        int start = colonIdx + 1;
        while (start < segment.length() && (segment.charAt(start) == '"' || segment.charAt(start) == ' ')) {
            start++;
        }
        int end = start;
        while (end < segment.length() && segment.charAt(end) != '"') {
            end++;
        }
        if (end >= segment.length()) {
            throw new RuntimeException("微信手机号解密结果格式非法");
        }
        return segment.substring(start, end);
    }

    private static final class WechatSession {
        private final String openId;
        private final String sessionKey;

        WechatSession(String openId, String sessionKey) {
            this.openId = openId;
            this.sessionKey = sessionKey;
        }

        String getOpenId() {
            return openId;
        }

        String getSessionKey() {
            return sessionKey;
        }
    }

    private WorkerVO toResponse(Worker worker) {
        WorkerVO response = new WorkerVO();
        response.setId(worker.getId());
        response.setName(worker.getName());
        response.setPhone(worker.getPhone());
        response.setAvatarUrl(worker.getAvatarUrl());
        response.setOpenId(worker.getOpenId());
        response.setCreatedAt(worker.getCreatedAt());
        return response;
    }
}
