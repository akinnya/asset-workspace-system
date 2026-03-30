package com.xxr.lingtuthinktank.model.dto.admin;

import com.xxr.lingtuthinktank.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 敏感操作日志查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SensitiveOperationLogQueryRequest extends PageRequest implements Serializable {

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 操作用户名称
     */
    private String userName;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 关键字
     */
    private String searchText;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private static final long serialVersionUID = 1L;
}
