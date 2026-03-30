package com.xxr.lingtuthinktank.model.vo.asset;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片编辑锁信息
 */
@Data
public class PictureEditLockVO implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 是否支持编辑锁（仅团队空间资源启用）
     */
    private Boolean supported;

    /**
     * 当前是否存在锁
     */
    private Boolean locked;

    /**
     * 是否由当前用户持有锁
     */
    private Boolean lockedByCurrentUser;

    /**
     * 加锁用户 id
     */
    private Long userId;

    /**
     * 加锁用户名称
     */
    private String userName;

    /**
     * 加锁用户头像
     */
    private String userAvatar;

    /**
     * 锁过期时间戳（毫秒）
     */
    private Long expireAt;

    private static final long serialVersionUID = 1L;
}
