import request from '@/utils/request';

/** Add Attachment POST /picture/attachment/add */
export async function addPictureAttachmentUsingPost(
    body: API.PictureAttachmentAddRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseLong_>('/api/asset/attachment/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** Delete Attachment POST /picture/attachment/delete */
export async function deletePictureAttachmentUsingPost(
    body: API.DeleteRequest,
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseBoolean_>('/api/asset/attachment/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: body,
        ...(options || {}),
    });
}

/** List Attachments GET /picture/attachment/list */
export async function listPictureAttachmentsUsingGet(
    params: { pictureId: number },
    options?: { [key: string]: any },
) {
    return request<API.BaseResponseListPictureAttachment_>('/api/asset/attachment/list', { // Note: Need to verify if BaseResponseListPictureAttachment_ exists or just BaseResponse_ with data array
        method: 'GET',
        params: {
            ...params,
        },
        ...(options || {}),
    });
}
