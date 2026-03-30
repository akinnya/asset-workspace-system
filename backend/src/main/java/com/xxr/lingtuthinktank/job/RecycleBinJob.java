package com.xxr.lingtuthinktank.job;

import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.mapper.asset.PictureMapper;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 回收站自动清理任务
 */
@Component
@Slf4j
public class RecycleBinJob {

    @Resource
    private PictureService pictureService;

    @Resource
    private OssManager ossManager;

    /**
     * 每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearRecycleBin() {
        log.info("开始清理回收站...");

        // 1. 查询 7 天前逻辑删除的图片
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = calendar.getTime();

        // 我们直接使用 pictureService 的 baseMapper 调用自定义 SQL 绕过逻辑删除过滤
        List<Picture> deleteList = ((PictureMapper) pictureService.getBaseMapper())
                .listAllDeletedOlderThan(sevenDaysAgo);

        if (deleteList == null || deleteList.isEmpty()) {
            log.info("没有需要清理的图片");
            return;
        }

        log.info("发现 {} 张待清理图片", deleteList.size());

        List<Long> deletedIds = new ArrayList<>();
        for (Picture picture : deleteList) {
            try {
                // 2. 清理 OSS 文件
                String url = picture.getUrl();
                String thumbnailUrl = picture.getThumbnailUrl();

                deleteOssFile(url);
                deleteOssFile(thumbnailUrl);

                deletedIds.add(picture.getId());
                log.info("成功清理图片文件: {}", picture.getId());
            } catch (Exception e) {
                log.error("清理图片 {} 失败: {}", picture.getId(), e.getMessage());
            }
        }

        if (!deletedIds.isEmpty()) {
            int removed = ((PictureMapper) pictureService.getBaseMapper()).deleteDeletedByIds(deletedIds);
            log.info("已物理删除回收站记录: {}/{}", removed, deletedIds.size());
        }

        log.info("回收站清理完成");
    }

    private void deleteOssFile(String url) {
        if (url == null || !url.contains("/")) {
            return;
        }
        try {
            // 尝试从 URL 中提取路径
            java.net.URL parsedUrl = new java.net.URL(url);
            String path = parsedUrl.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }

            if (path != null && !path.isEmpty()) {
                ossManager.deleteObject(path);
            }
        } catch (Exception e) {
            log.warn("无法解析或删除 OSS 文件: {}, error: {}", url, e.getMessage());
        }
    }
}
