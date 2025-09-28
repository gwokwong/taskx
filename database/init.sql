-- =============================================
-- DooTask 数据库初始化脚本
-- 版本: 1.0
-- 创建时间: 2025-09-28
-- 说明: Java Spring Boot + MyBatis Plus 版本
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `dootask`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dootask`;

-- =============================================
-- 用户和部门相关表
-- =============================================

-- 部门表
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门ID',
  `description` text COMMENT '部门描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active-正常, inactive-停用',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 用户表
CREATE TABLE `users` (
  `userid` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `nickname` varchar(50) NOT NULL COMMENT '昵称',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `user_img` varchar(500) DEFAULT NULL COMMENT '头像',
  `profession` varchar(100) DEFAULT NULL COMMENT '职业',
  `tel` varchar(20) DEFAULT NULL COMMENT '电话',
  `identity` json DEFAULT NULL COMMENT '身份标识',
  `department` json DEFAULT NULL COMMENT '部门ID数组',
  `az` varchar(10) DEFAULT NULL COMMENT '首字母',
  `pinyin` varchar(200) DEFAULT NULL COMMENT '拼音',
  `py` varchar(50) DEFAULT NULL COMMENT '拼音首字母',
  `bot` tinyint DEFAULT '0' COMMENT '是否机器人: 0-否, 1-是',
  `change_nickname` tinyint DEFAULT '1' COMMENT '是否可修改昵称: 0-否, 1-是',
  `department_owner` json DEFAULT NULL COMMENT '担任部门负责人的部门ID数组',
  `disable_at` timestamp NULL DEFAULT NULL COMMENT '禁用时间',
  `email_time` timestamp NULL DEFAULT NULL COMMENT '邮箱验证时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
  `last_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `line_ip` varchar(45) DEFAULT NULL COMMENT '在线IP',
  `line_at` timestamp NULL DEFAULT NULL COMMENT '在线时间',
  PRIMARY KEY (`userid`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_nickname` (`nickname`),
  KEY `idx_department` ((cast(json_extract(`department`,'$[0]') as unsigned))),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户设置表
CREATE TABLE `user_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_key` (`user_id`, `setting_key`),
  CONSTRAINT `fk_user_settings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- =============================================
-- 项目相关表
-- =============================================

-- 项目表
CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(200) NOT NULL COMMENT '项目名称',
  `description` text COMMENT '项目描述',
  `owner_id` bigint NOT NULL COMMENT '项目负责人ID',
  `color` varchar(10) DEFAULT '#1890ff' COMMENT '项目颜色',
  `archived_at` timestamp NULL DEFAULT NULL COMMENT '归档时间',
  `archived_user_id` bigint DEFAULT NULL COMMENT '归档操作用户ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_archived_at` (`archived_at`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_projects_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 项目成员表
CREATE TABLE `project_members` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(50) DEFAULT 'member' COMMENT '角色: owner-负责人, manager-管理员, member-成员',
  `permissions` json DEFAULT NULL COMMENT '权限配置',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active-正常, left-已离开',
  `joined_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `left_at` timestamp NULL DEFAULT NULL COMMENT '离开时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`),
  CONSTRAINT `fk_project_members_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- =============================================
-- 任务相关表
-- =============================================

-- 任务列表表
CREATE TABLE `project_columns` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '列表ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `name` varchar(100) NOT NULL COMMENT '列表名称',
  `color` varchar(10) DEFAULT NULL COMMENT '列表颜色',
  `sort` int DEFAULT '0' COMMENT '排序',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_sort` (`sort`),
  CONSTRAINT `fk_project_columns_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目任务列表表';

-- 任务表
CREATE TABLE `project_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `column_id` bigint DEFAULT NULL COMMENT '任务列表ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父任务ID',
  `name` varchar(500) NOT NULL COMMENT '任务名称',
  `content` longtext COMMENT '任务内容',
  `desc` text COMMENT '任务描述',
  `color` varchar(10) DEFAULT NULL COMMENT '任务颜色',
  `priority` varchar(20) DEFAULT 'medium' COMMENT '优先级: low-低, medium-中, high-高, urgent-紧急',
  `owner` varchar(500) DEFAULT NULL COMMENT '负责人,多个用逗号分隔',
  `assist` varchar(500) DEFAULT NULL COMMENT '协助人,多个用逗号分隔',
  `subtasks` int DEFAULT '0' COMMENT '子任务数量',
  `subtasks_complete` int DEFAULT '0' COMMENT '已完成子任务数量',
  `level` int DEFAULT '1' COMMENT '任务层级',
  `sort` int DEFAULT '0' COMMENT '排序',
  `complete_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `start_at` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_at` timestamp NULL DEFAULT NULL COMMENT '截止时间',
  `archived_at` timestamp NULL DEFAULT NULL COMMENT '归档时间',
  `archived_userid` bigint DEFAULT NULL COMMENT '归档操作用户ID',
  `flow_item_id` bigint DEFAULT NULL COMMENT '工作流项目ID',
  `flow_item_name` varchar(100) DEFAULT NULL COMMENT '工作流项目名称',
  `flow_item_status` varchar(50) DEFAULT NULL COMMENT '工作流状态',
  `flow_item_color` varchar(10) DEFAULT NULL COMMENT '工作流颜色',
  `tags` json DEFAULT NULL COMMENT '标签数组',
  `attachments_count` int DEFAULT '0' COMMENT '附件数量',
  `comments_count` int DEFAULT '0' COMMENT '评论数量',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_column_id` (`column_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_owner` (`owner`(191)),
  KEY `idx_priority` (`priority`),
  KEY `idx_complete_at` (`complete_at`),
  KEY `idx_end_at` (`end_at`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_project_tasks_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_tasks_column` FOREIGN KEY (`column_id`) REFERENCES `project_columns` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_project_tasks_parent` FOREIGN KEY (`parent_id`) REFERENCES `project_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目任务表';

