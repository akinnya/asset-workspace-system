package com.xxr.lingtuthinktank.manager.workspace;

import cn.hutool.core.util.StrUtil;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceTypeEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceUserRoleEnum;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.workspace.SpaceUserService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 空间权限管理器
 */
@Component
public class SpaceAuthManager {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 检查用户对空间的访问权限
     *
     * @param user    用户
     * @param spaceId 空间id
     */
    public void checkSpaceViewAuth(User user, Long spaceId) {
        if (spaceId == null) {
            return; // 公共图库无需校验
        }

        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 系统管理员拥有所有权限
        if (userService.isAdmin(user)) {
            return;
        }

        // 私有空间只有创建者可以访问
        if (SpaceTypeEnum.PRIVATE.getValue() == space.getSpaceType()) {
            if (!space.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该空间");
            }
        } else {
            // 团队空间检查是否是成员
            if (!space.getUserId().equals(user.getId()) &&
                !spaceUserService.hasSpaceAuth(spaceId, user.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该空间");
            }
        }
    }

    /**
     * 检查用户对空间的编辑权限
     *
     * @param user    用户
     * @param spaceId 空间id
     */
    public void checkSpaceEditAuth(User user, Long spaceId) {
        if (spaceId == null) {
            return; // 公共图库无需校验
        }

        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 系统管理员拥有所有权限
        if (userService.isAdmin(user)) {
            return;
        }

        // 私有空间只有创建者可以编辑
        if (SpaceTypeEnum.PRIVATE.getValue() == space.getSpaceType()) {
            if (!space.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该空间");
            }
        } else {
            // 团队空间检查角色
            String userRole = spaceUserService.getSpaceUserRole(spaceId, user.getId());
            if (userRole == null && !space.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该空间");
            }
            // 只有编辑者和管理员可以编辑
            List<String> editRoles = Arrays.asList(
                    SpaceUserRoleEnum.EDITOR.getValue(),
                    SpaceUserRoleEnum.ADMIN.getValue()
            );
            if (!editRoles.contains(userRole) && !space.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该空间内容");
            }
        }
    }

    /**
     * 检查用户对空间的管理权限
     *
     * @param user    用户
     * @param spaceId 空间id
     */
    public void checkSpaceAdminAuth(User user, Long spaceId) {
        if (spaceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间id不能为空");
        }

        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 系统管理员拥有所有权限
        if (userService.isAdmin(user)) {
            return;
        }

        // 空间创建者拥有管理权限
        if (space.getUserId().equals(user.getId())) {
            return;
        }

        // 团队空间检查是否是管理员角色
        if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
            String userRole = spaceUserService.getSpaceUserRole(spaceId, user.getId());
            if (!SpaceUserRoleEnum.ADMIN.getValue().equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权管理该空间");
            }
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权管理该空间");
        }
    }

    /**
     * 检查用户对图片的查看权限
     *
     * @param user      用户，可为空
     * @param picture   图片
     * @param shareCode 分享码，可为空
     */
    public void checkPictureViewAuth(User user, Picture picture, String shareCode) {
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        if (user != null && userService.isAdmin(user)) {
            return;
        }

        if (user != null && picture.getUserId().equals(user.getId())) {
            return;
        }

        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            if (user != null) {
                try {
                    checkSpaceViewAuth(user, spaceId);
                    return;
                } catch (BusinessException e) {
                    if (e.getCode() != ErrorCode.NO_AUTH_ERROR.getCode()) {
                        throw e;
                    }
                }
            }
            if (!Objects.equals(PictureReviewStatusEnum.PASS.getValue(), picture.getReviewStatus())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品未公开");
            }
            if (StrUtil.isBlank(picture.getShareCode())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该空间作品");
            }
            validateShareCode(picture, shareCode);
            return;
        }

        if (!Objects.equals(PictureReviewStatusEnum.PASS.getValue(), picture.getReviewStatus())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品未公开");
        }
        if (StrUtil.isNotBlank(picture.getShareCode())) {
            validateShareCode(picture, shareCode);
        }
    }

    /**
     * 检查用户对图片的查看权限（不允许通过分享码绕过）
     *
     * @param user    用户
     * @param picture 图片
     */
    public void checkPictureViewAuth(User user, Picture picture) {
        checkPictureViewAuth(user, picture, null);
    }

    private void validateShareCode(Picture picture, String shareCode) {
        if (StrUtil.isBlank(shareCode)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要分享码");
        }
        if (!StrUtil.equals(picture.getShareCode(), shareCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分享码错误");
        }
    }

    /**
     * 检查用户对图片的编辑权限
     *
     * @param user    用户
     * @param picture 图片
     */
    public void checkPictureEditAuth(User user, Picture picture) {
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 系统管理员拥有所有权限
        if (userService.isAdmin(user)) {
            return;
        }

        // 图片上传者可以编辑自己的图片
        if (picture.getUserId().equals(user.getId())) {
            return;
        }

        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            // 检查空间编辑权限
            checkSpaceEditAuth(user, spaceId);
        } else {
            // 公共图库只有管理员和上传者可以编辑
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该图片");
        }
    }

    /**
     * 检查用户对图片的删除权限
     *
     * @param user    用户
     * @param picture 图片
     */
    public void checkPictureDeleteAuth(User user, Picture picture) {
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 系统管理员拥有所有权限
        if (userService.isAdmin(user)) {
            return;
        }

        // 图片上传者可以删除自己的图片
        if (picture.getUserId().equals(user.getId())) {
            return;
        }

        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            // 检查空间管理权限
            checkSpaceAdminAuth(user, spaceId);
        } else {
            // 公共图库只有管理员和上传者可以删除
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该图片");
        }
    }
}
