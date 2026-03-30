import request from '@/utils/request'

/** 点赞 / 取消点赞 POST /picture_like/ */
export async function doPictureLikeUsingPost(
    body: API.PictureQueryRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseInt_>('/api/asset-like/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}
