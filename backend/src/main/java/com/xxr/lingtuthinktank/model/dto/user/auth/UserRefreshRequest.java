package com.xxr.lingtuthinktank.model.dto.user.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRefreshRequest implements Serializable {
    /**
     * 刷新令牌
     */
    private String refreshToken;

    private static final long serialVersionUID = 1L;
}
