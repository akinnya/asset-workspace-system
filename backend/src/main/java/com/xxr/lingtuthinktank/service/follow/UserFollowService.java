package com.xxr.lingtuthinktank.service.follow;

import com.xxr.lingtuthinktank.model.entity.user.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;

import java.util.List;

/**
 * @author akin
 * @description 针对表【user_follows(用户关注表)】的数据库操作Service
 */
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     * 
     * @param followingId 被关注者ID
     * @param followerId  关注者ID
     */
    void followUser(Long followingId, Long followerId);

    /**
     * 取消关注
     * 
     * @param followingId 被关注者ID
     * @param followerId  关注者ID
     */
    void unfollowUser(Long followingId, Long followerId);

    /**
     * 获取关注列表
     * 
     * @param userId 用户ID
     * @return 关注的用户列表
     */
    List<UserVO> listFollowings(Long userId);

    /**
     * 获取粉丝列表
     * 
     * @param userId 用户ID
     * @return 粉丝列表
     */
    List<UserVO> listFollowers(Long userId);

    /**
     * 是否已关注
     * 
     * @param followingId 被关注者ID
     * @param followerId  关注者ID
     * @return boolean
     */
    boolean isFollowing(Long followingId, Long followerId);
}
