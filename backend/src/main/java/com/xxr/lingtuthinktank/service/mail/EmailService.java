package com.xxr.lingtuthinktank.service.mail;

import com.xxr.lingtuthinktank.model.vo.admin.MailHealthStatusVO;

/**
 * 邮件服务
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param to   收件邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送测试邮件
     *
     * @param to 收件邮箱
     */
    void sendTestMail(String to);

    /**
     * 获取邮件服务健康状态
     */
    MailHealthStatusVO getMailHealthStatus();
}
