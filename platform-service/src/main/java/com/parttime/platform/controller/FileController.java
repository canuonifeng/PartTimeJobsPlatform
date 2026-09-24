package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/api/admin/files")
public class FileController {

    private static final long MAX_FILE_SIZE = 100L * 1024L * 1024L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".mp4", ".mov", ".webm", ".mkv",
            ".mp3", ".wav", ".aac", ".flac", ".ogg", ".m4a",
            ".pdf", ".doc", ".docx", ".txt", ".md");

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.public-base-url:}")
    private String publicBaseUrl;

    /**
     * @deprecated 已由 STS 前端直传替代（见 /api/admin/files/sts），仅保留本地 dev fallback。
     */
    @Deprecated
    @Operation(summary = "上传文件", description = "上传培训课时媒体/文档/图片，返回可访问URL")
    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件不能超过100MB");
        }
        try {
            String extension = extensionOf(file.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ApiResponse.error("不支持的文件类型");
            }
            String datePath = LocalDate.now().format(DATE_FORMATTER);
            String filename = UUID.randomUUID() + extension;
            Path relativePath = Path.of("platform", datePath, filename);
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
            return ApiResponse.error("文件保存失败");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot).toLowerCase(Locale.ROOT);
    }

    private String normalizeBaseUrl(String base) {
        if (base == null || base.isBlank()) {
            return "";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
