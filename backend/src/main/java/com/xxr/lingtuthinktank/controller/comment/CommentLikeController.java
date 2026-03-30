package com.xxr.lingtuthinktank.controller.comment;

import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.comment.CommentLikeService;
import com.xxr.lingtuthinktank.service.comment.CommentService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论点赞接口
 */
@RestController
@RequestMapping("/comment_like")
public class CommentLikeController {

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    @PostMapping("/do")
    public BaseResponse<Integer> doCommentLike(@RequestBody CommentLikeRequest commentLikeRequest,
            HttpServletRequest request) {
        if (commentLikeRequest == null || commentLikeRequest.getCommentId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long commentId = commentLikeRequest.getCommentId();
        ensureCommentAccess(commentId, loginUser);
        int result = commentLikeService.doCommentLike(commentId, loginUser);
        return ResultUtils.success(result);
    }

    private void ensureCommentAccess(long commentId, User loginUser) {
        Comment comment = commentService.getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        Picture picture = pictureService.getById(comment.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (picture.getSpaceId() != null) {
            spaceAuthManager.checkSpaceViewAuth(loginUser, picture.getSpaceId());
        }
    }

    /**
     * 点赞请求
     */
    @Data
    static class CommentLikeRequest {
        private long commentId;
    }
}
