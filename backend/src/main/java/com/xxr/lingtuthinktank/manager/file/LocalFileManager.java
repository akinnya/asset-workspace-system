package com.xxr.lingtuthinktank.manager.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.model.dto.asset.upload.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
@Slf4j
public class LocalFileManager {

    @Value("${local.file.storage.path:/home/app/images}")
    private String storagePath;

    @PostConstruct
    public void init() {
        // 确保存储目录存在
        FileUtil.mkdir(storagePath);
    }

    /**
     * 上传图片到本地存储
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return 上传结果
     */
    public UploadPictureResult uploadPictureToLocal(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);

        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String originFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originFilename));

        // 构造完整存储路径
        String fullPath = String.format("%s/%s/%s", storagePath, uploadPathPrefix, uploadFilename);
        File file = null;

        try {
            // 创建目录
            FileUtil.mkdir(FileUtil.getParent(fullPath, 1));

            // 保存文件
            file = new File(fullPath);
            multipartFile.transferTo(file);

            // 获取图片信息
            BufferedImage image = Thumbnails.of(file).scale(1).asBufferedImage();
            int picWidth = image.getWidth();
            int picHeight = image.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(FileUtil.getSuffix(originFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));

            // 构造访问URL (根据实际部署情况调整)
            String relativePath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
            uploadPictureResult.setUrl("/api/images/" + relativePath);
            uploadPictureResult.setUploadPath(relativePath);

            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到本地存储失败", e);
            // 删除已创建的文件
            if (file != null && file.exists()) {
                file.delete();
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile multipart 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 1. 校验文件大小 (限制为2MB)
        long fileSize = multipartFile.getSize();
        final long TWO_MB = 2 * 1024 * 1024L;
        if (fileSize > TWO_MB) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        }

        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename()).toLowerCase();
        // 允许上传的文件后缀
        String[] allowFormats = { "jpeg", "jpg", "png", "webp", "gif" };
        boolean isValidFormat = false;
        for (String format : allowFormats) {
            if (format.equals(fileSuffix)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }
}
