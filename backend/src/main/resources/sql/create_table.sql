-- ================================================
-- asset-workspace-system 数据库初始化脚本
-- 兼容 H2 (开发) 和 MySQL (生产)
-- ================================================

-- 设置字符集 (MySQL only, H2 ignores)
SET NAMES utf8mb4;

-- ================================================
-- 1. 用户表
-- ================================================
CREATE TABLE IF NOT EXISTS user (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    userAccount  VARCHAR(256)                           NOT NULL,
    userPassword VARCHAR(512)                           NOT NULL,
    userName     VARCHAR(256)                           NULL,
    userAvatar   VARCHAR(1024)                          NULL,
    userProfile  VARCHAR(512)                           NULL,
    userRole     VARCHAR(256) DEFAULT 'user'            NOT NULL,
    editTime     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    createTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete     TINYINT      DEFAULT 0                 NOT NULL
);
CREATE UNIQUE INDEX uk_userAccount ON user(userAccount);
CREATE INDEX idx_userName ON user(userName);

-- ================================================
-- 2. 空间表
-- ================================================
CREATE TABLE IF NOT EXISTS space (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    spaceName   VARCHAR(128)                       NOT NULL,
    spaceLevel  INT          DEFAULT 0             NOT NULL,
    spaceType   INT          DEFAULT 0             NOT NULL,
    maxSize     BIGINT                             NULL,
    maxCount    BIGINT                             NULL,
    totalSize   BIGINT       DEFAULT 0             NOT NULL,
    totalCount  BIGINT       DEFAULT 0             NOT NULL,
    userId      BIGINT                             NOT NULL,
    introduction TEXT                             NULL,
    coverUrl     VARCHAR(512)                      NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    editTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL
);
CREATE INDEX idx_spaceName ON space(spaceName);
CREATE INDEX idx_space_userId ON space(userId);
CREATE INDEX idx_spaceType ON space(spaceType);
CREATE INDEX idx_spaceLevel ON space(spaceLevel);

-- ================================================
-- 3. 空间用户关联表
-- ================================================
CREATE TABLE IF NOT EXISTS space_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    spaceId     BIGINT                             NOT NULL,
    userId      BIGINT                             NOT NULL,
    spaceRole   VARCHAR(64) DEFAULT 'viewer'       NOT NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX uk_spaceId_userId ON space_user(spaceId, userId);
CREATE INDEX idx_space_user_spaceId ON space_user(spaceId);
CREATE INDEX idx_space_user_userId ON space_user(userId);

-- ================================================
-- 3.1 空间加入申请表
-- ================================================
CREATE TABLE IF NOT EXISTS space_join_request (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    spaceId       BIGINT                             NOT NULL,
    userId        BIGINT                             NOT NULL,
    requestType   TINYINT  DEFAULT 0                 NOT NULL COMMENT '0-加入申请 1-团队邀请',
    inviterId     BIGINT                             NULL,
    status        TINYINT  DEFAULT 0                 NOT NULL COMMENT '0-待审核 1-通过 2-拒绝',
    reviewMessage VARCHAR(512)                      NULL,
    reviewerId    BIGINT                             NULL,
    reviewTime    DATETIME                           NULL,
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_space_join_spaceId ON space_join_request(spaceId);
CREATE INDEX idx_space_join_userId ON space_join_request(userId);
CREATE INDEX idx_space_join_type ON space_join_request(requestType);
CREATE INDEX idx_space_join_status ON space_join_request(status);

-- ================================================
-- 4. 图片表
-- ================================================
CREATE TABLE IF NOT EXISTS picture (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    url           VARCHAR(512)                       NOT NULL,
    name          VARCHAR(128)                       NOT NULL,
    introduction  VARCHAR(512)                       NULL,
    category      VARCHAR(64)                        NULL,
    tags          VARCHAR(512)                       NULL,
    picSize       BIGINT                             NULL,
    picWidth      INT                                NULL,
    picHeight     INT                                NULL,
    picScale      DOUBLE                             NULL,
    picFormat     VARCHAR(32)                        NULL,
    picColor      VARCHAR(16)                        NULL,
    thumbnailUrl  VARCHAR(512)                       NULL,
    shareCode     VARCHAR(32)                        NULL,
    pHash         VARCHAR(128)                       NULL,
    picColorSpace VARCHAR(64)                        NULL,
    userId        BIGINT                             NOT NULL,
    spaceId       BIGINT                             NULL,
    status        INT          DEFAULT 3             NOT NULL,
    reviewStatus  INT          DEFAULT 0             NOT NULL,
    reviewMessage VARCHAR(512)                       NULL,
    reviewerId    BIGINT                             NULL,
    reviewTime    DATETIME                           NULL,
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    editTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL,
    viewCount     BIGINT   DEFAULT 0                 NOT NULL COMMENT '浏览量'
);
CREATE INDEX idx_picture_name ON picture(name);
CREATE INDEX idx_picture_category ON picture(category);
CREATE INDEX idx_picture_userId ON picture(userId);
CREATE INDEX idx_picture_spaceId ON picture(spaceId);
CREATE INDEX idx_picture_reviewStatus ON picture(reviewStatus);
CREATE INDEX idx_picture_picColor ON picture(picColor);

-- ================================================
-- 5. 图片协作编辑日志表
-- ================================================
CREATE TABLE IF NOT EXISTS picture_edit_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    pictureId     BIGINT                             NOT NULL,
    spaceId       BIGINT                             NOT NULL,
    operatorUserId BIGINT                            NOT NULL,
    actionType    VARCHAR(64)                        NOT NULL,
    changeSummary VARCHAR(512)                       NULL,
    beforeSummary VARCHAR(1024)                      NULL,
    afterSummary  VARCHAR(1024)                      NULL,
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL
);
CREATE INDEX idx_picture_edit_log_pictureId ON picture_edit_log(pictureId);
CREATE INDEX idx_picture_edit_log_spaceId ON picture_edit_log(spaceId);
CREATE INDEX idx_picture_edit_log_operatorUserId ON picture_edit_log(operatorUserId);

