package com.xxr.lingtuthinktank.model.dto.asset.meta;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片标签和分类 DTO
 *
 * @author xxr
 */
@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}

