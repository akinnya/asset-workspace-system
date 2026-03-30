// @ts-ignore
/* eslint-disable */
import request from '@/utils/request';

/** 获取空间使用排行（仅管理员） POST /api/workspace/analyze/rank */
export async function getSpaceRankAnalyzeUsingPost(
    body: API.SpaceRankAnalyzeRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceRankAnalyzeResponse_>('/api/workspace/analyze/rank', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取空间使用分析 POST /api/workspace/analyze/usage */
export async function getSpaceUsageAnalyzeUsingPost(
    body: API.SpaceUsageAnalyzeRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseSpaceUsageAnalyzeResponse_>('/api/workspace/analyze/usage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取空间分类分析 POST /api/workspace/analyze/category */
export async function getSpaceCategoryAnalyzeUsingPost(
    body: API.SpaceCategoryAnalyzeRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceCategoryAnalyzeResponse_>('/api/workspace/analyze/category', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取空间大小分析 POST /api/workspace/analyze/size */
export async function getSpaceSizeAnalyzeUsingPost(
    body: API.SpaceSizeAnalyzeRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListSpaceSizeAnalyzeResponse_>('/api/workspace/analyze/size', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}
