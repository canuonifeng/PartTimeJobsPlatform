package com.parttime.cservice.service;

import com.parttime.cservice.config.WeChatPayConfig;
import com.parttime.cservice.pojo.vo.TransferResult;
import com.parttime.cservice.service.impl.WeChatPayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WeChatPayServiceTest {

    private WeChatPayServiceImpl weChatPayService;

    @BeforeEach
    void setUp() {
        weChatPayService = new WeChatPayServiceImpl();
        WeChatPayConfig config = new WeChatPayConfig();
        config.setMchId("test_mch_id");
        config.setApiKey("test_api_key");
        ReflectionTestUtils.setField(weChatPayService, "weChatPayConfig", config);
    }

    @Test
    void transferToWechat_shouldReturnSuccess() {
        TransferResult result = weChatPayService.transferToWechat(
                100L, 
                BigDecimal.valueOf(100), 
                "test_open_id", 
                "测试转账"
        );
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransferNo());
        assertNotNull(result.getTransferTime());
    }

    @Test
    void transferToBankCard_shouldReturnSuccess() {
        TransferResult result = weChatPayService.transferToBankCard(
                100L, 
                BigDecimal.valueOf(100), 
                "6222021234567890123", 
                "工商银行", 
                "测试转账"
        );
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransferNo());
        assertNotNull(result.getTransferTime());
    }

    @Test
    void queryTransferStatus_shouldReturnSuccess() {
        TransferResult result = weChatPayService.queryTransferStatus("test_transfer_no");
        
        assertTrue(result.isSuccess());
        assertEquals("test_transfer_no", result.getTransferNo());
    }
}