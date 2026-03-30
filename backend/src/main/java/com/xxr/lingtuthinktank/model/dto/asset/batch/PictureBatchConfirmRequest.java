package com.xxr.lingtuthinktank.model.dto.asset.batch;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureBatchConfirmRequest implements Serializable {

    /**
     * 预览批次 id
     */
    private String batchId;

    /**
     * 选中的预览项 id
     */
    private List<String> previewIds;

    private static final long serialVersionUID = 1L;
}
