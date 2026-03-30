package com.xxr.lingtuthinktank.model.vo.asset;

import com.xxr.lingtuthinktank.model.entity.asset.PictureEditLog;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片协作编辑日志视图
 */
@Data
public class PictureEditLogVO implements Serializable {

    private Long id;

    private Long pictureId;

    private Long spaceId;

    private Long operatorUserId;

    private String actionType;

    private String changeSummary;

    private String beforeSummary;

    private String afterSummary;

    private Date createTime;

    private Date updateTime;

    private UserVO operatorUser;

    private static final long serialVersionUID = 1L;

    public static PictureEditLogVO objToVo(PictureEditLog pictureEditLog) {
        if (pictureEditLog == null) {
            return null;
        }
        PictureEditLogVO vo = new PictureEditLogVO();
        BeanUtils.copyProperties(pictureEditLog, vo);
        return vo;
    }
}
