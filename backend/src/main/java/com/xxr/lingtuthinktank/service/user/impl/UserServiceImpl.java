package com.xxr.lingtuthinktank.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.constant.UserConstant;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserAdminResetPasswordRequest;
import com.xxr.lingtuthinktank.model.dto.user.admin.UserQueryRequest;
import com.xxr.lingtuthinktank.model.dto.user.auth.UserRegisterRequest;
import com.xxr.lingtuthinktank.model.dto.user.profile.UserUpdatePasswordRequest;
import com.xxr.lingtuthinktank.model.entity.favorite.Favorite;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.entity.user.UserToken;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.model.enums.user.UserRoleEnum;
import com.xxr.lingtuthinktank.model.vo.user.LoginUserVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.model.vo.user.UserStatsVO;
import com.xxr.lingtuthinktank.model.entity.user.UserFollow;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import com.xxr.lingtuthinktank.service.user.UserService;
import com.xxr.lingtuthinktank.mapper.user.UserMapper;
import com.xxr.lingtuthinktank.mapper.follow.UserFollowMapper;
import com.xxr.lingtuthinktank.mapper.asset.PictureMapper;
import com.xxr.lingtuthinktank.mapper.asset.PictureLikeMapper;
import com.xxr.lingtuthinktank.mapper.user.UserTokenMapper;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 40655
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-06-07 18:24:43
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private PictureLikeMapper pictureLikeMapper;

    @Resource
    private UserTokenMapper userTokenMapper;

    @Resource
    private OssManager ossManager;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private FavoriteService favoriteService;

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 1. 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String code = userRegisterRequest.getCode();
        if (StrUtil.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (!Validator.isEmail(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserAvatar(UserConstant.DEFAULT_AVATAR_URL);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 生成并保存 Token
        String accessToken = jwtUtils.generateToken(user.getId(), user.getUserAccount());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUserAccount());
        revokeUserTokens(user.getId());
        saveToken(user.getId(), accessToken, "access", jwtUtils.getExpirationFromToken(accessToken));
        saveToken(user.getId(), refreshToken, "refresh", jwtUtils.getExpirationFromToken(refreshToken));

        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setAccessToken(accessToken);
        loginUserVO.setRefreshToken(refreshToken);
        return loginUserVO;
    }

    @Override
    public LoginUserVO refreshToken(String refreshToken) {
        ThrowUtils.throwIf(StrUtil.isBlank(refreshToken), ErrorCode.PARAMS_ERROR, "刷新令牌不能为空");
        ThrowUtils.throwIf(!jwtUtils.validateToken(refreshToken), ErrorCode.NOT_LOGIN_ERROR);

        UserToken tokenRecord = userTokenMapper.selectOne(new QueryWrapper<UserToken>()
                .eq("token", refreshToken)
                .eq("tokenType", "refresh")
                .eq("isRevoked", 0));
        ThrowUtils.throwIf(tokenRecord == null, ErrorCode.NOT_LOGIN_ERROR);

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        ThrowUtils.throwIf(userId == null || !userId.equals(tokenRecord.getUserId()), ErrorCode.NOT_LOGIN_ERROR);
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        // 旋转刷新令牌并撤销旧的访问令牌
        revokeAccessTokens(userId);
        revokeTokenByValue(refreshToken);

        String newAccessToken = jwtUtils.generateToken(userId, user.getUserAccount());
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, user.getUserAccount());
        saveToken(userId, newAccessToken, "access", jwtUtils.getExpirationFromToken(newAccessToken));
        saveToken(userId, newRefreshToken, "refresh", jwtUtils.getExpirationFromToken(newRefreshToken));

        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setAccessToken(newAccessToken);
        loginUserVO.setRefreshToken(newRefreshToken);
        return loginUserVO;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "xxr";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        if (user.getId() != null) {
            loginUserVO.setIdStr(String.valueOf(user.getId()));
        }
        if (loginUserVO.getUserAvatar() != null) {
            loginUserVO.setUserAvatar(ossManager.signUrlIfNeeded(loginUserVO.getUserAvatar()));
        }
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!jwtUtils.validateToken(token), ErrorCode.NOT_LOGIN_ERROR);

        UserToken tokenRecord = userTokenMapper.selectOne(new QueryWrapper<UserToken>()
                .eq("token", token)
                .eq("tokenType", "access")
                .eq("isRevoked", 0));
        ThrowUtils.throwIf(tokenRecord == null, ErrorCode.NOT_LOGIN_ERROR);

        Long userId = jwtUtils.getUserIdFromToken(token);
        User currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.NOT_LOGIN_ERROR);
        UserToken tokenRecord = userTokenMapper.selectOne(new QueryWrapper<UserToken>()
                .eq("token", token)
                .eq("tokenType", "access")
                .eq("isRevoked", 0));
        ThrowUtils.throwIf(tokenRecord == null, ErrorCode.NOT_LOGIN_ERROR);
        revokeUserTokens(tokenRecord.getUserId());
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if (user.getId() != null) {
            userVO.setIdStr(String.valueOf(user.getId()));
        }
        if (userVO.getUserAvatar() != null) {
            userVO.setUserAvatar(ossManager.signUrlIfNeeded(userVO.getUserAvatar()));
        }
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token) || !jwtUtils.validateToken(token)) {
            return null;
        }
        UserToken tokenRecord = userTokenMapper.selectOne(new QueryWrapper<UserToken>()
                .eq("token", token)
                .eq("tokenType", "access")
                .eq("isRevoked", 0));
        if (tokenRecord == null) {
            return null;
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        return this.getById(userId);
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO stats = new UserStatsVO();
        stats.setUserId(userId);

        // 1. 关注数 (我关注的人)
        long followingCount = userFollowMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("followerId", userId));
        stats.setFollowingCount(followingCount);

        // 2. 粉丝数 (关注我的人)
        long followerCount = userFollowMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("followingId", userId));
        stats.setFollowerCount(followerCount);

        // 3. 作品数
        long pictureCount = pictureMapper.selectCount(
                new QueryWrapper<Picture>().eq("userId", userId).eq("isDelete", 0));
        stats.setPictureCount(pictureCount);

        // 4. 获赞数 (所有作品的点赞总和)
        Long likeCount = pictureLikeMapper.countLikesByUserId(userId);
        stats.setLikeCount(likeCount != null ? likeCount : 0L);

        // 6. 存储空间
        Picture sumPicture = pictureMapper.selectOne(
                new QueryWrapper<Picture>()
                        .select("ifnull(sum(picSize), 0) as picSize")
                        .eq("userId", userId)
                        .eq("isDelete", 0));
        stats.setStorageUsed(sumPicture != null && sumPicture.getPicSize() != null ? sumPicture.getPicSize() : 0L);
        stats.setStorageTotal(1073741824L); // 1GB

        return stats;
    }

    @Override
    public com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO getUserProfileSummary(Long userId) {
        com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO summary = new com.xxr.lingtuthinktank.model.vo.user.UserProfileSummaryVO();

        // 1. 用户基本信息
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        summary.setUserInfo(getUserVO(user));

        // 2. 统计数据
        summary.setStats(getUserStats(userId));

        // 3. 我的作品 (前3条)
        List<Picture> myPictures = pictureMapper.selectList(
                new QueryWrapper<Picture>()
                        .eq("userId", userId)
                        .eq("isDelete", 0)
                        .eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue())
                        .orderByDesc("createTime")
                        .last("LIMIT 3"));
        List<com.xxr.lingtuthinktank.model.vo.asset.PictureVO> myPostVos = myPictures.stream()
                .map(com.xxr.lingtuthinktank.model.vo.asset.PictureVO::objToVo)
                .collect(Collectors.toList());
        myPostVos.forEach(this::fillSignedPictureUrls);
        summary.setMyPosts(myPostVos);

        // 4. 我的收藏 (前3条)
        List<Favorite> favorites = favoriteService.list(
                new QueryWrapper<Favorite>()
                        .eq("userId", userId)
                        .orderByDesc("createTime")
                        .last("LIMIT 3"));
        List<Long> pictureIds = favorites.stream().map(Favorite::getPictureId).collect(Collectors.toList());
        List<Picture> favoritePictures = pictureIds.isEmpty()
                ? new ArrayList<>()
                : pictureMapper.selectList(new QueryWrapper<Picture>()
                        .in("id", pictureIds)
                        .eq("isDelete", 0)
                        .eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue()));
        java.util.Map<Long, Picture> pictureMap = favoritePictures.stream()
                .collect(Collectors.toMap(Picture::getId, picture -> picture));
        List<PictureVO> favoriteVOList = pictureIds.stream()
                .map(pictureMap::get)
                .filter(Objects::nonNull)
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        favoriteVOList.forEach(this::fillSignedPictureUrls);
        summary.setMyFavorites(favoriteVOList);

        return summary;
    }

    private void fillSignedPictureUrls(PictureVO pictureVO) {
        if (pictureVO == null) {
            return;
        }
        String format = pictureVO.getPicFormat();
        if (pictureVO.getUrl() != null) {
            if (ossManager.isImageFormat(format)) {
                pictureVO.setUrl(ossManager.signImageUrlWithWatermarkIfNeeded(pictureVO.getUrl(), format));
            } else {
                pictureVO.setUrl(ossManager.signUrlIfNeeded(pictureVO.getUrl()));
            }
        }
        if (pictureVO.getThumbnailUrl() != null) {
            pictureVO.setThumbnailUrl(ossManager.signImageUrlWithWatermarkIfNeeded(pictureVO.getThumbnailUrl()));
        }
    }

    @Override
    public boolean updatePassword(
            UserUpdatePasswordRequest userUpdatePasswordRequest,
            User loginUser) {
        if (userUpdatePasswordRequest == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        String newPassword = userUpdatePasswordRequest.getNewPassword();
        String checkPassword = userUpdatePasswordRequest.getCheckPassword();

        if (StrUtil.hasBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameter cannot be empty");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "New passwords do not match");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "New password is too short");
        }

        // Verify old password
        String encryptOldPassword = getEncryptPassword(oldPassword);
        if (!encryptOldPassword.equals(loginUser.getUserPassword())) {
            log.error("Password update failed: old password mismatch for user {}", loginUser.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Old password incorrect");
        }

        // Update password
        String encryptNewPassword = getEncryptPassword(newPassword);
        loginUser.setUserPassword(encryptNewPassword);
        boolean result = this.updateById(loginUser);
        if (result) {
            // Revoke all tokens for this user to force re-login on other devices if needed
            // revokeUserTokens(loginUser.getId());
        }
        return result;
    }


    @Override
    public boolean adminResetPassword(
            UserAdminResetPasswordRequest userAdminResetPasswordRequest,
            User loginUser) {
        if (userAdminResetPasswordRequest == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long userId = userAdminResetPasswordRequest.getUserId();
        String newPassword = userAdminResetPasswordRequest.getNewPassword();
        String checkPassword = userAdminResetPasswordRequest.getCheckPassword();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (StrUtil.hasBlank(newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        User targetUser = this.getById(userId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String encryptNewPassword = getEncryptPassword(newPassword);
        targetUser.setUserPassword(encryptNewPassword);
        boolean result = this.updateById(targetUser);
        if (result) {
            revokeUserTokens(targetUser.getId());
        }
        return result;
    }

    private void saveToken(Long userId, String token, String tokenType, java.util.Date expiresAt) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setToken(token);
        userToken.setTokenType(tokenType);
        userToken.setExpiresAt(expiresAt);
        userToken.setIsRevoked(0);
        userTokenMapper.insert(userToken);
    }

    private void revokeUserTokens(Long userId) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserToken> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("userId", userId).eq("isRevoked", 0).set("isRevoked", 1);
        userTokenMapper.update(null, updateWrapper);
    }

    private void revokeAccessTokens(Long userId) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserToken> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("userId", userId)
                .eq("tokenType", "access")
                .eq("isRevoked", 0)
                .set("isRevoked", 1);
        userTokenMapper.update(null, updateWrapper);
    }

    private void revokeTokenByValue(String token) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserToken> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("token", token).eq("isRevoked", 0).set("isRevoked", 1);
        userTokenMapper.update(null, updateWrapper);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isBlank(authHeader)) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
