package com.xxr.lingtuthinktank.controller.workspace;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xxr.lingtuthinktank.annotation.LogSensitive;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.DeleteRequest;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.dto.workspace.join.SpaceInviteRespondRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.join.SpaceJoinRequestQueryRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.join.SpaceJoinRequestReviewRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserAddRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserEditRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserJoinRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserQueryRequest;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceJoinRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceUser;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceJoinRequestStatusEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceJoinRequestTypeEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceUserRoleEnum;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceJoinRequestVO;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceUserVO;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.workspace.SpaceJoinRequestService;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.workspace.SpaceUserService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 空间成员接口
 */
@RestController
@RequestMapping("/workspace-user")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private SpaceJoinRequestService spaceJoinRequestService;

    /**
     * 发起团队邀请
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);

        Long spaceId = spaceUserAddRequest.getSpaceId();
        Long targetUserId = spaceUserAddRequest.getUserId();
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间 id 不合法");
        ThrowUtils.throwIf(targetUserId == null || targetUserId <= 0, ErrorCode.PARAMS_ERROR, "用户 id 不合法");
        ThrowUtils.throwIf(Objects.equals(targetUserId, loginUser.getId()), ErrorCode.OPERATION_ERROR, "不能邀请自己");

        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        checkSpaceManageAuth(loginUser, space);

        User targetUser = userService.getById(targetUserId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(Objects.equals(space.getUserId(), targetUserId), ErrorCode.OPERATION_ERROR, "空间创建者已在团队中");
        ThrowUtils.throwIf(spaceUserService.hasSpaceAuth(spaceId, targetUserId), ErrorCode.OPERATION_ERROR, "该用户已是空间成员");

        SpaceJoinRequest reviewingJoinRequest = getPendingRequest(spaceId, targetUserId, SpaceJoinRequestTypeEnum.JOIN);
        ThrowUtils.throwIf(reviewingJoinRequest != null, ErrorCode.OPERATION_ERROR, "该用户已提交加入申请，请在申请列表中审核");

        SpaceJoinRequest reviewingInvite = getPendingRequest(spaceId, targetUserId, SpaceJoinRequestTypeEnum.INVITE);
        if (reviewingInvite != null) {
            return ResultUtils.success(reviewingInvite.getId());
        }

        SpaceJoinRequest inviteRequest = new SpaceJoinRequest();
        inviteRequest.setSpaceId(spaceId);
        inviteRequest.setUserId(targetUserId);
        inviteRequest.setRequestType(SpaceJoinRequestTypeEnum.INVITE.getValue());
        inviteRequest.setInviterId(loginUser.getId());
        inviteRequest.setStatus(SpaceJoinRequestStatusEnum.REVIEWING.getValue());
        boolean saved = spaceJoinRequestService.save(inviteRequest);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "邀请发送失败");

        String spaceName = StrUtil.isNotBlank(space.getSpaceName()) ? space.getSpaceName() : "团队空间";
        String operatorName = StrUtil.isNotBlank(loginUser.getUserName()) ? loginUser.getUserName() : "管理员";
        Map<String, Object> params = new HashMap<>();
        params.put("spaceName", spaceName);
        params.put("operatorName", operatorName);
        saveI18nNotification(targetUserId, loginUser.getId(), "space_invite", space.getId(),
                "notification.spaceInvite.receive", params);
        return ResultUtils.success(inviteRequest.getId());
    }

    /**
     * 删除空间成员
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);

        Long spaceId = oldSpaceUser.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        boolean isSpaceOwner = space.getUserId().equals(loginUser.getId());
        String userRole = spaceUserService.getSpaceUserRole(spaceId, loginUser.getId());
        boolean isSpaceAdmin = SpaceUserRoleEnum.ADMIN.getValue().equals(userRole);
        boolean isSelf = oldSpaceUser.getUserId().equals(loginUser.getId());

        if (!isSpaceOwner && !isSpaceAdmin && !userService.isAdmin(loginUser) && !isSelf) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除空间成员");
        }

        if (oldSpaceUser.getUserId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能移除空间创建者");
        }

        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 主动加入团队空间
     */
    @PostMapping("/join")
    public BaseResponse<Long> joinSpace(@RequestBody SpaceUserJoinRequest spaceUserJoinRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserJoinRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceUserJoinRequest.getSpaceId();

        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(space.getUserId().equals(loginUser.getId()), ErrorCode.OPERATION_ERROR, "空间创建者无需申请");

        if (spaceUserService.hasSpaceAuth(spaceId, loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "你已加入该空间");
        }

        SpaceJoinRequest inviteRequest = getPendingRequest(spaceId, loginUser.getId(), SpaceJoinRequestTypeEnum.INVITE);
        ThrowUtils.throwIf(inviteRequest != null, ErrorCode.OPERATION_ERROR, "你已收到团队邀请，请在邀请页中确认");

        SpaceJoinRequest joinRequest = getPendingRequest(spaceId, loginUser.getId(), SpaceJoinRequestTypeEnum.JOIN);
        if (joinRequest != null) {
            return ResultUtils.success(joinRequest.getId());
        }

        joinRequest = new SpaceJoinRequest();
        joinRequest.setSpaceId(spaceId);
        joinRequest.setUserId(loginUser.getId());
        joinRequest.setRequestType(SpaceJoinRequestTypeEnum.JOIN.getValue());
        joinRequest.setStatus(SpaceJoinRequestStatusEnum.REVIEWING.getValue());
        boolean saved = spaceJoinRequestService.save(joinRequest);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "申请提交失败");
        return ResultUtils.success(joinRequest.getId());
    }

    /**
     * 获取当前用户在该空间的待处理邀请
     */
    @GetMapping("/invite/get")
    public BaseResponse<SpaceJoinRequestVO> getPendingInvite(@RequestParam Long spaceId, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间 id 不合法");
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        if (spaceUserService.hasSpaceAuth(spaceId, loginUser.getId())
                || Objects.equals(space.getUserId(), loginUser.getId())) {
            return ResultUtils.success(null);
        }

        SpaceJoinRequest inviteRequest = getPendingRequest(spaceId, loginUser.getId(), SpaceJoinRequestTypeEnum.INVITE);
        return ResultUtils.success(spaceJoinRequestService.getJoinRequestVO(inviteRequest, request));
    }

    /**
     * 接受 / 拒绝团队邀请
     */
    @PostMapping("/invite/respond")
    @LogSensitive(description = "响应团队邀请")
    public BaseResponse<Boolean> respondInvite(@RequestBody SpaceInviteRespondRequest respondRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(respondRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(respondRequest.getSpaceId() == null || respondRequest.getSpaceId() <= 0,
                ErrorCode.PARAMS_ERROR, "空间 id 不合法");
        ThrowUtils.throwIf(respondRequest.getAccept() == null, ErrorCode.PARAMS_ERROR, "请选择操作类型");

        User loginUser = userService.getLoginUser(request);
        Long spaceId = respondRequest.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        SpaceJoinRequest inviteRequest = getPendingRequest(spaceId, loginUser.getId(), SpaceJoinRequestTypeEnum.INVITE);
        ThrowUtils.throwIf(inviteRequest == null, ErrorCode.NOT_FOUND_ERROR, "邀请不存在或已处理");

        boolean accept = Boolean.TRUE.equals(respondRequest.getAccept());
        if (accept
                && !spaceUserService.hasSpaceAuth(spaceId, loginUser.getId())
                && !Objects.equals(space.getUserId(), loginUser.getId())) {
            SpaceUserAddRequest addRequest = new SpaceUserAddRequest();
            addRequest.setSpaceId(spaceId);
            addRequest.setUserId(loginUser.getId());
            addRequest.setSpaceRole(SpaceUserRoleEnum.VIEWER.getValue());
            spaceUserService.addSpaceUser(addRequest);
        }

        Date now = new Date();
        inviteRequest.setStatus(accept ? SpaceJoinRequestStatusEnum.PASS.getValue() : SpaceJoinRequestStatusEnum.REJECT.getValue());
        inviteRequest.setReviewerId(loginUser.getId());
        inviteRequest.setReviewTime(now);
        inviteRequest.setReviewMessage(accept ? "已接受邀请" : "已拒绝邀请");
        boolean updated = spaceJoinRequestService.updateById(inviteRequest);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "处理邀请失败");

        if (accept) {
            closeOtherReviewingRequests(spaceId, loginUser.getId(), loginUser.getId(), inviteRequest.getId(),
                    "用户已通过邀请加入团队", now);
        }

        if (inviteRequest.getInviterId() != null && !Objects.equals(inviteRequest.getInviterId(), loginUser.getId())) {
            String spaceName = StrUtil.isNotBlank(space.getSpaceName()) ? space.getSpaceName() : "团队空间";
            String userName = StrUtil.isNotBlank(loginUser.getUserName()) ? loginUser.getUserName() : "成员";
            Map<String, Object> params = new HashMap<>();
            params.put("spaceName", spaceName);
            params.put("userName", userName);
            saveI18nNotification(inviteRequest.getInviterId(), loginUser.getId(), "space_invite", space.getId(),
                    accept ? "notification.spaceInvite.accept" : "notification.spaceInvite.reject", params);
        }

        return ResultUtils.success(true);
    }

    /**
     * 撤回待处理团队邀请
     */
    @PostMapping("/invite/cancel")
    @LogSensitive(description = "撤回团队邀请")
    public BaseResponse<Boolean> cancelInvite(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "邀请 id 不能为空");
        User loginUser = userService.getLoginUser(request);

        SpaceJoinRequest inviteRequest = spaceJoinRequestService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(inviteRequest == null, ErrorCode.NOT_FOUND_ERROR, "邀请不存在");
        ThrowUtils.throwIf(!Objects.equals(inviteRequest.getRequestType(), SpaceJoinRequestTypeEnum.INVITE.getValue()),
                ErrorCode.OPERATION_ERROR, "该请求不是团队邀请");
        ThrowUtils.throwIf(!Objects.equals(inviteRequest.getStatus(), SpaceJoinRequestStatusEnum.REVIEWING.getValue()),
                ErrorCode.OPERATION_ERROR, "邀请已处理，无法撤回");

        Space space = spaceService.getById(inviteRequest.getSpaceId());
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        checkSpaceManageAuth(loginUser, space);

        inviteRequest.setStatus(SpaceJoinRequestStatusEnum.REJECT.getValue());
        inviteRequest.setReviewerId(loginUser.getId());
        inviteRequest.setReviewTime(new Date());
        inviteRequest.setReviewMessage("管理员已撤回邀请");
        boolean updated = spaceJoinRequestService.updateById(inviteRequest);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "撤回邀请失败");

        if (inviteRequest.getUserId() != null && !Objects.equals(inviteRequest.getUserId(), loginUser.getId())) {
            String spaceName = StrUtil.isNotBlank(space.getSpaceName()) ? space.getSpaceName() : "团队空间";
            String operatorName = StrUtil.isNotBlank(loginUser.getUserName()) ? loginUser.getUserName() : "管理员";
            Map<String, Object> params = new HashMap<>();
            params.put("spaceName", spaceName);
            params.put("operatorName", operatorName);
            saveI18nNotification(inviteRequest.getUserId(), loginUser.getId(), "space_invite", space.getId(),
                    "notification.spaceInvite.cancel", params);
        }

        return ResultUtils.success(true);
    }

    /**
     * 审核加入申请
     */
    @PostMapping("/join/review")
    @LogSensitive(description = "审核空间加入申请")
    public BaseResponse<Boolean> reviewJoinRequest(@RequestBody SpaceJoinRequestReviewRequest reviewRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(reviewRequest == null || reviewRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "申请 id 不能为空");
        ThrowUtils.throwIf(reviewRequest.getStatus() == null, ErrorCode.PARAMS_ERROR, "审核状态不能为空");
        User loginUser = userService.getLoginUser(request);

        SpaceJoinRequest joinRequest = spaceJoinRequestService.getById(reviewRequest.getId());
        ThrowUtils.throwIf(joinRequest == null, ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        ThrowUtils.throwIf(!Objects.equals(joinRequest.getRequestType(), SpaceJoinRequestTypeEnum.JOIN.getValue()),
                ErrorCode.OPERATION_ERROR, "该请求不是加入申请");
        ThrowUtils.throwIf(!Objects.equals(joinRequest.getStatus(), SpaceJoinRequestStatusEnum.REVIEWING.getValue()),
                ErrorCode.OPERATION_ERROR, "申请已处理");

        Space space = spaceService.getById(joinRequest.getSpaceId());
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        checkSpaceManageAuth(loginUser, space);

        Integer status = reviewRequest.getStatus();
        SpaceJoinRequestStatusEnum statusEnum = SpaceJoinRequestStatusEnum.getEnumByValue(status);
        ThrowUtils.throwIf(statusEnum == null || statusEnum == SpaceJoinRequestStatusEnum.REVIEWING,
                ErrorCode.PARAMS_ERROR, "审核状态非法");

        Date now = new Date();
        if (statusEnum == SpaceJoinRequestStatusEnum.PASS) {
            if (!spaceUserService.hasSpaceAuth(space.getId(), joinRequest.getUserId())
                    && !Objects.equals(space.getUserId(), joinRequest.getUserId())) {
                SpaceUserAddRequest addRequest = new SpaceUserAddRequest();
                addRequest.setSpaceId(space.getId());
                addRequest.setUserId(joinRequest.getUserId());
                addRequest.setSpaceRole(SpaceUserRoleEnum.VIEWER.getValue());
                spaceUserService.addSpaceUser(addRequest);
            }
            closeOtherReviewingRequests(space.getId(), joinRequest.getUserId(), loginUser.getId(), joinRequest.getId(),
                    "已通过其他协作流程加入团队", now);
        }

        joinRequest.setStatus(statusEnum.getValue());
        joinRequest.setReviewMessage(reviewRequest.getReviewMessage());
        joinRequest.setReviewerId(loginUser.getId());
        joinRequest.setReviewTime(now);
        boolean updated = spaceJoinRequestService.updateById(joinRequest);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "审核失败");

        String spaceName = StrUtil.isNotBlank(space.getSpaceName()) ? space.getSpaceName() : "团队空间";
        Map<String, Object> params = new HashMap<>();
        params.put("spaceName", spaceName);
        saveI18nNotification(joinRequest.getUserId(), loginUser.getId(), "space_join", space.getId(),
                statusEnum == SpaceJoinRequestStatusEnum.PASS
                        ? "notification.spaceJoin.pass"
                        : "notification.spaceJoin.reject",
                params);

        return ResultUtils.success(true);
    }

    /**
     * 获取加入申请列表（空间管理员）
     */
    @PostMapping("/join/list")
    public BaseResponse<List<SpaceJoinRequestVO>> listJoinRequests(@RequestBody SpaceJoinRequestQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);

        Long spaceId = queryRequest.getSpaceId();
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间 id 不能为空");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        checkSpaceManageAuth(loginUser, space);

        if (queryRequest.getRequestType() == null) {
            queryRequest.setRequestType(SpaceJoinRequestTypeEnum.JOIN.getValue());
        }

        List<SpaceJoinRequest> list = spaceJoinRequestService.list(
                spaceJoinRequestService.getQueryWrapper(queryRequest));
        return ResultUtils.success(spaceJoinRequestService.getJoinRequestVOList(list));
    }

    /**
     * 编辑空间成员（修改角色）
     */
    @PostMapping("/edit")
    @LogSensitive(description = "修改空间成员角色")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() == null
                || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "成员 id 不能为空");
        User loginUser = userService.getLoginUser(request);

        SpaceUser spaceUser = spaceUserService.getById(spaceUserEditRequest.getId());
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "成员不存在");

        Space space = spaceService.getById(spaceUser.getSpaceId());
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        boolean isOwner = space.getUserId().equals(loginUser.getId());
        String currentUserRole = spaceUserService.getSpaceUserRole(space.getId(), loginUser.getId());
        boolean isSpaceAdmin = SpaceUserRoleEnum.ADMIN.getValue().equals(currentUserRole);
        boolean isSystemAdmin = userService.isAdmin(loginUser);
        if (!isOwner && !isSpaceAdmin && !isSystemAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改成员角色");
        }

        if (space.getUserId().equals(spaceUser.getUserId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能修改空间创建者角色");
        }

        SpaceUserRoleEnum targetRole = SpaceUserRoleEnum.getEnumByValue(spaceUserEditRequest.getSpaceRole());
        ThrowUtils.throwIf(targetRole == null, ErrorCode.PARAMS_ERROR, "空间角色不存在");

        if (!isOwner && !isSystemAdmin && SpaceUserRoleEnum.ADMIN == targetRole) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅空间创建者可授予管理员角色");
        }

        if (Objects.equals(spaceUser.getSpaceRole(), targetRole.getValue())) {
            return ResultUtils.success(true);
        }

        spaceUser.setSpaceRole(targetRole.getValue());
        boolean updated = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "成员角色修改失败");

        Notification notification = new Notification();
        notification.setUserId(spaceUser.getUserId());
        notification.setFromUserId(loginUser.getId());
        notification.setType("space_role");
        notification.setTargetId(space.getId());
        String operatorName = loginUser.getUserName() != null ? loginUser.getUserName() : "管理员";
        String spaceName = space.getSpaceName() != null ? space.getSpaceName() : "团队空间";
        notification.setContent(operatorName + " 将你在“" + spaceName + "”中的角色调整为" + targetRole.getText());
        notification.setIsRead(0);
        notificationService.save(notification);

        return ResultUtils.success(true);
    }

    /**
     * 获取空间成员列表
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);

        Long spaceId = spaceUserQueryRequest.getSpaceId();
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间 id 不能为空");

        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        spaceService.checkSpaceAuth(loginUser, space);

        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));

        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 获取我加入的团队空间列表
     */
    @GetMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMySpaceUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());

        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));

        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    private void checkSpaceManageAuth(User loginUser, Space space) {
        boolean isOwner = Objects.equals(space.getUserId(), loginUser.getId());
        String userRole = spaceUserService.getSpaceUserRole(space.getId(), loginUser.getId());
        boolean isSpaceAdmin = SpaceUserRoleEnum.ADMIN.getValue().equals(userRole);
        if (!isOwner && !isSpaceAdmin && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权管理空间成员");
        }
    }

    private SpaceJoinRequest getPendingRequest(Long spaceId, Long userId, SpaceJoinRequestTypeEnum requestTypeEnum) {
        if (spaceId == null || userId == null || requestTypeEnum == null) {
            return null;
        }
        SpaceJoinRequestQueryRequest queryRequest = new SpaceJoinRequestQueryRequest();
        queryRequest.setSpaceId(spaceId);
        queryRequest.setUserId(userId);
        queryRequest.setRequestType(requestTypeEnum.getValue());
        queryRequest.setStatus(SpaceJoinRequestStatusEnum.REVIEWING.getValue());
        return spaceJoinRequestService.getOne(
                spaceJoinRequestService.getQueryWrapper(queryRequest).last("limit 1"),
                false);
    }

    private void closeOtherReviewingRequests(Long spaceId, Long userId, Long reviewerId, Long excludeId,
            String reviewMessage, Date reviewTime) {
        SpaceJoinRequestQueryRequest queryRequest = new SpaceJoinRequestQueryRequest();
        queryRequest.setSpaceId(spaceId);
        queryRequest.setUserId(userId);
        queryRequest.setStatus(SpaceJoinRequestStatusEnum.REVIEWING.getValue());
        List<SpaceJoinRequest> reviewingList = spaceJoinRequestService.list(
                spaceJoinRequestService.getQueryWrapper(queryRequest));
        if (reviewingList.isEmpty()) {
            return;
        }
        List<SpaceJoinRequest> updateList = new ArrayList<>();
        for (SpaceJoinRequest item : reviewingList) {
            if (excludeId != null && Objects.equals(excludeId, item.getId())) {
                continue;
            }
            item.setStatus(SpaceJoinRequestStatusEnum.PASS.getValue());
            item.setReviewerId(reviewerId);
            item.setReviewTime(reviewTime);
            if (StrUtil.isBlank(item.getReviewMessage())) {
                item.setReviewMessage(reviewMessage);
            }
            updateList.add(item);
        }
        if (!updateList.isEmpty()) {
            spaceJoinRequestService.updateBatchById(updateList);
        }
    }

    private void saveI18nNotification(Long userId, Long fromUserId, String type, Long targetId,
            String key, Map<String, Object> params) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTargetId(targetId);
        Map<String, Object> content = new HashMap<>();
        content.put("key", key);
        content.put("params", params == null ? new HashMap<>() : params);
        notification.setContent(JSONUtil.toJsonStr(content));
        notification.setIsRead(0);
        notificationService.save(notification);
    }
}
