-- 文件记录表
CREATE TABLE IF NOT EXISTS `t_file_record` (
    `id` BIGINT NOT NULL COMMENT '文件ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_type` VARCHAR(50) COMMENT '文件类型（后缀）',
    `mime_type` VARCHAR(100) COMMENT 'MIME类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `cos_path` VARCHAR(500) NOT NULL COMMENT 'COS存储路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `folder_id` BIGINT COMMENT '所属文件夹ID（null表示根目录）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-删除 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 文件目录表
CREATE TABLE IF NOT EXISTS `t_file_folder` (
    `id` BIGINT NOT NULL COMMENT '目录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `folder_name` VARCHAR(100) NOT NULL COMMENT '目录名称',
    `parent_id` BIGINT COMMENT '父目录ID（null表示根目录）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-删除 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    UNIQUE KEY `uk_user_parent_name` (`user_id`, `parent_id`, `folder_name`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件目录表';
