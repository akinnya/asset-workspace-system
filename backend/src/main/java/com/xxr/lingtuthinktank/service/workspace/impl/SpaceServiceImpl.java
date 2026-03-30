package com.xxr.lingtuthinktank.service.workspace.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.mapper.workspace.SpaceMapper;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.model.dto.workspace.command.SpaceAddRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.query.SpaceQueryRequest;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.workspace.SpaceUser;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceLevelEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceTypeEnum;
import com.xxr.lingtuthinktank.model.enums.workspace.SpaceUserRoleEnum;
import com.xxr.lingtuthinktank.model.vo.workspace.SpaceVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.workspace.SpaceUserService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间服务实现
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceUserService spaceUserService;

    @Resource
    private OssManager ossManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 填充默认值
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);

        // 默认值
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        }

        // 填充空间级别信息
        fillSpaceBySpaceLevel(space);
        // 数据校验
        validSpace(space, true);

        // 设置用户id
        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 保存空间
        boolean result = this.save(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");

        // 创建者加入空间成员（管理员角色）
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(space.getId());
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(SpaceUserRoleEnum.ADMIN.getValue());
        boolean addResult = spaceUserService.save(spaceUser);
        ThrowUtils.throwIf(!addResult, ErrorCode.OPERATION_ERROR, "创建空间失败");

        return space.getId();
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);

        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();

        // 创建时必须校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        }

        // 修改时，有参数则校验
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtils.throwIf(spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceLevel != null) {
            SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
            ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (spaceType != null) {
            SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
            ThrowUtils.throwIf(spaceTypeEnum == null, ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }

        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        Long userId = spaceQueryRequest.getUserId();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                "ascend".equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        if (spaceVO.getCoverUrl() != null) {
            spaceVO.setCoverUrl(ossManager.signUrlIfNeeded(spaceVO.getCoverUrl()));
        }
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                spaceVO.setUser(userVO);
            }
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        spaceVOList.forEach(spaceVO -> {
            if (spaceVO.getCoverUrl() != null) {
                spaceVO.setCoverUrl(ossManager.signUrlIfNeeded(spaceVO.getCoverUrl()));
            }
        });
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
                spaceVO.setUser(userService.getUserVO(user));
            }
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            // 如果未设置则填充默认值
            if (space.getMaxSize() == null) {
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }
            if (space.getMaxCount() == null) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
        }
        // 初始化统计字段
        if (space.getTotalSize() == null) {
            space.setTotalSize(0L);
        }
        if (space.getTotalCount() == null) {
            space.setTotalCount(0L);
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 空间不存在
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        // 邀请制：创建者或成员可访问
        boolean isOwner = space.getUserId().equals(loginUser.getId());
        boolean hasAuth = spaceUserService.hasSpaceAuth(space.getId(), loginUser.getId());
        if (!isOwner && !hasAuth) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该空间");
        }
    }

    @Resource
    @Lazy
    private com.xxr.lingtuthinktank.service.asset.PictureService pictureService;

    @Override
    public long getSpaceUsage(long userId) {
        // Calculate total size of pictures uploaded by user (including all spaces)
        // Or simply sum "picSize" from Picture table where userId = userId and isDelete
        // = 0
        QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.select("IFNULL(SUM(picSize), 0) as totalSize");
        Map<String, Object> map = pictureService.getMap(queryWrapper);
        if (map != null && map.get("totalSize") != null) {
            return Long.parseLong(map.get("totalSize").toString());
        }
        return 0L;
    }

    @Override
    public void downloadSpace(long spaceId, javax.servlet.http.HttpServletResponse response) {
        // 1. 获取空间下所有图片
        QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spaceId", spaceId);
        List<com.xxr.lingtuthinktank.model.entity.asset.Picture> pictureList = pictureService.list(queryWrapper);
        if (CollUtil.isEmpty(pictureList)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间无图片");
        }

        // 2. 设置响应头
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"space_" + spaceId + ".zip\"");

        // 3. 压缩流
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
            for (com.xxr.lingtuthinktank.model.entity.asset.Picture picture : pictureList) {
                String picName = picture.getName();
                String originUrl = picture.getUrl();
                String url = ossManager.signUrlIfNeeded(originUrl);
                if (StrUtil.isBlank(url))
                    continue;

                // 处理文件名后缀
                String extension = "";
                int dotIndex = StrUtil.blankToDefault(originUrl, "").lastIndexOf('.');
                if (dotIndex > -1) {
                    extension = url.substring(dotIndex);
                }
                // 确保文件名合法且唯一 (简化处理，若重名可能会覆盖，实际需 map 计数)
                String zipEntryName = (StrUtil.isBlank(picName) ? "pic_" + picture.getId() : picName) + extension;

                try {
                    zos.putNextEntry(new java.util.zip.ZipEntry(zipEntryName));
                    // 读取网络流
                    // 注意：实际生产环境需考虑大文件流式转发，避免 OOM.
                    // 这里简化使用 Hutool 或者 URL.openStream
                    java.io.InputStream inputStream = new java.net.URL(url).openStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    inputStream.close();
                    zos.closeEntry();
                } catch (Exception e) {
                    // 单个文件失败不影响整体
                    log.error("Failed to download file to zip: " + url, e);
                }
            }
            zos.flush();
        } catch (Exception e) {
            log.error("Zip export failed", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "导出失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpaceCascade(long spaceId) {
        return this.removeById(spaceId);
    }
}
