package com.xxr.lingtuthinktank.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxr.lingtuthinktank.model.entity.asset.PictureLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 图片点赞数据库操作
 */
public interface PictureLikeMapper extends BaseMapper<PictureLike> {
    /**
     * 统计用户所有作品的获赞总数
     */
    @Select("SELECT COUNT(*) FROM picture_like pl JOIN picture p ON pl.pictureId = p.id WHERE p.userId = #{userId} AND p.isDelete = 0")
    Long countLikesByUserId(@Param("userId") Long userId);
}
