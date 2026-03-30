package com.xxr.lingtuthinktank.model.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 构图分析结果 VO
 */
@Data
public class CompositionAnalysisVO implements Serializable {

    /**
     * 原图宽度
     */
    private Integer originalWidth;

    /**
     * 原图高度
     */
    private Integer originalHeight;

    /**
     * 推荐裁剪区域
     */
    private CropRect recommendedCrop;

    /**
     * 推荐宽高比
     */
    private String recommendedAspectRatio;

    /**
     * 构图类型 (如 "Rule of Thirds", "Golden Ratio")
     */
    private String compositionType;

    /**
     * 构图得分 (0-100)
     */
    private Integer compositionScore;

    /**
     * 构图建议列表
     */
    private List<String> suggestions;

    /**
     * 焦点位置列表
     */
    private List<FocalPoint> focalPoints;

    private static final long serialVersionUID = 1L;

    /**
     * 裁剪区域
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CropRect implements Serializable {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        private static final long serialVersionUID = 1L;
    }

    /**
     * 焦点
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FocalPoint implements Serializable {
        private Integer x;
        private Integer y;
        private Double weight;
        private static final long serialVersionUID = 1L;
    }
}
