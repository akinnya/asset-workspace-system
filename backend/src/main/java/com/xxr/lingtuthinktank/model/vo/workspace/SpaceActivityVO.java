package com.xxr.lingtuthinktank.model.vo.workspace;

import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间动态视图
 */
@Data
public class SpaceActivityVO implements Serializable {

    private Long id;

    /**
     * 动态类型：edit / comment / like / favorite
     */
    private String activityType;

    private Long spaceId;

    private Long actorUserId;

    private UserVO actorUser;

    private Long pictureId;

    private String pictureName;

    private String pictureThumbnailUrl;

    private String pictureUrl;

    /**
     * 动态详细内容，如评论内容、编辑摘要等
     */
    private String detail;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
