package com.parttime.enterprise.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.config.WeChatMiniProgramConfig;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.vo.JobShareCodeVO;
import com.parttime.enterprise.pojo.vo.JobShareLinkVO;
import com.parttime.enterprise.service.JobShareService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.net.URLEncoder;
import javax.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class JobShareServiceImpl implements JobShareService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private JobMapper jobMapper;

    @Resource
    private WeChatMiniProgramConfig weChatMiniProgramConfig;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public JobShareCodeVO getShareCode(Long jobId) {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        Long currentCompanyId = SecurityUtil.getCurrentCompanyId();
        if (!currentCompanyId.equals(job.getCompanyId())) {
            throw new BusinessException("无权分享该职位");
        }
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new BusinessException("仅已发布职位可生成二维码");
        }

        String scene = String.valueOf(jobId);
        String path = "/pages/jobs/jobDetail?scene=" + scene;
        String accessToken = fetchAccessToken();
        byte[] imageBytes = fetchCodeImage(accessToken, scene, "/pages/jobs/jobDetail");
        return new JobShareCodeVO(jobId, path, Base64.getEncoder().encodeToString(imageBytes));
    }

    @Override
    public JobShareLinkVO getShareLink(Long jobId) {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        Long currentCompanyId = SecurityUtil.getCurrentCompanyId();
        if (!currentCompanyId.equals(job.getCompanyId())) {
            throw new BusinessException("无权分享该职位");
        }
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new BusinessException("仅已发布职位可生成链接");
        }

        String query = URLEncoder.encode("id=" + jobId, StandardCharsets.UTF_8);
        String link = "weixin://dl/business/?appid=" + weChatMiniProgramConfig.getCustomerAppId()
                + "&path=/pages/jobs/jobDetail"
                + "&query=" + query
                + "&env_version=release";
        return new JobShareLinkVO(jobId, link);
    }

    private String fetchAccessToken() {
        String url = UriComponentsBuilder.fromHttpUrl(weChatMiniProgramConfig.getTokenUrl())
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", weChatMiniProgramConfig.getWorkerAppId())
                .queryParam("secret", weChatMiniProgramConfig.getWorkerAppSecret())
                .toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        try {
            Map<String, Object> payload = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object accessToken = payload.get("access_token");
            if (accessToken instanceof String token && !token.isBlank()) {
                return token;
            }
            throw new BusinessException("获取微信 access_token 失败");
        } catch (Exception e) {
            throw new BusinessException("获取微信 access_token 失败");
        }
    }

    private byte[] fetchCodeImage(String accessToken, String scene, String page) {
        String url = UriComponentsBuilder.fromHttpUrl(weChatMiniProgramConfig.getCodeUrl())
                .queryParam("access_token", accessToken)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "scene", scene,
                "page", page,
                "check_path", false,
                "width", 430
        );

        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), byte[].class);
        byte[] imageBytes = response.getBody();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException("生成二维码失败");
        }

        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            String message = new String(imageBytes, StandardCharsets.UTF_8);
            throw new BusinessException("生成二维码失败：" + message);
        }

        return imageBytes;
    }
}
