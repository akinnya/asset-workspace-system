package com.xxr.lingtuthinktank.manager.mail;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xxr.lingtuthinktank.exception.BusinessException;
import com.xxr.lingtuthinktank.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码管理器（内存存储）
 */
@Component
public class EmailVerificationManager {

    private static final long CODE_TTL_SECONDS = 10 * 60L;
    private static final long COOLDOWN_SECONDS = 60L;
    private static final int IP_DAILY_LIMIT = 10;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public String createCode(Long userId, String email, String ip) {
        if (userId == null || StrUtil.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String normalizedEmail = email.trim().toLowerCase();
        String safeIp = StrUtil.blankToDefault(ip, "unknown");

        String cooldownKey = buildCooldownKey(normalizedEmail);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送过于频繁");
        }

        enforceIpDailyLimit(safeIp);

        String codeKey = buildCodeKey(userId, normalizedEmail);
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
        return code;
    }

    public String createRegisterCode(String email, String ip) {
        if (StrUtil.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String normalizedEmail = email.trim().toLowerCase();
        String safeIp = StrUtil.blankToDefault(ip, "unknown");

        String cooldownKey = buildCooldownKey(normalizedEmail);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送过于频繁");
        }

        enforceIpDailyLimit(safeIp);

        String codeKey = buildRegisterCodeKey(normalizedEmail);
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
        return code;
    }

    public boolean verifyCode(Long userId, String email, String code) {
        if (userId == null || StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String codeKey = buildCodeKey(userId, normalizedEmail);
        String stored = stringRedisTemplate.opsForValue().get(codeKey);
        if (!code.equals(stored)) {
            return false;
        }
        stringRedisTemplate.delete(codeKey);
        return true;
    }

    public boolean verifyRegisterCode(String email, String code) {
        if (StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String codeKey = buildRegisterCodeKey(normalizedEmail);
        String stored = stringRedisTemplate.opsForValue().get(codeKey);
        if (!code.equals(stored)) {
            return false;
        }
        stringRedisTemplate.delete(codeKey);
        return true;
    }

    public void clearCode(Long userId, String email) {
        if (userId == null || StrUtil.isBlank(email)) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        stringRedisTemplate.delete(buildCodeKey(userId, normalizedEmail));
        stringRedisTemplate.delete(buildCooldownKey(normalizedEmail));
    }

    public void clearRegisterCode(String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        stringRedisTemplate.delete(buildRegisterCodeKey(normalizedEmail));
        stringRedisTemplate.delete(buildCooldownKey(normalizedEmail));
    }

    private void enforceIpDailyLimit(String ip) {
        String dateKey = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.BASIC_ISO_DATE);
        String ipKey = buildIpDailyKey(ip, dateKey);
        Long count = stringRedisTemplate.opsForValue().increment(ipKey);
        if (count != null && count == 1L) {
            long secondsToEndOfDay = calcSecondsToEndOfDay();
            stringRedisTemplate.expire(ipKey, secondsToEndOfDay, TimeUnit.SECONDS);
        }
        if (count != null && count > IP_DAILY_LIMIT) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送次数已达上限");
        }
    }

    private long calcSecondsToEndOfDay() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        LocalDateTime end = now.toLocalDate().plusDays(1).atStartOfDay();
        long seconds = Duration.between(now, end).getSeconds();
        return Math.max(seconds, 60);
    }

    private String buildCodeKey(Long userId, String email) {
        return String.format("email:code:%s:%s", userId, email);
    }

    private String buildRegisterCodeKey(String email) {
        return String.format("email:register:code:%s", email);
    }

    private String buildCooldownKey(String email) {
        return String.format("email:cooldown:%s", email);
    }

    private String buildIpDailyKey(String ip, String dateKey) {
        return String.format("email:ip:%s:%s", ip, dateKey);
    }
}
