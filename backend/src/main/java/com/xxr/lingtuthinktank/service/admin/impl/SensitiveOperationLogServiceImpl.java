package com.xxr.lingtuthinktank.service.admin.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.mapper.admin.SensitiveOperationLogMapper;
import com.xxr.lingtuthinktank.model.dto.admin.SensitiveOperationLogQueryRequest;
import com.xxr.lingtuthinktank.model.entity.admin.SensitiveOperationLog;
import com.xxr.lingtuthinktank.service.admin.SensitiveOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 敏感操作日志服务实现
 */
@Service
public class SensitiveOperationLogServiceImpl
        extends ServiceImpl<SensitiveOperationLogMapper, SensitiveOperationLog>
        implements SensitiveOperationLogService {

    @Override
    public QueryWrapper<SensitiveOperationLog> getQueryWrapper(SensitiveOperationLogQueryRequest queryRequest) {
        QueryWrapper<SensitiveOperationLog> queryWrapper = new QueryWrapper<>();
        if (queryRequest == null) {
            return queryWrapper;
        }

        Long userId = queryRequest.getUserId();
        String userName = queryRequest.getUserName();
        String description = queryRequest.getDescription();
        String requestPath = queryRequest.getRequestPath();
        String status = queryRequest.getStatus();
        String searchText = queryRequest.getSearchText();
        String startTime = queryRequest.getStartTime();
        String endTime = queryRequest.getEndTime();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(description), "description", description);
        queryWrapper.like(StrUtil.isNotBlank(requestPath), "requestPath", requestPath);
        queryWrapper.eq(StrUtil.isNotBlank(status), "status", status);
        queryWrapper.ge(StrUtil.isNotBlank(startTime), "createTime", startTime);
        queryWrapper.le(StrUtil.isNotBlank(endTime), "createTime", endTime);
        queryWrapper.and(StrUtil.isNotBlank(searchText), wrapper -> wrapper
                .like("userName", searchText)
                .or()
                .like("description", searchText)
                .or()
                .like("requestPath", searchText)
                .or()
                .like("methodName", searchText)
                .or()
                .like("requestParams", searchText));
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        queryWrapper.orderByDesc(StrUtil.isBlank(sortField), "createTime");
        return queryWrapper;
    }
}
