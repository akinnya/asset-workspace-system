package com.xxr.lingtuthinktank.model.entity.favorite;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏
 */
@TableName(value = "favorite")
@Data
public class Favorite implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long pictureId;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
