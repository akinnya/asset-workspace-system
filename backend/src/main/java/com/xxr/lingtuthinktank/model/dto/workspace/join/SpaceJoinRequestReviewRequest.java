package com.xxr.lingtuthinktank.model.dto.workspace.join;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间加入申请审核请求
 */
@Data
public class SpaceJoinRequestReviewRequest implements Serializable {

    /**
     * 申请 id
     */
    private Long id;

    /**
     * 状态：1-通过 2-拒绝
     */
    private Integer status;

    /**
     * 审核说明
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
