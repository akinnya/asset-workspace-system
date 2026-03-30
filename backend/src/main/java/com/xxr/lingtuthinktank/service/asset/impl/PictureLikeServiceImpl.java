package com.xxr.lingtuthinktank.service.asset.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.mapper.asset.PictureLikeMapper;
import com.xxr.lingtuthinktank.mapper.asset.PictureMapper;
import com.xxr.lingtuthinktank.model.entity.asset.PictureLike;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.service.asset.PictureLikeService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 图片点赞服务实现
 */
@Service
public class PictureLikeServiceImpl extends ServiceImpl<PictureLikeMapper, PictureLike>
        implements PictureLikeService {

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPictureLike(long pictureId, User loginUser) {
        // 判断实体是否存在，根据实际情况可加校验

        long userId = loginUser.getId();
        // 每一个用户只能点赞一次
        QueryWrapper<PictureLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        queryWrapper.eq("userId", userId);
        PictureLike oldPictureLike = this.getOne(queryWrapper);
        if (oldPictureLike != null) {
            // 已点赞，取消点赞
            boolean result = this.remove(queryWrapper);
            if (result) {
                // 收到 -1 代表取消点赞
                return -1;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞，进行点赞
            PictureLike pictureLike = new PictureLike();
            pictureLike.setUserId(userId);
            pictureLike.setPictureId(pictureId);
            boolean result = this.save(pictureLike);
            if (result) {
                Picture picture = pictureMapper.selectById(pictureId);
                if (picture != null && !picture.getUserId().equals(userId)) {
                    Notification notification = new Notification();
                    notification.setUserId(picture.getUserId());
                    notification.setFromUserId(userId);
                    notification.setType("like");
                    notification.setTargetId(pictureId);
                    String userName = loginUser.getUserName() != null ? loginUser.getUserName() : "用户";
                    notification.setContent(userName + " 点赞了你的作品");
                    notification.setIsRead(0);
                    notificationService.save(notification);
                }
                // 收到 1 代表点赞
                return 1;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}
