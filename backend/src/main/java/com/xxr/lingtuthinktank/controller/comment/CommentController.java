package com.xxr.lingtuthinktank.controller.comment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.DeleteRequest;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.dto.comment.CommentAddRequest;
import com.xxr.lingtuthinktank.model.dto.comment.CommentQueryRequest;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.comment.CommentVO;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.service.comment.CommentService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    /**
     * 创建评论
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (commentAddRequest.getPictureId() == null || commentAddRequest.getPictureId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(commentAddRequest.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        ensurePictureCommentAccess(picture, request, true);

        if (commentAddRequest.getParentId() != null) {
            Comment parent = commentService.getById(commentAddRequest.getParentId());
            ThrowUtils.throwIf(parent == null || !parent.getPictureId().equals(commentAddRequest.getPictureId()),
                    ErrorCode.PARAMS_ERROR, "父评论不存在或不匹配");
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);

        User loginUser = userService.getLoginUser(request);
        comment.setUserId(loginUser.getId());

        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        if (!picture.getUserId().equals(loginUser.getId())) {
            Notification notification = new Notification();
            notification.setUserId(picture.getUserId());
            notification.setFromUserId(loginUser.getId());
            notification.setType("comment");
            notification.setTargetId(picture.getId());
            String userName = loginUser.getUserName() != null ? loginUser.getUserName() : "用户";
            notification.setContent(userName + " 评论了你的作品");
            notification.setIsRead(0);
            notificationService.save(notification);
        }
        return ResultUtils.success(comment.getId());
    }

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = commentService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取评论列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
            HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        if (commentQueryRequest.getPictureId() == null || commentQueryRequest.getPictureId() <= 0) {
            Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                    commentService.getQueryWrapper(commentQueryRequest));
            return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
        }
        Picture picture = pictureService.getById(commentQueryRequest.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        ensurePictureCommentAccess(picture, request, false);

        // 仅查询顶级评论
        Page<Comment> parentPage = commentService.page(new Page<>(current, size),
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Comment>()
                        .eq("pictureId", commentQueryRequest.getPictureId())
                        .isNull("parentId")
                        .orderByDesc("createTime"));
        Page<CommentVO> parentVOPage = commentService.getCommentVOPage(parentPage, request);

        java.util.List<Long> parentIds = parentPage.getRecords().stream().map(Comment::getId)
                .collect(java.util.stream.Collectors.toList());
        if (!parentIds.isEmpty()) {
            java.util.List<Comment> childList = commentService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Comment>()
                            .eq("pictureId", commentQueryRequest.getPictureId())
                            .in("parentId", parentIds)
                            .orderByAsc("createTime"));
            java.util.Map<Long, java.util.List<CommentVO>> childMap = commentService
                    .getCommentVOList(childList, request)
                    .stream()
                    .collect(java.util.stream.Collectors.groupingBy(CommentVO::getParentId));
            parentVOPage.getRecords().forEach(parent -> parent
                    .setChildren(childMap.getOrDefault(parent.getId(), java.util.Collections.emptyList())));
        }
        return ResultUtils.success(parentVOPage);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> likeComment(@RequestParam Long commentId, HttpServletRequest request) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ensureCommentAccess(commentId, request);
        User loginUser = userService.getLoginUser(request);
        boolean result = commentService.likeComment(commentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 取消点赞评论
     */
    @PostMapping("/unlike")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> unlikeComment(@RequestParam Long commentId, HttpServletRequest request) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ensureCommentAccess(commentId, request);
        User loginUser = userService.getLoginUser(request);
        boolean result = commentService.unlikeComment(commentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    private void ensureCommentAccess(Long commentId, HttpServletRequest request) {
        Comment comment = commentService.getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        Picture picture = pictureService.getById(comment.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        ensurePictureCommentAccess(picture, request, true);
    }

    private void ensurePictureCommentAccess(Picture picture, HttpServletRequest request, boolean requireLogin) {
        if (picture == null || picture.getSpaceId() == null) {
            return;
        }
        User loginUser = requireLogin ? userService.getLoginUser(request) : userService.getLoginUserPermitNull(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "仅空间成员可查看或参与该评论区");
        spaceAuthManager.checkSpaceViewAuth(loginUser, picture.getSpaceId());
    }
}
