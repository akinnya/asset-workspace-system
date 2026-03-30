package com.xxr.lingtuthinktank.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 字段自动填充处理器
 * <p>
 * 由于 H2 不支持 MySQL 的 {@code on update CURRENT_TIMESTAMP} 语法，这里在更新时自动刷新 {@code updateTime}，
 * 保持“更新时间随更新操作变化”的语义一致。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时补齐更新时间，避免业务层遗漏
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时强制刷新更新时间
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}

