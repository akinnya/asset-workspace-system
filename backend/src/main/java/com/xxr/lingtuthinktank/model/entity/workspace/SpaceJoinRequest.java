package com.xxr.lingtuthinktank.model.entity.workspace;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间协作请求（加入申请 / 团队邀请）
 * @TableName space_join_request
 */
@TableName(value = "space_join_request")
@Data
public class SpaceJoinRequest implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 目标用户 id（申请人 / 被邀请人）
     */
    private Long userId;

    /**
     * 请求类型：0-加入申请 1-团队邀请
     */
    private Integer requestType;

    /**
     * 邀请人 id（仅团队邀请时存在）
     */
    private Long inviterId;

    /**
     * 状态：0-待审核 1-通过 2-拒绝
     */
    private Integer status;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
