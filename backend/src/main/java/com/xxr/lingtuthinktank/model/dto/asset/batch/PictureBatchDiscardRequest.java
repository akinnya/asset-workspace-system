package com.xxr.lingtuthinktank.model.dto.asset.batch;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureBatchDiscardRequest implements Serializable {

    /**
     * 预览批次 id
     */
    private String batchId;

    private static final long serialVersionUID = 1L;
}
