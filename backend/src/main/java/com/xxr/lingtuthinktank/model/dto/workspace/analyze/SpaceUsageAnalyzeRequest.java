package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间资源使用分析请求
 */
@Data
public class SpaceUsageAnalyzeRequest implements Serializable {

    /**
     * 全空间分析（admin）还是特定空间（user）
     */
    private boolean queryAll;

    /**
     * 仅 queryAll=false 时有效
     */
    private Long spaceId;

    /**
     * 预留其他过滤条件
     */

    private static final long serialVersionUID = 1L;
}
