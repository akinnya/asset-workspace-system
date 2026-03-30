package com.xxr.lingtuthinktank.controller.user;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxr.lingtuthinktank.annotation.AuthCheck;
import com.xxr.lingtuthinktank.annotation.LogSensitive;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.DeleteRequest;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.constant.UserConstant;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.manager.mail.EmailVerificationManager;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserAddRequest;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserAdminResetPasswordRequest;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserQueryRequest;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserUpdateRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserEmailCodeRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserLoginRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserRefreshRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserRegisterRequest;
import com.xxr.lingtuthinktank.model.dto.user.profile.UserEmailUpdateRequest;
import com.xxr.lingtuthinktank.model.dto.user.profile.UserUpdateMyRequest;
import com.xxr.lingtuthinktank.model.dto.user.profile.UserUpdatePasswordRequest;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.user.LoginUserVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.model.vo.user.UserStatsVO;
import com.xxr.lingtuthinktank.service.mail.EmailService;
import com.xxr.lingtuthinktank.service.user.UserService;
import com.xxr.lingtuthinktank.utils.NetUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;

    @Resource
    private EmailVerificationManager emailVerificationManager;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String email = userRegisterRequest.getUserAccount();
        String code = userRegisterRequest.getCode();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(!Validator.isEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "验证码不能为空");
        boolean verified = emailVerificationManager.verifyRegisterCode(email, code);
        ThrowUtils.throwIf(!verified, ErrorCode.PARAMS_ERROR, "验证码无效或已过期");
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 发送注册邮箱验证码
     */
    @PostMapping("/register/code")
    public BaseResponse<Boolean> sendRegisterCode(@RequestBody UserEmailCodeRequest userEmailCodeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userEmailCodeRequest == null, ErrorCode.PARAMS_ERROR);
        String email = userEmailCodeRequest.getEmail();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(!Validator.isEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");

        long count = userService.count(new QueryWrapper<User>().eq("userAccount", email));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "邮箱已被占用");

        String clientIp = NetUtils.getIpAddress(request);
        String code = emailVerificationManager.createRegisterCode(email, clientIp);
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            emailVerificationManager.clearRegisterCode(email);
            throw e;
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public BaseResponse<LoginUserVO> refreshToken(@RequestBody UserRefreshRequest userRefreshRequest) {
        ThrowUtils.throwIf(userRefreshRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.refreshToken(userRefreshRequest.getRefreshToken());
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(userAddRequest.getUserAccount()), ErrorCode.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userAddRequest.getUserPassword()) || userAddRequest.getUserPassword().length() < 8,
                ErrorCode.PARAMS_ERROR, "初始密码至少 8 位");
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        if (StrUtil.isBlank(user.getUserAvatar())) {
            user.setUserAvatar(UserConstant.DEFAULT_AVATAR_URL);
        }
        if (StrUtil.isBlank(user.getUserRole())) {
            user.setUserRole(UserConstant.DEFAULT_ROLE);
        }
        String encryptPassword = userService.getEncryptPassword(userAddRequest.getUserPassword());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        if (user == null) {
            return ResultUtils.success(null);
        }
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @LogSensitive(description = "删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @LogSensitive(description = "更新用户")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 兼容 ID 精度丢失
        if (userUpdateRequest.getId() == null && userUpdateRequest.getIdStr() != null) {
            userUpdateRequest.setId(Long.parseLong(userUpdateRequest.getIdStr()));
        }
        if (userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/get/stats")
    public BaseResponse<UserStatsVO> getUserStats(long userId) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR);
        UserStatsVO stats = userService.getUserStats(userId);
        return ResultUtils.success(stats);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/code")
    public BaseResponse<Boolean> sendEmailCode(@RequestBody UserEmailCodeRequest userEmailCodeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userEmailCodeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String email = userEmailCodeRequest.getEmail();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(!Validator.isEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        ThrowUtils.throwIf(email.equals(loginUser.getUserAccount()), ErrorCode.PARAMS_ERROR, "邮箱未变更");

        long count = userService.count(new QueryWrapper<User>()
                .eq("userAccount", email)
                .ne("id", loginUser.getId()));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "邮箱已被占用");

        String clientIp = NetUtils.getIpAddress(request);
        String code = emailVerificationManager.createCode(loginUser.getId(), email, clientIp);
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            emailVerificationManager.clearCode(loginUser.getId(), email);
            throw e;
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新邮箱
     */
    @PostMapping("/email/update")
    public BaseResponse<Boolean> updateEmail(@RequestBody UserEmailUpdateRequest userEmailUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userEmailUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String email = userEmailUpdateRequest.getEmail();
        String code = userEmailUpdateRequest.getCode();
        ThrowUtils.throwIf(StrUtil.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "验证码不能为空");
        ThrowUtils.throwIf(!Validator.isEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        ThrowUtils.throwIf(email.equals(loginUser.getUserAccount()), ErrorCode.PARAMS_ERROR, "邮箱未变更");

        long count = userService.count(new QueryWrapper<User>()
                .eq("userAccount", email)
                .ne("id", loginUser.getId()));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "邮箱已被占用");

        boolean verified = emailVerificationManager.verifyCode(loginUser.getId(), email, code);
        ThrowUtils.throwIf(!verified, ErrorCode.PARAMS_ERROR, "验证码无效或已过期");

        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAccount(email);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdatePasswordRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.updatePassword(userUpdatePasswordRequest, loginUser);
        if (result) {
            // Remove login state
            // request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        }
        return ResultUtils.success(result);
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/admin/password/reset")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminResetPassword(
            @RequestBody UserAdminResetPasswordRequest userAdminResetPasswordRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userAdminResetPasswordRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.adminResetPassword(userAdminResetPasswordRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户个人主页摘要（OC、作品、收藏各前3条 + 统计）
     */
    @GetMapping("/profile/summary")
    public BaseResponse<com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO> getProfileSummary(
            @RequestParam Long userId, HttpServletRequest request) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO summary = userService.getUserProfileSummary(userId);
        return ResultUtils.success(summary);
    }

}
