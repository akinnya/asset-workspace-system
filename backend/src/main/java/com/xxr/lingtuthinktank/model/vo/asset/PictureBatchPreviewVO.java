package com.xxr.lingtuthinktank.model.vo.asset;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureBatchPreviewVO implements Serializable {

    /**
     * 预览批次 id
     */
    private String batchId;

    /**
     * 预览项列表
     */
    private List<PictureBatchPreviewItemVO> items;

    private static final long serialVersionUID = 1L;
}
