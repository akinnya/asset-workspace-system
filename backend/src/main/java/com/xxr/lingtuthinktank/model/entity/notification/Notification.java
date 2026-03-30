package com.xxr.lingtuthinktank.model.entity.notification;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知
 */
@TableName(value = "notification")
@Data
public class Notification implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    private Long userId;

    /**
     * 触发者ID
     */
    private Long fromUserId;

    /**
     * 类型: like/comment/follow
     */
    private String type;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