-- 任务模板表
CREATE TABLE `task_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(200) NOT NULL COMMENT '模板名称',
  `description` text COMMENT '模板描述',
  `template_data` json NOT NULL COMMENT '模板数据',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `is_public` tinyint DEFAULT '0' COMMENT '是否公开: 0-私有, 1-公开',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_is_public` (`is_public`),
  KEY `idx_use_count` (`use_count`),
  CONSTRAINT `fk_task_templates_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务模板表';

-- 任务评论表
CREATE TABLE `task_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(用于回复)',
  `mentions` json DEFAULT NULL COMMENT '提及的用户ID数组',
  `attachments` json DEFAULT NULL COMMENT '附件数组',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否已删除: 0-否, 1-是',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_task_comments_task` FOREIGN KEY (`task_id`) REFERENCES `project_tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `task_comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务评论表';

-- 任务标签表
CREATE TABLE `task_tags` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(100) NOT NULL COMMENT '标签名称',
  `color` varchar(10) DEFAULT '#1890ff' COMMENT '标签颜色',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID(null表示全局标签)',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_name` (`project_id`, `name`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_use_count` (`use_count`),
  CONSTRAINT `fk_task_tags_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_tags_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务标签表';

-- 任务标签关联表
CREATE TABLE `task_tag_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_tag` (`task_id`, `tag_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_task_tag_relations_task` FOREIGN KEY (`task_id`) REFERENCES `project_tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_tag_relations_tag` FOREIGN KEY (`tag_id`) REFERENCES `task_tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务标签关联表';

