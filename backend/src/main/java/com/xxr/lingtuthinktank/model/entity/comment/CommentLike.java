package com.xxr.lingtuthinktank.model.entity.comment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论点赞
 */
@TableName(value = "comment_like")
@Data
public class CommentLike implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private Long userId;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
