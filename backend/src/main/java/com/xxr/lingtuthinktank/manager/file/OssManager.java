package com.xxr.lingtuthinktank.manager.file;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.xxr.lingtuthinktank.config.OssClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/**
 * 阿里云 OSS 管理器
 */
@Component
@Slf4j
public class OssManager {

    @Resource
    private OssClientConfig ossClientConfig;

    @Autowired(required = false)
    private OSS ossClient;

    /**
     * 上传文件
     *
     * @param key  对象键（文件路径）
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            log.warn("OSS client not initialized or bucket not configured, skipping upload");
            return null;
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossClientConfig.getBucketName(), key, file);
        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件（从输入流）
     *
     * @param key         对象键（文件路径）
     * @param inputStream 输入流
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, InputStream inputStream) {
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            log.warn("OSS client not initialized or bucket not configured, skipping upload");
            return null;
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossClientConfig.getBucketName(), key, inputStream);
        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片
     *
     * @param key  对象键
     * @param file 图片文件
     * @return 上传结果
     */
    public PutObjectResult putPictureObject(String key, File file) {
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            log.warn("OSS client not initialized or bucket not configured, skipping upload");
            return null;
        }

        // 设置图片元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(getContentType(file.getName()));

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossClientConfig.getBucketName(), key, file);
        putObjectRequest.setMetadata(metadata);

        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 删除文件
     *
     * @param key 对象键
     */
    public void deleteObject(String key) {
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            log.warn("OSS client not initialized or bucket not configured, skipping delete");
            return;
        }

        ossClient.deleteObject(ossClientConfig.getBucketName(), key);
    }

    /**
     * 获取文件访问 URL
     *
     * @param key 对象键
     * @return 访问 URL
     */
    public String getObjectUrl(String key) {
        String host = normalizeHost(ossClientConfig.getHost());
        String normalizedKey = key == null ? "" : key;
        if (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        if (!StringUtils.hasText(host)) {
            return "/" + normalizedKey;
        }
        return host + "/" + normalizedKey;
    }

    /**
     * 获取对象的预签名 URL
     *
     * @param key           对象键
     * @param expireSeconds 有效期（秒）
     * @return 预签名 URL
     */
    public String getSignedObjectUrl(String key, long expireSeconds) {
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            return getObjectUrl(key);
        }
        String normalizedKey = key == null ? "" : key;
        if (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        long ttl = expireSeconds > 0 ? expireSeconds : 600L;
        Date expiration = new Date(System.currentTimeMillis() + ttl * 1000);
        URL signedUrl = ossClient.generatePresignedUrl(ossClientConfig.getBucketName(), normalizedKey, expiration);
        return signedUrl != null ? signedUrl.toString() : getObjectUrl(normalizedKey);
    }

    /**
     * 获取对象的预签名 URL（含图片处理参数）
     *
     * @param key           对象键
     * @param expireSeconds 有效期（秒）
     * @param process       图片处理参数
     * @return 预签名 URL
     */
    public String getSignedObjectUrl(String key, long expireSeconds, String process) {
        if (!StringUtils.hasText(process)) {
            return getSignedObjectUrl(key, expireSeconds);
        }
        if (ossClient == null || !StringUtils.hasText(ossClientConfig.getBucketName())) {
            return appendProcessToUrl(getObjectUrl(key), process);
        }
        String normalizedKey = key == null ? "" : key;
        if (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        long ttl = expireSeconds > 0 ? expireSeconds : 600L;
        Date expiration = new Date(System.currentTimeMillis() + ttl * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                ossClientConfig.getBucketName(), normalizedKey);
        request.setExpiration(expiration);
        request.setProcess(process);
        URL signedUrl = ossClient.generatePresignedUrl(request);
        return signedUrl != null ? signedUrl.toString() : appendProcessToUrl(getObjectUrl(normalizedKey), process);
    }

    /**
     * 对现有 URL 进行签名（仅处理 OSS 资源）
     */
    public String signUrlIfNeeded(String url) {
        return signUrlIfNeeded(url, ossClientConfig.getSignExpireSeconds());
    }

    /**
     * 对现有 URL 进行签名（仅处理 OSS 资源）
     */
    public String signUrlIfNeeded(String url, long expireSeconds) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("data:") || trimmed.startsWith("blob:")) {
            return url;
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return url;
        }
        String key = resolveKeyFromUrl(trimmed);
        if (!StringUtils.hasText(key)) {
            return url;
        }
        return getSignedObjectUrl(key, expireSeconds);
    }

    /**
     * 对现有 URL 进行签名（仅处理 OSS 资源），支持图片处理参数
     */
    public String signUrlIfNeeded(String url, long expireSeconds, String process) {
        if (!StringUtils.hasText(process)) {
            return signUrlIfNeeded(url, expireSeconds);
        }
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("data:") || trimmed.startsWith("blob:")) {
            return url;
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return url;
        }
        String key = resolveKeyFromUrl(trimmed);
        if (!StringUtils.hasText(key)) {
            return url;
        }
        // 历史数据可能已携带 x-oss-process 和过期签名参数，这里统一重新签名，避免老图失效
        return getSignedObjectUrl(key, expireSeconds, process);
    }

    /**
     * 图片展示用水印签名（仅图片格式）
     */
    public String signImageUrlWithWatermarkIfNeeded(String url, String format) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        if (!isImageFormat(format)) {
            return signUrlIfNeeded(url);
        }
        String process = buildWatermarkProcess();
        if (!StringUtils.hasText(process)) {
            return signUrlIfNeeded(url);
        }
        return signUrlIfNeeded(url, ossClientConfig.getSignExpireSeconds(), process);
    }

    /**
     * 图片展示用水印签名（默认认为是图片）
     */
    public String signImageUrlWithWatermarkIfNeeded(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String process = buildWatermarkProcess();
        if (!StringUtils.hasText(process)) {
            return signUrlIfNeeded(url);
        }
        return signUrlIfNeeded(url, ossClientConfig.getSignExpireSeconds(), process);
    }

    public boolean isImageFormat(String format) {
        if (!StringUtils.hasText(format)) {
            return false;
        }
        String lower = format.trim().toLowerCase();
        return IMAGE_FORMATS.contains(lower);
    }

    private String buildWatermarkProcess() {
        if (!Boolean.TRUE.equals(ossClientConfig.getWatermarkEnabled())) {
            return null;
        }
        String text = ossClientConfig.getWatermarkText();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));
        String color = normalizeColor(ossClientConfig.getWatermarkColor());
        int size = clamp(ossClientConfig.getWatermarkFontSize(), 12, 80, 24);
        int opacity = clamp(ossClientConfig.getWatermarkOpacity(), 10, 100, 40);
        String gravity = normalizeGravity(ossClientConfig.getWatermarkGravity());
        int x = clamp(ossClientConfig.getWatermarkX(), 0, 500, 20);
        int y = clamp(ossClientConfig.getWatermarkY(), 0, 500, 20);
        return "image/watermark,text_" + encoded
                + ",color_" + color
                + ",size_" + size
                + ",t_" + opacity
                + ",g_" + gravity
                + ",x_" + x
                + ",y_" + y;
    }

    private String appendProcessToUrl(String url, String process) {
        if (!StringUtils.hasText(url) || !StringUtils.hasText(process)) {
            return url;
        }
        if (url.contains("x-oss-process=")) {
            return url;
        }
        String encoded;
        try {
            encoded = URLEncoder.encode(process, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            encoded = process;
        }
        return url + (url.contains("?") ? "&" : "?") + "x-oss-process=" + encoded;
    }

    private String normalizeColor(String color) {
        if (!StringUtils.hasText(color)) {
            return "FFFFFF";
        }
        String normalized = color.trim().toUpperCase();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() == 3) {
            normalized = "" + normalized.charAt(0) + normalized.charAt(0)
                    + normalized.charAt(1) + normalized.charAt(1)
                    + normalized.charAt(2) + normalized.charAt(2);
        }
        if (normalized.length() != 6 || !normalized.matches("[0-9A-F]{6}")) {
            return "FFFFFF";
        }
        return normalized;
    }

    private String normalizeGravity(String gravity) {
        if (!StringUtils.hasText(gravity)) {
            return "se";
        }
        String value = gravity.trim().toLowerCase();
        Set<String> allowed = WATERMARK_GRAVITIES;
        if (allowed.contains(value)) {
            return value;
        }
        return "se";
    }

    private int clamp(Integer value, int min, int max, int defaultValue) {
        int v = value != null ? value : defaultValue;
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static final Set<String> IMAGE_FORMATS = new HashSet<>();
    private static final Set<String> WATERMARK_GRAVITIES = new HashSet<>();

    static {
        IMAGE_FORMATS.add("jpg");
        IMAGE_FORMATS.add("jpeg");
        IMAGE_FORMATS.add("png");
        IMAGE_FORMATS.add("webp");
        IMAGE_FORMATS.add("gif");
        IMAGE_FORMATS.add("bmp");
        IMAGE_FORMATS.add("tif");
        IMAGE_FORMATS.add("tiff");

        WATERMARK_GRAVITIES.add("nw");
        WATERMARK_GRAVITIES.add("north");
        WATERMARK_GRAVITIES.add("ne");
        WATERMARK_GRAVITIES.add("west");
        WATERMARK_GRAVITIES.add("center");
        WATERMARK_GRAVITIES.add("east");
        WATERMARK_GRAVITIES.add("sw");
        WATERMARK_GRAVITIES.add("south");
        WATERMARK_GRAVITIES.add("se");
    }

    private String resolveKeyFromUrl(String url) {
        String trimmed = url.trim();
        try {
            URL parsed = new URL(trimmed);
            String host = parsed.getHost();
            String bucketName = ossClientConfig.getBucketName();
            String ossHost = normalizeDomain(ossClientConfig.getHost());
            if (!StringUtils.hasText(host)) {
                return null;
            }
            if (StringUtils.hasText(ossHost) && !host.equalsIgnoreCase(ossHost)) {
                if (!StringUtils.hasText(bucketName) || !host.contains(bucketName)) {
                    return null;
                }
            }
            String path = parsed.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeDomain(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        try {
            if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
                return new URL(normalized).getHost();
            }
        } catch (Exception ignored) {
            // ignore
        }
        if (normalized.contains("/")) {
            normalized = normalized.substring(0, normalized.indexOf("/"));
        }
        return normalized.replace("http://", "").replace("https://", "");
    }

    private String normalizeHost(String host) {
        if (!StringUtils.hasText(host)) {
            return "";
        }
        String normalized = host.trim();
        if (normalized.startsWith("http://")) {
            normalized = "https://" + normalized.substring("http://".length());
        } else if (!normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 根据文件名获取 Content-Type
     */
    private String getContentType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (suffix) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "flac":
                return "audio/flac";
            case "ogg":
                return "audio/ogg";
            case "m4a":
                return "audio/mp4";
            case "aac":
                return "audio/aac";
            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "avi":
                return "video/x-msvideo";
            case "webm":
                return "video/webm";
            case "txt":
                return "text/plain";
            case "md":
            case "markdown":
                return "text/markdown";
            case "csv":
                return "text/csv";
            case "xml":
                return "application/xml";
            case "yaml":
            case "yml":
                return "application/x-yaml";
            case "json":
                return "application/json";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "rtf":
                return "application/rtf";
            case "pmx":
            case "pmd":
            case "vmd":
                return "application/octet-stream";
            case "glb":
            case "gltf":
                return "model/gltf-binary";
            default:
                return "application/octet-stream";
        }
    }
}
