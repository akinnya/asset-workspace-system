package com.xxr.lingtuthinktank.controller.asset;

import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.model.dto.asset.query.PictureQueryRequest;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.asset.PictureLikeService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片点赞接口
 */
@RestController
@RequestMapping("/asset-like")
@Slf4j
public class PictureLikeController {

    @Resource
    private PictureLikeService pictureLikeService;

    @Resource
    private UserService userService;

    @Resource
    private com.xxr.lingtuthinktank.service.asset.PictureService pictureService;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    /**
     * 点赞 / 取消点赞
     *
     * @param pictureQueryRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Integer> doPictureLike(@RequestBody PictureQueryRequest pictureQueryRequest,
            HttpServletRequest request) {
        if (pictureQueryRequest == null || pictureQueryRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long pictureId = pictureQueryRequest.getId();
        Picture picture = pictureService.getById(pictureId);
        if (picture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        spaceAuthManager.checkPictureViewAuth(loginUser, picture);
        int result = pictureLikeService.doPictureLike(pictureId, loginUser);
        return ResultUtils.success(result);
    }
}
