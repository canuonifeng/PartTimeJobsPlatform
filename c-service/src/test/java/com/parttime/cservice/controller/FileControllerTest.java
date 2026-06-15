package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.FileUploadVO;
import com.parttime.cservice.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest {

    private MockMvc mockMvc;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private FileController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(controller, "publicBaseUrl", "https://api.example.com");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void upload_shouldReturnFileUrl() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        FileUploadVO vo = new FileUploadVO();
        vo.setUrl("https://api.example.com/api/worker/uploads/worker/1/front.jpg");
        vo.setFileName("front.jpg");
        vo.setSize(10L);
        vo.setContentType("image/jpeg");
        when(fileStorageService.uploadWorkerImage(eq(1L), any(), eq("https://api.example.com"))).thenReturn(vo);

        MockMultipartFile file = new MockMultipartFile("file", "front.jpg", "image/jpeg", "image".getBytes());

        mockMvc.perform(multipart("/api/worker/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://api.example.com/api/worker/uploads/worker/1/front.jpg"))
                .andExpect(jsonPath("$.data.fileName").value("front.jpg"));
    }

    @Test
    void upload_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        MockMultipartFile file = new MockMultipartFile("file", "front.jpg", "image/jpeg", "image".getBytes());

        mockMvc.perform(multipart("/api/worker/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
