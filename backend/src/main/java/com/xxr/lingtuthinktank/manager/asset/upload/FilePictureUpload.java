package com.xxr.lingtuthinktank.manager.asset.upload;

import cn.hutool.core.io.FileUtil;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class FilePictureUpload extends PictureUploadTemplate {

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        // 扩容到 100M 以支持 3D 模型
        ThrowUtils.throwIf(fileSize > 100 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 100M");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        String normalizedSuffix = fileSuffix == null ? "" : fileSuffix.toLowerCase();
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList(
                "jpeg", "jpg", "png", "webp", "gif",
                "mp4", "mov", "avi", "webm",
                "mp3", "wav", "flac", "m4a", "ogg", "aac",
                "pmx", "vrm", "glb", "gltf", "obj",
                "txt", "md", "markdown", "pdf", "doc", "docx", "rtf", "csv", "json", "xml", "yaml", "yml");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(normalizedSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    // 上传流程统一走父类（OSS）
}
