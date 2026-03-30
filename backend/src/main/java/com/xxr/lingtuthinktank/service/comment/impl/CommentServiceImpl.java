package com.xxr.lingtuthinktank.service.comment.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xxr.lingtuthinktank.mapper.comment.CommentMapper;
import com.xxr.lingtuthinktank.model.dto.comment.CommentQueryRequest;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.entity.comment.CommentLike;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.comment.CommentVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.service.comment.CommentService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.user.UserService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author akin
 * @description 针对表【comment(评论)】的数据库操作Service实现
 * @createDate 2024-05-18 12:00:00
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }
        Long id = commentQueryRequest.getId();
        Long pictureId = commentQueryRequest.getPictureId();
        Long userId = commentQueryRequest.getUserId();
        String content = commentQueryRequest.getContent();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(pictureId != null, "pictureId", pictureId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);

        return queryWrapper;
    }

    @Override
    public CommentVO getCommentVO(Comment comment, HttpServletRequest request) {
        CommentVO commentVO = CommentVO.objToVo(comment);
        Long userId = comment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        commentVO.setUser(userVO);
        if (commentVO.getLikeCount() == null) {
            commentVO.setLikeCount(0L);
        }
        if (commentVO.getIsLiked() == null) {
            commentVO.setIsLiked(false);
        }
        return commentVO;
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(),
                commentPage.getTotal());
        if (CollUtil.isEmpty(commentList)) {
            return commentVOPage;
        }
        List<CommentVO> commentVOList = getCommentVOList(commentList, request);
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

    @Override
    public List<CommentVO> getCommentVOList(List<Comment> commentList, HttpServletRequest request) {
        if (CollUtil.isEmpty(commentList)) {
            return new ArrayList<>();
        }
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Set<Long> commentIdSet = commentList.stream().map(Comment::getId).collect(Collectors.toSet());
        Map<Long, Long> likeCountMap = getLikeCountMap(commentIdSet);

        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> likedCommentIds = loginUser == null
                ? new HashSet<>()
                : getLikedCommentIdSet(commentIdSet, loginUser.getId());

        return commentList.stream().map(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUser(userService.getUserVO(user));
            commentVO.setLikeCount(likeCountMap.getOrDefault(comment.getId(), 0L));
            commentVO.setIsLiked(likedCommentIds.contains(comment.getId()));
            return commentVO;
        }).collect(Collectors.toList());
    }

    private Map<Long, Long> getLikeCountMap(Set<Long> commentIds) {
        if (CollUtil.isEmpty(commentIds)) {
            return new HashMap<>();
        }
        List<CommentLike> likeList = commentLikeMapper.selectList(
                new QueryWrapper<CommentLike>().in("commentId", commentIds));
        return likeList.stream().collect(Collectors.groupingBy(CommentLike::getCommentId, Collectors.counting()));
    }

    private Set<Long> getLikedCommentIdSet(Set<Long> commentIds, Long userId) {
        if (CollUtil.isEmpty(commentIds) || userId == null) {
            return new HashSet<>();
        }
        List<CommentLike> likeList = commentLikeMapper.selectList(
                new QueryWrapper<CommentLike>().in("commentId", commentIds).eq("userId", userId));
        return likeList.stream().map(CommentLike::getCommentId).collect(Collectors.toSet());
    }

    @Resource
    private com.xxr.lingtuthinktank.mapper.comment.CommentLikeMapper commentLikeMapper;

    @Override
    public boolean likeComment(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 检查是否已点赞
        long count = commentLikeMapper.selectCount(
                new QueryWrapper<com.xxr.lingtuthinktank.model.entity.comment.CommentLike>()
                        .eq("commentId", commentId)
                        .eq("userId", userId));
        if (count > 0) {
            return false; // 已点赞
        }
        com.xxr.lingtuthinktank.model.entity.comment.CommentLike like = new com.xxr.lingtuthinktank.model.entity.comment.CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        boolean result = commentLikeMapper.insert(like) > 0;
        if (result && !comment.getUserId().equals(userId)) {
            User fromUser = userService.getById(userId);
            Notification notification = new Notification();
            notification.setUserId(comment.getUserId());
            notification.setFromUserId(userId);
            notification.setType("like_comment");
            notification.setTargetId(commentId);
            String userName = fromUser != null && fromUser.getUserName() != null ? fromUser.getUserName() : "用户";
            notification.setContent(userName + " 点赞了你的评论");
            notification.setIsRead(0);
            notificationService.save(notification);
        }
        return result;
    }

    @Override
    public boolean unlikeComment(Long commentId, Long userId) {
        return commentLikeMapper.delete(
                new QueryWrapper<com.xxr.lingtuthinktank.model.entity.comment.CommentLike>()
                        .eq("commentId", commentId)
                        .eq("userId", userId)) > 0;
    }
}
