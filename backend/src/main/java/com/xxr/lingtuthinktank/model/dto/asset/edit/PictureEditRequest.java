package com.xxr.lingtuthinktank.model.dto.asset.edit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private Integer status;

    /**
     * 前端保存前看到的最近编辑时间戳
     */
    private Long lastKnownEditTime;

    private static final long serialVersionUID = 1L;
}
