package com.xxr.lingtuthinktank.model.dto.user.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -1859865201041248445L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    /**
     * 邮箱验证码
     */
    private String code;
}
