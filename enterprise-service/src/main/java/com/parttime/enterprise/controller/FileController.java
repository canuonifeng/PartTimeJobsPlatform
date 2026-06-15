package com.parttime.enterprise.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.FileUploadVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/enterprise/files")
public class FileController {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".pdf");

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.public-base-url:}")
    private String publicBaseUrl;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件不能超过10MB");
        }
        try {
            String extension = extensionOf(file.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ApiResponse.error("仅支持JPG、JPEG、PNG、PDF文件");
            }
            String datePath = LocalDate.now().format(DATE_FORMATTER);
            String filename = UUID.randomUUID() + extension;
            Path relativePath = Path.of("enterprise", datePath, filename);
            Path target = Path.of(uploadDir).resolve(relativePath).normalize();
            Files.createDirectories(target.getParent());
            file.transferTo(target);

            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(normalizeBaseUrl(publicBaseUrl) + "/uploads/" + relativePath.toString().replace('\\', '/'));
            vo.setFileName(filename);
            vo.setSize(file.getSize());
            vo.setContentType(file.getContentType());
            return ApiResponse.success(vo);
        } catch (IOException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String normalizeBaseUrl(String publicBaseUrl) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return "";
        }
        return publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }
}
