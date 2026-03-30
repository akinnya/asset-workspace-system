package com.xxr.lingtuthinktank.controller.asset;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.DeleteRequest;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.model.dto.asset.attachment.PictureAttachmentAddRequest;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureAttachment;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.service.asset.PictureAttachmentService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/asset/attachment")
public class PictureAttachmentController {

    @Resource
    private PictureAttachmentService pictureAttachmentService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private OssManager ossManager;

    @PostMapping("/add")
    public BaseResponse<Long> addPictureAttachment(@RequestBody PictureAttachmentAddRequest request,
            HttpServletRequest httpRequest) {
        if (request == null || request.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Picture picture = pictureService.getById(request.getPictureId());
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // Permission check: only owner or admin can add attachment
        if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        PictureAttachment attachment = new PictureAttachment();
        BeanUtils.copyProperties(request, attachment);
        boolean result = pictureAttachmentService.save(attachment);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(attachment.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePictureAttachment(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        PictureAttachment attachment = pictureAttachmentService.getById(deleteRequest.getId());
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        Picture picture = pictureService.getById(attachment.getPictureId());
        if (picture != null && !picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = pictureAttachmentService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    public BaseResponse<List<PictureAttachment>> listPictureAttachments(long pictureId,
            HttpServletRequest httpRequest) {
        if (pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(pictureId);
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUserPermitNull(httpRequest);
        boolean isOwnerOrAdmin = loginUser != null
                && (picture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser));
        if (!isOwnerOrAdmin) {
            if (!Objects.equals(PictureReviewStatusEnum.PASS.getValue(), picture.getReviewStatus())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品未公开");
            }
            if (picture.getShareCode() != null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要分享码");
            }
        }
        QueryWrapper<PictureAttachment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        List<PictureAttachment> list = pictureAttachmentService.list(queryWrapper);
        list.forEach(item -> {
            if (item.getFileUrl() != null) {
                item.setFileUrl(ossManager.signUrlIfNeeded(item.getFileUrl()));
            }
        });
        return ResultUtils.success(list);
    }
}
