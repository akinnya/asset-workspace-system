package com.xxr.lingtuthinktank.annotation;

import com.xxr.lingtuthinktank.model.enums.workspace.SpaceUserRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 空间权限校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpaceAuthCheck {

    /**
     * 必须具备的空间角色（任一满足即可）
     */
    SpaceUserRoleEnum[] mustRole() default {};

    /**
     * 是否只允许空间创建者
     */
    boolean onlyOwner() default false;
}
