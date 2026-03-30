// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** deletePicture POST /api/asset/delete */
export async function deletePictureUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deletePictureBatch POST /api/asset/delete/batch */
export async function deletePictureBatchUsingPost(
  body: API.PictureBatchDeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/delete/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** clearRecycleBin POST /api/asset/recycle/clear */
export async function clearRecycleBinUsingPost(
  body: API.PictureBatchDeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/recycle/clear', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPicture POST /api/asset/edit */
export async function editPictureUsingPost(
  body: API.PictureEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureEditLock GET /api/asset/edit-lock */
export async function getPictureEditLockUsingGet(
  params: API.getPictureEditLockUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureEditLockVO_>('/api/asset/edit-lock', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** acquirePictureEditLock POST /api/asset/edit-lock/acquire */
export async function acquirePictureEditLockUsingPost(
  body: API.PictureEditLockRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureEditLockVO_>('/api/asset/edit-lock/acquire', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** renewPictureEditLock POST /api/asset/edit-lock/renew */
export async function renewPictureEditLockUsingPost(
  body: API.PictureEditLockRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureEditLockVO_>('/api/asset/edit-lock/renew', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** releasePictureEditLock POST /api/asset/edit-lock/release */
export async function releasePictureEditLockUsingPost(
  body: API.PictureEditLockRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/edit-lock/release', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureById GET /api/asset/get */
export async function getPictureByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePicture_>('/api/asset/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getPictureVOById GET /api/asset/get/vo */
export async function getPictureVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO_>('/api/asset/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listPictureEditLogs GET /api/asset/edit-log/list */
export async function listPictureEditLogsUsingGet(
  params: API.listPictureEditLogsUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPictureEditLogVO_>('/api/asset/edit-log/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listPictureByPage POST /api/asset/list/page */
export async function listPictureByPageUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePicture_>('/api/asset/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureVOByPage POST /api/asset/list/page/vo */
export async function listPictureVoByPageUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePictureVO_>('/api/asset/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** doPictureReview POST /api/asset/review */
export async function doPictureReviewUsingPost(
  body: API.PictureReviewRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureTagCategory GET /api/asset/tag-category */
export async function listPictureTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponsePictureTagCategory_>('/api/asset/tag-category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** updatePicture POST /api/asset/update */
export async function updatePictureUsingPost(
  body: API.PictureUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadPicture POST /api/asset/upload */
export async function uploadPictureUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureUsingPOSTParams,
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponsePictureVO_>('/api/asset/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** uploadPictureByBatch POST /api/asset/upload/batch */
export async function uploadPictureByBatchUsingPost(
  body: API.PictureUploadByBatchRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureBatchPreviewVO_>('/api/asset/upload/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** confirmUploadPictureByBatch POST /api/asset/upload/batch/confirm */
export async function confirmUploadPictureByBatchUsingPost(
  body: API.PictureBatchConfirmRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPictureVO_>('/api/asset/upload/batch/confirm', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** discardUploadPictureBatch POST /api/asset/upload/batch/discard */
export async function discardUploadPictureBatchUsingPost(
  body: API.PictureBatchDiscardRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/upload/batch/discard', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadPictureByUrl POST /api/asset/upload/url */
export async function uploadPictureByUrlUsingPost(
  body: API.PictureUploadRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO_>('/api/asset/upload/url', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
/** listRelatedPictures POST /api/asset/list/related */
export async function listRelatedPicturesUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPictureVO_>('/api/asset/list/related', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listDeletedPictures GET /api/asset/recycle/list */
export async function listDeletedPicturesUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPictureVO_>('/api/asset/recycle/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** recoverPicture POST /api/asset/recycle/recover */
export async function recoverPictureUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/recycle/recover', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** searchPictureByColor POST /api/asset/search/color */
export async function searchPictureByColorUsingPost(
  body: API.SearchPictureByColorRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPictureVO_>('/api/asset/search/color', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 以图搜图 POST /api/asset/search/picture */
export async function searchPictureByPictureUsingPost(
  body: API.SearchPictureByPictureRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPictureVO_>('/api/asset/search/picture', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** searchPictureByFile POST /api/asset/search/picture/file */
export async function searchPictureByFileUsingPost(
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  return request<API.BaseResponseListPictureVO_>('/api/asset/search/picture/file', {
    method: 'POST',
    data: formData,
    // @ts-ignore
    requestType: 'form',
    ...(options || {}),
  })
}

/** 生成分享码 /api/asset/share/generate */
export async function generatePictureShareCodeUsingPost(
  params: { pictureId: number },
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString_>('/api/asset/share/generate', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 清除分享码 /api/asset/share/clear */
export async function clearPictureShareCodeUsingPost(
  params: { pictureId: number },
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/asset/share/clear', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取下载信息 /api/asset/download/info */
export async function getPictureDownloadInfoUsingGet(
  params: { pictureId: number },
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseDownloadInfoVO_>('/api/asset/download/info', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
