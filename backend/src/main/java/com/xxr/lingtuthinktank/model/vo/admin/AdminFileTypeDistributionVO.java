package com.xxr.lingtuthinktank.model.vo.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件类型分布
 */
@Data
public class AdminFileTypeDistributionVO implements Serializable {

    /**
     * 类型标识
     */
    private String type;

    /**
     * 数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
