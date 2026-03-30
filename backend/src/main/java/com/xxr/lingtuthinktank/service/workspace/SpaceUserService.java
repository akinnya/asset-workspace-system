package com.xxr.lingtuthinktank.service.workspace;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserAddRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.member.SpaceUserQueryRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceUser;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间用户服务
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     *
     * @param spaceUserAddRequest 添加请求
     * @return 成员id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间用户
     *
     * @param spaceUser 空间用户
     * @param add       是否为创建校验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间用户查询条件
     *
     * @param spaceUserQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户视图对象
     *
     * @param spaceUser 空间用户
     * @param request   请求
     * @return 空间用户视图
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间用户视图对象列表
     *
     * @param spaceUserList 空间用户列表
     * @return 空间用户视图列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 检查用户是否有空间权限
     *
     * @param spaceId   空间id
     * @param userId    用户id
     * @return 是否有权限
     */
    boolean hasSpaceAuth(Long spaceId, Long userId);

    /**
     * 获取用户在空间中的角色
     *
     * @param spaceId 空间id
     * @param userId  用户id
     * @return 空间角色
     */
    String getSpaceUserRole(Long spaceId, Long userId);
}
