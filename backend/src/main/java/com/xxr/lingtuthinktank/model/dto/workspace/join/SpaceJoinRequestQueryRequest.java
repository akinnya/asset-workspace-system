package com.xxr.lingtuthinktank.model.dto.workspace.join;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间协作请求查询
 */
@Data
public class SpaceJoinRequestQueryRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 目标用户 id（申请人 / 被邀请人）
     */
    private Long userId;

    /**
     * 请求类型：0-加入申请 1-团队邀请
     */
    private Integer requestType;

    /**
     * 状态：0-待审核 1-通过 2-拒绝
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
