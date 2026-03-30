package com.xxr.lingtuthinktank.service.workspace;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.workspace.command.SpaceAddRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.query.SpaceQueryRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 空间服务
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest 空间创建请求
     * @param loginUser       登录用户
     * @return 空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     *
     * @param space 空间
     * @param add   是否为创建校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间查询条件
     *
     * @param spaceQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间视图对象
     *
     * @param space   空间
     * @param request 请求
     * @return 空间视图
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间视图对象
     *
     * @param spacePage 空间分页
     * @param request   请求
     * @return 空间视图分页
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据空间级别填充空间信息
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    void checkSpaceAuth(User loginUser, Space space);

    /**
     * 获取空间/用户存储使用情况
     * 
     * @param userId
     * @return usage in bytes
     */
    long getSpaceUsage(long userId);

    /**
     * 空间打包下载
     */
    void downloadSpace(long spaceId, javax.servlet.http.HttpServletResponse response);

    /**
     * 删除空间
     *
     * @param spaceId 空间id
     * @return 是否删除成功
     */
    boolean deleteSpaceCascade(long spaceId);
}
