package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileUploadVO uploadWorkerImage(Long workerId, MultipartFile file, String publicBaseUrl);
}
