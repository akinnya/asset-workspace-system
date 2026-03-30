import request from '@/utils/request'

/** 创建评论 POST /comment/add */
export async function addCommentUsingPost(
    body: API.CommentAddRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseLong_>('/api/comment/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}

/** 删除评论 POST /comment/delete */
export async function deleteCommentUsingPost(
    body: API.DeleteRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseBoolean_>('/api/comment/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}

/** 分页获取评论列表 POST /comment/list/page/vo */
export async function listCommentVoByPageUsingPost(
    body: API.CommentQueryRequest,
    options?: { [key: string]: any }
) {
    return request<API.BaseResponsePageCommentVO_>('/api/comment/list/page/vo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}

/** 点赞评论 /api/comment_like/do */
export async function doCommentLikeUsingPost(
    body: { commentId: number },
    options?: { [key: string]: any }
) {
    return request<API.BaseResponseInt_>('/api/comment_like/do', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    })
}
