package com.xxr.lingtuthinktank.model.dto.workspace.join;

import lombok.Data;

import java.io.Serializable;

/**
 * 响应空间邀请
 */
@Data
public class SpaceInviteRespondRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 是否接受邀请
     */
    private Boolean accept;

    private static final long serialVersionUID = 1L;
}
