package com.xxr.lingtuthinktank.model.vo.user;

import lombok.Data;

/**
 * 用户统计数据 VO
 */
@Data
public class UserStatsVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 粉丝数
     */
    private Long followerCount;

    /**
     * 作品数
     */
    private Long pictureCount;

    /**
     * 获赞数
     */
    private Long likeCount;

    /**
     * 空间使用量 (bytes)
     */
    private Long storageUsed;

    /**
     * 空间总量 (bytes)
     */
    private Long storageTotal;
}
