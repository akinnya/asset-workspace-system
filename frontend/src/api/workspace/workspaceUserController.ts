// @ts-ignore
/* eslint-disable */
import request from '@/utils/request';

/** 发起团队邀请 POST /api/workspace-user/add */
export async function addSpaceUserUsingPost(
    body: API.SpaceUserAddRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseLong_>('/api/workspace-user/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 删除空间成员 POST /api/workspace-user/delete */
export async function deleteSpaceUserUsingPost(
    body: API.DeleteRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace-user/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 编辑空间成员（修改角色） POST /api/workspace-user/edit */
export async function editSpaceUserUsingPost(
    body: API.SpaceUserEditRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace-user/edit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取空间成员列表 POST /api/workspace-user/list */
export async function listSpaceUserUsingPost(
    body: API.SpaceUserQueryRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceUserVO_>('/api/workspace-user/list', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取我加入的团队空间列表 GET /api/workspace-user/list/my */
export async function listMySpaceUserUsingGet(options?: { [key: string]: any }) {
    return request<API.BaseResponseListSpaceUserVO_>('/api/workspace-user/list/my', {
        method: 'GET',
        ...(options || {}),
    });
}

/** 主动加入团队空间 POST /api/workspace-user/join */
export async function joinSpaceUsingPost(
    body: API.SpaceUserJoinRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseLong_>('/api/workspace-user/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取当前用户在该空间的待处理邀请 GET /api/workspace-user/invite/get */
export async function getCurrentSpaceInviteUsingGet(
    params: API.getCurrentSpaceInviteUsingGETParams,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseSpaceJoinRequestVO_>('/api/workspace-user/invite/get', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}

/** 响应团队邀请 POST /api/workspace-user/invite/respond */
export async function respondSpaceInviteUsingPost(
    body: API.SpaceInviteRespondRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace-user/invite/respond', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 撤回待处理团队邀请 POST /api/workspace-user/invite/cancel */
export async function cancelSpaceInviteUsingPost(
    body: API.DeleteRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace-user/invite/cancel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取空间加入申请列表 POST /api/workspace-user/join/list */
export async function listSpaceJoinRequestUsingPost(
    body: API.SpaceJoinRequestQueryRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceJoinRequestVO_>('/api/workspace-user/join/list', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 审核空间加入申请 POST /api/workspace-user/join/review */
export async function reviewSpaceJoinRequestUsingPost(
    body: API.SpaceJoinRequestReviewRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace-user/join/review', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}
