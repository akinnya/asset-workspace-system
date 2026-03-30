package com.xxr.lingtuthinktank.model.vo.admin;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 管理后台统计 VO
 */
@Data
public class AdminStatsVO implements Serializable {
    /**
     * 用户统计
     */
    private Long totalUsers;
    private Long todayNewUsers;
    private Long weekNewUsers;
    private Long monthNewUsers;

    /**
     * 内容统计
     */
    private Long totalPictures;
    private Long todayNewPictures;
    private Long totalComments;
    private Long totalLikes;

    /**
     * 审核统计
     */
    private Long pendingReview;
    private Long passedReview;
    private Long rejectedReview;

    /**
     * 存储统计
     */
    private Long totalStorageUsed;

    /**
     * 趋势与分布
     */
    private List<AdminTrendPointVO> userTrend7d;
    private List<AdminTrendPointVO> userTrend30d;
    private List<AdminTrendPointVO> pictureTrend7d;
    private List<AdminTrendPointVO> pictureTrend30d;
    private List<AdminFileTypeDistributionVO> fileTypeDistribution;
    private List<AdminSpaceActivityVO> spaceActivityRank;

    private static final long serialVersionUID = 1L;
}
