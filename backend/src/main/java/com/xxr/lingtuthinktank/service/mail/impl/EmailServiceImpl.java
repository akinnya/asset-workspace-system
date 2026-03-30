package com.xxr.lingtuthinktank.service.mail.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.model.vo.admin.MailHealthStatusVO;
import com.xxr.lingtuthinktank.service.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务实现（阿里云邮件推送 API + 模板）
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String DM_API_VERSION = "2015-11-23";
    private static final String DM_SEND_TEMPLATE_ACTION = "SendTestByTemplate";
    private static final String TEMPLATE_VAR_CODE_KEY = "code";

    @Value("${aliyun.mail.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.mail.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.mail.region-id:ap-southeast-1}")
    private String regionId;

    @Value("${aliyun.mail.endpoint:dm.ap-southeast-1.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.mail.account-name:}")
    private String accountName;

    @Value("${aliyun.mail.from-alias:}")
    private String fromAlias;

    @Value("${aliyun.mail.verification-template-id:}")
    private String verificationTemplateId;

    @Value("${aliyun.mail.test-template-id:}")
    private String testTemplateId;

    @Override
    public MailHealthStatusVO getMailHealthStatus() {
        MailHealthStatusVO status = new MailHealthStatusVO();

        String keyId = normalize(accessKeyId);
        String keySecret = normalize(accessKeySecret);
        String region = normalize(regionId);
        String endpointValue = normalize(endpoint);
        String account = normalize(accountName);
        String verifyTemplate = normalize(verificationTemplateId);
        String testTemplate = normalize(testTemplateId);
        String alias = normalize(fromAlias);

        boolean accessKeyReady = StrUtil.isNotBlank(keyId) && StrUtil.isNotBlank(keySecret);
        boolean regionReady = StrUtil.isNotBlank(region);
        boolean endpointReady = StrUtil.isNotBlank(endpointValue);
        boolean accountReady = StrUtil.isNotBlank(account);
        boolean verifyTemplateReady = StrUtil.isNotBlank(verifyTemplate);
        boolean testTemplateReady = StrUtil.isNotBlank(testTemplate);
        boolean aliasReady = StrUtil.isNotBlank(alias);

        List<String> issues = new ArrayList<>();
        if (!accessKeyReady) {
            issues.add("AccessKey 未配置（ALIYUN_MAIL_ACCESS_KEY_ID / ALIYUN_MAIL_ACCESS_KEY_SECRET）");
        }
        if (!regionReady) {
            issues.add("Region 未配置（ALIYUN_MAIL_REGION_ID）");
        }
        if (!endpointReady) {
            issues.add("Endpoint 未配置（ALIYUN_MAIL_ENDPOINT）");
        }
        if (!accountReady) {
            issues.add("发信地址未配置（ALIYUN_MAIL_ACCOUNT_NAME）");
        }
        if (!verifyTemplateReady) {
            issues.add("验证码模板ID未配置（ALIYUN_MAIL_VERIFICATION_TEMPLATE_ID）");
        }

        status.setAccessKeyConfigured(accessKeyReady);
        status.setRegionConfigured(regionReady);
        status.setEndpointConfigured(endpointReady);
        status.setAccountNameConfigured(accountReady);
        status.setVerificationTemplateConfigured(verifyTemplateReady);
        status.setTestTemplateConfigured(testTemplateReady);
        status.setFromAliasConfigured(aliasReady);
        status.setMaskedAccessKeyId(maskAccessKeyId(keyId));
        status.setRegionId(region);
        status.setEndpoint(endpointValue);
        status.setAccountName(account);
        status.setVerificationTemplateId(verifyTemplate);
        status.setTestTemplateId(testTemplate);
        status.setFromAlias(alias);
        status.setIssues(issues);
        status.setReady(issues.isEmpty());
        return status;
    }

    @Override
    @Retryable(
            value = AliyunMailSendException.class,
            maxAttemptsExpression = "${mail.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${mail.retry.backoff-ms:1000}",
                    maxDelayExpression = "${mail.retry.max-backoff-ms:8000}",
                    multiplierExpression = "${mail.retry.multiplier:2.0}"
            )
    )
    public void sendVerificationCode(String to, String code) {
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }
        String trimmedCode = code.trim();
        String templateId = normalize(verificationTemplateId);
        if (StrUtil.isBlank(templateId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码模板ID未配置");
        }
        sendTemplateMail(
                normalizeRecipient(to),
                templateId,
                buildTemplateParams(trimmedCode)
        );
    }

    @Override
    @Retryable(
            value = AliyunMailSendException.class,
            maxAttemptsExpression = "${mail.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${mail.retry.backoff-ms:1000}",
                    maxDelayExpression = "${mail.retry.max-backoff-ms:8000}",
                    multiplierExpression = "${mail.retry.multiplier:2.0}"
            )
    )
    public void sendTestMail(String to) {
        String templateId = resolveTestTemplateId();
        String marker = "TEST-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        sendTemplateMail(
                normalizeRecipient(to),
                templateId,
                buildTemplateParams(marker)
        );
    }

    private String resolveTestTemplateId() {
        String testTemplate = normalize(testTemplateId);
        if (StrUtil.isNotBlank(testTemplate)) {
            return testTemplate;
        }
        String verifyTemplate = normalize(verificationTemplateId);
        if (StrUtil.isBlank(verifyTemplate)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮件模板ID未配置");
        }
        return verifyTemplate;
    }

    private void sendTemplateMail(String to, String templateId, String templateParams) {
        validateRequiredConfig();
        try {
            IAcsClient client = createClient();
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.GET);
            request.setSysDomain(normalize(endpoint));
            request.setSysVersion(DM_API_VERSION);
            request.setSysAction(DM_SEND_TEMPLATE_ACTION);
            request.putQueryParameter("AccountName", normalize(accountName));
            request.putQueryParameter("TemplateId", templateId);
            request.putQueryParameter("Email", to);
            request.putQueryParameter("TemplateParams", templateParams);
            CommonResponse response = client.getCommonResponse(request);
            if (response == null || response.getHttpStatus() >= 300) {
                throw new AliyunMailSendException(buildUnexpectedResponseMessage(response), null);
            }
        } catch (AliyunMailSendException e) {
            throw e;
        } catch (ClientException e) {
            throw new AliyunMailSendException(buildAliyunError(e), e);
        } catch (Exception e) {
            throw new AliyunMailSendException(extractBriefExceptionMessage(e), e);
        }
    }

    private String buildUnexpectedResponseMessage(CommonResponse response) {
        if (response == null) {
            return "阿里云邮件发送失败：无响应";
        }
        String payload = normalize(response.getData());
        if (StrUtil.isBlank(payload)) {
            return "阿里云邮件发送失败：HTTP " + response.getHttpStatus();
        }
        String brief = payload.replaceAll("\\s+", " ").trim();
        if (brief.length() > 120) {
            brief = brief.substring(0, 120) + "...";
        }
        return "阿里云邮件发送失败：HTTP " + response.getHttpStatus() + "（" + brief + "）";
    }

    private void validateRequiredConfig() {
        if (StrUtil.isBlank(accessKeyId) || StrUtil.isBlank(accessKeySecret)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "阿里云邮件 AccessKey 未配置");
        }
        if (StrUtil.isBlank(regionId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "阿里云邮件 Region 未配置");
        }
        if (StrUtil.isBlank(endpoint)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "阿里云邮件 Endpoint 未配置");
        }
        if (StrUtil.isBlank(accountName)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "阿里云发信账号未配置");
        }
    }

    private IAcsClient createClient() {
        DefaultProfile profile = DefaultProfile.getProfile(
                normalize(regionId),
                normalize(accessKeyId),
                normalize(accessKeySecret)
        );
        return new DefaultAcsClient(profile);
    }

    private String buildTemplateParams(String code) {
        return JSONUtil.createObj().set(TEMPLATE_VAR_CODE_KEY, code).toString();
    }

    private String normalizeRecipient(String to) {
        if (StrUtil.isBlank(to)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "收件邮箱不能为空");
        }
        return to.trim();
    }

    @Recover
    public void recover(AliyunMailSendException exception, String to, String code) {
        log.warn("邮箱验证码发送失败，已达最大重试次数，to={}, endpoint={}, region={}, account={}, template={}, reason={}",
                to,
                endpoint,
                regionId,
                accountName,
                verificationTemplateId,
                exception.getMessage(),
                exception);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, buildMailFailureMessage(exception, false));
    }

    @Recover
    public void recoverTestMail(AliyunMailSendException exception, String to) {
        log.warn("测试邮件发送失败，已达最大重试次数，to={}, endpoint={}, region={}, account={}, template={}, reason={}",
                to,
                endpoint,
                regionId,
                accountName,
                resolveTestTemplateId(),
                exception.getMessage(),
                exception);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, buildMailFailureMessage(exception, true));
    }

    private String maskAccessKeyId(String keyId) {
        if (StrUtil.isBlank(keyId)) {
            return "";
        }
        String trimmed = keyId.trim();
        if (trimmed.length() <= 6) {
            return trimmed.charAt(0) + "***";
        }
        return trimmed.substring(0, 4) + "****" + trimmed.substring(trimmed.length() - 2);
    }

    private String normalize(String value) {
        return StrUtil.isBlank(value) ? "" : value.trim();
    }

    private String buildMailFailureMessage(Throwable exception, boolean testMail) {
        String prefix = testMail ? "测试邮件发送失败" : "邮箱验证码发送失败";
        String reason = resolveFailureReason(exception);
        if (StrUtil.isBlank(reason)) {
            String brief = extractBriefExceptionMessage(exception);
            if (StrUtil.isBlank(brief)) {
                return prefix + "，请稍后再试";
            }
            return prefix + "，请稍后再试（" + brief + "）";
        }
        return prefix + "：" + reason;
    }

    private String resolveFailureReason(Throwable exception) {
        String message = flattenExceptionMessage(exception);
        if (StrUtil.isBlank(message)) {
            return null;
        }
        String lower = message.toLowerCase();
        if (lower.contains("invalidaccesskeyid") || lower.contains("signaturedoesnotmatch")
                || lower.contains("forbidden.accesskey") || lower.contains("accesskey")) {
            return "阿里云 AccessKey 校验失败，请检查 ALIYUN_MAIL_ACCESS_KEY_ID / ALIYUN_MAIL_ACCESS_KEY_SECRET";
        }
        if (lower.contains("permission") && lower.contains("denied")) {
            return "阿里云 AccessKey 权限不足，请给邮件推送 API 授权";
        }
        if (lower.contains("accountname") && (lower.contains("illegal")
                || lower.contains("invalid") || lower.contains("not exist"))) {
            return "发信地址无效，请检查 ALIYUN_MAIL_ACCOUNT_NAME 与发信域设置";
        }
        if (lower.contains("template") && (lower.contains("not exist")
                || lower.contains("invalid") || lower.contains("not found"))) {
            return "邮件模板不存在或未通过审核，请检查模板ID与模板状态";
        }
        if (lower.contains("missingtemplateid") || lower.contains("invalidtemplateid")) {
            return "模板ID无效，请检查 ALIYUN_MAIL_VERIFICATION_TEMPLATE_ID";
        }
        if (lower.contains("templateparams") && (lower.contains("invalid") || lower.contains("malformed"))) {
            return "模板变量格式无效，请检查 TemplateParams JSON 与模板变量名";
        }
        if (lower.contains("sendtestbytemplate") || lower.contains("dm:sendtestbytemplate")) {
            return "缺少 SendTestByTemplate 权限，请为 RAM 用户授权 dm:SendTestByTemplate";
        }
        if (lower.contains("toaddress") || lower.contains("invalid address")
                || lower.contains("bad recipient") || lower.contains("recipient")) {
            return "收件邮箱地址无效";
        }
        if (lower.contains("throttl") || lower.contains("frequency")
                || lower.contains("quota") || lower.contains("limit")) {
            return "邮件发送频率受限，请稍后再试";
        }
        if (lower.contains("domain") && (lower.contains("not verified")
                || lower.contains("forbidden") || lower.contains("invalid"))) {
            return "发信域名未验证或不可用，请检查阿里云邮件推送域名配置";
        }
        if (lower.contains("timeout") || lower.contains("timed out")
                || lower.contains("could not connect") || lower.contains("connectexception")
                || lower.contains("connection refused")) {
            return "连接阿里云邮件推送服务超时，请检查网络和 Endpoint";
        }
        return null;
    }

    private String buildAliyunError(ClientException exception) {
        if (exception == null) {
            return "阿里云邮件发送异常";
        }
        String errCode = normalize(exception.getErrCode());
        String errMsg = normalize(exception.getErrMsg());
        if (StrUtil.isBlank(errCode) && StrUtil.isBlank(errMsg)) {
            return StrUtil.blankToDefault(exception.getMessage(), "阿里云邮件发送异常");
        }
        if (StrUtil.isBlank(errCode)) {
            return errMsg;
        }
        if (StrUtil.isBlank(errMsg)) {
            return errCode;
        }
        return errCode + ": " + errMsg;
    }

    private String flattenExceptionMessage(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 6) {
            String msg = current.getMessage();
            if (StrUtil.isNotBlank(msg)) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(msg);
            }
            current = current.getCause();
            depth++;
        }
        return sb.toString();
    }

    private String extractBriefExceptionMessage(Throwable throwable) {
        String text = flattenExceptionMessage(throwable);
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 120 ? normalized.substring(0, 120) + "..." : normalized;
    }

    private static class AliyunMailSendException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        AliyunMailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
