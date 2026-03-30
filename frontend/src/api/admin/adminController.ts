import request from '@/utils/request'

/** 获取后台统计数据 GET /api/admin/stats */
export async function getAdminStatsUsingGet(options?: { [key: string]: any }) {
    return request<API.BaseResponseAdminStatsVO_>('/api/admin/stats', {
        method: 'GET',
        ...(options || {}),
    })
}

/** 获取邮件服务健康状态 GET /api/admin/mail/health */
export async function getAdminMailHealthUsingGet(options?: { [key: string]: any }) {
    return request<API.BaseResponseMailHealthStatusVO_>('/api/admin/mail/health', {
        method: 'GET',
        ...(options || {}),
    })
}

/** 发送测试邮件 POST /api/admin/mail/test */
export async function sendAdminMailTestUsingPost(
    body: API.AdminMailTestRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>('/api/admin/mail/test', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}

/** 分页获取敏感操作日志 POST /api/admin/log/list/page */
export async function listSensitiveOperationLogsUsingPost(
    body: API.SensitiveOperationLogQueryRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponsePageSensitiveOperationLog_>('/api/admin/log/list/page', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}
