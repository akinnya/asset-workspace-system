package com.xxr.lingtuthinktank.model.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户 Token 实体（用于 JWT 黑名单/刷新）
 */
@TableName(value = "user_token")
@Data
public class UserToken implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * Token 值
     */
    private String token;

    /**
     * Token 类型 (access/refresh)
     */
    private String tokenType;

    /**
     * 过期时间
     */
    private Date expiresAt;

    /**
     * 是否失效
     */
    private Integer isRevoked;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
