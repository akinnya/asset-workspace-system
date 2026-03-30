package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间大小分析请求
 */
@Data
public class SpaceSizeAnalyzeRequest implements Serializable {

    /**
     * 全空间分析（admin）还是特定空间（user）
     */
    private boolean queryAll;

    /**
     * 仅 queryAll=false 时有效
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
