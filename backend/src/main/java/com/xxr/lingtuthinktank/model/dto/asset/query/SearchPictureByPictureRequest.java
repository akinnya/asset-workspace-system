package com.xxr.lingtuthinktank.model.dto.asset.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图请求
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 参考图片 id
     */
    private String pictureId;

    private static final long serialVersionUID = 1L;
}
