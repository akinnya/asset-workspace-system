package com.xxr.lingtuthinktank.model.dto.user.auth;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮箱验证码请求
 */
@Data
public class UserEmailCodeRequest implements Serializable {

    /**
     * 邮箱地址
     */
    private String email;

    private static final long serialVersionUID = 1L;
}
