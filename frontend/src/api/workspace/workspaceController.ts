// @ts-ignore
/* eslint-disable */
import request from '@/utils/request';

/** 创建空间 POST /api/workspace/add */
export async function addSpaceUsingPost(
    body: API.SpaceAddRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseLong_>('/api/workspace/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 删除空间 POST /api/workspace/delete */
export async function deleteSpaceUsingPost(
    body: API.DeleteRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 编辑空间（给用户使用） POST /api/workspace/edit */
export async function editSpaceUsingPost(
    body: API.SpaceEditRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace/edit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 根据 id 获取空间（仅管理员可用） GET /api/workspace/get */
export async function getSpaceByIdUsingGet(
    // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
    params: API.getSpaceByIdUsingGETParams,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseSpace_>('/api/workspace/get', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}

/** 根据 id 获取空间（封装类） GET /api/workspace/get/vo */
export async function getSpaceVoByIdUsingGet(
    // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
    params: API.getSpaceVOByIdUsingGETParams,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseSpaceVO_>('/api/workspace/get/vo', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}

/** 获取空间动态流 GET /api/workspace/activity/list */
export async function listSpaceActivityUsingGet(
    params: { spaceId: string | number; limit?: number },
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceActivityVO_>('/api/workspace/activity/list', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}

/** 获取空间级别列表 GET /api/workspace/list/level */
export async function listSpaceLevelUsingGet(options?: { [key: string]: any }) {
    return request<API.BaseResponseListSpaceLevelEnum_>('/api/workspace/list/level', {
        method: 'GET',
        ...(options || {}),
    });
}

/** 分页获取空间列表（仅管理员可用） POST /api/workspace/list/page */
export async function listSpaceByPageUsingPost(
    body: API.SpaceQueryRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponsePageSpace_>('/api/workspace/list/page', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 分页获取空间列表（封装类） POST /api/workspace/list/page/vo */
export async function listSpaceVoByPageUsingPost(
    body: API.SpaceQueryRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponsePageSpaceVO_>('/api/workspace/list/page/vo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 管理员分页获取团队空间列表（封装类） POST /api/workspace/admin/team/list/page/vo */
export async function listTeamSpaceVoByPageUsingPost(
    body: API.SpaceQueryRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponsePageSpaceVO_>('/api/workspace/admin/team/list/page/vo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 更新空间（仅管理员可用） POST /api/workspace/update */
export async function updateSpaceUsingPost(
    body: API.SpaceUpdateRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/workspace/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}
