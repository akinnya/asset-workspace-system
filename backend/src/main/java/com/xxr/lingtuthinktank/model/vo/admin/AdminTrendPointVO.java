package com.xxr.lingtuthinktank.model.vo.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理后台趋势点
 */
@Data
public class AdminTrendPointVO implements Serializable {

    /**
     * 横轴标签
     */
    private String label;

    /**
     * 数值
     */
    private Long value;

    private static final long serialVersionUID = 1L;
}
