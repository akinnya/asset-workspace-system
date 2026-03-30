package com.xxr.lingtuthinktank.model.dto.asset.edit;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片编辑锁请求
 */
@Data
public class PictureEditLockRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