-- 任务时间记录表
CREATE TABLE `task_time_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `description` varchar(500) DEFAULT NULL COMMENT '工作描述',
  `duration` int NOT NULL COMMENT '工作时长(分钟)',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `is_billable` tinyint DEFAULT '1' COMMENT '是否计费: 0-否, 1-是',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_task_time_logs_task` FOREIGN KEY (`task_id`) REFERENCES `project_tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_time_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务时间记录表';

-- =============================================
-- 日历和事件表
-- =============================================

-- 日历事件表
CREATE TABLE `calendar_events` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '事件标题',
  `description` text COMMENT '事件描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `all_day` tinyint DEFAULT '0' COMMENT '是否全天: 0-否, 1-是',
  `location` varchar(200) DEFAULT NULL COMMENT '地点',
  `color` varchar(10) DEFAULT '#1890ff' COMMENT '事件颜色',
  `type` varchar(50) DEFAULT 'event' COMMENT '事件类型: event-事件, meeting-会议, reminder-提醒',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active-正常, cancelled-取消',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联类型: task, project',
  `recurrence_rule` text COMMENT '重复规则',
  `participants` json DEFAULT NULL COMMENT '参与者ID数组',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_type` (`type`),
  KEY `idx_related` (`related_id`, `related_type`),
  CONSTRAINT `fk_calendar_events_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日历事件表';

-- =============================================
-- 文件管理表
-- =============================================

-- 文件实体表
CREATE TABLE `file_entities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `name` varchar(500) NOT NULL COMMENT '文件名',
  `path` varchar(1000) NOT NULL COMMENT '文件路径',
  `size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `hash` varchar(64) DEFAULT NULL COMMENT '文件哈希值',
  `owner_id` bigint NOT NULL COMMENT '文件所有者ID',
  `folder_id` bigint DEFAULT NULL COMMENT '所属文件夹ID',
  `project_id` bigint DEFAULT NULL COMMENT '关联项目ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联任务ID',
  `version` int DEFAULT '1' COMMENT '版本号',
  `downloads` int DEFAULT '0' COMMENT '下载次数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active-正常, deleted-已删除',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_hash` (`hash`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_file_entities_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`userid`),
  CONSTRAINT `fk_file_entities_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_file_entities_task` FOREIGN KEY (`task_id`) REFERENCES `project_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件实体表';

-- 文件夹表
CREATE TABLE `file_folders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `name` varchar(200) NOT NULL COMMENT '文件夹名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父文件夹ID',
  `owner_id` bigint NOT NULL COMMENT '文件夹所有者ID',
  `project_id` bigint DEFAULT NULL COMMENT '关联项目ID',
  `permissions` json DEFAULT NULL COMMENT '权限配置',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_project_id` (`project_id`),
  CONSTRAINT `fk_file_folders_parent` FOREIGN KEY (`parent_id`) REFERENCES `file_folders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_file_folders_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`userid`),
  CONSTRAINT `fk_file_folders_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹表';

-- =============================================
-- 消息和通知表
-- =============================================

-- 消息表
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID（私聊）',
  `channel_type` varchar(50) NOT NULL COMMENT '频道类型: private-私聊, project-项目, task-任务',
  `channel_id` bigint DEFAULT NULL COMMENT '频道ID',
  `content` longtext NOT NULL COMMENT '消息内容',
  `message_type` varchar(50) DEFAULT 'text' COMMENT '消息类型: text-文本, image-图片, file-文件, system-系统',
  `reply_to_id` bigint DEFAULT NULL COMMENT '回复的消息ID',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `read_at` timestamp NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_channel` (`channel_type`, `channel_id`),
  KEY `idx_reply_to_id` (`reply_to_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`userid`),
  CONSTRAINT `fk_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`userid`),
  CONSTRAINT `fk_messages_reply` FOREIGN KEY (`reply_to_id`) REFERENCES `messages` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 通知表
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(100) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `data` json DEFAULT NULL COMMENT '附加数据',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `read_at` timestamp NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =============================================
-- WebSocket和实时通信表
-- =============================================

