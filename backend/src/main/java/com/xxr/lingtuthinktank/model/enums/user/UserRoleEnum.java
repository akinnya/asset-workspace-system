package com.xxr.lingtuthinktank.model.enums.user;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("普通用户", "user"),
    ADMIN("管理员", "admin"),
    BAN("被封禁", "ban");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnums : UserRoleEnum.values()) {
            if (userRoleEnums.value.equals(value)) {
                return userRoleEnums;
            }
        }
        return null;
    }
}
