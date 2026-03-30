package com.xxr.lingtuthinktank.controller.workspace;

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
import com.xxr.lingtuthinktank.model.dto.workspace.command.SpaceAddRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.command.SpaceEditRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.query.SpaceQueryRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.command.SpaceUpdateRequest;
import com.xxr.lingtuthinktank.model.entity.comment.Comment;
import com.xxr.lingtuthinktank.model.entity.favorite.Favorite;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureEditLog;
import com.xxr.lingtuthinktank.model.entity.asset.PictureLike;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceJoinRequest;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceJoinRequestStatusEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceJoinRequestTypeEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceLevelEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceTypeEnum;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceActivityVO;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceVO;
import com.xxr.lingtuthinktank.manager.workspace.SpaceAuthManager;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.service.comment.CommentService;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.asset.PictureEditLogService;
import com.xxr.lingtuthinktank.service.asset.PictureLikeService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.workspace.SpaceJoinRequestService;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 空间接口
 */
@RestController
@RequestMapping("/workspace")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceAuthManager spaceAuthManager;

    @Resource
    private OssManager ossManager;

    @Resource
    private PictureService pictureService;

    @Resource
    private CommentService commentService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private PictureLikeService pictureLikeService;

    @Resource
    private PictureEditLogService pictureEditLogService;

    @Resource
    private SpaceJoinRequestService spaceJoinRequestService;

    @Resource
    private NotificationService notificationService;

    /**
     * 创建空间
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    /**
     * 删除空间
     */
    @PostMapping("/delete")
    @LogSensitive(description = "删除空间")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 删除空间并解绑角色
        boolean result = spaceService.deleteSpaceCascade(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
            HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限校验
        User loginUser = userService.getLoginUser(request);
        spaceService.checkSpaceAuth(loginUser, space);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 获取空间动态流
     */
    @GetMapping("/activity/list")
    public BaseResponse<List<SpaceActivityVO>> listSpaceActivity(@RequestParam long spaceId,
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceId <= 0, ErrorCode.PARAMS_ERROR);
        int safeLimit = limit == null ? 30 : Math.max(1, Math.min(limit, 100));

        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        User loginUser = userService.getLoginUser(request);
        spaceService.checkSpaceAuth(loginUser, space);

        List<Picture> pictureList = pictureService.list(
                new QueryWrapper<Picture>()
                        .eq("spaceId", spaceId)
                        .eq("isDelete", 0)
                        .select("id", "name", "thumbnailUrl", "url", "userId", "spaceId"));

        Map<Long, Picture> pictureMap = (pictureList == null ? new ArrayList<Picture>() : pictureList).stream()
                .filter(item -> item != null && item.getId() != null)
                .collect(Collectors.toMap(Picture::getId, item -> item, (left, right) -> left));
        List<Long> pictureIdList = new ArrayList<>(pictureMap.keySet());

        List<PictureEditLog> editLogList = pictureEditLogService.list(
                new QueryWrapper<PictureEditLog>()
                        .eq("spaceId", spaceId)
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));
        List<Comment> commentList = pictureIdList.isEmpty() ? java.util.Collections.emptyList() : commentService.list(
                new QueryWrapper<Comment>()
                        .in("pictureId", pictureIdList)
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));
        List<PictureLike> pictureLikeList = pictureIdList.isEmpty() ? java.util.Collections.emptyList() : pictureLikeService.list(
                new QueryWrapper<PictureLike>()
                        .in("pictureId", pictureIdList)
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));
        List<Favorite> favoriteList = pictureIdList.isEmpty() ? java.util.Collections.emptyList() : favoriteService.list(
                new QueryWrapper<Favorite>()
                        .in("pictureId", pictureIdList)
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));
        List<Notification> roleNotificationList = notificationService.list(
                new QueryWrapper<Notification>()
                        .eq("type", "space_role")
                        .eq("targetId", spaceId)
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));
        List<SpaceJoinRequest> joinRequestList = spaceJoinRequestService.list(
                new QueryWrapper<SpaceJoinRequest>()
                        .eq("spaceId", spaceId)
                        .orderByDesc("updateTime")
                        .orderByDesc("createTime")
                        .last("limit " + safeLimit));

        Set<Long> actorUserIdSet = new HashSet<>();
        editLogList.forEach(item -> addActorUserId(actorUserIdSet, item == null ? null : item.getOperatorUserId()));
        commentList.forEach(item -> addActorUserId(actorUserIdSet, item == null ? null : item.getUserId()));
        pictureLikeList.forEach(item -> addActorUserId(actorUserIdSet, item == null ? null : item.getUserId()));
        favoriteList.forEach(item -> addActorUserId(actorUserIdSet, item == null ? null : item.getUserId()));
        roleNotificationList.forEach(item -> {
            addActorUserId(actorUserIdSet, item == null ? null : item.getFromUserId());
            addActorUserId(actorUserIdSet, item == null ? null : item.getUserId());
        });
        joinRequestList.forEach(item -> {
            addActorUserId(actorUserIdSet, resolveJoinRequestActorUserId(item));
            addActorUserId(actorUserIdSet, item == null ? null : item.getUserId());
            addActorUserId(actorUserIdSet, item == null ? null : item.getInviterId());
            addActorUserId(actorUserIdSet, item == null ? null : item.getReviewerId());
        });

        Map<Long, User> actorUserMap = actorUserIdSet.isEmpty()
                ? new HashMap<>()
                : userService.listByIds(actorUserIdSet).stream()
                        .filter(item -> item != null && item.getId() != null)
                        .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left));

        List<SpaceActivityVO> activityList = new ArrayList<>();
        editLogList.forEach(item -> {
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "edit",
                    spaceId,
                    item == null ? null : item.getOperatorUserId(),
                    actorUserMap,
                    item == null ? null : item.getPictureId(),
                    pictureMap,
                    item == null ? null : item.getChangeSummary(),
                    item == null ? null : item.getCreateTime());
            if (vo != null) {
                activityList.add(vo);
            }
        });
        commentList.forEach(item -> {
            String detail = item == null ? null : item.getContent();
            if (item != null && item.getParentId() != null && detail != null) {
                detail = "回复：" + detail;
            }
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "comment",
                    spaceId,
                    item == null ? null : item.getUserId(),
                    actorUserMap,
                    item == null ? null : item.getPictureId(),
                    pictureMap,
                    detail,
                    item == null ? null : item.getCreateTime());
            if (vo != null) {
                activityList.add(vo);
            }
        });
        pictureLikeList.forEach(item -> {
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "like",
                    spaceId,
                    item == null ? null : item.getUserId(),
                    actorUserMap,
                    item == null ? null : item.getPictureId(),
                    pictureMap,
                    null,
                    item == null ? null : item.getCreateTime());
            if (vo != null) {
                activityList.add(vo);
            }
        });
        favoriteList.forEach(item -> {
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "favorite",
                    spaceId,
                    item == null ? null : item.getUserId(),
                    actorUserMap,
                    item == null ? null : item.getPictureId(),
                    pictureMap,
                    null,
                    item == null ? null : item.getCreateTime());
            if (vo != null) {
                activityList.add(vo);
            }
        });
        roleNotificationList.forEach(item -> {
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "member_role",
                    spaceId,
                    item == null ? null : item.getFromUserId(),
                    actorUserMap,
                    null,
                    pictureMap,
                    item == null ? null : item.getContent(),
                    item == null ? null : item.getCreateTime());
            if (vo != null) {
                activityList.add(vo);
            }
        });
        joinRequestList.forEach(item -> {
            SpaceActivityVO vo = buildSpaceActivityVO(
                    item == null ? null : item.getId(),
                    "member",
                    spaceId,
                    resolveJoinRequestActorUserId(item),
                    actorUserMap,
                    null,
                    pictureMap,
                    buildJoinRequestActivityDetail(item, actorUserMap),
                    resolveJoinRequestActivityTime(item));
            if (vo != null) {
                activityList.add(vo);
            }
        });

        List<SpaceActivityVO> resultList = activityList.stream()
                .sorted((left, right) -> {
                    Date leftTime = left.getCreateTime();
                    Date rightTime = right.getCreateTime();
                    long leftTimestamp = leftTime == null ? 0L : leftTime.getTime();
                    long rightTimestamp = rightTime == null ? 0L : rightTime.getTime();
                    return Long.compare(rightTimestamp, leftTimestamp);
                })
                .limit(safeLimit)
                .collect(Collectors.toList());
        return ResultUtils.success(resultList);
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
            HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 公共列表默认仅展示团队空间
        if (spaceQueryRequest.getSpaceType() == null && spaceQueryRequest.getUserId() == null) {
            spaceQueryRequest.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        }
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

    /**
     * 管理员分页获取团队空间列表（封装类）
     */
    @PostMapping("/admin/team/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SpaceVO>> listTeamSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 仅团队空间
        spaceQueryRequest.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    @LogSensitive(description = "编辑空间")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevelEnum>> listSpaceLevel() {
        List<SpaceLevelEnum> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

    /**
     * 获取空间/用户存储使用情况
     */
    @GetMapping("/usage")
    public BaseResponse<Long> getSpaceUsage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long usage = spaceService.getSpaceUsage(loginUser.getId());
        return ResultUtils.success(usage);
    }

    /**
     * 空间数据打包下载 (ZIP)
     */
    @GetMapping("/download")
    public void downloadSpace(@RequestParam long spaceId, HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response) {
        if (spaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        spaceAuthManager.checkSpaceAdminAuth(loginUser, spaceId);

        // 调用 Service 进行流式输出
        try {
            spaceService.downloadSpace(spaceId, response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "打包失败");
        }
    }

    private void addActorUserId(Set<Long> userIdSet, Long userId) {
        if (userId != null && userId > 0) {
            userIdSet.add(userId);
        }
    }

    private SpaceActivityVO buildSpaceActivityVO(Long id, String activityType, Long spaceId, Long actorUserId,
            Map<Long, User> actorUserMap, Long pictureId, Map<Long, Picture> pictureMap, String detail,
            Date createTime) {
        Picture picture = pictureId == null ? null : pictureMap.get(pictureId);
        SpaceActivityVO vo = new SpaceActivityVO();
        vo.setId(id);
        vo.setActivityType(activityType);
        vo.setSpaceId(spaceId);
        vo.setActorUserId(actorUserId);
        vo.setActorUser(userService.getUserVO(actorUserMap.get(actorUserId)));
        if (picture != null) {
            vo.setPictureId(picture.getId());
            vo.setPictureName(picture.getName());
            vo.setPictureThumbnailUrl(ossManager.signUrlIfNeeded(picture.getThumbnailUrl()));
            vo.setPictureUrl(ossManager.signUrlIfNeeded(picture.getUrl()));
        }
        vo.setDetail(detail);
        vo.setCreateTime(createTime);
        return vo;
    }

    private Long resolveJoinRequestActorUserId(SpaceJoinRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getStatus() != null && !request.getStatus().equals(SpaceJoinRequestStatusEnum.REVIEWING.getValue())) {
            if (request.getReviewerId() != null) {
                return request.getReviewerId();
            }
            return request.getUserId();
        }
        if (request.getRequestType() != null && request.getRequestType().equals(SpaceJoinRequestTypeEnum.INVITE.getValue())) {
            return request.getInviterId();
        }
        return request.getUserId();
    }

    private Date resolveJoinRequestActivityTime(SpaceJoinRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getReviewTime() != null) {
            return request.getReviewTime();
        }
        if (request.getUpdateTime() != null) {
            return request.getUpdateTime();
        }
        return request.getCreateTime();
    }

    private String buildJoinRequestActivityDetail(SpaceJoinRequest request, Map<Long, User> actorUserMap) {
        if (request == null) {
            return null;
        }
        User targetUser = actorUserMap.get(request.getUserId());
        User inviterUser = actorUserMap.get(request.getInviterId());
        String targetName = targetUser == null ? "成员" : targetUser.getUserName();
        String inviterName = inviterUser == null ? "成员" : inviterUser.getUserName();
        boolean isInvite = request.getRequestType() != null
                && request.getRequestType().equals(SpaceJoinRequestTypeEnum.INVITE.getValue());
        boolean isReviewing = request.getStatus() != null
                && request.getStatus().equals(SpaceJoinRequestStatusEnum.REVIEWING.getValue());
        if (isInvite) {
            if (isReviewing) {
                return inviterName + " 发起了团队邀请，邀请 " + targetName + " 加入空间";
            }
            String reviewMessage = request.getReviewMessage() == null ? "" : request.getReviewMessage();
            if (reviewMessage.contains("撤回")) {
                return "团队邀请已被撤回，目标成员：" + targetName;
            }
            if (request.getStatus().equals(SpaceJoinRequestStatusEnum.PASS.getValue())) {
                return targetName + " 接受了团队邀请";
            }
            return targetName + " 拒绝了团队邀请";
        }
        if (isReviewing) {
            return targetName + " 提交了加入申请";
        }
        if (request.getStatus().equals(SpaceJoinRequestStatusEnum.PASS.getValue())) {
            return targetName + " 的加入申请已通过";
        }
        return targetName + " 的加入申请已被拒绝";
    }
}
