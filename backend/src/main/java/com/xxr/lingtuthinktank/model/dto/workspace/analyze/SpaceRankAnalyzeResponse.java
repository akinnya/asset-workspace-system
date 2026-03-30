package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排行分析响应
 */
@Data
public class SpaceRankAnalyzeResponse implements Serializable {

    /**
     * 空间 ID
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 已使用大小
     */
    private Long totalSize;

    private static final long serialVersionUID = 1L;
}
