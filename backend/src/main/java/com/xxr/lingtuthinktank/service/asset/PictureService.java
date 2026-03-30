package com.xxr.lingtuthinktank.service.asset;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxr.lingtuthinktank.model.dto.asset.batch.PictureBatchConfirmRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.PictureQueryRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadByBatchRequest;
import com.xxr.lingtuthinktank.model.dto.asset.upload.PictureUploadRequest;
import com.xxr.lingtuthinktank.model.dto.asset.query.SearchPictureByColorRequest;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.asset.PictureBatchPreviewVO;
import com.xxr.lingtuthinktank.model.vo.asset.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 40655
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-06-08 22:23:05
 */
public interface PictureService extends IService<Picture> {

        /**
         * 上传图片
         *
         * @param pictureUploadRequest
         * @param loginUser
         * @return
         */
        PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

        QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

        PictureVO getPictureVO(Picture picture, HttpServletRequest request);

        Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

        void validPicture(Picture picture);

        void fillReviewParams(Picture picture, User loginUser);

        /**
         * 批量抓取图片并生成预览
         *
         * @param pictureUploadByBatchRequest
         * @param loginUser
         * @return 预览批次
         */
        PictureBatchPreviewVO previewUploadPictureByBatch(
                        PictureUploadByBatchRequest pictureUploadByBatchRequest,
                        User loginUser);

        /**
         * 确认批量预览入库
         *
         * @param pictureBatchConfirmRequest
         * @param loginUser
         * @return 成功入库的图片列表
         */
        List<PictureVO> confirmUploadPictureByBatch(
                        PictureBatchConfirmRequest pictureBatchConfirmRequest,
                        User loginUser);

        /**
         * 放弃预览批次
         *
         * @param batchId
         * @param loginUser
         */
        void discardBatchPreview(String batchId, User loginUser);

        /**
         * 按颜色搜索图片
         *
         * @param searchPictureByColorRequest 搜索请求
         * @param request                     HTTP请求
         * @return 图片列表
         */
        List<PictureVO> searchPictureByColor(SearchPictureByColorRequest searchPictureByColorRequest,
                        HttpServletRequest request);

        /**
         * 以图搜图
         *
         * @param pictureId 参考图片id
         * @param request   HTTP请求
         * @return 相似图片列表
         */
    List<PictureVO> searchPictureByPicture(Long pictureId, HttpServletRequest request);

    /**
     * 通过文件搜索相似图片（不入库）
     */
    List<PictureVO> searchPictureByFile(org.springframework.web.multipart.MultipartFile file, HttpServletRequest request);

        /**
         * 获取相关推荐图片
         *
         * @param pictureQueryRequest
         * @param request
         * @return
         */
        List<PictureVO> listRelatedPictures(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

        /**
         * 恢复被删除的图片
         */
        void recoverPicture(Long pictureId, User loginUser);

        /**
         * 查询被删除的图片（回收站）
         */
    List<PictureVO> listDeletedPictures(User loginUser);

    /**
     * 清空回收站（物理删除）
     */
    void clearRecycleBin(List<Long> idList, User loginUser);

        /**
         * 增加浏览量 (原子性增加)
         */
        void addViewCount(Long pictureId);
}
