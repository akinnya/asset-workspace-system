package com.xxr.lingtuthinktank.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.xxr.lingtuthinktank.annotation.AuthCheck;
import com.xxr.lingtuthinktank.common.BaseResponse;
import com.xxr.lingtuthinktank.common.ResultUtils;
import com.xxr.lingtuthinktank.constant.UserConstant;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import com.xxr.lingtuthinktank.exception.ThrowUtils;
import com.xxr.lingtuthinktank.model.dto.admin.AdminMailTestRequest;
import com.xxr.lingtuthinktank.model.dto.admin.SensitiveOperationLogQueryRequest;
import com.xxr.lingtuthinktank.model.entity.admin.SensitiveOperationLog;
import com.xxr.lingtuthinktank.model.entity.asset.Picture;
import com.xxr.lingtuthinktank.model.entity.asset.PictureEditLog;
import com.xxr.lingtuthinktank.model.entity.workspace.Space;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.admin.AdminFileTypeDistributionVO;
import com.xxr.lingtuthinktank.model.vo.admin.AdminSpaceActivityVO;
import com.xxr.lingtuthinktank.model.vo.admin.AdminStatsVO;
import com.xxr.lingtuthinktank.model.vo.admin.AdminTrendPointVO;
import com.xxr.lingtuthinktank.model.vo.admin.MailHealthStatusVO;
import com.xxr.lingtuthinktank.service.admin.SensitiveOperationLogService;
import com.xxr.lingtuthinktank.service.comment.CommentService;
import com.xxr.lingtuthinktank.service.mail.EmailService;
import com.xxr.lingtuthinktank.service.asset.PictureEditLogService;
import com.xxr.lingtuthinktank.service.asset.PictureService;
import com.xxr.lingtuthinktank.service.workspace.SpaceService;
import com.xxr.lingtuthinktank.service.user.UserService;
import com.xxr.lingtuthinktank.mapper.asset.PictureLikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 管理后台接口
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

        private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

        @Resource
        private UserService userService;

        @Resource
        private PictureService pictureService;

        @Resource
        private CommentService commentService;

        @Resource
        private PictureLikeMapper pictureLikeMapper;

        @Resource
        private EmailService emailService;

        @Resource
        private SensitiveOperationLogService sensitiveOperationLogService;

        @Resource
        private PictureEditLogService pictureEditLogService;

        @Resource
        private SpaceService spaceService;

        /**
         * 获取后台统计数据（仅管理员）
         */
        @GetMapping("/stats")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<AdminStatsVO> getAdminStats(HttpServletRequest request) {
                AdminStatsVO stats = new AdminStatsVO();

                // 用户统计
                stats.setTotalUsers(userService.count());
                stats.setTodayNewUsers(countByDateRange(User.class, 0));
                stats.setWeekNewUsers(countByDateRange(User.class, 7));
                stats.setMonthNewUsers(countByDateRange(User.class, 30));

                // 内容统计
                stats.setTotalPictures(pictureService.count(
                                new QueryWrapper<Picture>().eq("isDelete", 0)));
                stats.setTodayNewPictures(pictureService.count(
                                new QueryWrapper<Picture>()
                                                .eq("isDelete", 0)
                                                .ge("createTime", getDateBefore(0))));
                stats.setTotalComments(commentService.count());
                stats.setTotalLikes(pictureLikeMapper.selectCount(null));

                // 审核统计
                stats.setPendingReview(pictureService.count(
                                new QueryWrapper<Picture>()
                                                .eq("isDelete", 0)
                                                .eq("reviewStatus", 0)));
                stats.setPassedReview(pictureService.count(
                                new QueryWrapper<Picture>()
                                                .eq("isDelete", 0)
                                                .eq("reviewStatus", 1)));
                stats.setRejectedReview(pictureService.count(
                                new QueryWrapper<Picture>()
                                                .eq("isDelete", 0)
                                                .eq("reviewStatus", 2)));

                // 存储统计（简化：统计所有图片大小）
                Long totalSize = pictureService.getBaseMapper().selectObjs(
                                new QueryWrapper<Picture>()
                                                .select("COALESCE(SUM(picSize), 0)")
                                                .eq("isDelete", 0))
                                .stream().findFirst().map(o -> ((Number) o).longValue()).orElse(0L);
                stats.setTotalStorageUsed(totalSize);
                stats.setUserTrend7d(buildUserTrend(7));
                stats.setUserTrend30d(buildUserTrend(30));
                stats.setPictureTrend7d(buildPictureTrend(7));
                stats.setPictureTrend30d(buildPictureTrend(30));
                stats.setFileTypeDistribution(buildFileTypeDistribution());
                stats.setSpaceActivityRank(buildSpaceActivityRank(10));

                return ResultUtils.success(stats);
        }

        /**
         * 获取邮件服务健康状态（仅管理员）
         */
        @GetMapping("/mail/health")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<MailHealthStatusVO> getMailHealthStatus() {
                return ResultUtils.success(emailService.getMailHealthStatus());
        }

        /**
         * 发送测试邮件（仅管理员）
         */
        @PostMapping("/mail/test")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Boolean> sendMailTest(@RequestBody(required = false) AdminMailTestRequest request,
                        HttpServletRequest httpRequest) {
                User loginUser = userService.getLoginUser(httpRequest);
                String targetEmail = request != null ? request.getEmail() : null;
                if (StrUtil.isBlank(targetEmail)) {
                        targetEmail = loginUser.getUserAccount();
                }
                ThrowUtils.throwIf(StrUtil.isBlank(targetEmail), ErrorCode.PARAMS_ERROR, "请填写测试邮箱");
                ThrowUtils.throwIf(!Validator.isEmail(targetEmail), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
                emailService.sendTestMail(targetEmail.trim());
                return ResultUtils.success(true);
        }

        /**
         * 分页获取敏感操作日志（仅管理员）
         */
        @PostMapping("/log/list/page")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Page<SensitiveOperationLog>> listSensitiveOperationLogs(
                        @RequestBody SensitiveOperationLogQueryRequest queryRequest) {
                ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
                long current = queryRequest.getCurrent();
                long pageSize = queryRequest.getPageSize();
                Page<SensitiveOperationLog> logPage = sensitiveOperationLogService.page(
                                new Page<>(current, pageSize),
                                sensitiveOperationLogService.getQueryWrapper(queryRequest));
                return ResultUtils.success(logPage);
        }

        private long countByDateRange(Class<?> entityClass, int daysAgo) {
                Date startDate = getDateBefore(daysAgo);
                if (entityClass == User.class) {
                        return userService.count(
                                        new QueryWrapper<User>().ge("createTime", startDate));
                }
                return 0;
        }

        private Date getDateBefore(int daysAgo) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
        }

        private Date getStartOfPastDays(int days) {
                int safeDays = Math.max(days, 1);
                return Date.from(LocalDate.now(ZONE_ID)
                                .minusDays(safeDays - 1L)
                                .atStartOfDay(ZONE_ID)
                                .toInstant());
        }

        private List<AdminTrendPointVO> buildUserTrend(int days) {
                List<Date> createTimes = userService.list(new QueryWrapper<User>()
                                .select("id", "createTime")
                                .ge("createTime", getStartOfPastDays(days)))
                                .stream()
                                .map(User::getCreateTime)
                                .collect(Collectors.toList());
                return buildTrend(createTimes, days);
        }

        private List<AdminTrendPointVO> buildPictureTrend(int days) {
                List<Date> createTimes = pictureService.list(new QueryWrapper<Picture>()
                                .select("id", "createTime")
                                .eq("isDelete", 0)
                                .ge("createTime", getStartOfPastDays(days)))
                                .stream()
                                .map(Picture::getCreateTime)
                                .collect(Collectors.toList());
                return buildTrend(createTimes, days);
        }

        private List<AdminTrendPointVO> buildTrend(List<Date> sourceDates, int days) {
                int safeDays = Math.max(days, 1);
                LocalDate startDate = LocalDate.now(ZONE_ID).minusDays(safeDays - 1L);
                Map<LocalDate, Long> countMap = sourceDates == null ? Collections.emptyMap() : sourceDates.stream()
                                .filter(Objects::nonNull)
                                .map(date -> date.toInstant().atZone(ZONE_ID).toLocalDate())
                                .filter(date -> !date.isBefore(startDate))
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                List<AdminTrendPointVO> result = new ArrayList<>();
                for (int i = 0; i < safeDays; i++) {
                        LocalDate current = startDate.plusDays(i);
                        AdminTrendPointVO point = new AdminTrendPointVO();
                        point.setLabel(current.format(DateTimeFormatter.ISO_LOCAL_DATE));
                        point.setValue(countMap.getOrDefault(current, 0L));
                        result.add(point);
                }
                return result;
        }

        private List<AdminFileTypeDistributionVO> buildFileTypeDistribution() {
                Map<String, Long> countMap = new LinkedHashMap<>();
                Arrays.asList("image", "video", "model", "audio", "text", "other")
                                .forEach(type -> countMap.put(type, 0L));
                pictureService.list(new QueryWrapper<Picture>()
                                .select("id", "picFormat")
                                .eq("isDelete", 0))
                                .forEach(picture -> {
                                        String type = mapFileType(picture.getPicFormat());
                                        countMap.put(type, countMap.getOrDefault(type, 0L) + 1);
                                });
                return countMap.entrySet().stream().map(entry -> {
                        AdminFileTypeDistributionVO item = new AdminFileTypeDistributionVO();
                        item.setType(entry.getKey());
                        item.setCount(entry.getValue());
                        return item;
                }).collect(Collectors.toList());
        }

        private List<AdminSpaceActivityVO> buildSpaceActivityRank(int limit) {
                Date recentStart = getStartOfPastDays(30);
                Map<Long, Long> uploadCountMap = pictureService.list(new QueryWrapper<Picture>()
                                .select("id", "spaceId")
                                .eq("isDelete", 0)
                                .isNotNull("spaceId")
                                .ge("createTime", recentStart))
                                .stream()
                                .map(Picture::getSpaceId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                Map<Long, Long> editCountMap = pictureEditLogService.list(new QueryWrapper<PictureEditLog>()
                                .select("id", "spaceId")
                                .isNotNull("spaceId")
                                .ge("createTime", recentStart))
                                .stream()
                                .map(PictureEditLog::getSpaceId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                Set<Long> spaceIdSet = new java.util.HashSet<>();
                spaceIdSet.addAll(uploadCountMap.keySet());
                spaceIdSet.addAll(editCountMap.keySet());
                if (spaceIdSet.isEmpty()) {
                        return Collections.emptyList();
                }
                Map<Long, Space> spaceMap = spaceService.listByIds(spaceIdSet).stream()
                                .collect(Collectors.toMap(Space::getId, Function.identity(), (left, right) -> left));
                return spaceIdSet.stream()
                                .map(spaceId -> {
                                        long uploadCount = uploadCountMap.getOrDefault(spaceId, 0L);
                                        long editCount = editCountMap.getOrDefault(spaceId, 0L);
                                        Space space = spaceMap.get(spaceId);
                                        AdminSpaceActivityVO item = new AdminSpaceActivityVO();
                                        item.setSpaceId(spaceId);
                                        item.setSpaceName(space == null || StrUtil.isBlank(space.getSpaceName())
                                                        ? "空间#" + spaceId
                                                        : space.getSpaceName());
                                        item.setUploadCount(uploadCount);
                                        item.setEditCount(editCount);
                                        item.setActivityScore(uploadCount + editCount);
                                        return item;
                                })
                                .sorted((left, right) -> Long.compare(
                                                right.getActivityScore() == null ? 0L : right.getActivityScore(),
                                                left.getActivityScore() == null ? 0L : left.getActivityScore()))
                                .limit(Math.max(limit, 1))
                                .collect(Collectors.toList());
        }

        private String mapFileType(String format) {
                String normalized = StrUtil.blankToDefault(format, "").trim().toLowerCase();
                if (Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "tif", "tiff").contains(normalized)) {
                        return "image";
                }
                if (Arrays.asList("mp4", "mov", "avi", "mkv", "webm").contains(normalized)) {
                        return "video";
                }
                if (Arrays.asList("glb", "gltf", "vrm", "pmx", "pmd", "obj", "fbx").contains(normalized)) {
                        return "model";
                }
                if (Arrays.asList("mp3", "wav", "flac", "m4a", "ogg", "aac").contains(normalized)) {
                        return "audio";
                }
                if (Arrays.asList("txt", "md", "markdown", "pdf", "doc", "docx", "rtf", "csv", "json", "xml", "yaml", "yml").contains(normalized)) {
                        return "text";
                }
                return "other";
        }
}
