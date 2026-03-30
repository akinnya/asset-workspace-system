// @ts-ignore
/* eslint-disable */
import request from '@/utils/request';

/** 关注用户 POST /api/user_follow/follow */
export async function followUserUsingPost(
    body: number,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/user_follow/follow', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 取消关注 POST /api/user_follow/unfollow */
export async function unfollowUserUsingPost(
    body: number,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/user_follow/unfollow', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** 获取我关注的人 GET /api/user_follow/list/following */
export async function listMyFollowingsUsingGet(
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListUserVO_>('/api/user_follow/list/following', {
        method: 'GET',
        ...(options || {}),
    });
}

/** 获取我的粉丝 GET /api/user_follow/list/follower */
export async function listMyFollowersUsingGet(
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListUserVO_>('/api/user_follow/list/follower', {
        method: 'GET',
        ...(options || {}),
    });
}

/** 获取某人关注的人 GET /api/user_follow/list/following/user */
export async function listUserFollowingsUsingGet(
    params: { userId: number },
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListUserVO_>('/api/user_follow/list/following/user', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}

/** 检查是否已关注 GET /api/user_follow/is_following */
export async function isFollowingUsingGet(
    params: { followingId: number },
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/user_follow/is_following', {
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}
