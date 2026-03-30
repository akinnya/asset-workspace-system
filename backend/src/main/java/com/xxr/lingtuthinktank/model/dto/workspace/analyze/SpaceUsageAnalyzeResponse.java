package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间资源使用分析响应
 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 已使用大小（字节）
     */
    private Long usedSize;

    /**
     * 总大小（字节）
     */
    private Long maxSize;

    /**
     * 使用比例 (0-100)
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 数量比例 (0-100)
     */
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;
}
