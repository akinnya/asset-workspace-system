package com.xxr.lingtuthinktank.model.dto.asset.upload;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 空间 id（上传到指定空间）
     */
    private Long spaceId;

    /**
     * 状态: 0-草稿, 1-线稿, 2-色稿, 3-成图
     */
    private Integer status;

    /**
     * 标签列表
     */
    private java.util.List<String> tags;

    private static final long serialVersionUID = 1L;
}
