package com.parttime.cservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.config.WeChatPayConfig;
import com.parttime.cservice.pojo.vo.TransferResult;
import com.parttime.cservice.service.WeChatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    private static final Logger log = LoggerFactory.getLogger(WeChatPayServiceImpl.class);

    private static final String API_BASE = "https://api.mch.weixin.qq.com";
    private static final String SCHEMA = "WECHATPAY2-SHA256-RSA2048";
    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private WeChatPayConfig weChatPayConfig;

    private final RestTemplate restTemplate = buildRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    @Override
    public TransferResult transferToWechat(Long workerId, BigDecimal amount, String openId, String description) {
        log.info("开始微信零钱转账: workerId={}, amount={}", workerId, amount);
        if (!isRealPayConfigured()) {
            return mockSuccess("WX");
        }
        TransferResult result = new TransferResult();
        try {
            String outBatchNo = "WXB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
            String outDetailNo = "WXD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
            long amountFen = amount.multiply(new BigDecimal("100")).longValueExact();

            String url = API_BASE + "/v3/transfer/batches";
            String body = "{\"appid\":\"" + weChatPayConfig.getAppId() + "\","
                    + "\"out_batch_no\":\"" + outBatchNo + "\","
                    + "\"batch_name\":\"零工提现\","
                    + "\"batch_remark\":\"" + escape(description) + "\","
                    + "\"total_amount\":" + amountFen + ","
                    + "\"total_num\":1,"
                    + "\"transfer_detail_list\":[{\"out_detail_no\":\"" + outDetailNo + "\","
                    + "\"transfer_amount\":" + amountFen + ","
                    + "\"transfer_remark\":\"" + escape(description) + "\","
                    + "\"openid\":\"" + escape(openId) + "\"}]}";

            JsonNode resp = doRequest(HttpMethod.POST, "/v3/transfer/batches", body);
            result.setSuccess(true);
            result.setTransferNo(outBatchNo);
            result.setTransferTime(LocalDateTime.now());
            log.info("微信零钱转账成功: outBatchNo={}, resp={}", outBatchNo, resp);
        } catch (Exception e) {
            log.error("微信零钱转账失败: workerId={}, amount={}", workerId, amount, e);
            result.setSuccess(false);
            result.setErrorCode("TRANSFER_FAILED");
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public TransferResult transferToBankCard(Long workerId, BigDecimal amount, String bankAccount, String bankName, String description) {
        log.info("开始银行卡转账: workerId={}, amount={}, bankName={}", workerId, amount, bankName);
        if (!isRealPayConfigured()) {
            return mockSuccess("BANK");
        }
        TransferResult result = new TransferResult();
        try {
            String outTradeNo = "BANK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
            long amountFen = amount.multiply(new BigDecimal("100")).longValueExact();

            String url = API_BASE + "/v3/fund-app/mch-transfer/transfer-bills";
            String body = "{\"appid\":\"" + weChatPayConfig.getAppId() + "\","
                    + "\"out_bill_no\":\"" + outTradeNo + "\","
                    + "\"transfer_scene_id\":\"1001\","
                    + "\"openid\":\"\","
                    + "\"transfer_amount\":" + amountFen + ","
                    + "\"transfer_remark\":\"" + escape(description) + "\","
                    + "\"bank_type\":\"\","
                    + "\"recipient_name\":\"" + escape(bankName) + "\","
                    + "\"recipient_account\":\"" + escape(bankAccount) + "\"}";

            JsonNode resp = doRequest(HttpMethod.POST, "/v3/fund-app/mch-transfer/transfer-bills", body);
            result.setSuccess(true);
            result.setTransferNo(outTradeNo);
            result.setTransferTime(LocalDateTime.now());
            log.info("银行卡转账成功: outTradeNo={}, resp={}", outTradeNo, resp);
        } catch (Exception e) {
            log.error("银行卡转账失败: workerId={}, amount={}", workerId, amount, e);
            result.setSuccess(false);
            result.setErrorCode("TRANSFER_FAILED");
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public TransferResult queryTransferStatus(String transferNo) {
        log.info("查询转账状态: transferNo={}", transferNo);
        if (!isRealPayConfigured()) {
            TransferResult result = new TransferResult();
            result.setSuccess(true);
            result.setTransferNo(transferNo);
            result.setTransferTime(LocalDateTime.now());
            return result;
        }
        TransferResult result = new TransferResult();
        try {
            String path = "/v3/transfer/batches/out-batch-no/" + transferNo + "?need_query_detail=0";
            JsonNode resp = doRequest(HttpMethod.GET, path, null);
            String state = resp.path("batch_status").asText("");
            result.setSuccess(!"CLOSED".equals(state) && !"WAIT_PAY".equals(state));
            result.setTransferNo(transferNo);
            result.setTransferTime(LocalDateTime.now());
            if (!result.isSuccess()) {
                result.setErrorCode("QUERY_STATE_" + state);
                result.setErrorMessage("转账状态: " + state);
            }
            log.info("查询转账状态成功: transferNo={}, state={}", transferNo, state);
        } catch (Exception e) {
            log.error("查询转账状态失败: transferNo={}", transferNo, e);
            result.setSuccess(false);
            result.setErrorCode("QUERY_FAILED");
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    private TransferResult mockSuccess(String prefix) {
        TransferResult result = new TransferResult();
        result.setSuccess(true);
        result.setTransferNo(prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8));
        result.setTransferTime(LocalDateTime.now());
        return result;
    }

    /**
     * 是否具备真实调用条件：商户号/APIv3密钥/商户证书私钥齐全且私钥文件存在。
     * 仅用于生产配置；dev/测试配置（占位符或文件缺失）自动走 mock。
     */
    private boolean isRealPayConfigured() {
        if (weChatPayConfig == null) {
            return false;
        }
        String mchId = weChatPayConfig.getMchId();
        String apiV3Key = weChatPayConfig.getApiV3Key();
        String privateKeyPath = weChatPayConfig.getPrivateKeyPath();
        if (!StringUtils.hasText(mchId) || !StringUtils.hasText(apiV3Key) || !StringUtils.hasText(privateKeyPath)) {
            return false;
        }
        if (mchId.startsWith("your_") || mchId.startsWith("test_") || apiV3Key.startsWith("your_")) {
            return false;
        }
        Path keyFile = Path.of(privateKeyPath);
        return Files.isReadable(keyFile);
    }

    private JsonNode doRequest(HttpMethod method, String path, String body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String message = method.name() + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + (body == null ? "" : body) + "\n";
        String signature = sign(message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", SCHEMA + " mchid=\"" + weChatPayConfig.getMchId() + "\",nonce_str=\"" + nonce
                + "\",signature=\"" + signature + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + certSerialNo() + "\"");
        if (body == null) {
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(API_BASE + path, method, entity, String.class);
        String respBody = response.getBody();
        if (respBody == null || respBody.isBlank()) {
            throw new RuntimeException("微信支付响应为空，http=" + response.getStatusCode());
        }
        JsonNode json = objectMapper.readTree(respBody);
        if (response.getStatusCodeValue() >= 400) {
            throw new RuntimeException("微信支付API错误: " + response.getStatusCode() + " " + json.path("message").asText(respBody));
        }
        return json;
    }

    /** 微信支付 v3 请求签名：SHA256withRSA（商户私钥） */
    private String sign(String message) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String pem = Files.readString(Path.of(weChatPayConfig.getPrivateKeyPath()), StandardCharsets.UTF_8);
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /** 从商户证书读取序列号（Authorization 头的 serial_no） */
    private String certSerialNo() {
        try {
            String certPath = weChatPayConfig.getCertPath();
            if (!StringUtils.hasText(certPath) || !Files.isReadable(Path.of(certPath))) {
                throw new IllegalStateException("商户证书不存在: " + certPath);
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (java.io.InputStream in = Files.newInputStream(Path.of(certPath))) {
                X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
                return cert.getSerialNumber().toString(16).toUpperCase();
            }
        } catch (Exception e) {
            throw new IllegalStateException("读取商户证书序列号失败", e);
        }
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
