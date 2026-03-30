package com.xxr.lingtuthinktank.controller.file;

import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
@Slf4j
public class FileController {
    
    @Value("${local.file.storage.path:/home/app/images}")
    private String localStoragePath;
    
    /**
     * 本地文件访问接口
     */
    @GetMapping("/images/**")
    public ResponseEntity<Resource> serveLocalFile(HttpServletRequest request) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchingPattern, path);
        
        Path file = Paths.get(localStoragePath, filePath);
        if (!Files.exists(file)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        
        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
