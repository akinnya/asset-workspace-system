package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间大小分析响应（单条，按大小区间分组）
 */
@Data
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 大小区间标签（如 "0-100KB", "100KB-1MB", "1MB-10MB", ">10MB")
     */
    private String sizeRange;

    /**
     * 该区间的图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
