package com.xxr.lingtuthinktank.model.dto.workspace.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑空间请求（给用户使用）
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 世界观介绍
     */
    private String introduction;

    /**
     * 空间封面图
     */
    private String coverUrl;

    private static final long serialVersionUID = 1L;
}
