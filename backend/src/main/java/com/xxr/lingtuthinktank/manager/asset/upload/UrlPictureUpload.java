package com.xxr.lingtuthinktank.manager.asset.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    private static final ThreadLocal<String> INFERRED_SUFFIX = new ThreadLocal<>();

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            int status = response.getStatus();
            if (status != HttpStatus.HTTP_OK && status != HttpStatus.HTTP_PARTIAL) {
                response.close();
                response = HttpUtil.createRequest(Method.GET, fileUrl)
                        .header("Range", "bytes=0-0")
                        .execute();
                status = response.getStatus();
            }
            ThrowUtils.throwIf(status != HttpStatus.HTTP_OK && status != HttpStatus.HTTP_PARTIAL,
                    ErrorCode.PARAMS_ERROR, "文件不可访问");
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                String normalizedContentType = normalizeContentType(contentType);
                // 允许的文件类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif",
                        "video/mp4", "video/quicktime", "video/x-msvideo", "video/webm",
                        "audio/mpeg", "audio/wav", "audio/flac", "audio/ogg", "audio/mp4", "audio/aac",
                        "text/plain", "text/markdown", "text/csv", "text/xml", "text/yaml",
                        "application/pdf", "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/rtf", "application/json", "application/xml", "application/x-yaml",
                        "model/gltf-binary", "model/gltf+json", "application/octet-stream");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(normalizedContentType),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
                INFERRED_SUFFIX.set(resolveSuffixFromContentType(normalizedContentType));
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            String contentRangeStr = response.header("Content-Range");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long MAX_SIZE = 100 * 1024 * 1024L; // 与文件上传保持一致
                    ThrowUtils.throwIf(contentLength > MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 100M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            } else if (StrUtil.isNotBlank(contentRangeStr) && contentRangeStr.contains("/")) {
                String totalSizeStr = contentRangeStr.substring(contentRangeStr.lastIndexOf("/") + 1).trim();
                if (StrUtil.isNotBlank(totalSizeStr) && totalSizeStr.chars().allMatch(Character::isDigit)) {
                    long totalSize = Long.parseLong(totalSizeStr);
                    final long MAX_SIZE = 100 * 1024 * 1024L;
                    ThrowUtils.throwIf(totalSize > MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 100M");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 路径中提取文件名（保留后缀）
        try {
            String path = new URL(fileUrl).getPath();
            String name = FileUtil.getName(path);
            String inferredSuffix = INFERRED_SUFFIX.get();
            if (StrUtil.isBlank(name)) {
                return buildFallbackFilename(inferredSuffix);
            }
            String currentSuffix = FileUtil.getSuffix(name);
            if (StrUtil.isBlank(currentSuffix) && StrUtil.isNotBlank(inferredSuffix)) {
                return name + "." + inferredSuffix;
            }
            return name;
        } catch (MalformedURLException e) {
            return buildFallbackFilename(INFERRED_SUFFIX.get());
        } finally {
            INFERRED_SUFFIX.remove();
        }
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }

    private String normalizeContentType(String contentType) {
        String trimmed = contentType.trim().toLowerCase();
        int index = trimmed.indexOf(";");
        if (index > 0) {
            return trimmed.substring(0, index).trim();
        }
        return trimmed;
    }

    private String resolveSuffixFromContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            case "image/gif":
                return "gif";
            case "video/mp4":
                return "mp4";
            case "video/quicktime":
                return "mov";
            case "video/x-msvideo":
                return "avi";
            case "video/webm":
                return "webm";
            case "audio/mpeg":
                return "mp3";
            case "audio/wav":
                return "wav";
            case "audio/flac":
                return "flac";
            case "audio/ogg":
                return "ogg";
            case "audio/mp4":
                return "m4a";
            case "audio/aac":
                return "aac";
            case "text/plain":
                return "txt";
            case "text/markdown":
                return "md";
            case "text/csv":
                return "csv";
            case "text/xml":
            case "application/xml":
                return "xml";
            case "text/yaml":
            case "application/x-yaml":
                return "yaml";
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "application/rtf":
                return "rtf";
            case "application/json":
                return "json";
            case "model/gltf-binary":
                return "glb";
            case "model/gltf+json":
                return "gltf";
            case "application/octet-stream":
                return "bin"; // Generic binary tag to be refined by MUST_SUFFIX in template
            default:
                return null;
        }
    }

    private String buildFallbackFilename(String inferredSuffix) {
        if (StrUtil.isNotBlank(inferredSuffix)) {
            return "file." + inferredSuffix;
        }
        return "file.bin";
    }
}
