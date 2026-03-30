package com.xxr.lingtuthinktank.service.asset.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.manager.asset.PictureBatchPreviewManager;
import com.xxr.lingtuthinktank.manager.asset.PictureColorExtractor;
import com.xxr.lingtuthinktank.manager.asset.upload.FilePictureUpload;
import com.xxr.lingtuthinktank.manager.asset.upload.PictureUploadTemplate;
import com.xxr.lingtuthinktank.manager.asset.upload.UrlPictureUpload;
import com.xxr.lingtuthinktank.mapper.asset.PictureMapper;
import com.xxr.lingtuthinktank.model.dto.asset.upload.UploadPictureResult;
import com.xxr.lingtuthinktank.model.dto.asset.batch.PictureBatchConfirmRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.PictureQueryRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadByBatchRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.SearchPictureByColorRequest;
import com.xxr.lingtuthinktank.model.entity.favorite.Favorite;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.enums.asset.PictureReviewStatusEnum;
import com.xxr.lingtuthinktank.model.vo.asset.PictureBatchPreviewVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;
import com.xxr.lingtuthinktank.model.vo.user.UserVO;
import com.xxr.lingtuthinktank.service.favorite.FavoriteService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.user.UserService;
import com.xxr.lingtuthinktank.utils.ColorUtils;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 40655
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-08 22:23:05
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Autowired
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private PictureColorExtractor pictureColorExtractor;

    @Resource
    private OssManager ossManager;

    @Resource
    private PictureBatchPreviewManager pictureBatchPreviewManager;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        Picture oldPicture = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 上传图片，得到信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());

        // 进阶功能：计算哈希、提取元数据 (仅针对文件上传)
        String pHash = null;
        String picColorSpace = null;
        if (inputSource instanceof org.springframework.web.multipart.MultipartFile) {
            try {
                org.springframework.web.multipart.MultipartFile file = (org.springframework.web.multipart.MultipartFile) inputSource;
                // 计算 pHash
                pHash = com.xxr.lingtuthinktank.utils.ColorTransform.getPHash(file.getInputStream());

                // 查重检测 (pHash 相似度)
                if (pHash != null) {
                    // 查询数据库中已有的图片哈希
                    // 这里为了性能，应该只查同一用户的，或者建立专门的索引结构
                    // 简单实现：查该用户的前 100 张图比较?
                    // 更好的方式：Wait，SQL 查询 pHash 相似度太慢，一般配合 ES 或 Hammington Distance function in DB.
                    // 毕设简单策略：如果完全相同则提示；相似的仅记录。
                    QueryWrapper<Picture> duplicateQuery = new QueryWrapper<>();
                    duplicateQuery.eq("pHash", pHash);
                    duplicateQuery.eq("userId", loginUser.getId());
                    if (this.count(duplicateQuery) > 0) {
                        // 发现完全一致的指纹，可能是重复上传
                        // 可以选择 throw Exception 或者 标记 duplicate
                        // log.warn("Duplicate picture detected for user {}", loginUser.getId());
                    }
                }

                // 提取 Metadata (Color Space, etc.)
                try {
                    com.drew.metadata.Metadata metadata = com.drew.imaging.ImageMetadataReader
                            .readMetadata(file.getInputStream());
                    // 简单遍历查找 ICC Profile 或 Exif Interop
                    for (com.drew.metadata.Directory directory : metadata.getDirectories()) {
                        if (directory.getName().contains("ICC")) {
                            // 粗略判断
                            picColorSpace = directory.getName();
                        }
                    }
                } catch (Exception e) {
                    // ignore metadata error
                }

            } catch (Exception e) {
                log.error("Advanced image processing failed", e);
            }
        }

        // 根据 inputSource 类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPHash(pHash); // Set pHash
        picture.setPicColorSpace(picColorSpace); // Set ColorSpace
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());

        // 3D Model Special Handling
        if ("pmx".equalsIgnoreCase(uploadPictureResult.getPicFormat()) && inputSource instanceof java.io.File) {
            // If local file, we can parse it
            // But inputSource is MultipartFile or String.
            // Logic complexity: UploadTemplate returns info, but doesn't return local file
            // path easily unless we cache it.
            // Skip deep parsing for now to keep flow safe, or read from MultipartFile
            // stream above if format detected.
        }

        // 设置颜色与自动标签策略
        String picColor = uploadPictureResult.getPicColor();
        if (StrUtil.isNotBlank(picColor)) {
            picture.setPicColor(picColor);
        }
        List<String> tags = buildAutoTags(picColor);
        picture.setTags(JSONUtil.toJsonStr(tags));

        picture.setUserId(loginUser.getId());
        // 设置空间id
        if (pictureUploadRequest != null && pictureUploadRequest.getSpaceId() != null) {
            picture.setSpaceId(pictureUploadRequest.getSpaceId());
        }
        // 新上传默认进入待发布状态，避免未点击发布就公开
        if (pictureId != null && oldPicture != null) {
            picture.setReviewStatus(oldPicture.getReviewStatus());
            picture.setReviewerId(oldPicture.getReviewerId());
            picture.setReviewMessage(oldPicture.getReviewMessage());
            picture.setReviewTime(oldPicture.getReviewTime());
        } else {
            fillDraftParams(picture);
        }
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        PictureVO pictureVO = PictureVO.objToVo(picture);
        fillSignedUrls(pictureVO);
        return pictureVO;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Boolean nullSpaceId = pictureQueryRequest.getNullSpaceId();
        String picColor = pictureQueryRequest.getPicColor();
        Integer fileType = pictureQueryRequest.getFileType();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
                    .or()
                    .like("tags", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // 空间查询
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        // 查询公共图库（spaceId 为 null）
        if (Boolean.TRUE.equals(nullSpaceId)) {
            queryWrapper.isNull("spaceId");
        }
        // 颜色搜索（支持模糊匹配）
        queryWrapper.like(StrUtil.isNotBlank(picColor), "picColor", picColor);
        // 文件类型过滤
        if (fileType != null) {
            List<String> formats = new ArrayList<>();
            if (fileType == 1) { // 图片
                formats.addAll(java.util.Arrays.asList("jpg", "jpeg", "png", "webp", "gif", "bmp", "tif", "tiff"));
            } else if (fileType == 2) { // 视频
                formats.addAll(java.util.Arrays.asList("mp4", "mov", "avi", "webm", "mkv"));
            } else if (fileType == 3) { // 3D
                formats.addAll(java.util.Arrays.asList("pmx", "vrm", "obj", "fbx", "glb", "gltf"));
            } else if (fileType == 4) { // 音频
                formats.addAll(java.util.Arrays.asList("mp3", "wav", "flac", "ogg", "m4a", "aac"));
            } else if (fileType == 5) { // 文本
                formats.addAll(java.util.Arrays.asList("txt", "md", "markdown", "pdf", "doc", "docx", "rtf", "csv", "json", "xml", "yaml", "yml"));
            }
            if (!formats.isEmpty()) {
                queryWrapper.in("picFormat", formats);
            }
        }
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        if (StrUtil.isBlank(category) && StrUtil.isBlank(searchText) && StrUtil.isBlank(sortField)) {
            // “精选” 逻辑：随机排序（可以加缓存或固定种子以保证分页一致性，这里简单实现随机）
            queryWrapper.last("ORDER BY RAND()");
        } else {
            queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        }
        return queryWrapper;
    }

    @Resource
    private com.xxr.lingtuthinktank.service.asset.PictureLikeService pictureLikeService;

    @Resource
    private FavoriteService favoriteService;

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        fillSignedUrls(pictureVO);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                pictureVO.setUser(userVO);
            }
        }
        // 点赞状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.PictureLike> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pictureId", picture.getId());
            queryWrapper.eq("userId", loginUser.getId());
            long count = pictureLikeService.count(queryWrapper);
            pictureVO.setIsLiked(count > 0);
            long favoriteCount = favoriteService.count(new QueryWrapper<Favorite>()
                    .eq("pictureId", picture.getId())
                    .eq("userId", loginUser.getId()));
            pictureVO.setIsFavorited(favoriteCount > 0);
        } else {
            pictureVO.setIsLiked(false);
            pictureVO.setIsFavorited(false);
        }
        long likeCount = pictureLikeService.count(new QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.PictureLike>()
                .eq("pictureId", picture.getId()));
        pictureVO.setLikeCount(likeCount);
        if (loginUser == null || !picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            pictureVO.setShareCode(null);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(),
                picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        pictureVOList.forEach(this::fillSignedUrls);
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 2. 批量查询点赞状态/收藏状态/点赞数量
        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> likedPictureIds = new java.util.HashSet<>();
        Set<Long> favoritedPictureIds = new java.util.HashSet<>();
        Map<Long, Long> likeCountMap = new java.util.HashMap<>();
        Set<Long> pictureIds = pictureList.stream().map(Picture::getId).collect(Collectors.toSet());
        if (loginUser != null) {
            QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.PictureLike> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("pictureId", pictureIds);
            queryWrapper.eq("userId", loginUser.getId());
            List<com.xxr.lingtuthinktank.model.entity.asset.PictureLike> likeList = pictureLikeService.list(queryWrapper);
            likedPictureIds = likeList.stream().map(com.xxr.lingtuthinktank.model.entity.asset.PictureLike::getPictureId)
                    .collect(Collectors.toSet());

            List<Favorite> favoriteList = favoriteService.list(new QueryWrapper<Favorite>()
                    .in("pictureId", pictureIds)
                    .eq("userId", loginUser.getId()));
            favoritedPictureIds = favoriteList.stream().map(Favorite::getPictureId).collect(Collectors.toSet());
        }
        List<com.xxr.lingtuthinktank.model.entity.asset.PictureLike> allLikeList = pictureLikeService.list(
                new QueryWrapper<com.xxr.lingtuthinktank.model.entity.asset.PictureLike>()
                        .in("pictureId", pictureIds));
        likeCountMap = allLikeList.stream().collect(Collectors.groupingBy(
                com.xxr.lingtuthinktank.model.entity.asset.PictureLike::getPictureId,
                Collectors.counting()));

        // 3. 填充信息
        final Set<Long> finalLikedPictureIds = likedPictureIds;
        final Set<Long> finalFavoritedPictureIds = favoritedPictureIds;
        final Map<Long, Long> finalLikeCountMap = likeCountMap;
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
                pictureVO.setUser(userService.getUserVO(user));
            }
            pictureVO.setIsLiked(finalLikedPictureIds.contains(pictureVO.getId()));
            pictureVO.setIsFavorited(finalFavoritedPictureIds.contains(pictureVO.getId()));
            pictureVO.setLikeCount(finalLikeCountMap.getOrDefault(pictureVO.getId(), 0L));
            if (loginUser == null
                    || !pictureVO.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                pictureVO.setShareCode(null);
            }
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        // 设置为已通过，用于发布确认
        picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        picture.setReviewerId(loginUser != null ? loginUser.getId() : 0L);
        picture.setReviewMessage("系统自动通过");
        picture.setReviewTime(new Date());
    }

    private void fillDraftParams(Picture picture) {
        picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        picture.setReviewerId(0L);
        picture.setReviewMessage("待发布");
        picture.setReviewTime(null);
    }

    @Override
    public PictureBatchPreviewVO previewUploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        String searchText = pictureUploadByBatchRequest.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "数量必须大于 0");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        List<UploadPictureResult> uploadResults = new ArrayList<>();
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            try {
                UploadPictureResult uploadResult = uploadPictureForPreview(fileUrl, namePrefix, uploadCount + 1,
                        loginUser);
                uploadResults.add(uploadResult);
                uploadCount++;
            } catch (Exception e) {
                log.error("图片预览上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return pictureBatchPreviewManager.createPreview(loginUser.getId(), uploadResults);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PictureVO> confirmUploadPictureByBatch(PictureBatchConfirmRequest pictureBatchConfirmRequest,
            User loginUser) {
        ThrowUtils.throwIf(pictureBatchConfirmRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        String batchId = pictureBatchConfirmRequest.getBatchId();
        List<String> previewIds = pictureBatchConfirmRequest.getPreviewIds();
        ThrowUtils.throwIf(StrUtil.isBlank(batchId), ErrorCode.PARAMS_ERROR, "批次 id 不能为空");
        ThrowUtils.throwIf(CollUtil.isEmpty(previewIds), ErrorCode.PARAMS_ERROR, "请选择需要入库的图片");

        PictureBatchPreviewManager.PreviewBatch batch = pictureBatchPreviewManager.getValidBatch(batchId,
                loginUser.getId());
        Map<String, UploadPictureResult> itemMap = batch.getItems();
        ThrowUtils.throwIf(itemMap == null || itemMap.isEmpty(), ErrorCode.OPERATION_ERROR, "预览批次为空");

        LinkedHashSet<String> selectedIds = new LinkedHashSet<>(previewIds);
        List<PictureVO> result = new ArrayList<>();

        for (String previewId : selectedIds) {
            UploadPictureResult uploadResult = itemMap.get(previewId);
            ThrowUtils.throwIf(uploadResult == null, ErrorCode.PARAMS_ERROR, "预览项不存在");
            Picture picture = buildPictureFromUploadResult(uploadResult, loginUser);
            boolean saveResult = this.save(picture);
            ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "图片入库失败");
            PictureVO pictureVO = PictureVO.objToVo(picture);
            fillSignedUrls(pictureVO);
            result.add(pictureVO);
        }

        pictureBatchPreviewManager.removeBatchAndDeleteUnselected(batchId, selectedIds);
        return result;
    }

    @Override
    public void discardBatchPreview(String batchId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(batchId), ErrorCode.PARAMS_ERROR, "批次 id 不能为空");
        pictureBatchPreviewManager.discardBatch(batchId, loginUser.getId());
    }

    private UploadPictureResult uploadPictureForPreview(String fileUrl, String namePrefix, int index, User loginUser) {
        PictureUploadTemplate pictureUploadTemplate = urlPictureUpload;
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(fileUrl, uploadPathPrefix);
        if (StrUtil.isNotBlank(namePrefix)) {
            uploadPictureResult.setPicName(namePrefix + index);
        }
        return uploadPictureResult;
    }

    private Picture buildPictureFromUploadResult(UploadPictureResult uploadPictureResult, User loginUser) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setPicColor(uploadPictureResult.getPicColor());

        String picColor = uploadPictureResult.getPicColor();
        List<String> tags = buildAutoTags(picColor);
        picture.setTags(JSONUtil.toJsonStr(tags));
        picture.setUserId(loginUser.getId());
        fillReviewParams(picture, loginUser);
        return picture;
    }

    private List<String> buildAutoTags(String picColor) {
        List<String> tags = new ArrayList<>();
        if (StrUtil.isNotBlank(picColor)) {
            String colorTag = ColorUtils.classifyColor(picColor);
            if (StrUtil.isNotBlank(colorTag)) {
                tags.add(colorTag);
            }
        }
        return tags;
    }

    @Override
    public List<PictureVO> searchPictureByColor(SearchPictureByColorRequest searchPictureByColorRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);

        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        Double similarityThreshold = searchPictureByColorRequest.getSimilarityThreshold();

        ThrowUtils.throwIf(StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR, "颜色参数不能为空");

        // 设置默认阈值
        if (similarityThreshold == null || similarityThreshold <= 0 || similarityThreshold > 1) {
            similarityThreshold = 0.8;
        }

        // 查询所有有颜色信息的图片
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("picColor");
        queryWrapper.ne("picColor", "");
        // 仅公开分享作品
        queryWrapper.isNull("shareCode");
        // 仅展示已发布作品
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());
        // 空间过滤
        if (spaceId != null && spaceId > 0) {
            queryWrapper.eq("spaceId", spaceId);
        }

        List<Picture> pictureList = this.list(queryWrapper);

        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }

        // 计算颜色相似度并过滤
        final Double threshold = similarityThreshold;
        List<Picture> filteredPictures = pictureList.stream()
                .filter(picture -> {
                    double similarity = pictureColorExtractor.calculateColorSimilarity(picColor, picture.getPicColor());
                    return similarity >= threshold;
                })
                .sorted((p1, p2) -> {
                    // 按相似度降序排序
                    double sim1 = pictureColorExtractor.calculateColorSimilarity(picColor, p1.getPicColor());
                    double sim2 = pictureColorExtractor.calculateColorSimilarity(picColor, p2.getPicColor());
                    return Double.compare(sim2, sim1);
                })
                .limit(20) // 最多返回20条
                .collect(Collectors.toList());

        // 转换为VO
        return filteredPictures.stream()
                .map(picture -> getPictureVO(picture, request))
                .collect(Collectors.toList());
    }

    @Override
    public List<PictureVO> searchPictureByFile(MultipartFile file, HttpServletRequest request) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        String originalName = file.getOriginalFilename();
        String suffix = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ossManager.isImageFormat(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持图片文件搜索");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "读取图片失败");
        }
        String referencePHash;
        String referenceColor;
        Double referenceScale = null;
        try {
            referencePHash = com.xxr.lingtuthinktank.utils.ColorTransform
                    .getPHash(new ByteArrayInputStream(bytes));
            referenceColor = pictureColorExtractor.extractMainColor(new ByteArrayInputStream(bytes));
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image != null && image.getHeight() > 0) {
                referenceScale = Math.round(image.getWidth() * 100.0 / image.getHeight()) / 100.0;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片解析失败");
        }

        Picture referencePicture = new Picture();
        referencePicture.setPHash(referencePHash);
        referencePicture.setPicColor(referenceColor);
        referencePicture.setPicScale(referenceScale);

        User loginUser = userService.getLoginUserPermitNull(request);
        boolean isAdmin = loginUser != null && userService.isAdmin(loginUser);

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());
        if (!isAdmin) {
            queryWrapper.and(qw -> {
                qw.isNull("shareCode");
                if (loginUser != null) {
                    qw.or().eq("userId", loginUser.getId());
                }
            });
        }
        List<Picture> pictureList = this.list(queryWrapper);
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }

        boolean requirePHash = StrUtil.isNotBlank(referencePHash);
        final double minScore = requirePHash ? 0.55 : 0.45;
        final double minColorSimilarity = 0.6;
        final int maxHammingDistance = 12;

        List<java.util.AbstractMap.SimpleEntry<Picture, Double>> scoredPictures = new ArrayList<>();
        for (Picture candidate : pictureList) {
            if (requirePHash) {
                if (StrUtil.isBlank(candidate.getPHash())) {
                    continue;
                }
                int distance = com.xxr.lingtuthinktank.utils.ColorTransform.getHammingDistance(referencePHash,
                        candidate.getPHash());
                if (distance > maxHammingDistance) {
                    continue;
                }
            } else {
                if (StrUtil.isBlank(referenceColor) || StrUtil.isBlank(candidate.getPicColor())) {
                    continue;
                }
                double colorSimilarity = pictureColorExtractor.calculateColorSimilarity(referenceColor,
                        candidate.getPicColor());
                if (colorSimilarity < minColorSimilarity) {
                    continue;
                }
            }
            double score = calculateSimilarityScore(referencePicture, candidate);
            if (score >= minScore) {
                scoredPictures.add(new java.util.AbstractMap.SimpleEntry<>(candidate, score));
            }
        }

        List<Picture> sortedPictures = scoredPictures.stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(java.util.AbstractMap.SimpleEntry::getKey)
                .limit(20)
                .collect(Collectors.toList());

        return sortedPictures.stream()
                .map(picture -> getPictureVO(picture, request))
                .collect(Collectors.toList());
    }

    @Override
    public List<PictureVO> searchPictureByPicture(Long pictureId, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片id不能为空");

        // 获取参考图片
        Picture referencePicture = this.getById(pictureId);
        ThrowUtils.throwIf(referencePicture == null, ErrorCode.NOT_FOUND_ERROR, "参考图片不存在");
        User loginUser = userService.getLoginUserPermitNull(request);
        boolean isOwnerOrAdmin = loginUser != null
                && (referencePicture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser));
        if (!isOwnerOrAdmin) {
            if (!Objects.equals(PictureReviewStatusEnum.PASS.getValue(), referencePicture.getReviewStatus())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品未公开");
            }
            if (referencePicture.getShareCode() != null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要分享码");
            }
        }

        // 查询相似图片（仅已发布）
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 排除自己
        queryWrapper.ne("id", pictureId);
        queryWrapper.eq("reviewStatus", PictureReviewStatusEnum.PASS.getValue());

        // 鉴权：他人只能看公开分享作品
        if (loginUser == null || !userService.isAdmin(loginUser)) {
            queryWrapper.and(qw -> {
                qw.isNull("shareCode");
                if (loginUser != null) {
                    qw.or().eq("userId", loginUser.getId());
                }
            });
        }

        List<Picture> pictureList = this.list(queryWrapper);

        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }

        String referencePHash = referencePicture.getPHash();
        String referenceColor = referencePicture.getPicColor();
        boolean requirePHash = StrUtil.isNotBlank(referencePHash);
        List<Picture> candidates = pictureList;
        if (requirePHash) {
            List<Picture> withPHash = pictureList.stream()
                    .filter(p -> StrUtil.isNotBlank(p.getPHash()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(withPHash)) {
                candidates = withPHash;
            }
        }

        final double minScore = requirePHash ? 0.55 : 0.45;
        final double minColorSimilarity = 0.6;
        final int maxHammingDistance = 12;

        List<java.util.AbstractMap.SimpleEntry<Picture, Double>> scoredPictures = new ArrayList<>();
        for (Picture candidate : candidates) {
            if (requirePHash) {
                if (StrUtil.isBlank(candidate.getPHash())) {
                    continue;
                }
                int distance = com.xxr.lingtuthinktank.utils.ColorTransform.getHammingDistance(referencePHash,
                        candidate.getPHash());
                if (distance > maxHammingDistance) {
                    continue;
                }
            } else {
                if (StrUtil.isBlank(referenceColor) || StrUtil.isBlank(candidate.getPicColor())) {
                    continue;
                }
                double colorSimilarity = pictureColorExtractor.calculateColorSimilarity(referenceColor,
                        candidate.getPicColor());
                if (colorSimilarity < minColorSimilarity) {
                    continue;
                }
            }
            double score = calculateSimilarityScore(referencePicture, candidate);
            if (score >= minScore) {
                scoredPictures.add(new java.util.AbstractMap.SimpleEntry<>(candidate, score));
            }
        }

        // 计算综合相似度并排序
        List<Picture> sortedPictures = scoredPictures.stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(java.util.AbstractMap.SimpleEntry::getKey)
                .limit(20) // 最多返回20条
                .collect(Collectors.toList());

        // 转换为VO
        return sortedPictures.stream()
                .map(picture -> getPictureVO(picture, request))
                .collect(Collectors.toList());
    }

    /**
     * 计算两张图片的综合相似度分数（增强版：集成 pHash）
     */
    private double calculateSimilarityScore(Picture reference, Picture target) {
        double score = 0.0;

        // 1. 结构相似度（pHash 感知哈希，权重 50%）
        if (StrUtil.isNotBlank(reference.getPHash()) && StrUtil.isNotBlank(target.getPHash())) {
            // 计算汉明距离
            int distance = com.xxr.lingtuthinktank.utils.ColorTransform.getHammingDistance(reference.getPHash(),
                    target.getPHash());
            // 64 位哈希，距离越小越相似。通常距离 < 5 极度相似，< 10 较相似。
            double phashSimilarity = Math.max(0, (64 - distance) / 64.0);
            score += phashSimilarity * 0.5;
        }

        // 2. 颜色相似度（权重 20%）
        if (StrUtil.isNotBlank(reference.getPicColor()) && StrUtil.isNotBlank(target.getPicColor())) {
            double colorSimilarity = pictureColorExtractor.calculateColorSimilarity(
                    reference.getPicColor(), target.getPicColor());
            score += colorSimilarity * 0.2;
        }

        // 3. 分类匹配（权重 15%）
        if (StrUtil.isNotBlank(reference.getCategory()) &&
                reference.getCategory().equals(target.getCategory())) {
            score += 0.15;
        }

        // 4. 尺寸比例相似度（权重 10%）
        if (reference.getPicScale() != null && target.getPicScale() != null) {
            double scaleDiff = Math.abs(reference.getPicScale() - target.getPicScale());
            double scaleSimilarity = Math.max(0, 1 - scaleDiff);
            score += scaleSimilarity * 0.1;
        }

        // 5. 格式匹配（权重 5%）
        if (StrUtil.isNotBlank(reference.getPicFormat()) &&
                reference.getPicFormat().equalsIgnoreCase(target.getPicFormat())) {
            score += 0.05;
        }

        return score;
    }

    @Override
    public List<PictureVO> listRelatedPictures(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
        // 排除自己
        if (pictureQueryRequest.getId() != null) {
            queryWrapper.ne("id", pictureQueryRequest.getId());
        }
        // 按时间倒序
        queryWrapper.orderByDesc("createTime");
        // 限制数量
        Page<Picture> page = this.page(new Page<>(1, pictureQueryRequest.getPageSize()), queryWrapper);
        List<Picture> pictureList = page.getRecords();

        List<PictureVO> result = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        result.forEach(this::fillSignedUrls);
        return result;
    }

    private void fillSignedUrls(PictureVO pictureVO) {
        if (pictureVO == null) {
            return;
        }
        String format = pictureVO.getPicFormat();
        if (pictureVO.getUrl() != null) {
            if (ossManager.isImageFormat(format)) {
                pictureVO.setUrl(ossManager.signImageUrlWithWatermarkIfNeeded(pictureVO.getUrl(), format));
            } else {
                pictureVO.setUrl(ossManager.signUrlIfNeeded(pictureVO.getUrl()));
            }
        }
        if (pictureVO.getThumbnailUrl() != null) {
            pictureVO.setThumbnailUrl(ossManager.signImageUrlWithWatermarkIfNeeded(pictureVO.getThumbnailUrl()));
        }
    }

    @Override
    public void recoverPicture(Long pictureId, User loginUser) {
        if (pictureId == null || pictureId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验权限：为了安全，管理员或本人才能恢复
        // 管理员可以恢复任何被逻辑删除的图片
        if (userService.isAdmin(loginUser)) {
            this.baseMapper.recoverPicture(pictureId);
            return;
        }
        // 普通用户只能恢复自己的
        List<Picture> pictures = this.baseMapper.listDeletedPicture(loginUser.getId());
        boolean exists = pictures.stream().anyMatch(p -> p.getId().equals(pictureId));
        if (!exists) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "作品不存在或无权操作");
        }
        this.baseMapper.recoverPicture(pictureId);
    }

    @Override
    public List<PictureVO> listDeletedPictures(User loginUser) {
        List<Picture> pictureList = this.baseMapper.listDeletedPicture(loginUser.getId());
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        return pictureList.stream().map(picture -> getPictureVO(picture, null)).collect(Collectors.toList());
    }

    @Override
    public void clearRecycleBin(List<Long> idList, User loginUser) {
        if (CollUtil.isEmpty(idList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(loginUser);
        List<Picture> deleteList = isAdmin
                ? this.baseMapper.listDeletedByIds(idList)
                : this.baseMapper.listDeletedByIdsForUser(idList, loginUser.getId());

        if (CollUtil.isEmpty(deleteList)) {
            return;
        }

        if (!isAdmin && deleteList.size() != idList.size()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "存在无权删除的作品");
        }

        for (Picture picture : deleteList) {
            deleteOssFile(picture.getUrl());
            deleteOssFile(picture.getThumbnailUrl());
        }

        List<Long> deleteIds = deleteList.stream().map(Picture::getId).collect(Collectors.toList());
        this.baseMapper.deleteDeletedByIds(deleteIds);
    }

    private void deleteOssFile(String url) {
        if (StrUtil.isBlank(url) || !url.contains("/")) {
            return;
        }
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String path = parsedUrl.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            if (StrUtil.isNotBlank(path)) {
                ossManager.deleteObject(path);
            }
        } catch (Exception e) {
            log.warn("删除 OSS 文件失败: {}", e.getMessage());
        }
    }

    @Override
    public void addViewCount(Long pictureId) {
        if (pictureId == null || pictureId <= 0) {
            return;
        }
        // 使用 update 语句实现原子自增
        this.update(new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Picture>()
                .eq("id", pictureId)
                .setSql("viewCount = viewCount + 1"));
    }
}
