package com.xxr.lingtuthinktank.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 40655
 * @description 针对表【picture(图片)】的数据库操作Mapper
 * @createDate 2025-06-08 22:23:05
 * @Entity generator.domain.Picture
 */
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 恢复图片
     */
    @Update("UPDATE picture SET isDelete = 0 WHERE id = #{id}")
    void recoverPicture(Long id);

    /**
     * 查询已删除的图片
     */
    @Select("SELECT * FROM picture WHERE isDelete = 1 AND userId = #{userId}")
    List<Picture> listDeletedPicture(@org.apache.ibatis.annotations.Param("userId") Long userId);

    /**
     * 根据 id 列表查询已删除图片（忽略用户）
     */
    @Select("<script>SELECT * FROM picture WHERE isDelete = 1 AND id IN " +
            "<foreach collection='idList' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Picture> listDeletedByIds(@Param("idList") List<Long> idList);

    /**
     * 根据 id 列表查询用户已删除图片
     */
    @Select("<script>SELECT * FROM picture WHERE isDelete = 1 AND userId = #{userId} AND id IN " +
            "<foreach collection='idList' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Picture> listDeletedByIdsForUser(@Param("idList") List<Long> idList, @Param("userId") Long userId);

    /**
     * 物理删除已删除图片
     */
    @Delete("<script>DELETE FROM picture WHERE id IN " +
            "<foreach collection='idList' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteDeletedByIds(@Param("idList") List<Long> idList);

    /**
     * 查询所有待清理的已删除图片（逻辑删除且超过 7 天）
     */
    @Select("SELECT * FROM picture WHERE isDelete = 1 AND updateTime < #{sevenDaysAgo}")
    List<Picture> listAllDeletedOlderThan(java.util.Date sevenDaysAgo);
}
