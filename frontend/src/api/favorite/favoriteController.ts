import request from '@/utils/request'

/** 添加收藏 POST /favorite/add */
export async function addFavoriteUsingPost(
    params: { pictureId: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>('/api/favorite/add', {
        method: 'POST',
        params: {
            ...params,
        },
        ...(options || {}),
    })
}

/** 取消收藏 POST /favorite/cancel */
export async function cancelFavoriteUsingPost(
    params: { pictureId: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>('/api/favorite/cancel', {
        method: 'POST',
        params: {
            ...params,
        },
        ...(options || {}),
    })
}

/** 检查是否已收藏 GET /favorite/check */
export async function checkFavoriteUsingGet(
    params: { pictureId: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>('/api/favorite/check', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    })
}

/** 分页获取收藏列表 POST /favorite/list/page/vo */
/** 分页获取收藏列表 POST /favorite/list */
export async function listFavoriteVoByPageUsingPost(
    params: { current?: number; pageSize?: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponsePagePictureVO_>('/api/favorite/list', {
        method: 'POST',
        params: {
            ...params,
        },
        ...(options || {}),
    })
}
