package com.xxr.lingtuthinktank.model.dto.asset.query;

import com.xxr.lingtuthinktank.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 按颜色搜索图片请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchPictureByColorRequest extends PageRequest implements Serializable {

    /**
     * 目标颜色（十六进制，如 #FF5733）
     */
    private String picColor;

    /**
     * 空间 id（可选）
     */
    private Long spaceId;

    /**
     * 相似度阈值（0-1之间，默认0.8）
     */
    private Double similarityThreshold = 0.8;

    private static final long serialVersionUID = 1L;
}
