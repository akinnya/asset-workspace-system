package com.xxr.lingtuthinktank.model.enums.workspace;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 空间加入申请状态枚举
 */
@Getter
public enum SpaceJoinRequestStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    SpaceJoinRequestStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static SpaceJoinRequestStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceJoinRequestStatusEnum statusEnum : SpaceJoinRequestStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
