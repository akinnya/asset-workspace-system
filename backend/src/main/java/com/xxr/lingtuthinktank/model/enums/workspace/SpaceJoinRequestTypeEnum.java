package com.xxr.lingtuthinktank.model.enums.workspace;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 空间协作请求类型枚举
 */
@Getter
public enum SpaceJoinRequestTypeEnum {
    JOIN("加入申请", 0),
    INVITE("团队邀请", 1);

    private final String text;
    private final int value;

    SpaceJoinRequestTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceJoinRequestTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceJoinRequestTypeEnum typeEnum : SpaceJoinRequestTypeEnum.values()) {
            if (typeEnum.value == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
