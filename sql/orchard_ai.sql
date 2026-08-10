-- AI会话表
CREATE TABLE `t_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(255) NOT NULL COMMENT '会话标题',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI会话表';

-- AI消息表（一段对话=一条记录：提问+回答，删除即整段删除）
CREATE TABLE `t_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `question` text NOT NULL COMMENT '用户提问',
  `answer` text DEFAULT NULL COMMENT 'AI回答',
  `attachments_json` text DEFAULT NULL COMMENT '提问附件JSON',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-生成中 1-完成 2-失败',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI消息表';

-- AI素材表（作品收录/首页案例展示，仅保留社交属性字段，无生成过程数据）
CREATE TABLE `t_ai_asset` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '作者用户ID',
  `type` varchar(20) NOT NULL COMMENT '素材类型：image-图片 video-视频',
  `prompt` text DEFAULT NULL COMMENT '提示词',
  `params` text DEFAULT NULL COMMENT '参数JSON（模型/尺寸等）',
  `url` varchar(500) NOT NULL COMMENT '素材URL',
  `tags` text DEFAULT NULL COMMENT '标签JSON数组',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI素材表';

-- AI素材点赞表（防重复点赞：asset_id+user_id+deleted 唯一）
CREATE TABLE `t_ai_asset_like` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `asset_id` bigint NOT NULL COMMENT '素材ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_user` (`asset_id`, `user_id`, `deleted`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI素材点赞表';

-- AI作品表（生成任务记录，AI对话中的图片/视频作品）
CREATE TABLE `t_ai_work` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '作品类型：image-文生图/图生图 video-文生视频',
  `prompt` text DEFAULT NULL COMMENT '提示词',
  `model` varchar(50) NOT NULL DEFAULT 'default' COMMENT '模型标识',
  `params` text DEFAULT NULL COMMENT '参数JSON（style/尺寸/数量等）',
  `result_url` varchar(500) DEFAULT NULL COMMENT '结果URL（当前展示图）',
  `data_list` text DEFAULT NULL COMMENT '结果数据列表JSON（多图场景，元素为{id,url}）',
  `origin_image_list` text DEFAULT NULL COMMENT '原图数据列表JSON（图生图参考图，元素为{id,url}）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-等待中 1-生成中 2-已完成 3-失败 4-待操作',
  `operation_data` text DEFAULT NULL COMMENT '待操作数据JSON（如选择列表）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI作品表';
