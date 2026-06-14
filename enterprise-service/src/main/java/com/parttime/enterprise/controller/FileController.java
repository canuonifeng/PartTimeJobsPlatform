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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/enterprise/files")
public class FileController {

    @Value("${server.port:8081}")
    private String port;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        try {
            String dir = System.getProperty("user.dir") + "/uploads";
            new File(dir).mkdirs();
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) {
                ext = name.substring(name.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);
            String url = "http://localhost:" + port + "/uploads/" + filename;
            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(url);
            return ApiResponse.success(vo);
        } catch (IOException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
