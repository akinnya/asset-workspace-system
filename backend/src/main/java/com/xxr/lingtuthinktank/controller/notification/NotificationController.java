package com.xxr.lingtuthinktank.controller.notification;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.model.entity.notification.Notification;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.notification.NotificationService;
import com.xxr.lingtuthinktank.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 通知接口
 */
@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    /**
     * 获取当前用户通知列表
     */
    @PostMapping("/list")
    public BaseResponse<Page<Notification>> listNotifications(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId())
                .orderByDesc("createTime");
        Page<Notification> page = notificationService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long count = notificationService.count(new QueryWrapper<Notification>()
                .eq("userId", loginUser.getId())
                .eq("isRead", 0));
        return ResultUtils.success(count);
    }

    /**
     * 标记为已读
     */
    @PostMapping("/read")
    public BaseResponse<Boolean> markAsRead(@RequestBody List<Long> ids, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = notificationService.update(new UpdateWrapper<Notification>()
                .set("isRead", 1)
                .eq("userId", loginUser.getId())
                .in("id", ids));
        return ResultUtils.success(result);
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/read/all")
    public BaseResponse<Boolean> markAllAsRead(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = notificationService.update(new UpdateWrapper<Notification>()
                .set("isRead", 1)
                .eq("userId", loginUser.getId())
                .eq("isRead", 0));
        return ResultUtils.success(result);
    }
}
