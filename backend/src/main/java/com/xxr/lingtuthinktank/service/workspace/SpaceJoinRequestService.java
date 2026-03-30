package com.xxr.lingtuthinktank.service.workspace;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.workspace.join.SpaceJoinRequestQueryRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceJoinRequest;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceJoinRequestVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间加入申请服务
 */
public interface SpaceJoinRequestService extends IService<SpaceJoinRequest> {

    /**
     * 获取查询条件
     */
    QueryWrapper<SpaceJoinRequest> getQueryWrapper(SpaceJoinRequestQueryRequest queryRequest);

    /**
     * 获取视图对象
     */
    SpaceJoinRequestVO getJoinRequestVO(SpaceJoinRequest joinRequest, HttpServletRequest request);

    /**
     * 获取视图对象列表
     */
    List<SpaceJoinRequestVO> getJoinRequestVOList(List<SpaceJoinRequest> joinRequestList);
}
