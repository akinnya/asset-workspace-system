package com.xxr.lingtuthinktank.model.entity.asset;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@TableName(value = "picture_attachment")
@Data
public class PictureAttachment implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pictureId;

    private String fileUrl;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
