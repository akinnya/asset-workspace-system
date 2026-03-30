package com.xxr.lingtuthinktank.model.dto.workspace.member;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户加入空间请求
 */
@Data
public class SpaceUserJoinRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
