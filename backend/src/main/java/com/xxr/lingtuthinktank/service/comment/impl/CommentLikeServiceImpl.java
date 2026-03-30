package com.xxr.lingtuthinktank.service.comment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.mapper.comment.CommentLikeMapper;
import com.xxr.lingtuthinktank.mapper.comment.CommentMapper;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.entity.comment.CommentLike;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.comment.CommentLikeService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 评论点赞服务实现
 */
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike>
        implements CommentLikeService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doCommentLike(long commentId, User loginUser) {
        // 判断实体是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        long userId = loginUser.getId();
        // 每一个用户只能点赞一次
        QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("commentId", commentId);
        queryWrapper.eq("userId", userId);
        CommentLike oldCommentLike = this.getOne(queryWrapper);
        if (oldCommentLike != null) {
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
            CommentLike commentLike = new CommentLike();
            commentLike.setUserId(userId);
            commentLike.setCommentId(commentId);
            boolean result = this.save(commentLike);
            if (result) {
                // 发送通知
                if (!comment.getUserId().equals(userId)) {
                    Notification notification = new Notification();
                    notification.setUserId(comment.getUserId());
                    notification.setFromUserId(userId);
                    notification.setType("like_comment");
                    notification.setTargetId(commentId);
                    String userName = loginUser.getUserName() != null ? loginUser.getUserName() : "用户";
                    notification.setContent(userName + " 点赞了你的评论");
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
