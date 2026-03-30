package com.xxr.lingtuthinktank.model.dto.user.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员重置用户密码请求
 */
@Data
public class UserAdminResetPasswordRequest implements Serializable {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 1L;
}
