package com.xxr.lingtuthinktank.model.dto.workspace.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排行分析请求
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排行数量，默认 10
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
