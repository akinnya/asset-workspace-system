package com.xxr.lingtuthinktank.service.asset;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.entity.asset.PictureLike;
import com.xxr.lingtuthinktank.model.entity.user.User;

/**
 * 图片点赞服务
 */
public interface PictureLikeService extends IService<PictureLike> {

    /**
     * 点赞 / 取消点赞
     *
     * @param pictureId
     * @param loginUser
     * @return
     */
    int doPictureLike(long pictureId, User loginUser);
}
