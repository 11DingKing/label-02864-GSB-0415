-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `real_name` VARCHAR(50) NOT NULL,
    `role` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 考试表
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `duration` INT NOT NULL DEFAULT 60,
    `total_score` INT NOT NULL DEFAULT 100,
    `pass_score` INT NOT NULL DEFAULT 60,
    `creator_id` BIGINT NOT NULL,
    `start_time` DATETIME,
    `end_time` DATETIME,
    `status` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_creator` (`creator_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 题目表
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `exam_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL DEFAULT 1,
    `content` TEXT NOT NULL,
    `score` INT NOT NULL DEFAULT 5,
    `sort_order` INT NOT NULL DEFAULT 0,
    `reference_answer` TEXT,
    `keywords` VARCHAR(500),
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_exam` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 选项表
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `option_key` VARCHAR(10) NOT NULL,
    `content` VARCHAR(500) NOT NULL,
    `is_correct` TINYINT NOT NULL DEFAULT 0,
    `sort_order` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 考试记录表
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `exam_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `total_score` INT DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `start_time` DATETIME NOT NULL,
    `submit_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam_user` (`exam_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 答题记录表
DROP TABLE IF EXISTS `answer_record`;
CREATE TABLE `answer_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `record_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `answer` TEXT,
    `is_correct` TINYINT DEFAULT NULL,
    `score` INT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_record` (`record_id`),
    KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 初始化测试数据
-- =====================================================

-- 用户数据 (密码都是 123456 的MD5值)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('teacher', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 1),
('student1', 'e10adc3949ba59abbe56e057f20f883e', '李明', 0),
('student2', 'e10adc3949ba59abbe56e057f20f883e', '王芳', 0),
('student3', 'e10adc3949ba59abbe56e057f20f883e', '陈伟', 0);

-- 考试数据
INSERT INTO `exam` (`title`, `description`, `duration`, `total_score`, `pass_score`, `creator_id`, `start_time`, `end_time`, `status`) VALUES
('Java基础知识测试', '本次考试涵盖Java基础语法、面向对象、集合框架等核心知识点', 60, 100, 60, 1, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('前端开发基础测试', '本次考试涵盖HTML、CSS、JavaScript基础知识', 45, 100, 60, 1, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('数据库基础测试', 'SQL查询、数据库设计与优化', 30, 50, 30, 1, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1);

-- Java考试题目 (exam_id = 1)
INSERT INTO `question` (`exam_id`, `type`, `content`, `score`, `sort_order`) VALUES
(1, 1, 'Java中哪个关键字用于定义类？', 10, 1),
(1, 1, '以下哪个不是Java的基本数据类型？', 10, 2),
(1, 1, 'Java中用于实现接口的关键字是？', 10, 3),
(1, 2, '以下哪些是Java的访问修饰符？（多选）', 10, 4),
(1, 3, 'Java是一种面向对象的编程语言。', 10, 5),
(1, 1, 'String类位于哪个包中？', 10, 6),
(1, 1, '以下哪个集合类是线程安全的？', 10, 7),
(1, 3, 'Java中的main方法必须是public static void。', 10, 8),
(1, 2, '以下哪些是Java集合框架的接口？（多选）', 10, 9),
(1, 1, 'Java中用于捕获异常的关键字是？', 10, 10);

-- Java题目选项
INSERT INTO `question_option` (`question_id`, `option_key`, `content`, `is_correct`, `sort_order`) VALUES
-- 题目1
(1, 'A', 'class', 1, 1),
(1, 'B', 'struct', 0, 2),
(1, 'C', 'define', 0, 3),
(1, 'D', 'type', 0, 4),
-- 题目2
(2, 'A', 'int', 0, 1),
(2, 'B', 'String', 1, 2),
(2, 'C', 'boolean', 0, 3),
(2, 'D', 'double', 0, 4),
-- 题目3
(3, 'A', 'extends', 0, 1),
(3, 'B', 'implements', 1, 2),
(3, 'C', 'interface', 0, 3),
(3, 'D', 'abstract', 0, 4),
-- 题目4（多选）
(4, 'A', 'public', 1, 1),
(4, 'B', 'private', 1, 2),
(4, 'C', 'protected', 1, 3),
(4, 'D', 'internal', 0, 4),
-- 题目5（判断）
(5, 'A', '正确', 1, 1),
(5, 'B', '错误', 0, 2),
-- 题目6
(6, 'A', 'java.util', 0, 1),
(6, 'B', 'java.lang', 1, 2),
(6, 'C', 'java.io', 0, 3),
(6, 'D', 'java.net', 0, 4),
-- 题目7
(7, 'A', 'ArrayList', 0, 1),
(7, 'B', 'LinkedList', 0, 2),
(7, 'C', 'Vector', 1, 3),
(7, 'D', 'HashSet', 0, 4),
-- 题目8（判断）
(8, 'A', '正确', 1, 1),
(8, 'B', '错误', 0, 2),
-- 题目9（多选）
(9, 'A', 'List', 1, 1),
(9, 'B', 'Set', 1, 2),
(9, 'C', 'Map', 1, 3),
(9, 'D', 'Array', 0, 4),
-- 题目10
(10, 'A', 'try-catch', 1, 1),
(10, 'B', 'if-else', 0, 2),
(10, 'C', 'switch-case', 0, 3),
(10, 'D', 'for-each', 0, 4);


-- 前端考试题目 (exam_id = 2)
INSERT INTO `question` (`exam_id`, `type`, `content`, `score`, `sort_order`) VALUES
(2, 1, 'HTML中用于定义段落的标签是？', 10, 1),
(2, 1, 'CSS中用于设置背景颜色的属性是？', 10, 2),
(2, 1, 'JavaScript中用于声明变量的关键字不包括？', 10, 3),
(2, 3, 'HTML5是HTML的最新版本。', 10, 4),
(2, 2, '以下哪些是CSS的选择器？（多选）', 10, 5),
(2, 1, 'JavaScript中用于输出内容到控制台的方法是？', 10, 6),
(2, 3, 'CSS中的flex布局是一种一维布局模型。', 10, 7),
(2, 1, 'Vue.js中用于双向数据绑定的指令是？', 10, 8),
(2, 2, '以下哪些是JavaScript的数据类型？（多选）', 10, 9),
(2, 1, 'HTTP状态码200表示？', 10, 10);

-- 前端题目选项
INSERT INTO `question_option` (`question_id`, `option_key`, `content`, `is_correct`, `sort_order`) VALUES
-- 题目11
(11, 'A', '<p>', 1, 1),
(11, 'B', '<div>', 0, 2),
(11, 'C', '<span>', 0, 3),
(11, 'D', '<br>', 0, 4),
-- 题目12
(12, 'A', 'color', 0, 1),
(12, 'B', 'background-color', 1, 2),
(12, 'C', 'bg-color', 0, 3),
(12, 'D', 'back-color', 0, 4),
-- 题目13
(13, 'A', 'var', 0, 1),
(13, 'B', 'let', 0, 2),
(13, 'C', 'const', 0, 3),
(13, 'D', 'int', 1, 4),
-- 题目14（判断）
(14, 'A', '正确', 1, 1),
(14, 'B', '错误', 0, 2),
-- 题目15（多选）
(15, 'A', '类选择器(.class)', 1, 1),
(15, 'B', 'ID选择器(#id)', 1, 2),
(15, 'C', '元素选择器(div)', 1, 3),
(15, 'D', '变量选择器($var)', 0, 4),
-- 题目16
(16, 'A', 'console.log()', 1, 1),
(16, 'B', 'print()', 0, 2),
(16, 'C', 'echo()', 0, 3),
(16, 'D', 'write()', 0, 4),
-- 题目17（判断）
(17, 'A', '正确', 1, 1),
(17, 'B', '错误', 0, 2),
-- 题目18
(18, 'A', 'v-bind', 0, 1),
(18, 'B', 'v-model', 1, 2),
(18, 'C', 'v-on', 0, 3),
(18, 'D', 'v-if', 0, 4),
-- 题目19（多选）
(19, 'A', 'String', 1, 1),
(19, 'B', 'Number', 1, 2),
(19, 'C', 'Boolean', 1, 3),
(19, 'D', 'Integer', 0, 4),
-- 题目20
(20, 'A', '请求成功', 1, 1),
(20, 'B', '服务器错误', 0, 2),
(20, 'C', '页面未找到', 0, 3),
(20, 'D', '重定向', 0, 4);

-- 数据库考试题目 (exam_id = 3)
INSERT INTO `question` (`exam_id`, `type`, `content`, `score`, `sort_order`) VALUES
(3, 1, '哪个SQL语句用于从数据库中检索数据？', 10, 1),
(3, 1, '哪个子句用于在SQL中过滤记录？', 10, 2),
(3, 3, 'PRIMARY KEY约束唯一标识表中的每条记录。', 10, 3),
(3, 2, '以下哪些是有效的SQL JOIN类型？（多选）', 10, 4),
(3, 1, '哪个SQL函数返回行数？', 10, 5);

-- 数据库题目选项
INSERT INTO `question_option` (`question_id`, `option_key`, `content`, `is_correct`, `sort_order`) VALUES
-- 题目21
(21, 'A', 'SELECT', 1, 1),
(21, 'B', 'GET', 0, 2),
(21, 'C', 'FETCH', 0, 3),
(21, 'D', 'RETRIEVE', 0, 4),
-- 题目22
(22, 'A', 'WHERE', 1, 1),
(22, 'B', 'FILTER', 0, 2),
(22, 'C', 'HAVING', 0, 3),
(22, 'D', 'CONDITION', 0, 4),
-- 题目23（判断）
(23, 'A', '正确', 1, 1),
(23, 'B', '错误', 0, 2),
-- 题目24（多选）
(24, 'A', 'INNER JOIN', 1, 1),
(24, 'B', 'LEFT JOIN', 1, 2),
(24, 'C', 'RIGHT JOIN', 1, 3),
(24, 'D', 'MIDDLE JOIN', 0, 4),
-- 题目25
(25, 'A', 'COUNT()', 1, 1),
(25, 'B', 'SUM()', 0, 2),
(25, 'C', 'TOTAL()', 0, 3),
(25, 'D', 'NUM()', 0, 4);
