package com.xxr.lingtuthinktank.service.follow.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.mapper.follow.UserFollowMapper;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.entity.user.UserFollow;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.service.follow.UserFollowService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author akin
 * @description 针对表【user_follows(用户关注表)】的数据库操作Service实现
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followingId, Long followerId) {
        if (followingId.equals(followerId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }
        // check exist
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", followerId);
        queryWrapper.eq("followingId", followingId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已关注");
        }
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        boolean result = this.save(userFollow);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "关注失败");
        }
        if (!followingId.equals(followerId)) {
            User follower = userService.getById(followerId);
            Notification notification = new Notification();
            notification.setUserId(followingId);
            notification.setFromUserId(followerId);
            notification.setType("follow");
            notification.setTargetId(followerId);
            String followerName = follower != null && follower.getUserName() != null ? follower.getUserName() : "用户";
            notification.setContent(followerName + " 关注了你");
            notification.setIsRead(0);
            notificationService.save(notification);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followingId, Long followerId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", followerId);
        queryWrapper.eq("followingId", followingId);
        boolean result = this.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消关注失败");
        }
    }

    @Override
    public List<UserVO> listFollowings(Long userId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", userId);
        List<UserFollow> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> followingIds = list.stream().map(UserFollow::getFollowingId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(followingIds);
        return users.stream().map(userService::getUserVO).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> listFollowers(Long userId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followingId", userId);
        List<UserFollow> list = this.list(queryWrapper);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> followerIds = list.stream().map(UserFollow::getFollowerId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(followerIds);
        return users.stream().map(userService::getUserVO).collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(Long followingId, Long followerId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followerId", followerId);
        queryWrapper.eq("followingId", followingId);
        return this.count(queryWrapper) > 0;
    }
}
