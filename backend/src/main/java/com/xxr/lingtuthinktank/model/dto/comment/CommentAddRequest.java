package com.xxr.lingtuthinktank.model.dto.comment;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建评论请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 评论内容
     */
    private String content;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 父评论 id
     */
    private Long parentId;

    private static final long serialVersionUID = 1L;
}
