package com.xxr.lingtuthinktank.manager.asset;

import cn.hutool.json.JSONUtil;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.model.vo.asset.PictureEditLockVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 图片编辑锁管理器
 */
@Component
@Slf4j
public class PictureEditLockManager {

    private static final long LOCK_TTL_SECONDS = 120L;
    private static final String LOCK_KEY_PREFIX = "picture:edit:lock:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public PictureEditLockVO buildUnsupportedLockInfo(Long pictureId) {
        PictureEditLockVO lockInfo = buildUnlockedInfo(pictureId, null);
        lockInfo.setSupported(false);
        return lockInfo;
    }

    public PictureEditLockVO getLockInfo(Long pictureId, Long currentUserId) {
        PictureEditLockVO stored = getStoredLock(pictureId);
        if (stored == null) {
            return buildUnlockedInfo(pictureId, currentUserId);
        }
        stored.setSupported(true);
        stored.setLocked(true);
        stored.setLockedByCurrentUser(currentUserId != null && currentUserId.equals(stored.getUserId()));
        return stored;
    }

    public PictureEditLockVO acquireLock(Long pictureId, User user) {
        PictureEditLockVO current = getStoredLock(pictureId);
        if (current != null) {
            if (user != null && user.getId() != null && user.getId().equals(current.getUserId())) {
                return renewLock(pictureId, user);
            }
            current.setSupported(true);
            current.setLocked(true);
            current.setLockedByCurrentUser(false);
            return current;
        }
        PictureEditLockVO lockInfo = buildLockInfo(pictureId, user);
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                buildLockKey(pictureId),
                JSONUtil.toJsonStr(lockInfo),
                LOCK_TTL_SECONDS,
                TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            lockInfo.setSupported(true);
            lockInfo.setLocked(true);
            lockInfo.setLockedByCurrentUser(true);
            return lockInfo;
        }
        return getLockInfo(pictureId, user == null ? null : user.getId());
    }

    public PictureEditLockVO renewLock(Long pictureId, User user) {
        PictureEditLockVO current = getStoredLock(pictureId);
        if (current == null) {
            return acquireLock(pictureId, user);
        }
        if (user == null || user.getId() == null || !user.getId().equals(current.getUserId())) {
            current.setSupported(true);
            current.setLocked(true);
            current.setLockedByCurrentUser(false);
            return current;
        }
        PictureEditLockVO lockInfo = buildLockInfo(pictureId, user);
        stringRedisTemplate.opsForValue().set(
                buildLockKey(pictureId),
                JSONUtil.toJsonStr(lockInfo),
                LOCK_TTL_SECONDS,
                TimeUnit.SECONDS);
        lockInfo.setSupported(true);
        lockInfo.setLocked(true);
        lockInfo.setLockedByCurrentUser(true);
        return lockInfo;
    }

    public void releaseLock(Long pictureId, User user) {
        PictureEditLockVO current = getStoredLock(pictureId);
        if (current == null || user == null || user.getId() == null) {
            return;
        }
        if (user.getId().equals(current.getUserId())) {
            stringRedisTemplate.delete(buildLockKey(pictureId));
        }
    }

    public boolean isLockedByOtherUser(Long pictureId, Long currentUserId) {
        PictureEditLockVO current = getStoredLock(pictureId);
        if (current == null || current.getUserId() == null) {
            return false;
        }
        return currentUserId == null || !current.getUserId().equals(currentUserId);
    }

    private PictureEditLockVO getStoredLock(Long pictureId) {
        if (pictureId == null || pictureId <= 0) {
            return null;
        }
        String raw = stringRedisTemplate.opsForValue().get(buildLockKey(pictureId));
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            PictureEditLockVO lockInfo = JSONUtil.toBean(raw, PictureEditLockVO.class);
            if (lockInfo == null || lockInfo.getExpireAt() == null || lockInfo.getExpireAt() <= System.currentTimeMillis()) {
                stringRedisTemplate.delete(buildLockKey(pictureId));
                return null;
            }
            return lockInfo;
        } catch (Exception e) {
            log.warn("解析图片编辑锁失败, pictureId={}", pictureId, e);
            stringRedisTemplate.delete(buildLockKey(pictureId));
            return null;
        }
    }

    private PictureEditLockVO buildLockInfo(Long pictureId, User user) {
        PictureEditLockVO lockInfo = new PictureEditLockVO();
        lockInfo.setPictureId(pictureId);
        lockInfo.setSupported(true);
        lockInfo.setLocked(true);
        lockInfo.setLockedByCurrentUser(true);
        lockInfo.setUserId(user == null ? null : user.getId());
        lockInfo.setUserName(user == null ? null : user.getUserName());
        lockInfo.setUserAvatar(user == null ? null : user.getUserAvatar());
        lockInfo.setExpireAt(System.currentTimeMillis() + LOCK_TTL_SECONDS * 1000);
        return lockInfo;
    }

    private PictureEditLockVO buildUnlockedInfo(Long pictureId, Long currentUserId) {
        PictureEditLockVO lockInfo = new PictureEditLockVO();
        lockInfo.setPictureId(pictureId);
        lockInfo.setSupported(true);
        lockInfo.setLocked(false);
        lockInfo.setLockedByCurrentUser(false);
        lockInfo.setUserId(currentUserId);
        lockInfo.setExpireAt(null);
        return lockInfo;
    }

    private String buildLockKey(Long pictureId) {
        return LOCK_KEY_PREFIX + pictureId;
    }
}
