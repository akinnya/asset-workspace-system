package com.xxr.lingtuthinktank.model.vo.asset;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureBatchPreviewItemVO implements Serializable {

    /**
     * 预览项 id
     */
    private String previewId;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

    private static final long serialVersionUID = 1L;
}
