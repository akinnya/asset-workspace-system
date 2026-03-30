package com.xxr.lingtuthinktank.model.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 敏感操作日志
 */
@TableName(value = "sensitive_operation_log")
@Data
public class SensitiveOperationLog implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 操作用户名称
     */
    private String userName;

    /**
     * 客户端 ip
     */
    private String ip;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String httpMethod;

    /**
     * 后端方法名
     */
    private String methodName;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求参数摘要
     */
    private String requestParams;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
