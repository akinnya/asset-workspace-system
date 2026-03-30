package com.xxr.lingtuthinktank.model.enums.workspace;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间用户角色枚举
 */
@Getter
public enum SpaceUserRoleEnum {

    VIEWER("浏览者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    SpaceUserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceUserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceUserRoleEnum spaceUserRoleEnum : SpaceUserRoleEnum.values()) {
            if (spaceUserRoleEnum.value.equals(value)) {
                return spaceUserRoleEnum;
            }
        }
        return null;
    }

    public static List<String> getAllValues() {
        return Arrays.stream(SpaceUserRoleEnum.values())
                .map(SpaceUserRoleEnum::getValue)
                .collect(Collectors.toList());
    }
}
