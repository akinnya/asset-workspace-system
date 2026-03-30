package com.xxr.lingtuthinktank.service.asset;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureEditLog;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.asset.PictureEditLogVO;

import java.util.List;

/**
 * 图片协作编辑日志服务
 */
public interface PictureEditLogService extends IService<PictureEditLog> {

    /**
     * 保存一条协作编辑日志
     */
    void saveEditLog(Picture picture, User operator, String changeSummary, String beforeSummary, String afterSummary);

    /**
     * 获取图片协作编辑日志列表
     */
    List<PictureEditLogVO> listPictureEditLogVOList(Long pictureId);
}
