package com.xxr.lingtuthinktank.service.workspace.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.mapper.workspace.SpaceMapper;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeResponse;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.workspace.SpaceAnalyzeService;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空间分析服务实现
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private com.xxr.lingtuthinktank.mapper.asset.PictureMapper pictureMapper;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            User loginUser) {
        SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
        if (spaceUsageAnalyzeRequest.isQueryAll()) {
            // 全系统分析 (仅限管理员)
            checkAdminAuth(loginUser);
            Picture sumPicture = pictureMapper.selectOne(new QueryWrapper<Picture>()
                    .select("ifnull(sum(picSize), 0) as picSize"));
            Long totalUsedSize = sumPicture != null && sumPicture.getPicSize() != null ? sumPicture.getPicSize() : 0L;
            Long totalCount = pictureMapper.selectCount(null);

            response.setUsedSize(totalUsedSize);
            response.setUsedCount(totalCount);
            Map<String, Object> sumMap = baseMapper.selectMaps(new QueryWrapper<Space>()
                    .select("ifnull(sum(maxSize), 0) as maxSize", "ifnull(sum(maxCount), 0) as maxCount"))
                    .stream()
                    .findFirst()
                    .orElse(null);
            long totalMaxSize = 0L;
            long totalMaxCount = 0L;
            if (sumMap != null) {
                Object maxSizeObj = sumMap.get("maxSize");
                Object maxCountObj = sumMap.get("maxCount");
                if (maxSizeObj instanceof Number) {
                    totalMaxSize = ((Number) maxSizeObj).longValue();
                }
                if (maxCountObj instanceof Number) {
                    totalMaxCount = ((Number) maxCountObj).longValue();
                }
            }
            response.setMaxSize(totalMaxSize);
            response.setMaxCount(totalMaxCount);
            if (totalMaxSize > 0) {
                response.setSizeUsageRatio(totalUsedSize * 100.0 / totalMaxSize);
            } else {
                response.setSizeUsageRatio(0.0);
            }
            if (totalMaxCount > 0) {
                response.setCountUsageRatio(totalCount * 100.0 / totalMaxCount);
            } else {
                response.setCountUsageRatio(0.0);
            }
        } else {
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(loginUser, space);

            response.setUsedSize(space.getTotalSize());
            response.setMaxSize(space.getMaxSize());
            response.setUsedCount(space.getTotalCount());
            response.setMaxCount(space.getMaxCount());
            if (space.getMaxSize() != null && space.getMaxSize() > 0) {
                response.setSizeUsageRatio(space.getTotalSize() * 100.0 / space.getMaxSize());
            }
            if (space.getMaxCount() != null && space.getMaxCount() > 0) {
                response.setCountUsageRatio(space.getTotalCount() * 100.0 / space.getMaxCount());
            }
        }
        return response;
    }

    @Override
    public List<SpaceRankAnalyzeResponse> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            User loginUser) {
        checkAdminAuth(loginUser);
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());
        List<Space> spaceList = baseMapper.selectList(queryWrapper);
        return spaceList.stream().map(space -> {
            SpaceRankAnalyzeResponse response = new SpaceRankAnalyzeResponse();
            response.setId(space.getId());
            response.setSpaceName(space.getSpaceName());
            response.setUserId(space.getUserId());
            response.setTotalSize(space.getTotalSize());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
            SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            User loginUser) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (!spaceCategoryAnalyzeRequest.isQueryAll()) {
            Long spaceId = spaceCategoryAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(loginUser, space);
            queryWrapper.eq("spaceId", spaceId);
        } else {
            checkAdminAuth(loginUser);
        }
        queryWrapper.select("category", "count(*) as cnt", "sum(picSize) as totalSize")
                .groupBy("category");
        List<Map<String, Object>> result = pictureMapper.selectMaps(queryWrapper);
        List<SpaceCategoryAnalyzeResponse> responseList = new ArrayList<>();
        for (Map<String, Object> row : result) {
            SpaceCategoryAnalyzeResponse response = new SpaceCategoryAnalyzeResponse();
            response.setCategory(row.get("category") != null ? row.get("category").toString() : "未分类");
            response.setCount(((Number) row.get("cnt")).longValue());
            response.setTotalSize(row.get("totalSize") != null ? ((Number) row.get("totalSize")).longValue() : 0L);
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            User loginUser) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (!spaceSizeAnalyzeRequest.isQueryAll()) {
            Long spaceId = spaceSizeAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(loginUser, space);
            queryWrapper.eq("spaceId", spaceId);
        } else {
            checkAdminAuth(loginUser);
        }
        // 按大小区间分组统计
        queryWrapper.select("picSize");
        List<Picture> pictures = pictureMapper.selectList(queryWrapper);

        // 定义区间
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("0-100KB", 0L);
        sizeRanges.put("100KB-1MB", 0L);
        sizeRanges.put("1MB-10MB", 0L);
        sizeRanges.put(">10MB", 0L);

        for (Picture pic : pictures) {
            Long size = pic.getPicSize();
            if (size == null)
                size = 0L;
            if (size < 100 * 1024) {
                sizeRanges.put("0-100KB", sizeRanges.get("0-100KB") + 1);
            } else if (size < 1024 * 1024) {
                sizeRanges.put("100KB-1MB", sizeRanges.get("100KB-1MB") + 1);
            } else if (size < 10 * 1024 * 1024) {
                sizeRanges.put("1MB-10MB", sizeRanges.get("1MB-10MB") + 1);
            } else {
                sizeRanges.put(">10MB", sizeRanges.get(">10MB") + 1);
            }
        }

        List<SpaceSizeAnalyzeResponse> responseList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sizeRanges.entrySet()) {
            SpaceSizeAnalyzeResponse response = new SpaceSizeAnalyzeResponse();
            response.setSizeRange(entry.getKey());
            response.setCount(entry.getValue());
            responseList.add(response);
        }
        return responseList;
    }

    private void checkAdminAuth(User loginUser) {
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
