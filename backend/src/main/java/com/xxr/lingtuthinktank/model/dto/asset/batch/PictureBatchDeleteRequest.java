package com.xxr.lingtuthinktank.model.dto.asset.batch;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求
 */
@Data
public class PictureBatchDeleteRequest implements Serializable {
    /**
     * id 列表
     */
    private List<Long> idList;

    private static final long serialVersionUID = 1L;
}
