package com.xxr.lingtuthinktank.controller.asset;

import cn.hutool.json.JSONUtil;
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
import com.xxr.lingtuthinktank.model.dto.asset.batch.PictureBatchConfirmRequest;
import com.xxr.lingtuthinktank.model.dto.asset.batch.PictureBatchDeleteRequest;
import com.xxr.lingtuthinktank.model.dto.asset.batch.PictureBatchDiscardRequest;
import com.xxr.lingtuthinktank.model.dto.asset.edit.PictureEditLockRequest;
import com.xxr.lingtuthinktank.model.dto.asset.edit.PictureEditRequest;
import com.xxr.lingtuthinktank.model.dto.asset.edit.PictureUpdateRequest;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureLike;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.model.dto.asset.meta.PictureTagCategory;
import com.xxr.lingtuthinktank.model.dto.asset.query.PictureQueryRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.SearchPictureByColorRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.SearchPictureByPictureRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadByBatchRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadRequest;
import com.xxr.lingtuthinktank.model.vo.asset.PictureBatchPreviewVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureEditLockVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureEditLogVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.manager.asset.PictureEditLockManager;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.service.asset.PictureEditLogService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import com.xxr.lingtuthinktank.service.asset.PictureLikeService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;

@RestController
@RequestMapping("/asset")
@Slf4j
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PictureEditLogService pictureEditLogService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private PictureLikeService pictureLikeService;

    @Resource
    private OssManager ossManager;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    @Resource
    private PictureEditLockManager pictureEditLockManager;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/multi")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<List<PictureVO>> uploadPictures(
            @RequestPart("files") List<MultipartFile> multipartFiles,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFiles == null || multipartFiles.isEmpty(), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = multipartFiles.stream()
                .map(file -> pictureService.uploadPicture(file, pictureUploadRequest, loginUser))
                .collect(java.util.stream.Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @Resource
    private com.xxr.lingtuthinktank.service.workspace.SpaceUserService spaceUserService;

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    @LogSensitive(description = "Delete Picture")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 鉴权逻辑：
        // 1. 本人可删除
        // 2. 系统管理员可删除
        // 3. 空间管理员可删除该空间下的图片
        boolean isOwner = oldPicture.getUserId().equals(loginUser.getId());
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isSpaceAdmin = false;
        if (oldPicture.getSpaceId() != null) {
            String role = spaceUserService.getSpaceUserRole(oldPicture.getSpaceId(), loginUser.getId());
            isSpaceAdmin = "admin".equals(role);
        }

        if (!isOwner && !isAdmin && !isSpaceAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 批量删除图片
     */
    @PostMapping("/delete/batch")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    @LogSensitive(description = "Delete Pictures Batch")
    public BaseResponse<Boolean> deletePictureBatch(@RequestBody PictureBatchDeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getIdList() == null || deleteRequest.getIdList().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<Long> idList = deleteRequest.getIdList();

        // 鉴权：循环判断是否都有权限
        for (Long id : idList) {
            Picture oldPicture = pictureService.getById(id);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

            boolean isOwner = oldPicture.getUserId().equals(loginUser.getId());
            boolean isAdmin = userService.isAdmin(loginUser);
            boolean isSpaceAdmin = false;
            if (oldPicture.getSpaceId() != null) {
                String role = spaceUserService.getSpaceUserRole(oldPicture.getSpaceId(), loginUser.getId());
                isSpaceAdmin = "admin".equals(role);
            }

            if (!isOwner && !isAdmin && !isSpaceAdmin) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 操作数据库
        boolean result = pictureService.removeByIds(idList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
            HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id,
            @RequestParam(required = false) String shareCode,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        spaceAuthManager.checkPictureViewAuth(loginUser, picture, shareCode);
        pictureService.addViewCount(id);
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
            HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫，同时允许空间作品库一次拉取更多内容
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        boolean isAdmin = loginUser != null && userService.isAdmin(loginUser);
        Long queryUserId = pictureQueryRequest.getUserId();
        Long querySpaceId = pictureQueryRequest.getSpaceId();
        boolean queryOwnPictures = loginUser != null && queryUserId != null && queryUserId.equals(loginUser.getId());
        if (querySpaceId != null) {
            ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "请先登录后访问空间内容");
            spaceAuthManager.checkSpaceViewAuth(loginUser, querySpaceId);
        }
        // 查询数据库
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Picture> queryWrapper = pictureService
                .getQueryWrapper(pictureQueryRequest);
        if (Boolean.TRUE.equals(pictureQueryRequest.getFavoriteOnly())) {
            User favoriteUser = loginUser != null ? loginUser : userService.getLoginUser(request);
            java.util.List<com.xxr.lingtuthinktank.model.entity.favorite.Favorite> favorites = favoriteService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xxr.lingtuthinktank.model.entity.favorite.Favorite>()
                            .eq("userId", favoriteUser.getId()));
            java.util.Set<Long> favoriteIds = favorites.stream()
                    .map(com.xxr.lingtuthinktank.model.entity.favorite.Favorite::getPictureId)
                    .collect(java.util.stream.Collectors.toSet());
            if (favoriteIds.isEmpty()) {
                return ResultUtils.success(new Page<>(current, size, 0));
            }
            queryWrapper.in("id", favoriteIds);
        }
        if (Boolean.TRUE.equals(pictureQueryRequest.getLikedOnly())) {
            User likedUser = loginUser != null ? loginUser : userService.getLoginUser(request);
            java.util.List<PictureLike> likes = pictureLikeService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PictureLike>()
                            .eq("userId", likedUser.getId()));
            java.util.Set<Long> likedIds = likes.stream()
                    .map(PictureLike::getPictureId)
                    .collect(java.util.stream.Collectors.toSet());
            if (likedIds.isEmpty()) {
                return ResultUtils.success(new Page<>(current, size, 0));
            }
            queryWrapper.in("id", likedIds);
        }
        // 非本人/非管理员默认仅展示公开作品；空间内查询遵循空间成员权限
        if (!isAdmin && querySpaceId == null && !queryOwnPictures) {
            queryWrapper.isNull("shareCode");
            queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());
            if (!Boolean.TRUE.equals(pictureQueryRequest.getFavoriteOnly())
                    && !Boolean.TRUE.equals(pictureQueryRequest.getLikedOnly())) {
                queryWrapper.isNull("spaceId");
            }
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
            HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        if (!hasPictureEditAuth(oldPicture, loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean hasAnyFieldToUpdate = pictureEditRequest.getName() != null
                || pictureEditRequest.getIntroduction() != null
                || pictureEditRequest.getCategory() != null
                || pictureEditRequest.getTags() != null
                || pictureEditRequest.getStatus() != null;
        if (!hasAnyFieldToUpdate) {
            return ResultUtils.success(true);
        }

        Date latestEditTime = resolveLatestEditTime(oldPicture);
        if (oldPicture.getSpaceId() != null) {
            if (pictureEditLockManager.isLockedByOtherUser(id, loginUser.getId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前资源正在被其他成员编辑");
            }
            if (pictureEditRequest.getLastKnownEditTime() != null
                    && latestEditTime != null
                    && !Objects.equals(latestEditTime.getTime(), pictureEditRequest.getLastKnownEditTime())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "资源已被他人更新，请刷新后重试");
            }
        }

        Date editTime = new Date();
        Picture editedSnapshot = buildEditedPictureSnapshot(oldPicture, pictureEditRequest, editTime);
        pictureService.validPicture(editedSnapshot);
        String changeSummary = buildPictureChangeSummary(oldPicture, editedSnapshot);
        String beforeSummary = buildPictureEditSummary(oldPicture);
        String afterSummary = buildPictureEditSummary(editedSnapshot);
        Picture reviewSnapshot = new Picture();
        pictureService.fillReviewParams(reviewSnapshot, loginUser);

        boolean result = pictureService.lambdaUpdate()
                .eq(Picture::getId, id)
                .eq(oldPicture.getSpaceId() != null && latestEditTime != null, Picture::getEditTime, latestEditTime)
                .set(Picture::getEditTime, editTime)
                .set(Picture::getReviewStatus, reviewSnapshot.getReviewStatus())
                .set(Picture::getReviewerId, reviewSnapshot.getReviewerId())
                .set(Picture::getReviewMessage, reviewSnapshot.getReviewMessage())
                .set(Picture::getReviewTime, reviewSnapshot.getReviewTime())
                .set(pictureEditRequest.getName() != null, Picture::getName, pictureEditRequest.getName())
                .set(pictureEditRequest.getIntroduction() != null, Picture::getIntroduction, pictureEditRequest.getIntroduction())
                .set(pictureEditRequest.getCategory() != null, Picture::getCategory, pictureEditRequest.getCategory())
                .set(pictureEditRequest.getTags() != null, Picture::getTags, JSONUtil.toJsonStr(pictureEditRequest.getTags()))
                .set(pictureEditRequest.getStatus() != null, Picture::getStatus, pictureEditRequest.getStatus())
                .update();
        if (!result) {
            if (oldPicture.getSpaceId() != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "资源已被他人更新，请刷新后重试");
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (oldPicture.getSpaceId() != null && StrUtil.isNotBlank(changeSummary)) {
            pictureEditLogService.saveEditLog(oldPicture, loginUser, changeSummary, beforeSummary, afterSummary);
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取图片编辑锁状态
     */
    @GetMapping("/edit-lock")
    @AuthCheck
    public BaseResponse<PictureEditLockVO> getPictureEditLock(@RequestParam Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (picture.getSpaceId() == null) {
            return ResultUtils.success(pictureEditLockManager.buildUnsupportedLockInfo(pictureId));
        }
        spaceAuthManager.checkSpaceViewAuth(loginUser, picture.getSpaceId());
        return ResultUtils.success(pictureEditLockManager.getLockInfo(pictureId, loginUser.getId()));
    }

    /**
     * 获取图片编辑锁
     */
    @PostMapping("/edit-lock/acquire")
    @AuthCheck
    public BaseResponse<PictureEditLockVO> acquirePictureEditLock(@RequestBody PictureEditLockRequest lockRequest,
            HttpServletRequest request) {
        Picture picture = validatePictureEditLockRequest(lockRequest);
        User loginUser = userService.getLoginUser(request);
        if (picture.getSpaceId() == null) {
            return ResultUtils.success(pictureEditLockManager.buildUnsupportedLockInfo(picture.getId()));
        }
        if (!hasPictureEditAuth(picture, loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该资源");
        }
        return ResultUtils.success(pictureEditLockManager.acquireLock(picture.getId(), loginUser));
    }

    /**
     * 续约图片编辑锁
     */
    @PostMapping("/edit-lock/renew")
    @AuthCheck
    public BaseResponse<PictureEditLockVO> renewPictureEditLock(@RequestBody PictureEditLockRequest lockRequest,
            HttpServletRequest request) {
        Picture picture = validatePictureEditLockRequest(lockRequest);
        User loginUser = userService.getLoginUser(request);
        if (picture.getSpaceId() == null) {
            return ResultUtils.success(pictureEditLockManager.buildUnsupportedLockInfo(picture.getId()));
        }
        if (!hasPictureEditAuth(picture, loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该资源");
        }
        return ResultUtils.success(pictureEditLockManager.renewLock(picture.getId(), loginUser));
    }

    /**
     * 释放图片编辑锁
     */
    @PostMapping("/edit-lock/release")
    @AuthCheck
    public BaseResponse<Boolean> releasePictureEditLock(@RequestBody PictureEditLockRequest lockRequest,
            HttpServletRequest request) {
        Picture picture = validatePictureEditLockRequest(lockRequest);
        User loginUser = userService.getLoginUser(request);
        if (picture.getSpaceId() == null) {
            return ResultUtils.success(true);
        }
        if (!hasPictureEditAuth(picture, loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权编辑该资源");
        }
        pictureEditLockManager.releaseLock(picture.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片协作编辑记录（团队空间成员可见）
     */
    @GetMapping("/edit-log/list")
    public BaseResponse<List<PictureEditLogVO>> listPictureEditLogs(@RequestParam Long pictureId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (picture.getSpaceId() == null) {
            return ResultUtils.success(java.util.Collections.emptyList());
        }
        User loginUser = userService.getLoginUser(request);
        spaceAuthManager.checkSpaceViewAuth(loginUser, picture.getSpaceId());
        return ResultUtils.success(pictureEditLogService.listPictureEditLogVOList(pictureId));
    }

    @GetMapping("/tag-category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @PostMapping("/upload/batch")
    public BaseResponse<PictureBatchPreviewVO> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        PictureBatchPreviewVO previewVO = pictureService.previewUploadPictureByBatch(pictureUploadByBatchRequest,
                loginUser);
        return ResultUtils.success(previewVO);
    }

    /**
     * 确认批量预览入库
     */
    @PostMapping("/upload/batch/confirm")
    public BaseResponse<List<PictureVO>> confirmUploadPictureByBatch(
            @RequestBody PictureBatchConfirmRequest pictureBatchConfirmRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureBatchConfirmRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.confirmUploadPictureByBatch(pictureBatchConfirmRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 放弃批量预览
     */
    @PostMapping("/upload/batch/discard")
    public BaseResponse<Boolean> discardUploadPictureBatch(
            @RequestBody PictureBatchDiscardRequest pictureBatchDiscardRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureBatchDiscardRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String batchId = pictureBatchDiscardRequest.getBatchId();
        ThrowUtils.throwIf(StrUtil.isBlank(batchId), ErrorCode.PARAMS_ERROR, "批次 id 不能为空");
        pictureService.discardBatchPreview(batchId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 按颜色搜索图片
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(
            @RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(searchPictureByColorRequest, request);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<PictureVO>> searchPictureByPicture(
            @RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        String pictureIdStr = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(StrUtil.isBlank(pictureIdStr), ErrorCode.PARAMS_ERROR, "图片id不能为空");
        Long pictureId;
        try {
            pictureId = Long.parseLong(pictureIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片id格式错误");
        }
        List<PictureVO> pictureVOList = pictureService.searchPictureByPicture(pictureId, request);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 通过文件搜索相似图片（不入库）
     */
    @PostMapping("/search/picture/file")
    public BaseResponse<List<PictureVO>> searchPictureByFile(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR);
        List<PictureVO> pictureVOList = pictureService.searchPictureByFile(file, request);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 获取回收站图片列表
     */
    @GetMapping("/recycle/list")
    public BaseResponse<List<PictureVO>> listDeletedPictures(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> result = pictureService.listDeletedPictures(loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 恢复图片
     */
    @PostMapping("/recycle/recover")
    @LogSensitive(description = "Recover Picture")
    public BaseResponse<Boolean> recoverPicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.recoverPicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 清空回收站（物理删除）
     */
    @PostMapping("/recycle/clear")
    @LogSensitive(description = "Clear Recycle Bin")
    public BaseResponse<Boolean> clearRecycleBin(@RequestBody PictureBatchDeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getIdList() == null || deleteRequest.getIdList().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.clearRecycleBin(deleteRequest.getIdList(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取相关图片推荐
     */
    @PostMapping("/list/related")
    public BaseResponse<List<PictureVO>> listRelatedPictures(@RequestBody PictureQueryRequest pictureQueryRequest,
            HttpServletRequest request) {
        Long pictureId = pictureQueryRequest.getId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        // 获取当前图片信息
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        spaceAuthManager.checkPictureViewAuth(loginUser, picture);
        // 根据分类和标签查找相似图片
        PictureQueryRequest queryRequest = new PictureQueryRequest();
        queryRequest.setCategory(picture.getCategory());
        queryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        queryRequest.setPageSize(10);
        queryRequest.setCurrent(1);
        // 排除当前图片
        Page<Picture> picturePage = pictureService.page(new Page<>(1, 10),
                pictureService.getQueryWrapper(queryRequest).ne("id", pictureId).isNull("shareCode"));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request).getRecords());
    }

    /**
     * 生成分享码（仅作者可操作）
     */
    @PostMapping("/share/generate")
    public BaseResponse<String> generateShareCode(@RequestParam Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (!canManagePictureShare(loginUser, picture)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权生成分享码");
        }
        // 生成6位随机码
        String shareCode = cn.hutool.core.util.RandomUtil.randomString(6).toUpperCase();
        picture.setShareCode(shareCode);
        pictureService.updateById(picture);
        return ResultUtils.success(shareCode);
    }

    /**
     * 清除分享码（设为公开）
     */
    @PostMapping("/share/clear")
    public BaseResponse<Boolean> clearShareCode(@RequestParam Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        if (!canManagePictureShare(loginUser, picture)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        picture.setShareCode(null);
        pictureService.updateById(picture);
        return ResultUtils.success(true);
    }

    /**
     * 通过分享码获取图片
     */
    @GetMapping("/share/get")
    public BaseResponse<PictureVO> getPictureByShareCode(@RequestParam Long pictureId,
            @RequestParam String shareCode, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        spaceAuthManager.checkPictureViewAuth(loginUser, picture, shareCode);
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 下载图片（非本人会标记水印）
     */
    @GetMapping("/download")
    public BaseResponse<String> downloadPicture(@RequestParam Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUserPermitNull(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "请先登录");
        checkDownloadAccess(picture, loginUser);
        return ResultUtils.success(resolveDownloadUrl(picture, loginUser));
    }

    /**
     * 获取图片下载信息（含水印标记）
     */
    @GetMapping("/download/info")
    public BaseResponse<DownloadInfoVO> getDownloadInfo(@RequestParam Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUserPermitNull(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "请先登录");
        checkDownloadAccess(picture, loginUser);

        DownloadInfoVO info = new DownloadInfoVO();
        info.setUrl(resolveDownloadUrl(picture, loginUser));
        info.setNeedsWatermark(shouldApplyDownloadWatermark(picture, loginUser));
        info.setFileName(resolveDownloadFileName(picture));
        info.setPicSize(picture.getPicSize());
        return ResultUtils.success(info);
    }

    /**
     * 下载图片文件（非本人自动加水印）
     */
    @GetMapping("/download/file")
    public void downloadPictureFile(@RequestParam Long pictureId, HttpServletRequest request,
            HttpServletResponse response) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUserPermitNull(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "请先登录");
        String format = picture.getPicFormat() != null ? picture.getPicFormat().toLowerCase() : "";
        checkDownloadAccess(picture, loginUser);

        String fileName = resolveDownloadFileName(picture);
        String downloadUrl = resolveDownloadUrl(picture, loginUser);
        try (InputStream inputStream = getPictureInputStream(downloadUrl, false)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "." + (StrUtil.isBlank(format) ? "bin" : format) + "\"");
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "下载失败");
        }
    }

    private Picture buildEditedPictureSnapshot(Picture oldPicture, PictureEditRequest pictureEditRequest, Date editTime) {
        Picture snapshot = new Picture();
        BeanUtils.copyProperties(oldPicture, snapshot);
        if (pictureEditRequest.getName() != null) {
            snapshot.setName(pictureEditRequest.getName());
        }
        if (pictureEditRequest.getIntroduction() != null) {
            snapshot.setIntroduction(pictureEditRequest.getIntroduction());
        }
        if (pictureEditRequest.getCategory() != null) {
            snapshot.setCategory(pictureEditRequest.getCategory());
        }
        if (pictureEditRequest.getTags() != null) {
            snapshot.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        }
        if (pictureEditRequest.getStatus() != null) {
            snapshot.setStatus(pictureEditRequest.getStatus());
        }
        snapshot.setEditTime(editTime);
        return snapshot;
    }

    private String buildPictureChangeSummary(Picture oldPicture, Picture newPicture) {
        java.util.List<String> changedFields = new java.util.ArrayList<>();
        if (!Objects.equals(StrUtil.nullToEmpty(oldPicture.getName()), StrUtil.nullToEmpty(newPicture.getName()))) {
            changedFields.add("名称");
        }
        if (!Objects.equals(StrUtil.nullToEmpty(oldPicture.getIntroduction()), StrUtil.nullToEmpty(newPicture.getIntroduction()))) {
            changedFields.add("简介");
        }
        if (!Objects.equals(StrUtil.nullToEmpty(oldPicture.getCategory()), StrUtil.nullToEmpty(newPicture.getCategory()))) {
            changedFields.add("分类");
        }
        if (!Objects.equals(joinTags(oldPicture.getTags()), joinTags(newPicture.getTags()))) {
            changedFields.add("标签");
        }
        if (!Objects.equals(oldPicture.getStatus(), newPicture.getStatus())) {
            changedFields.add("状态");
        }
        if (changedFields.isEmpty()) {
            return null;
        }
        return "修改了" + String.join("、", changedFields);
    }

    private String buildPictureEditSummary(Picture picture) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        parts.add("名称：" + defaultSummaryValue(picture.getName()));
        parts.add("分类：" + defaultSummaryValue(picture.getCategory()));
        parts.add("标签：" + defaultSummaryValue(joinTags(picture.getTags())));
        parts.add("状态：" + formatPictureStatus(picture.getStatus()));
        if (StrUtil.isNotBlank(picture.getIntroduction())) {
            parts.add("简介：" + abbreviate(picture.getIntroduction(), 36));
        }
        return String.join("；", parts);
    }

    private String joinTags(String tagsJson) {
        java.util.List<String> tagList = parseTags(tagsJson);
        if (tagList.isEmpty()) {
            return "";
        }
        return String.join("、", tagList);
    }

    private java.util.List<String> parseTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson) || Objects.equals(tagsJson, "null")) {
            return java.util.Collections.emptyList();
        }
        try {
            java.util.List<String> tagList = JSONUtil.toList(tagsJson, String.class);
            return tagList == null ? java.util.Collections.emptyList() : tagList;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private String formatPictureStatus(Integer status) {
        if (status == null) {
            return "未设置";
        }
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "线稿";
            case 2:
                return "色稿";
            case 3:
                return "成图";
            default:
                return "状态#" + status;
        }
    }

    private String defaultSummaryValue(String value) {
        return StrUtil.isBlank(value) ? "未填写" : value;
    }

    private String abbreviate(String value, int maxLength) {
        if (StrUtil.isBlank(value) || value.length() <= maxLength) {
            return defaultSummaryValue(value);
        }
        return value.substring(0, maxLength) + "...";
    }

    private boolean canManagePictureShare(User loginUser, Picture picture) {
        if (loginUser == null || picture == null) {
            return false;
        }
        if (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
            return true;
        }
        if (picture.getSpaceId() == null) {
            return false;
        }
        String role = spaceUserService.getSpaceUserRole(picture.getSpaceId(), loginUser.getId());
        return Arrays.asList("admin", "editor").contains(role);
    }

    private boolean hasPictureEditAuth(Picture picture, User loginUser) {
        if (picture == null || loginUser == null) {
            return false;
        }
        if (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
            return true;
        }
        if (picture.getSpaceId() == null) {
            return false;
        }
        String role = spaceUserService.getSpaceUserRole(picture.getSpaceId(), loginUser.getId());
        return Arrays.asList("admin", "editor").contains(role);
    }

    private Date resolveLatestEditTime(Picture picture) {
        if (picture == null) {
            return null;
        }
        if (picture.getEditTime() != null) {
            return picture.getEditTime();
        }
        if (picture.getUpdateTime() != null) {
            return picture.getUpdateTime();
        }
        return picture.getCreateTime();
    }

    private Picture validatePictureEditLockRequest(PictureEditLockRequest lockRequest) {
        ThrowUtils.throwIf(lockRequest == null || lockRequest.getPictureId() == null || lockRequest.getPictureId() <= 0,
                ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(lockRequest.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return picture;
    }

    private InputStream getPictureInputStream(String url) throws Exception {
        return getPictureInputStream(url, true);
    }

    private InputStream getPictureInputStream(String url, boolean signIfNeeded) throws Exception {
        if (StrUtil.isBlank(url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址为空");
        }
        if (url.startsWith("/api/images/")) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "已禁用本地存储");
        }
        String targetUrl = signIfNeeded ? ossManager.signUrlIfNeeded(url) : url;
        return new URL(targetUrl).openStream();
    }

    private void checkDownloadAccess(Picture picture, User loginUser) {
        boolean isOwnerOrAdmin = isOwnerOrAdmin(picture, loginUser);
        boolean hasDownloadPermission = hasDownloadPermission(picture, loginUser);
        if (!isOwnerOrAdmin && picture.getShareCode() != null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品未公开");
        }
        if (!hasDownloadPermission) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无下载权限");
        }
    }

    private boolean isOwnerOrAdmin(Picture picture, User loginUser) {
        return picture != null
                && loginUser != null
                && (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser));
    }

    private boolean shouldApplyDownloadWatermark(Picture picture, User loginUser) {
        if (picture == null || loginUser == null) {
            return false;
        }
        if (isOwnerOrAdmin(picture, loginUser)) {
            return false;
        }
        return ossManager.isImageFormat(picture.getPicFormat());
    }

    private String resolveDownloadUrl(Picture picture, User loginUser) {
        if (shouldApplyDownloadWatermark(picture, loginUser)) {
            return ossManager.signImageUrlWithWatermarkIfNeeded(picture.getUrl(), picture.getPicFormat());
        }
        return ossManager.signUrlIfNeeded(picture.getUrl());
    }

    private String resolveDownloadFileName(Picture picture) {
        if (picture == null || StrUtil.isBlank(picture.getName())) {
            return "download";
        }
        return picture.getName();
    }

    private boolean hasDownloadPermission(Picture picture, User loginUser) {
        if (picture == null || loginUser == null) {
            return false;
        }
        if (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
            return true;
        }
        return picture.getShareCode() == null;
    }

    @lombok.Data
    public static class DownloadInfoVO {
        private String url;
        private Boolean needsWatermark;
        private String fileName;
        private Long picSize;
    }
}
