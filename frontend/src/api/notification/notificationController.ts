import request from '@/utils/request'

/** 获取通知列表 POST /api/notification/list */
export async function listNotificationsUsingPost(
    params: { current?: number; pageSize?: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponsePageNotification>('/api/notification/list', {
        method: 'POST',
        params: {
            current: params.current || 1,
            pageSize: params.pageSize || 20,
        },
        ...(options || {}),
    })
}

/** 获取未读通知数量 GET /api/notification/unread/count */
export async function getUnreadCountUsingGet(options?: { [key: string]: any }) {
    return request<API.BaseResponseLong>('/api/notification/unread/count', {
        method: 'GET',
        ...(options || {}),
    })
}

/** 标记通知为已读 POST /api/notification/read */
export async function markAsReadUsingPost(
    body: number[],
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean>('/api/notification/read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}

/** 全部标记已读 POST /api/notification/read/all */
export async function markAllAsReadUsingPost(options?: { [key: string]: any }) {
    return request<API.BaseResponseBoolean>('/api/notification/read/all', {
        method: 'POST',
        ...(options || {}),
    })
}
