package com.xxr.lingtuthinktank.manager.asset;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.model.dto.asset.upload.UploadPictureResult;
import com.xxr.lingtuthinktank.model.vo.asset.PictureBatchPreviewItemVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureBatchPreviewVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PictureBatchPreviewManager {

    private static final long PREVIEW_EXPIRE_MILLIS = 30 * 60 * 1000L;

    private static final long CLEANUP_INTERVAL_MILLIS = 10 * 60 * 1000L;

    private final Map<String, PreviewBatch> previewCache = new ConcurrentHashMap<>();

    @Resource
    private OssManager ossManager;

    public PictureBatchPreviewVO createPreview(Long userId, List<UploadPictureResult> uploadResults) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(CollUtil.isEmpty(uploadResults), ErrorCode.OPERATION_ERROR, "未获取到可用图片");
        cleanupExpiredInternal();

        String batchId = RandomUtil.randomString(16);
        Map<String, UploadPictureResult> items = new LinkedHashMap<>();
        List<PictureBatchPreviewItemVO> itemVOList = new ArrayList<>();

        for (UploadPictureResult uploadResult : uploadResults) {
            String previewId = RandomUtil.randomString(12);
            items.put(previewId, uploadResult);
            PictureBatchPreviewItemVO itemVO = new PictureBatchPreviewItemVO();
            BeanUtils.copyProperties(uploadResult, itemVO);
            itemVO.setPreviewId(previewId);
            String format = uploadResult.getPicFormat();
            if (itemVO.getUrl() != null) {
                if (ossManager.isImageFormat(format)) {
                    itemVO.setUrl(ossManager.signImageUrlWithWatermarkIfNeeded(itemVO.getUrl(), format));
                } else {
                    itemVO.setUrl(ossManager.signUrlIfNeeded(itemVO.getUrl()));
                }
            }
            if (itemVO.getThumbnailUrl() != null) {
                itemVO.setThumbnailUrl(ossManager.signImageUrlWithWatermarkIfNeeded(itemVO.getThumbnailUrl()));
            }
            itemVOList.add(itemVO);
        }

        PreviewBatch batch = new PreviewBatch();
        batch.setUserId(userId);
        batch.setCreateTime(new Date());
        batch.setItems(items);
        previewCache.put(batchId, batch);

        PictureBatchPreviewVO previewVO = new PictureBatchPreviewVO();
        previewVO.setBatchId(batchId);
        previewVO.setItems(itemVOList);
        return previewVO;
    }

    public PreviewBatch getValidBatch(String batchId, Long userId) {
        ThrowUtils.throwIf(StrUtil.isBlank(batchId), ErrorCode.PARAMS_ERROR, "批次 id 不能为空");
        PreviewBatch batch = previewCache.get(batchId);
        ThrowUtils.throwIf(batch == null, ErrorCode.NOT_FOUND_ERROR, "预览批次不存在");
        if (!batch.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (isExpired(batch)) {
            previewCache.remove(batchId);
            deleteBatchFiles(batch);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "预览批次已过期");
        }
        return batch;
    }

    public void discardBatch(String batchId, Long userId) {
        PreviewBatch batch = getValidBatch(batchId, userId);
        previewCache.remove(batchId);
        deleteBatchFiles(batch);
    }

    public void removeBatchAndDeleteUnselected(String batchId, Set<String> keepIds) {
        PreviewBatch batch = previewCache.remove(batchId);
        if (batch == null) {
            return;
        }
        for (Map.Entry<String, UploadPictureResult> entry : batch.getItems().entrySet()) {
            if (keepIds != null && keepIds.contains(entry.getKey())) {
                continue;
            }
            deleteUploadResultFiles(entry.getValue());
        }
    }

    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MILLIS)
    public void cleanupExpired() {
        cleanupExpiredInternal();
    }

    private void cleanupExpiredInternal() {
        for (Map.Entry<String, PreviewBatch> entry : previewCache.entrySet()) {
            PreviewBatch batch = entry.getValue();
            if (!isExpired(batch)) {
                continue;
            }
            if (previewCache.remove(entry.getKey(), batch)) {
                deleteBatchFiles(batch);
            }
        }
    }

    private boolean isExpired(PreviewBatch batch) {
        if (batch == null || batch.getCreateTime() == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return now - batch.getCreateTime().getTime() > PREVIEW_EXPIRE_MILLIS;
    }

    private void deleteBatchFiles(PreviewBatch batch) {
        if (batch == null || batch.getItems() == null) {
            return;
        }
        for (UploadPictureResult result : batch.getItems().values()) {
            deleteUploadResultFiles(result);
        }
    }

    private void deleteUploadResultFiles(UploadPictureResult result) {
        if (result == null) {
            return;
        }
        deleteByPath(result.getUploadPath());
        deleteByPath(result.getThumbnailPath());
        if (StrUtil.isBlank(result.getUploadPath())) {
            deleteByUrl(result.getUrl());
        }
        if (StrUtil.isBlank(result.getThumbnailPath())) {
            deleteByUrl(result.getThumbnailUrl());
        }
    }

    private void deleteByPath(String path) {
        if (StrUtil.isBlank(path)) {
            return;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        ossManager.deleteObject(normalized);
    }

    private void deleteByUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return;
        }
        String normalized = url.trim();
        if (!normalized.startsWith("http")) {
            deleteByPath(normalized);
            return;
        }
        try {
            java.net.URL parsedUrl = new java.net.URL(normalized);
            String path = parsedUrl.getPath();
            deleteByPath(path);
        } catch (Exception e) {
            log.debug("预览文件删除失败: {}", e.getMessage());
        }
    }

    @Data
    public static class PreviewBatch {
        private Long userId;
        private Date createTime;
        private Map<String, UploadPictureResult> items;
    }
}
