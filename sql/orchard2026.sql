-- =============================================
-- Orchard 2026 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS `orchard2026` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `orchard2026`;

-- -------------------------------------------
-- 公司表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `t_company` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(100) NOT NULL COMMENT '公司名称',
    `credit_code`     VARCHAR(50)  DEFAULT NULL COMMENT '统一社会信用代码',
    `contact_person`  VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address`         VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_credit_code` (`credit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司表';

-- -------------------------------------------
-- 部门表（支持多级树形结构）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `t_department` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `company_id`      BIGINT       NOT NULL COMMENT '所属公司ID',
    `parent_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '上级部门ID（0表示顶级部门）',
    `name`            VARCHAR(100) NOT NULL COMMENT '部门名称',
    `sort`            INT          DEFAULT 0 COMMENT '排序',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- -------------------------------------------
-- 用户表（合并 auth + userManagement）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号（登录标识，第三方登录可为空）',
    `password`        VARCHAR(255) DEFAULT NULL COMMENT '密码（BCrypt加密，第三方登录可为空）',
    `username`        VARCHAR(50)  DEFAULT NULL COMMENT '用户名',
    `nickname`        VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `user_type`       TINYINT      NOT NULL DEFAULT 2 COMMENT '用户类型：1-公司 2-个人',
    `company_id`      BIGINT       DEFAULT NULL COMMENT '关联公司ID（公司类型时有效）',
    `department_id`   BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    `real_name`       VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`          VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 第三方登录绑定表
CREATE TABLE IF NOT EXISTS `t_oauth_bindding` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `oauth_type` VARCHAR(20) NOT NULL COMMENT '第三方平台：wechat-微信 alipay-支付宝 github-GitHub',
    `open_id` VARCHAR(100) NOT NULL COMMENT '第三方平台OpenID',
    `union_id` VARCHAR(100) COMMENT '第三方平台UnionID',
    `nickname` VARCHAR(50) COMMENT '第三方昵称',
    `avatar` VARCHAR(255) COMMENT '第三方头像',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth` (`oauth_type`, `open_id`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方登录绑定表';

-- Token黑名单表

CREATE TABLE IF NOT EXISTS `t_token_blacklist` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `token` VARCHAR(500) NOT NULL COMMENT 'Token',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_token` (`token`),
    KEY `idx_expire_time` (`expire_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- -------------------------------------------
-- 套餐表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `t_plan` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`            VARCHAR(100)   NOT NULL COMMENT '套餐名称',
    `description`     VARCHAR(500)   DEFAULT NULL COMMENT '套餐描述',
    `price`           DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `duration_days`   INT            NOT NULL COMMENT '有效期（天）',
    `user_type`       TINYINT        NOT NULL DEFAULT 0 COMMENT '适用用户类型：0-通用 1-公司 2-个人',
    `status`          TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(50)    DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(50)    DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- -------------------------------------------
-- 用户订阅表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user_subscription` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT         NOT NULL COMMENT '用户ID',
    `plan_id`         BIGINT         NOT NULL COMMENT '套餐ID',
    `order_no`        VARCHAR(50)    NOT NULL COMMENT '订单号',
    `status`          TINYINT        NOT NULL DEFAULT 1 COMMENT '订阅状态：1-生效中 2-已过期 3-已取消 4-待生效',
    `price`           DECIMAL(10, 2) NOT NULL COMMENT '订阅价格',
    `start_time`      DATETIME       NOT NULL COMMENT '开始时间',
    `end_time`        DATETIME       NOT NULL COMMENT '结束时间',
    `pay_time`        DATETIME       DEFAULT NULL COMMENT '支付时间',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(50)    DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(50)    DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅表';