-- ================================================
-- 6. 评论表
-- ================================================
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT                               NOT NULL,
    pictureId   BIGINT                             NOT NULL,
    userId      BIGINT                             NOT NULL,
    parentId    BIGINT                             NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    editTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL
);
CREATE INDEX idx_comment_pictureId ON comment(pictureId);
CREATE INDEX idx_comment_userId ON comment(userId);

-- ================================================
-- 7. 图片点赞表
-- ================================================
CREATE TABLE IF NOT EXISTS picture_like (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    pictureId   BIGINT                             NOT NULL,
    userId      BIGINT                             NOT NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX uk_picture_like ON picture_like(pictureId, userId);
CREATE INDEX idx_picture_like_pictureId ON picture_like(pictureId);
CREATE INDEX idx_picture_like_userId ON picture_like(userId);

-- ================================================
-- 8. 用户关注表
-- ================================================
CREATE TABLE IF NOT EXISTS user_follows (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    followerId   BIGINT                             NOT NULL,
    followingId  BIGINT                             NOT NULL,
    createTime   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete     TINYINT  DEFAULT 0                 NOT NULL
);
CREATE UNIQUE INDEX uk_follow ON user_follows(followerId, followingId);
CREATE INDEX idx_follower ON user_follows(followerId);
CREATE INDEX idx_following ON user_follows(followingId);

-- ================================================
-- 9. 图片附件表
-- ================================================
CREATE TABLE IF NOT EXISTS picture_attachment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    pictureId   BIGINT                             NOT NULL,
    fileUrl     VARCHAR(1024)                      NOT NULL,
    fileName    VARCHAR(128)                       NULL,
    fileType    VARCHAR(32)                        NULL,
    fileSize    BIGINT                             NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete    TINYINT  DEFAULT 0                 NOT NULL
);
CREATE INDEX idx_attachment_pictureId ON picture_attachment(pictureId);

-- ================================================
-- 10. 敏感操作日志表
-- ================================================
CREATE TABLE IF NOT EXISTS sensitive_operation_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId        BIGINT                             NULL,
    userName      VARCHAR(256)                       NULL,
    ip            VARCHAR(64)                        NULL,
    requestPath   VARCHAR(512)                       NULL,
    httpMethod    VARCHAR(16)                        NULL,
    methodName    VARCHAR(256)                       NULL,
    description   VARCHAR(256)                       NULL,
    requestParams TEXT                               NULL,
    status        VARCHAR(32)                        NOT NULL,
    errorMessage  VARCHAR(512)                       NULL,
    durationMs    BIGINT                             NULL,
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete      TINYINT  DEFAULT 0                 NOT NULL
);
CREATE INDEX idx_sensitive_log_userId ON sensitive_operation_log(userId);
CREATE INDEX idx_sensitive_log_status ON sensitive_operation_log(status);
CREATE INDEX idx_sensitive_log_createTime ON sensitive_operation_log(createTime);

-- ================================================
-- 11. 通知表
-- ================================================
CREATE TABLE IF NOT EXISTS notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId      BIGINT                             NOT NULL COMMENT '接收者',
    fromUserId  BIGINT                             NULL COMMENT '触发者',
    type        VARCHAR(32)                        NOT NULL COMMENT 'like/comment/follow',
    targetId    BIGINT                             NULL COMMENT '目标ID(图片/评论)',
    content     VARCHAR(512)                       NULL COMMENT '通知内容',
    isRead      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否已读',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_notification_userId ON notification(userId, isRead);

-- ================================================
-- 12. 收藏表
-- ================================================
CREATE TABLE IF NOT EXISTS favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId      BIGINT                             NOT NULL,
    pictureId   BIGINT                             NOT NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX uk_favorite ON favorite(userId, pictureId);

-- ================================================
-- 13. 评论点赞表
-- ================================================
CREATE TABLE IF NOT EXISTS comment_like (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    commentId   BIGINT                             NOT NULL,
    userId      BIGINT                             NOT NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX uk_comment_like ON comment_like(commentId, userId);

-- ================================================
-- 14. 用户 Token 表 (用于 JWT 黑名单)
-- ================================================
CREATE TABLE IF NOT EXISTS user_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId      BIGINT                             NOT NULL,
    token       VARCHAR(512)                       NOT NULL,
    tokenType   VARCHAR(32)                        NOT NULL,
    expiresAt   DATETIME                           NOT NULL,
    isRevoked   TINYINT  DEFAULT 0                 NOT NULL,
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_user_token ON user_token(userId);

-- ================================================
-- 字段扩展 (仅 MySQL 环境执行)
-- ================================================
ALTER TABLE space_join_request ADD COLUMN IF NOT EXISTS requestType TINYINT DEFAULT 0 NOT NULL COMMENT '0-加入申请 1-团队邀请';
ALTER TABLE space_join_request ADD COLUMN IF NOT EXISTS inviterId BIGINT NULL COMMENT '邀请人 id（仅团队邀请时存在）';
ALTER TABLE picture ADD COLUMN IF NOT EXISTS viewCount BIGINT DEFAULT 0 NOT NULL COMMENT '浏览量';
