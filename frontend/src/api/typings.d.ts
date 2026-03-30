declare namespace API {
  type SpaceAddRequest = {
    spaceName?: string;
    spaceLevel?: number;
    spaceType?: number;
    maxSize?: number;
    maxCount?: number;
    introduction?: string;
    coverUrl?: string;
  };

  type SpaceEditRequest = {
    id?: number;
    spaceName?: string;
    introduction?: string;
    coverUrl?: string;
  };

  type SpaceQueryRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    spaceName?: string;
    spaceLevel?: number;
    spaceType?: number;
    userId?: number;
  };

  type SpaceUpdateRequest = {
    id?: number;
    spaceName?: string;
    spaceLevel?: number;
    maxSize?: number;
    maxCount?: number;
    introduction?: string;
    coverUrl?: string;
  };

  type Space = {
    id?: number;
    spaceName?: string;
    spaceLevel?: number;
    spaceType?: number;
    maxSize?: number;
    maxCount?: number;
    totalSize?: number;
    totalCount?: number;
    userId?: number;
    createTime?: string;
    editTime?: string;
    updateTime?: string;
    isDelete?: number;
  };

  type SpaceVO = {
    id?: number;
    spaceName?: string;
    spaceLevel?: number;
    spaceType?: number;
    maxSize?: number;
    maxCount?: number;
    totalSize?: number;
    totalCount?: number;
    userId?: number;
    introduction?: string;
    coverUrl?: string;
    user?: UserVO;
    permissionList?: string[];
    createTime?: string;
    editTime?: string;
    updateTime?: string;
  };

  type SpaceLevelEnum = any;

  type SpaceUserAddRequest = {
    spaceId?: number;
    userId?: number;
    spaceRole?: string;
  };

  type SpaceUserEditRequest = {
    id?: number;
    spaceRole?: string;
  };

  type SpaceUserQueryRequest = {
    spaceId?: number;
    userId?: number;
    spaceRole?: string;
  };

  type SpaceUserVO = {
    id?: number;
    spaceId?: number;
    userId?: number;
    spaceRole?: string;
    user?: UserVO;
    space?: SpaceVO;
    createTime?: string;
    updateTime?: string;
  };

  type SpaceJoinRequestQueryRequest = {
    spaceId?: number;
    userId?: number;
    requestType?: number;
    status?: number;
  };

  type SpaceJoinRequestReviewRequest = {
    id?: number;
    status?: number;
    reviewMessage?: string;
  };

  type SpaceInviteRespondRequest = {
    spaceId?: number;
    accept?: boolean;
  };

  type SpaceJoinRequestVO = {
    id?: number;
    spaceId?: number;
    userId?: number;
    requestType?: number;
    inviterId?: number;
    status?: number;
    reviewMessage?: string;
    reviewerId?: number;
    reviewTime?: string;
    createTime?: string;
    updateTime?: string;
    user?: UserVO;
    inviter?: UserVO;
    space?: SpaceVO;
  };

  type SpaceActivityVO = {
    id?: number;
    activityType?: string;
    spaceId?: number;
    actorUserId?: number;
    actorUser?: UserVO;
    pictureId?: number;
    pictureName?: string;
    pictureThumbnailUrl?: string;
    pictureUrl?: string;
    detail?: string;
    createTime?: string;
  };

  type BaseResponseListSpaceUserVO_ = {
    code?: number;
    data?: SpaceUserVO[];
    message?: string;
  };

  type BaseResponseListSpaceActivityVO_ = {
    code?: number;
    data?: SpaceActivityVO[];
    message?: string;
  };

  type BaseResponseListSpaceJoinRequestVO_ = {
    code?: number;
    data?: SpaceJoinRequestVO[];
    message?: string;
  };

  type BaseResponseSpaceJoinRequestVO_ = {
    code?: number;
    data?: SpaceJoinRequestVO;
    message?: string;
  };

  type SpaceRankAnalyzeRequest = {
    topN?: number;
  };

  type SpaceRankAnalyzeResponse = {
    id?: number;
    spaceName?: string;
    userId?: number;
    totalSize?: number;
  };

  type SpaceUsageAnalyzeRequest = {
    queryAll?: boolean;
    spaceId?: number;
  };

  type SpaceUsageAnalyzeResponse = {
    usedSize?: number;
    maxSize?: number;
    sizeUsageRatio?: number;
    usedCount?: number;
    maxCount?: number;
    countUsageRatio?: number;
  };

  type SpaceCategoryAnalyzeRequest = {
    queryAll?: boolean;
    spaceId?: number;
  };

  type SpaceCategoryAnalyzeResponse = {
    category?: string;
    count?: number;
    totalSize?: number;
  };

  type SpaceSizeAnalyzeRequest = {
    queryAll?: boolean;
    spaceId?: number;
  };

  type SpaceSizeAnalyzeResponse = {
    sizeRange?: string;
    count?: number;
  };

  type BaseResponseSpace_ = {
    code?: number;
    data?: Space;
    message?: string;
  };

  type BaseResponseSpaceVO_ = {
    code?: number;
    data?: SpaceVO;
    message?: string;
  };

  type BaseResponsePageSpace_ = {
    code?: number;
    data?: PageSpace_;
    message?: string;
  };

  type BaseResponsePageSpaceVO_ = {
    code?: number;
    data?: PageSpaceVO_;
    message?: string;
  };

  type BaseResponseListSpaceLevelEnum_ = {
    code?: number;
    data?: SpaceLevelEnum[];
    message?: string;
  };

  type BaseResponseListSpaceRankAnalyzeResponse_ = {
    code?: number;
    data?: SpaceRankAnalyzeResponse[];
    message?: string;
  };

  type BaseResponseListSpaceCategoryAnalyzeResponse_ = {
    code?: number;
    data?: SpaceCategoryAnalyzeResponse[];
    message?: string;
  };

  type BaseResponseListSpaceSizeAnalyzeResponse_ = {
    code?: number;
    data?: SpaceSizeAnalyzeResponse[];
    message?: string;
  };

  type BaseResponseSpaceUsageAnalyzeResponse_ = {
    code?: number;
    data?: SpaceUsageAnalyzeResponse;
    message?: string;
  };

  type PageSpace_ = {
    current?: number;
    pages?: number;
    records?: Space[];
    size?: number;
    total?: number;
  };

  type PageSpaceVO_ = {
    current?: number;
    pages?: number;
    records?: SpaceVO[];
    size?: number;
    total?: number;
  };

  type getSpaceByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getSpaceVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };
  /** 
   *  Existing definitions start here.
   */
  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseInt_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponse_boolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseBoolean = BaseResponse_boolean_
  type BaseResponseLong = BaseResponseInt_
  type BaseResponseString = BaseResponseString_

  type BaseResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePagePicture_ = {
    code?: number
    data?: PagePicture_
    message?: string
  }

  type BaseResponsePagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    message?: string
  }

  type BaseResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BaseResponsePageSensitiveOperationLog_ = {
    code?: number
    data?: PageSensitiveOperationLog_
    message?: string
  }

  type BaseResponsePicture_ = {
    code?: number
    data?: Picture
    message?: string
  }

  type BaseResponsePictureTagCategory_ = {
    code?: number
    data?: PictureTagCategory
    message?: string
  }

  type BaseResponsePictureVO_ = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type BaseResponsePictureEditLockVO_ = {
    code?: number
    data?: PictureEditLockVO
    message?: string
  }

  type BaseResponseListPictureEditLogVO_ = {
    code?: number
    data?: PictureEditLogVO[]
    message?: string
  }

  type BaseResponsePictureBatchPreviewVO_ = {
    code?: number
    data?: PictureBatchPreviewVO
    message?: string
  }

  type BaseResponseString_ = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type getPictureByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getPictureVOByIdUsingGETParams = {
    /** id */
    id?: number
    shareCode?: string
  }

  type getPictureEditLockUsingGETParams = {
    pictureId?: number
  }

  type listPictureEditLogsUsingGETParams = {
    pictureId?: number
  }

  type getCurrentSpaceInviteUsingGETParams = {
    spaceId?: number
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type LoginUserVO = {
    accessToken?: string
    createTime?: string
    editTime?: string
    id?: number
    idStr?: string
    refreshToken?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRefreshRequest = {
    refreshToken?: string
  }

  type PagePicture_ = {
    current?: number
    pages?: number
    records?: Picture[]
    size?: number
    total?: number
  }

  type PagePictureVO_ = {
    current?: number
    pages?: number
    records?: PictureVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type PageSensitiveOperationLog_ = {
    current?: number
    pages?: number
    records?: SensitiveOperationLog[]
    size?: number
    total?: number
  }

  type Picture = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    isDelete?: number
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    tags?: string
    updateTime?: string
    url?: string
    userId?: number
    viewCount?: number
  }

  type PictureEditRequest = {
    category?: string
    id?: number
    introduction?: string
    lastKnownEditTime?: number
    name?: string
    status?: number
    tags?: string[]
  }

  type PictureEditLockRequest = {
    pictureId?: number
  }

  type PictureEditLockVO = {
    expireAt?: number
    locked?: boolean
    lockedByCurrentUser?: boolean
    pictureId?: number
    supported?: boolean
    userAvatar?: string
    userId?: number
    userName?: string
  }

  type PictureQueryRequest = {
    category?: string
    current?: number
    id?: number
    introduction?: string
    name?: string
    pageSize?: number
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewerId?: number
    searchText?: string
    sortField?: string
    sortOrder?: string
    tags?: string[]
    userId?: number
    fileType?: number
    favoriteOnly?: boolean
    likedOnly?: boolean
    nullSpaceId?: boolean
  }

  type PictureReviewRequest = {
    id?: number
    reviewMessage?: string
    reviewStatus?: number
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUpdateRequest = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    tags?: string[]
  }

  type CommentAddRequest = {
    content?: string
    pictureId?: number
    parentId?: number
  }

  type CommentQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    pictureId?: number
    userId?: number
    content?: string
  }

  type CommentVO = {
    id?: number
    content?: string
    pictureId?: number
    userId?: number
    user?: UserVO
    parentId?: number
    createTime?: string
    children?: CommentVO[]
    likeCount?: number
    isLiked?: boolean
  }

  type BaseResponsePageCommentVO_ = {
    code?: number
    data?: PageResult_CommentVO_
    message?: string
  }

  type PageResult_CommentVO_ = {
    records?: CommentVO[]
    total?: number
    size?: number
    current?: number
    pages?: number
  }

  type PictureUploadByBatchRequest = {
    count?: number
    namePrefix?: string
    searchText?: string
  }

  type PictureUploadRequest = {
    fileUrl?: string
    id?: number
    picName?: string
  }

  type PictureVO = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    idStr?: string
    introduction?: string
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    thumbnailUrl?: string
    tags?: string[]
    updateTime?: string
    url?: string
    user?: UserVO
    userId?: number
    spaceId?: number
    isLiked?: boolean
    likeCount?: number
    isFavorited?: boolean
    shareCode?: string
    status?: number
    reviewStatus?: number
    reviewMessage?: string
    viewCount?: number
  }

  type PictureEditLogVO = {
    actionType?: string
    afterSummary?: string
    beforeSummary?: string
    changeSummary?: string
    createTime?: string
    id?: number
    operatorUser?: UserVO
    operatorUserId?: number
    pictureId?: number
    spaceId?: number
    updateTime?: string
  }

  type PictureBatchPreviewItemVO = {
    previewId?: string
    url?: string
    thumbnailUrl?: string
    picName?: string
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    picColor?: string
  }

  type PictureBatchPreviewVO = {
    batchId?: string
    items?: PictureBatchPreviewItemVO[]
  }

  type PictureBatchConfirmRequest = {
    batchId?: string
    previewIds?: string[]
  }

  type PictureBatchDiscardRequest = {
    batchId?: string
  }

  type PictureBatchDeleteRequest = {
    idList?: number[];
  };

  type testDownloadFileUsingGETParams = {
    /** filepath */
    filepath?: string
  }

  type uploadPictureUsingPOSTParams = {
    fileUrl?: string
    id?: number
    picName?: string
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserAddRequest = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type SensitiveOperationLog = {
    id?: number
    userId?: number
    userName?: string
    ip?: string
    requestPath?: string
    httpMethod?: string
    methodName?: string
    description?: string
    requestParams?: string
    status?: string
    errorMessage?: string
    durationMs?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type SensitiveOperationLogQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userId?: number
    userName?: string
    description?: string
    requestPath?: string
    status?: string
    searchText?: string
    startTime?: string
    endTime?: string
  }

  type UserRegisterRequest = {
    checkPassword?: string
    code?: string
    userAccount?: string
    userPassword?: string
  }

  type UserUpdateMyRequest = {
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userAccount?: string;
  };

  type UserUpdatePasswordRequest = {
    oldPassword?: string;
    newPassword?: string;
    checkPassword?: string;
  };

  type UserEmailCodeRequest = {
    email?: string;
  };

  type UserEmailUpdateRequest = {
    email?: string;
    code?: string;
  };

  type UserUpdateRequest = {
    id?: number
    idStr?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: number
    idStr?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
  type UploadPictureResult = PictureVO;

  type PictureAttachment = {
    id?: number;
    pictureId?: number;
    fileUrl?: string;
    fileName?: string;
    fileType?: string;
    fileSize?: number;
    createTime?: string;
  };

  type PictureAttachmentAddRequest = {
    pictureId: number;
    fileUrl: string;
    fileName?: string;
    fileType?: string;
    fileSize?: number;
  };

  type BaseResponseListPictureAttachment_ = {
    code?: number;
    data?: PictureAttachment[];
    message?: string;
  };

  type BaseResponseListUserVO_ = {
    code?: number;
    data?: UserVO[];
    message?: string;
  };

  type SearchPictureByColorRequest = {
    picColor: string;
    spaceId?: number;
    similarityThreshold?: number;
  };

  type SearchPictureByPictureRequest = {
    pictureId: string;
  };

  type BaseResponseListPictureVO_ = {
    code?: number;
    data?: PictureVO[];
    message?: string;
  };

  type FavoriteAddRequest = {
    pictureId: number;
  };

  type FavoriteCancelRequest = {
    pictureId: number;
  };

  type FavoriteQueryRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    userId?: number;
  };

  type Notification = {
    id?: number;
    userId?: number;
    type?: string;
    targetId?: number;
    fromUserId?: number;
    content?: string;
    isRead?: number;
    createTime?: string;
  };

  type BaseResponsePageNotification = {
    code?: number;
    data?: PageResult_Notification_;
    message?: string;
  };

  type PageResult_Notification_ = {
    records?: Notification[];
    total?: number;
    size?: number;
    current?: number;
    pages?: number;
  };

  type BaseResponseListNotification = {
    code?: number;
    data?: Notification[];
    message?: string;
  };

  type UserProfileSummaryVO = {
    user: UserVO;
    stats: UserStatsVO;
    myPosts: PictureVO[];
    myFavorites: PictureVO[];
  };

  type UserStatsVO = {
    userId?: number;
    pictureCount?: number;
    followingCount?: number;
    followerCount?: number;
    likeCount?: number;
    viewCount?: number;
    storageUsed?: number;
    storageTotal?: number;
  };

  type BaseResponseUserProfileSummaryVO_ = {
    code?: number;
    data?: UserProfileSummaryVO;
    message?: string;
  };

  type BaseResponseUserStatsVO_ = {
    code?: number;
    data?: UserStatsVO;
    message?: string;
  };

  type AdminStatsVO = {
    fileTypeDistribution?: AdminFileTypeDistributionVO[];
    pictureTrend30d?: AdminTrendPointVO[];
    pictureTrend7d?: AdminTrendPointVO[];
    totalUsers?: number;
    todayNewUsers?: number;
    weekNewUsers?: number;
    monthNewUsers?: number;
    totalPictures?: number;
    todayNewPictures?: number;
    totalComments?: number;
    totalLikes?: number;
    pendingReview?: number;
    passedReview?: number;
    rejectedReview?: number;
    spaceActivityRank?: AdminSpaceActivityVO[];
    totalStorageUsed?: number;
    userTrend30d?: AdminTrendPointVO[];
    userTrend7d?: AdminTrendPointVO[];
  };

  type AdminTrendPointVO = {
    label?: string;
    value?: number;
  };

  type AdminFileTypeDistributionVO = {
    count?: number;
    type?: string;
  };

  type AdminSpaceActivityVO = {
    activityScore?: number;
    editCount?: number;
    spaceId?: number;
    spaceName?: string;
    uploadCount?: number;
  };

  type BaseResponseAdminStatsVO_ = {
    code?: number;
    data?: AdminStatsVO;
    message?: string;
  };

  type AdminMailTestRequest = {
    email?: string;
  };

  type MailHealthStatusVO = {
    ready?: boolean;
    accessKeyConfigured?: boolean;
    regionConfigured?: boolean;
    endpointConfigured?: boolean;
    accountNameConfigured?: boolean;
    verificationTemplateConfigured?: boolean;
    testTemplateConfigured?: boolean;
    fromAliasConfigured?: boolean;
    maskedAccessKeyId?: string;
    regionId?: string;
    endpoint?: string;
    accountName?: string;
    verificationTemplateId?: string;
    testTemplateId?: string;
    fromAlias?: string;
    issues?: string[];
  };

  type BaseResponseMailHealthStatusVO_ = {
    code?: number;
    data?: MailHealthStatusVO;
    message?: string;
  };

  type ShareGenerateRequest = {
    pictureId: number;
    expireInSeconds?: number;
  };

  type ShareClearRequest = {
    pictureId: number;
  };

  type BaseResponseShareInfo_ = {
    code?: number;
    data?: ShareInfo;
    message?: string;
  };

  type ShareInfo = {
    code: string;
    url: string;
  };

  type DownloadInfoVO = {
    url: string;
    needsWatermark: boolean;
    fileName: string;
    picSize: number;
  };

  type BaseResponseDownloadInfoVO_ = {
    code?: number;
    data?: DownloadInfoVO;
    message?: string;
  };
  type SpaceUserJoinRequest = {
    spaceId?: number | string;
  };
}
