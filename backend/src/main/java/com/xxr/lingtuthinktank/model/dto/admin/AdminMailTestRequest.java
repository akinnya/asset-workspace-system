package com.xxr.lingtuthinktank.model.dto.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员测试邮件请求
 */
@Data
public class AdminMailTestRequest implements Serializable {

    /**
     * 测试收件邮箱
     */
    private String email;

    private static final long serialVersionUID = 1L;
}
