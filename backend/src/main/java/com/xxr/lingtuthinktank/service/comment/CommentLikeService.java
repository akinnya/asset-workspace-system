package com.xxr.lingtuthinktank.service.comment;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.entity.comment.CommentLike;
import com.xxr.lingtuthinktank.model.entity.user.User;

/**
 * 评论点赞服务
 */
public interface CommentLikeService extends IService<CommentLike> {

    /**
     * 评论点赞 / 取消点赞
     *
     * @param commentId
     * @param loginUser
     * @return
     */
    int doCommentLike(long commentId, User loginUser);
}
