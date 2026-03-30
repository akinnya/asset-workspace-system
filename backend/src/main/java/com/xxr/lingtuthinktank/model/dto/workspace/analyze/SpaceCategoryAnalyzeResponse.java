package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间分类分析响应（单条）
 */
@Data
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 分类名称
     */
    private String category;

    /**
     * 该分类的图片数量
     */
    private Long count;

    /**
     * 该分类占用的存储大小（字节）
     */
    private Long totalSize;

    private static final long serialVersionUID = 1L;
}
