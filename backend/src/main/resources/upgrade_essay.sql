-- =====================================================
-- 简答题功能数据库升级脚本
-- =====================================================

USE exam_system;

-- 1. question表新增字段
ALTER TABLE `question` 
ADD COLUMN `reference_answer` TEXT COMMENT '参考答案' AFTER `content`,
ADD COLUMN `keywords` VARCHAR(500) COMMENT '评分关键词，多个用逗号分隔' AFTER `reference_answer`;

-- 2. answer_record表修改和新增字段
ALTER TABLE `answer_record` 
MODIFY COLUMN `answer` TEXT COMMENT '学生答案',
ADD COLUMN `auto_score` INT COMMENT '系统自动评分' AFTER `is_correct`,
ADD COLUMN `final_score` INT COMMENT '老师最终评分' AFTER `auto_score`,
ADD COLUMN `teacher_comment` VARCHAR(500) COMMENT '教师评语' AFTER `score`;

-- =====================================================
-- 验证：查询表结构确认字段已添加
-- =====================================================
-- DESCRIBE question;
-- DESCRIBE answer_record;
