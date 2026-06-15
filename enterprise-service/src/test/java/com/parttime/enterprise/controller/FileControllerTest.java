package com.parttime.enterprise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest {

    private MockMvc mockMvc;
    private FileController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new FileController();
        Path uploadDir = java.nio.file.Files.createTempDirectory("enterprise-upload-test");
        ReflectionTestUtils.setField(controller, "uploadDir", uploadDir.toString());
        ReflectionTestUtils.setField(controller, "publicBaseUrl", "https://api.example.com");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void upload_shouldUseConfiguredBaseUrlAndDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "job.png", "image/png", "image".getBytes());

        mockMvc.perform(multipart("/api/enterprise/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url", startsWith("https://api.example.com/uploads/enterprise/")))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void upload_shouldRejectUnsupportedFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "job.gif", "image/gif", "image".getBytes());

        mockMvc.perform(multipart("/api/enterprise/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("仅支持JPG、JPEG、PNG、PDF文件"));
    }
}
