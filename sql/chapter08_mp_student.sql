-- Chapter 08 MyBatis-Plus practice table.
-- Additive and safe to rerun: this script never drops or truncates tables.
USE mybatis_db;

CREATE TABLE IF NOT EXISTS mp_student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    age         INT          DEFAULT NULL COMMENT '年龄',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    major       VARCHAR(50)  DEFAULT NULL COMMENT '专业',
    score       DOUBLE       DEFAULT NULL COMMENT '分数',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，1已删除',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MyBatis-Plus 学生练习表';
