package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.FileUploadVO;
import com.parttime.cservice.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadWorkerImage_shouldSaveJpegAndReturnUrl() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "front.jpg", "image/jpeg", "image".getBytes());

        FileUploadVO vo = service.uploadWorkerImage(1L, file, "https://api.example.com");

        assertThat(vo.getUrl()).startsWith("https://api.example.com/api/worker/uploads/worker/1/");
        assertThat(vo.getFileName()).endsWith(".jpg");
        assertThat(vo.getSize()).isEqualTo(file.getSize());
        assertThat(tempDir.toFile().listFiles()).isNotEmpty();
    }

    @Test
    void uploadWorkerImage_shouldRejectUnsupportedFile() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "front.gif", "image/gif", "image".getBytes());

        assertThatThrownBy(() -> service.uploadWorkerImage(1L, file, "https://api.example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("仅支持");
    }
}
