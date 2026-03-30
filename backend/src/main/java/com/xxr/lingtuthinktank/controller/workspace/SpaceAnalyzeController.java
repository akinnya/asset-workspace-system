package com.xxr.lingtuthinktank.controller.workspace;

import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceCategoryAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceRankAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceSizeAnalyzeResponse;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeRequest;
import com.xxr.lingtuthinktank.model.dto.workspace.analyze.SpaceUsageAnalyzeResponse;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.workspace.SpaceAnalyzeService;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间分析接口
 */
@RestController
@RequestMapping("/workspace/analyze")
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

    /**
     * 获取空间使用分析
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间使用排行（仅管理员）
     */
    @PostMapping("/rank")
    public BaseResponse<List<SpaceRankAnalyzeResponse>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间分类分析
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser));
    }

    /**
     * 获取空间大小分析
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser));
    }
}
