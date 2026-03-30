package com.xxr.lingtuthinktank.model.vo.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间活跃度排行
 */
@Data
public class AdminSpaceActivityVO implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 近 30 天上传数
     */
    private Long uploadCount;

    /**
     * 近 30 天编辑数
     */
    private Long editCount;

    /**
     * 活跃分
     */
    private Long activityScore;

    private static final long serialVersionUID = 1L;
}
