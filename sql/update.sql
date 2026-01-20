-- =========================================
-- OA办公系统数据库更新脚本
-- =========================================

USE oa_system;

-- 更新菜单表添加缺少的字段
ALTER TABLE sys_menu 
ADD COLUMN IF NOT EXISTS keep_alive TINYINT(1) DEFAULT 0 COMMENT '是否缓存' AFTER visible,
ADD COLUMN IF NOT EXISTS deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除' AFTER update_time,
MODIFY COLUMN menu_type INT DEFAULT 1 COMMENT '菜单类型(1目录2菜单3按钮)';

-- 更新字典类型表添加逻辑删除字段
ALTER TABLE sys_dict_type 
ADD COLUMN IF NOT EXISTS deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除' AFTER update_time;

-- 更新字典数据表添加缺少的字段
ALTER TABLE sys_dict_data 
ADD COLUMN IF NOT EXISTS css_class VARCHAR(100) NULL COMMENT '样式属性' AFTER status,
ADD COLUMN IF NOT EXISTS list_class VARCHAR(100) NULL COMMENT '标签类型' AFTER css_class,
ADD COLUMN IF NOT EXISTS is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认' AFTER list_class,
ADD COLUMN IF NOT EXISTS update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
ADD COLUMN IF NOT EXISTS deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除' AFTER update_time;

-- 更新菜单表的permission字段（映射到perms）
-- 注意：如果已经有permission字段则忽略
ALTER TABLE sys_menu 
ADD COLUMN IF NOT EXISTS permission VARCHAR(100) NULL COMMENT '权限标识' AFTER perms;

-- 同步perms到permission
UPDATE sys_menu SET permission = perms WHERE permission IS NULL AND perms IS NOT NULL;

-- 超级管理员不需要强制修改密码
UPDATE sys_user SET password_changed = 1, initial_password = NULL 
WHERE username = 'admin';

SELECT 'Database update completed!' AS message;
