package com.xxr.lingtuthinktank.service.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.comment.CommentQueryRequest;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.vo.comment.CommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author akin
 * @description 针对表【comment(评论)】的数据库操作Service
 * @createDate 2024-05-18 12:00:00
 */
public interface CommentService extends IService<Comment> {

    /**
     * 获取查询封装类
     *
     * @param commentQueryRequest
     * @return
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    /**
     * 获取评论封装
     *
     * @param comment
     * @param request
     * @return
     */
    CommentVO getCommentVO(Comment comment, HttpServletRequest request);

    /**
     * 分页获取评论封装
     *
     * @param commentPage
     * @param request
     * @return
     */
    Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);

    /**
     * 批量封装评论
     *
     * @param commentList
     * @param request
     * @return
     */
    java.util.List<CommentVO> getCommentVOList(java.util.List<Comment> commentList, HttpServletRequest request);

    /**
     * 点赞评论
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Long commentId, Long userId);
}
