package com.xxr.lingtuthinktank.model.dto.user.profile;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱更新请求
 */
@Data
public class UserEmailUpdateRequest implements Serializable {

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
