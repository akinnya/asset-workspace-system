package com.xxr.lingtuthinktank.service.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserQueryRequest;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserAdminResetPasswordRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserRegisterRequest;
import com.xxr.lingtuthinktank.model.dto.user.profile.UserUpdatePasswordRequest;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.vo.user.LoginUserVO;
import com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.model.vo.user.UserStatsVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 40655
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-06-07 18:24:43
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    String getEncryptPassword(String userPassword);

    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录信息
     */
    LoginUserVO refreshToken(String refreshToken);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 获取当前登录用户（允许未登录，返回 null）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 获取用户统计信息
     *
     * @param userId
     * @return
     */
    UserStatsVO getUserStats(Long userId);

    /**
     * 获取用户个人主页摘要（OC、作品、收藏各前3条 + 统计）
     *
     * @param userId
     * @return
     */
    UserProfileSummaryVO getUserProfileSummary(Long userId);

    /**
     * 修改密码
     *
     * @param userUpdatePasswordRequest
     * @param loginUser
     * @return
     */
    boolean updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest,
            User loginUser);

    /**
     * 管理员重置用户密码
     *
     * @param userAdminResetPasswordRequest
     * @param loginUser
     * @return
     */
    boolean adminResetPassword(UserAdminResetPasswordRequest userAdminResetPasswordRequest,
            User loginUser);
}