-- WebSocket对话表
CREATE TABLE `websocket_dialogs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `type` varchar(50) NOT NULL COMMENT '对话类型: private-私聊, group-群聊, project-项目',
  `name` varchar(200) DEFAULT NULL COMMENT '对话名称',
  `description` text COMMENT '对话描述',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `participants` json NOT NULL COMMENT '参与者ID数组',
  `last_message_id` bigint DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_at` timestamp NULL DEFAULT NULL COMMENT '最后消息时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_last_message_at` (`last_message_at`),
  CONSTRAINT `fk_websocket_dialogs_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WebSocket对话表';

-- =============================================
-- 工作流表
-- =============================================

-- 工作流表
CREATE TABLE `workflows` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作流ID',
  `name` varchar(200) NOT NULL COMMENT '工作流名称',
  `description` text COMMENT '工作流描述',
  `definition` longtext NOT NULL COMMENT '工作流定义(JSON格式)',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态: draft-草稿, active-活跃, inactive-非活跃',
  `trigger_type` varchar(50) DEFAULT 'manual' COMMENT '触发类型: manual-手动, auto-自动, schedule-定时',
  `trigger_config` json DEFAULT NULL COMMENT '触发配置',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `version` int DEFAULT '1' COMMENT '版本号',
  `execution_count` int DEFAULT '0' COMMENT '执行次数',
  `last_executed_at` timestamp NULL DEFAULT NULL COMMENT '最后执行时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_trigger_type` (`trigger_type`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_workflows_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- =============================================
-- 系统表
-- =============================================

-- 系统设置表
CREATE TABLE `system_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` longtext COMMENT '设置值',
  `setting_type` varchar(50) DEFAULT 'string' COMMENT '设置类型: string, number, boolean, json',
  `description` varchar(500) DEFAULT NULL COMMENT '设置描述',
  `is_public` tinyint DEFAULT '0' COMMENT '是否公开: 0-否, 1-是',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- 搜索日志表
CREATE TABLE `search_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `keyword` varchar(500) NOT NULL COMMENT '搜索关键词',
  `result_count` int DEFAULT '0' COMMENT '结果数量',
  `search_time` int DEFAULT '0' COMMENT '搜索耗时(毫秒)',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COMMENT '用户代理',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`(191)),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_search_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认部门
INSERT INTO `departments` (`id`, `name`, `parent_id`, `description`, `sort`, `status`) VALUES
(1, '总公司', NULL, '公司总部', 1, 'active'),
(2, '技术部', 1, '技术研发部门', 1, 'active'),
(3, '产品部', 1, '产品设计部门', 2, 'active'),
(4, '运营部', 1, '运营推广部门', 3, 'active'),
(5, '人事部', 1, '人力资源部门', 4, 'active');

