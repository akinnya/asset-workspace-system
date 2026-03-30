package com.xxr.lingtuthinktank.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * 构图分析请求
 */
@Data
public class CompositionAnalysisRequest implements Serializable {

    /**
     * 目标宽高比 (可选)
     */
    private String targetAspectRatio;

    private static final long serialVersionUID = 1L;
}
