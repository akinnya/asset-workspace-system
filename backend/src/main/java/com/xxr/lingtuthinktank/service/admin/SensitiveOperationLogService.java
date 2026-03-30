package com.xxr.lingtuthinktank.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.admin.SensitiveOperationLogQueryRequest;
import com.xxr.lingtuthinktank.model.entity.admin.SensitiveOperationLog;

/**
 * 敏感操作日志服务
 */
public interface SensitiveOperationLogService extends IService<SensitiveOperationLog> {

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<SensitiveOperationLog> getQueryWrapper(SensitiveOperationLogQueryRequest queryRequest);
}