-- 插入默认管理员用户（密码: admin123）
INSERT INTO `users` (`userid`, `email`, `nickname`, `password`, `department`, `identity`, `created_at`) VALUES
(1, 'admin@dootask.com', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1VU8EX7xFULNDX3z9e', '[1]', '["admin"]', NOW());

-- 插入默认系统设置
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`, `is_public`) VALUES
('system.name', 'DooTask', 'string', '系统名称', 1),
('system.version', '1.0.0', 'string', '系统版本', 1),
('system.description', '现代化团队协作平台', 'string', '系统描述', 1),
('system.logo', '', 'string', '系统Logo', 1),
('system.timezone', 'Asia/Shanghai', 'string', '系统时区', 0),
('system.language', 'zh-CN', 'string', '默认语言', 1),
('email.smtp.host', '', 'string', 'SMTP服务器地址', 0),
('email.smtp.port', '587', 'number', 'SMTP端口', 0),
('email.smtp.username', '', 'string', 'SMTP用户名', 0),
('email.smtp.password', '', 'string', 'SMTP密码', 0),
('file.upload.max_size', '52428800', 'number', '文件上传最大大小(字节)', 0),
('file.upload.allowed_types', '["jpg","jpeg","png","gif","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip","rar"]', 'json', '允许上传的文件类型', 0);

-- 创建演示项目
INSERT INTO `projects` (`id`, `name`, `description`, `owner_id`, `color`, `created_at`) VALUES
(1, 'DooTask 演示项目', '这是一个演示项目，用于展示系统功能', 1, '#1890ff', NOW());

-- 添加项目成员
INSERT INTO `project_members` (`project_id`, `user_id`, `role`, `status`, `joined_at`) VALUES
(1, 1, 'owner', 'active', NOW());

-- 创建默认任务列表
INSERT INTO `project_columns` (`id`, `project_id`, `name`, `color`, `sort`, `created_at`) VALUES
(1, 1, '待办', '#f56a00', 1, NOW()),
(2, 1, '进行中', '#1890ff', 2, NOW()),
(3, 1, '待审核', '#722ed1', 3, NOW()),
(4, 1, '已完成', '#52c41a', 4, NOW());

-- 创建演示任务
INSERT INTO `project_tasks` (`id`, `project_id`, `column_id`, `name`, `content`, `priority`, `owner`, `created_at`) VALUES
(1, 1, 1, '欢迎使用 DooTask', '欢迎使用 DooTask 团队协作平台！\n\n这是您的第一个任务，您可以：\n\n1. 点击编辑按钮修改任务信息\n2. 拖拽任务到不同列表中\n3. 添加评论和附件\n4. 设置截止日期和优先级\n5. 分配给团队成员\n\n开始您的协作之旅吧！', 'medium', '1', NOW()),
(2, 1, 2, '学习基本操作', '熟悉系统的基本操作和功能', 'low', '1', NOW()),
(3, 1, 3, '邀请团队成员', '邀请您的团队成员加入项目协作', 'high', '1', NOW());

-- =============================================
-- 创建索引优化查询性能
-- =============================================

-- 为frequently查询的字段创建复合索引
CREATE INDEX `idx_tasks_project_status` ON `project_tasks` (`project_id`, `complete_at`, `archived_at`);
CREATE INDEX `idx_tasks_assignee_date` ON `project_tasks` (`owner`(100), `end_at`);
CREATE INDEX `idx_messages_channel_time` ON `messages` (`channel_type`, `channel_id`, `created_at`);
CREATE INDEX `idx_notifications_user_unread` ON `notifications` (`user_id`, `is_read`, `created_at`);
CREATE INDEX `idx_files_project_owner` ON `file_entities` (`project_id`, `owner_id`, `status`);

-- =============================================
-- 创建视图便于查询
-- =============================================

-- 任务统计视图
CREATE VIEW `task_statistics` AS
SELECT
    p.id as project_id,
    p.name as project_name,
    COUNT(t.id) as total_tasks,
    COUNT(CASE WHEN t.complete_at IS NOT NULL THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN t.complete_at IS NULL AND t.archived_at IS NULL THEN 1 END) as active_tasks,
    COUNT(CASE WHEN t.end_at < NOW() AND t.complete_at IS NULL AND t.archived_at IS NULL THEN 1 END) as overdue_tasks
FROM projects p
LEFT JOIN project_tasks t ON p.id = t.project_id
WHERE p.archived_at IS NULL
GROUP BY p.id, p.name;

-- 用户任务统计视图
CREATE VIEW `user_task_statistics` AS
SELECT
    u.userid,
    u.nickname,
    u.email,
    COUNT(CASE WHEN FIND_IN_SET(u.userid, t.owner) > 0 THEN 1 END) as assigned_tasks,
    COUNT(CASE WHEN FIND_IN_SET(u.userid, t.owner) > 0 AND t.complete_at IS NOT NULL THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN FIND_IN_SET(u.userid, t.owner) > 0 AND t.complete_at IS NULL AND t.archived_at IS NULL THEN 1 END) as active_tasks,
    COUNT(CASE WHEN FIND_IN_SET(u.userid, t.owner) > 0 AND t.end_at < NOW() AND t.complete_at IS NULL AND t.archived_at IS NULL THEN 1 END) as overdue_tasks
FROM users u
LEFT JOIN project_tasks t ON FIND_IN_SET(u.userid, t.owner) > 0
WHERE u.disable_at IS NULL
GROUP BY u.userid, u.nickname, u.email;

-- =============================================
-- 完成初始化
-- =============================================

-- 记录初始化完成时间
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`) VALUES
('system.initialized_at', NOW(), 'string', '系统初始化完成时间');

-- 显示初始化完成信息
SELECT
    '数据库初始化完成！' as message,
    '默认管理员账号: admin@dootask.com' as admin_account,
    '默认密码: admin123' as admin_password,
    '请及时修改默认密码！' as security_notice;