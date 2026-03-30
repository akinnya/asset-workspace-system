package com.xxr.lingtuthinktank.controller.follow;

import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.service.follow.UserFollowService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户关注接口
 */
@RestController
@RequestMapping("/user_follow")
public class UserFollowController {

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private UserService userService;

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> followUser(@RequestBody Long followingId, HttpServletRequest request) {
        if (followingId == null || followingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        userFollowService.followUser(followingId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> unfollowUser(@RequestBody Long followingId, HttpServletRequest request) {
        if (followingId == null || followingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        userFollowService.unfollowUser(followingId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 获取我关注的人
     */
    @GetMapping("/list/following")
    public BaseResponse<List<UserVO>> listMyFollowings(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userFollowService.listFollowings(loginUser.getId()));
    }

    /**
     * 获取我的粉丝
     */
    @GetMapping("/list/follower")
    public BaseResponse<List<UserVO>> listMyFollowers(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userFollowService.listFollowers(loginUser.getId()));
    }

    /**
     * 获取某人关注的人
     */
    @GetMapping("/list/following/user")
    public BaseResponse<List<UserVO>> listUserFollowings(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userFollowService.listFollowings(userId));
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/is_following")
    public BaseResponse<Boolean> isFollowing(Long followingId, HttpServletRequest request) {
        if (followingId == null || followingId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userFollowService.isFollowing(followingId, loginUser.getId()));
    }
}
