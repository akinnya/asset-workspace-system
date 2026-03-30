package com.xxr.lingtuthinktank.model.vo.admin;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 邮件服务健康状态
 */
@Data
public class MailHealthStatusVO implements Serializable {

    /**
     * 是否可用（配置完整且可发送）
     */
    private Boolean ready;

    /**
     * AccessKey 是否配置
     */
    private Boolean accessKeyConfigured;

    /**
     * Region 是否配置
     */
    private Boolean regionConfigured;

    /**
     * Endpoint 是否配置
     */
    private Boolean endpointConfigured;

    /**
     * 发信账号是否配置
     */
    private Boolean accountNameConfigured;

    /**
     * 验证码模板 ID 是否配置
     */
    private Boolean verificationTemplateConfigured;

    /**
     * 测试模板是否配置
     */
    private Boolean testTemplateConfigured;

    /**
     * 发件人别名是否配置
     */
    private Boolean fromAliasConfigured;

    /**
     * 掩码后的 AccessKeyId（防止泄露）
     */
    private String maskedAccessKeyId;

    /**
     * Region ID
     */
    private String regionId;

    /**
     * API Endpoint
     */
    private String endpoint;

    /**
     * 发信地址
     */
    private String accountName;

    /**
     * 验证码模板 ID
     */
    private String verificationTemplateId;

    /**
     * 测试模板 ID
     */
    private String testTemplateId;

    /**
     * 发件人别名
     */
    private String fromAlias;

    /**
     * 不可用原因列表
     */
    private List<String> issues;

    private static final long serialVersionUID = 1L;
}
