package com.xxr.lingtuthinktank.manager.asset.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xxr.lingtuthinktank.config.OssClientConfig;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.manager.file.OssManager;
import com.xxr.lingtuthinktank.model.dto.asset.upload.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected OssManager ossManager;

    @Resource
    protected OssClientConfig ossClientConfig;

    /**
     * 模板方法，定义上传流程
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(inputSource);

        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String originSuffix = FileUtil.getSuffix(originFilename);
        String safeSuffix = StrUtil.isBlank(originSuffix) ? "bin" : originSuffix;
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, safeSuffix);
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 3. 创建临时文件
            String tempSuffix = StrUtil.isBlank(originSuffix) ? ".tmp" : "." + originSuffix;
            file = File.createTempFile("upload_", tempSuffix);
            // 处理文件来源（本地或 URL）
            processFile(inputSource, file);

            // 4. 仅使用云存储 (OSS)
            return saveToCloud(file, originFilename, uploadPath);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 5. 清理临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 保存到云存储 (阿里云 OSS)
     */
    private UploadPictureResult saveToCloud(File file, String originFilename, String uploadPath) throws Exception {
        // 上传图片到 OSS
        ossManager.putPictureObject(uploadPath, file);

        // 获取访问 URL
        String url = ossManager.getObjectUrl(uploadPath);

        // 封装返回结果
        return buildCloudResult(originFilename, file, url, uploadPath);
    }

    /**
     * 构建云存储结果
     */
    private UploadPictureResult buildCloudResult(String originFilename, File file, String url, String uploadPath) {
        UploadPictureResult result = new UploadPictureResult();
        String suffix = FileUtil.getSuffix(originFilename);
        result.setPicName(FileUtil.mainName(originFilename));
        result.setPicFormat(suffix);
        result.setPicSize(FileUtil.size(file));
        result.setUrl(url);
        result.setUploadPath(uploadPath);

        if (isImageFormat(suffix)) {
            // 尝试获取图片尺寸
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    result.setPicWidth(width);
                    result.setPicHeight(height);
                    result.setPicScale(NumberUtil.round(width * 1.0 / height, 2).doubleValue());
                }
            } catch (Exception e) {
                log.warn("无法读取图片尺寸: {}", e.getMessage());
                result.setPicWidth(0);
                result.setPicHeight(0);
                result.setPicScale(0.0);
            }

            // 计算主色调
            try {
                String mainColor = com.xxr.lingtuthinktank.utils.ColorUtils.getMainColor(file);
                result.setPicColor(mainColor);
            } catch (Exception e) {
                log.error("计算主色调失败", e);
            }

            // 生成缩略图
            ThumbnailResult thumbnailResult = generateCloudThumbnail(file, uploadPath, suffix);
            if (thumbnailResult != null) {
                result.setThumbnailUrl(thumbnailResult.getThumbnailUrl());
                result.setThumbnailPath(thumbnailResult.getThumbnailPath());
            }
        }

        return result;
    }

    private boolean isImageFormat(String suffix) {
        if (suffix == null) {
            return false;
        }
        String lower = suffix.toLowerCase();
        return Arrays.asList("jpeg", "jpg", "png", "webp", "gif", "bmp", "tiff").contains(lower);
    }

    private ThumbnailResult generateCloudThumbnail(File file, String uploadPath, String suffix) {
        File thumbTemp = null;
        try {
            String thumbPath = buildThumbnailPath(uploadPath, suffix);
            thumbTemp = File.createTempFile("thumb_", "." + suffix);
            Thumbnails.of(file).size(320, 320).keepAspectRatio(true).toFile(thumbTemp);
            ossManager.putPictureObject(thumbPath, thumbTemp);
            String thumbnailUrl = ossManager.getObjectUrl(thumbPath);
            ThumbnailResult result = new ThumbnailResult();
            result.setThumbnailPath(thumbPath);
            result.setThumbnailUrl(thumbnailUrl);
            return result;
        } catch (Exception e) {
            log.warn("缩略图生成失败: {}", e.getMessage());
            return null;
        } finally {
            if (thumbTemp != null && thumbTemp.exists()) {
                if (!thumbTemp.delete()) {
                    log.debug("删除缩略图临时文件失败: {}", thumbTemp.getAbsolutePath());
                }
            }
        }
    }

    private String buildThumbnailPath(String uploadPath, String suffix) {
        int dotIndex = uploadPath.lastIndexOf(".");
        String basePath = dotIndex > -1 ? uploadPath.substring(0, dotIndex) : uploadPath;
        String safeSuffix = suffix != null && !suffix.isEmpty() ? suffix : "jpg";
        return basePath + "_thumb." + safeSuffix;
    }

    @lombok.Data
    private static class ThumbnailResult {
        private String thumbnailUrl;
        private String thumbnailPath;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    // 处理路径方法
    final List<String> MUST_SUFFIX = Arrays.asList(
            "jpeg", "jpg", "png", "webp", "gif", "bmp", "tiff", "svg",
            "mp4", "mov", "avi", "webm",
            "mp3", "wav", "flac", "m4a", "ogg", "aac",
            "pmx", "vrm", "glb", "gltf", "obj",
            "txt", "md", "markdown", "pdf", "doc", "docx", "rtf", "csv", "json", "xml", "yaml", "yml");

    public String processUploadPath(String uploadPath) {
        // 统一转为小写处理（保证后缀大小写不敏感）
        String lowerPath = uploadPath.toLowerCase();
        // 遍历所有合法后缀，检查是否以.后缀结尾
        for (String suffix : MUST_SUFFIX) {
            // 支持带参数链接（如image.png?width=200）
            if (lowerPath.matches(".*\\." + suffix + "($|\\?.*)")) {
                return uploadPath;
            }
        }
        return uploadPath + ".png"; // 默认补全为 PNG（兼容性最佳）
    }
}
