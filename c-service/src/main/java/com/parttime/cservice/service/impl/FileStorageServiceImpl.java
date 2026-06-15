package com.parttime.cservice.service.impl;

import com.parttime.cservice.pojo.vo.FileUploadVO;
import com.parttime.cservice.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public FileUploadVO uploadWorkerImage(Long workerId, MultipartFile file, String publicBaseUrl) {
        validate(workerId, file);
        String originalName = file.getOriginalFilename();
        String extension = extensionOf(originalName);
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID() + extension;
        Path relativePath = Path.of("worker", String.valueOf(workerId), datePath, fileName);
        Path target = Path.of(uploadDir).resolve(relativePath).normalize();
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败");
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setFileName(fileName);
        vo.setSize(file.getSize());
        vo.setContentType(file.getContentType());
        vo.setUrl(normalizeBaseUrl(publicBaseUrl) + "/api/worker/uploads/" + relativePath.toString().replace('\\', '/'));
        return vo;
    }

    private void validate(Long workerId, MultipartFile file) {
        if (workerId == null) {
            throw new RuntimeException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("图片不能超过5MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("仅支持JPG、JPEG、PNG图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("仅支持JPG、JPEG、PNG图片");
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
