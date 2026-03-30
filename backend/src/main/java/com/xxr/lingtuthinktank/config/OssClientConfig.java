package com.xxr.lingtuthinktank.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 客户端配置
 */
@Configuration
@ConfigurationProperties(prefix = "oss")
@Data
public class OssClientConfig {

    /**
     * OSS 服务地址，例如 https://oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * Bucket 名称
     */
    private String bucketName;

    /**
     * 预签名 URL 有效期（秒）
     */
    private Long signExpireSeconds = 600L;

    /**
     * 是否启用图片水印
     */
    private Boolean watermarkEnabled = true;

    /**
     * 水印文字
     */
    private String watermarkText = "AKILI";

    /**
     * 水印文字颜色（HEX）
     */
    private String watermarkColor = "FFFFFF";

    /**
     * 水印文字大小
     */
    private Integer watermarkFontSize = 24;

    /**
     * 水印透明度（0-100）
     */
    private Integer watermarkOpacity = 40;

    /**
     * 水印位置（nw/north/ne/west/center/east/sw/south/se）
     */
    private String watermarkGravity = "se";

    /**
     * 水印水平偏移
     */
    private Integer watermarkX = 20;

    /**
     * 水印垂直偏移
     */
    private Integer watermarkY = 20;

    /**
     * 获取访问域名 (用于拼接文件 URL)
     */
    public String getHost() {
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucketName)) {
            // 如果配置不完整，暂时返回空或前端可识别的占位，防止生成非法 URL
            return "";
        }
        // 处理 endpoint 可能带有的协议头
        String domain = endpoint.replace("https://", "").replace("http://", "");
        // 如果自定义了域名或者已经包含 bucket 前缀，则不再拼接
        if (domain.startsWith(bucketName + ".")) {
            return "https://" + domain;
        }
        return "https://" + bucketName + "." + domain;
    }

    @Bean
    public OSS ossClient() {
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(accessKeyId)
                || !StringUtils.hasText(accessKeySecret)) {
            return null;
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
