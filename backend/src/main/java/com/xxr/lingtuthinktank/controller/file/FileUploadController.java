package com.xxr.lingtuthinktank.controller.file;

import com.xxr.lingtuthinktank.annotation.AuthCheck;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.manager.asset.upload.FilePictureUpload;
import com.xxr.lingtuthinktank.model.dto.asset.upload.UploadPictureResult;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.user.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;

    /**
     * 上传文件到 OSS（不入库，仅返回 URL）
     */
    @PostMapping("/upload")
    @AuthCheck
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file,
            @RequestParam(value = "biz", required = false) String biz,
            HttpServletRequest request) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);
        String prefix = buildUploadPrefix(loginUser.getId(), biz);
        UploadPictureResult uploadResult = filePictureUpload.uploadPicture(file, prefix);
        return ResultUtils.success(uploadResult != null ? uploadResult.getUrl() : null);
    }

    private String buildUploadPrefix(Long userId, String biz) {
        String safeBiz = normalizeBiz(biz);
        return String.format("public/%s/assets/%s", userId, safeBiz);
    }

    private String normalizeBiz(String biz) {
        if (biz == null) {
            return "misc";
        }
        String value = biz.trim().toLowerCase();
        if (value.isEmpty()) {
            return "misc";
        }
        if (!value.matches("[a-z0-9_-]+")) {
            return "misc";
        }
        return value;
    }
}
