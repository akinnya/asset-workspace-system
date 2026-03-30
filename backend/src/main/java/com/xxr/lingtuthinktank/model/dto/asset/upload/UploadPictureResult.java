package com.xxr.lingtuthinktank.model.dto.asset.upload;

import lombok.Data;

@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String url;

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
    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 上传对象路径（不含域名）
     */
    private String uploadPath;

    /**
     * 缩略图对象路径（不含域名）
     */
    private String thumbnailPath;
}
