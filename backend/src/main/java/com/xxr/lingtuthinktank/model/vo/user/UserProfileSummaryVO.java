package com.xxr.lingtuthinktank.model.vo.user;

import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户个人主页摘要 VO
 */
@Data
public class UserProfileSummaryVO implements Serializable {
    /**
     * 用户基本信息
     */
    private UserVO userInfo;

    /**
     * 我的作品 (最多3个)
     */
    private List<PictureVO> myPosts;

    /**
     * 我的收藏 (最多3个)
     */
    private List<PictureVO> myFavorites;

    /**
     * 统计数据
     */
    private UserStatsVO stats;

    private static final long serialVersionUID = 1L;
}
