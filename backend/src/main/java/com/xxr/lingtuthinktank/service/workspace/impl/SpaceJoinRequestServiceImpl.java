package com.xxr.lingtuthinktank.service.workspace.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.mapper.workspace.SpaceJoinRequestMapper;
import com.xxr.lingtuthinktank.model.dto.workspace.join.SpaceJoinRequestQueryRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceJoinRequest;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceJoinRequestVO;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceVO;
import com.xxr.lingtuthinktank.service.workspace.SpaceJoinRequestService;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间协作请求服务实现
 */
@Service
public class SpaceJoinRequestServiceImpl extends ServiceImpl<SpaceJoinRequestMapper, SpaceJoinRequest>
        implements SpaceJoinRequestService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Override
    public QueryWrapper<SpaceJoinRequest> getQueryWrapper(SpaceJoinRequestQueryRequest queryRequest) {
        QueryWrapper<SpaceJoinRequest> queryWrapper = new QueryWrapper<>();
        if (queryRequest == null) {
            return queryWrapper.orderByDesc("createTime");
        }
        Long spaceId = queryRequest.getSpaceId();
        Long userId = queryRequest.getUserId();
        Integer requestType = queryRequest.getRequestType();
        Integer status = queryRequest.getStatus();

        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(requestType), "requestType", requestType);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), "status", status);
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }

    @Override
    public SpaceJoinRequestVO getJoinRequestVO(SpaceJoinRequest joinRequest, HttpServletRequest request) {
        SpaceJoinRequestVO joinRequestVO = SpaceJoinRequestVO.objToVo(joinRequest);
        if (joinRequestVO == null) {
            return null;
        }

        Long userId = joinRequest.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            joinRequestVO.setUser(userService.getUserVO(user));
        }

        Long inviterId = joinRequest.getInviterId();
        if (inviterId != null && inviterId > 0) {
            User inviter = userService.getById(inviterId);
            joinRequestVO.setInviter(userService.getUserVO(inviter));
        }

        Long spaceId = joinRequest.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            joinRequestVO.setSpace(SpaceVO.objToVo(space));
        }

        return joinRequestVO;
    }

    @Override
    public List<SpaceJoinRequestVO> getJoinRequestVOList(List<SpaceJoinRequest> joinRequestList) {
        if (CollUtil.isEmpty(joinRequestList)) {
            return new ArrayList<>();
        }

        List<SpaceJoinRequestVO> joinRequestVOList = joinRequestList.stream()
                .map(SpaceJoinRequestVO::objToVo)
                .collect(Collectors.toList());

        Set<Long> relatedUserIdSet = new HashSet<>();
        for (SpaceJoinRequest joinRequest : joinRequestList) {
            if (joinRequest.getUserId() != null) {
                relatedUserIdSet.add(joinRequest.getUserId());
            }
            if (joinRequest.getInviterId() != null) {
                relatedUserIdSet.add(joinRequest.getInviterId());
            }
        }

        Map<Long, List<User>> userIdUserListMap = userService.listByIds(relatedUserIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        Set<Long> spaceIdSet = joinRequestList.stream()
                .map(SpaceJoinRequest::getSpaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));

        joinRequestVOList.forEach(joinRequestVO -> {
            Long userId = joinRequestVO.getUserId();
            User user = null;
            if (userId != null && userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            joinRequestVO.setUser(userService.getUserVO(user));

            Long inviterId = joinRequestVO.getInviterId();
            User inviter = null;
            if (inviterId != null && userIdUserListMap.containsKey(inviterId)) {
                inviter = userIdUserListMap.get(inviterId).get(0);
            }
            joinRequestVO.setInviter(userService.getUserVO(inviter));

            Long spaceId = joinRequestVO.getSpaceId();
            Space space = null;
            if (spaceId != null && spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            joinRequestVO.setSpace(SpaceVO.objToVo(space));
        });

        return joinRequestVOList;
    }
}
