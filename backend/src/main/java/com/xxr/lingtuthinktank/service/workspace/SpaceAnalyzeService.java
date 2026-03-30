package com.xxr.lingtuthinktank.service.workspace;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeResponse;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.user.User;

import java.util.List;

/**
 * 空间分析服务
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间资源使用分析
     *
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param loginUser                登录用户
     * @return 空间资源使用分析
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间使用排行
     *
     * @param spaceRankAnalyzeRequest 请求参数
     * @param loginUser               登录用户
     * @return 空间使用排行
     */
    List<SpaceRankAnalyzeResponse> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类分析
     *
     * @param spaceCategoryAnalyzeRequest 请求参数
     * @param loginUser                   登录用户
     * @return 分类分析列表
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            User loginUser);

    /**
     * 获取空间大小分析
     *
     * @param spaceSizeAnalyzeRequest 请求参数
     * @param loginUser               登录用户
     * @return 大小分析列表
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);
}
