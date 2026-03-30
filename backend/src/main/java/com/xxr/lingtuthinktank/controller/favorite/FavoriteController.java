package com.xxr.lingtuthinktank.controller.favorite;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.entity.favorite.Favorite;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 收藏接口
 */
@RestController
@RequestMapping("/favorite")
@Slf4j
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> addFavorite(@RequestParam Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        spaceAuthManager.checkPictureViewAuth(loginUser, picture);
        // 检查是否已收藏
        long count = favoriteService.count(new QueryWrapper<Favorite>()
                .eq("userId", loginUser.getId())
                .eq("pictureId", pictureId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(loginUser.getId());
        favorite.setPictureId(pictureId);
        boolean result = favoriteService.save(favorite);
        if (result && !picture.getUserId().equals(loginUser.getId())) {
            Notification notification = new Notification();
            notification.setUserId(picture.getUserId());
            notification.setFromUserId(loginUser.getId());
            notification.setType("favorite");
            notification.setTargetId(picture.getId());
            String userName = loginUser.getUserName() != null ? loginUser.getUserName() : "用户";
            notification.setContent(userName + " 收藏了你的作品");
            notification.setIsRead(0);
            notificationService.save(notification);
        }
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/cancel")
    @com.xxr.lingtuthinktank.annotation.AuthCheck
    public BaseResponse<Boolean> cancelFavorite(@RequestParam Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = favoriteService.remove(new QueryWrapper<Favorite>()
                .eq("userId", loginUser.getId())
                .eq("pictureId", pictureId));
        return ResultUtils.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public BaseResponse<Boolean> checkFavorite(@RequestParam Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long count = favoriteService.count(new QueryWrapper<Favorite>()
                .eq("userId", loginUser.getId())
                .eq("pictureId", pictureId));
        return ResultUtils.success(count > 0);
    }

    /**
     * 获取收藏列表
     */
    @PostMapping("/list")
    public BaseResponse<Page<PictureVO>> listFavorites(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 先获取收藏的图片ID
        Page<Favorite> favoritePage = favoriteService.page(
                new Page<>(current, pageSize),
                new QueryWrapper<Favorite>()
                        .eq("userId", loginUser.getId())
                        .orderByDesc("createTime"));
        List<Long> pictureIds = favoritePage.getRecords().stream()
                .map(Favorite::getPictureId)
                .collect(Collectors.toList());

        if (pictureIds.isEmpty()) {
            return ResultUtils.success(new Page<>());
        }

        List<Picture> pictures = pictureService.list(new QueryWrapper<Picture>().in("id", pictureIds));
        java.util.Map<Long, Picture> pictureMap = pictures.stream()
                .collect(Collectors.toMap(Picture::getId, picture -> picture, (left, right) -> left));
        List<PictureVO> pictureVOList = pictureIds.stream()
                .map(pictureMap::get)
                .filter(Objects::nonNull)
                .filter(picture -> {
                    try {
                        spaceAuthManager.checkPictureViewAuth(loginUser, picture);
                        return true;
                    } catch (BusinessException e) {
                        return false;
                    }
                })
                .map(picture -> pictureService.getPictureVO(picture, request))
                .collect(Collectors.toList());

        Page<PictureVO> result = new Page<>(current, pageSize, pictureVOList.size());
        result.setRecords(pictureVOList);
        return ResultUtils.success(result);
    }
}
