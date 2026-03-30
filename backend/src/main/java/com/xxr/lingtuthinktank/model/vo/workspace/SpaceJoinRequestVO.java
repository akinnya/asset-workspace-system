package com.xxr.lingtuthinktank.model.vo.workspace;

import com.xxr.lingtuthinktank.model.entity.workspace.SpaceJoinRequest;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间协作请求视图
 */
@Data
public class SpaceJoinRequestVO implements Serializable {

    private Long id;

    private Long spaceId;

    private Long userId;

    /**
     * 请求类型：0-加入申请 1-团队邀请
     */
    private Integer requestType;

    /**
     * 邀请人 id
     */
    private Long inviterId;

    /**
     * 状态：0-待审核 1-通过 2-拒绝
     */
    private Integer status;

    private String reviewMessage;

    private Long reviewerId;

    private Date reviewTime;

    private Date createTime;

    private Date updateTime;

    /**
     * 请求相关用户（申请人 / 被邀请人）
     */
    private UserVO user;

    /**
     * 邀请人信息
     */
    private UserVO inviter;

    /**
     * 空间信息
     */
    private SpaceVO space;

    private static final long serialVersionUID = 1L;

    public static SpaceJoinRequestVO objToVo(SpaceJoinRequest joinRequest) {
        if (joinRequest == null) {
            return null;
        }
        SpaceJoinRequestVO vo = new SpaceJoinRequestVO();
        BeanUtils.copyProperties(joinRequest, vo);
        return vo;
    }

    public static SpaceJoinRequest voToObj(SpaceJoinRequestVO vo) {
        if (vo == null) {
            return null;
        }
        SpaceJoinRequest joinRequest = new SpaceJoinRequest();
        BeanUtils.copyProperties(vo, joinRequest);
        return joinRequest;
    }
}
