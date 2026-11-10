-- ============================================================
-- 校园二手交易平台 数据库初始化脚本
-- 执行方式：mysql -u root -p < sql/init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_secondhand;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no  VARCHAR(20)  NOT NULL                COMMENT '学号（登录账号）',
    nickname    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    password    VARCHAR(100) DEFAULT NULL            COMMENT '密码',
    phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    school      VARCHAR(100) DEFAULT NULL            COMMENT '学院',
    credit      INT          DEFAULT 0               COMMENT '积分',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      DEFAULT 0               COMMENT '逻辑删除 0否1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS tb_goods (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(100)  NOT NULL                 COMMENT '标题',
    description   TEXT                                   COMMENT '描述',
    category      VARCHAR(50)   DEFAULT NULL             COMMENT '分类',
    price         DECIMAL(10,2) NOT NULL                 COMMENT '价格',
    images        VARCHAR(1000) DEFAULT NULL             COMMENT '图片URL逗号分隔',
    status        TINYINT       DEFAULT 1                COMMENT '状态 1在售 2已售 3下架',
    seller_id     BIGINT        NOT NULL                 COMMENT '卖家id',
    longitude     DOUBLE        DEFAULT NULL             COMMENT '经度',
    latitude      DOUBLE        DEFAULT NULL             COMMENT '纬度',
    location      VARCHAR(100)  DEFAULT NULL             COMMENT '位置描述',
    condition_desc VARCHAR(200) DEFAULT NULL             COMMENT '成色描述',
    view_count    INT           DEFAULT 0                COMMENT '浏览量',
    stock         INT           DEFAULT 1                COMMENT '库存',
    is_hot        TINYINT       DEFAULT 0                COMMENT '是否热门抢购 0否1是',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       DEFAULT 0                COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_seller (seller_id),
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS tb_order (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no    VARCHAR(40)   NOT NULL                 COMMENT '订单号',
    goods_id    BIGINT        NOT NULL                 COMMENT '商品id',
    buyer_id    BIGINT        NOT NULL                 COMMENT '买家id',
    seller_id   BIGINT        NOT NULL                 COMMENT '卖家id',
    price       DECIMAL(10,2) NOT NULL                 COMMENT '成交价',
    status      TINYINT       DEFAULT 1                COMMENT '状态 1已拍 2面交中 3已完成 4已取消',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_buyer (buyer_id),
    KEY idx_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 关注表
CREATE TABLE IF NOT EXISTS tb_follow (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT   NOT NULL                 COMMENT '关注者id',
    follow_user_id BIGINT   NOT NULL                 COMMENT '被关注者id',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_follow (user_id, follow_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 评价表（交易完成后互评）
CREATE TABLE IF NOT EXISTS tb_review (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id       BIGINT       NOT NULL                COMMENT '订单id',
    reviewer_id    BIGINT       NOT NULL                COMMENT '评价人id',
    target_user_id BIGINT       NOT NULL                COMMENT '被评价人id',
    score          TINYINT      NOT NULL                COMMENT '评分 1-5',
    content        VARCHAR(500) DEFAULT NULL            COMMENT '评价内容',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_reviewer (order_id, reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 评论表（商品留言，支持回复）
CREATE TABLE IF NOT EXISTS tb_comment (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    goods_id    BIGINT       NOT NULL                COMMENT '商品id',
    user_id     BIGINT       NOT NULL                COMMENT '评论人id',
    parent_id   BIGINT       DEFAULT 0               COMMENT '父评论id，0=顶层',
    content     VARCHAR(500) NOT NULL                COMMENT '评论内容',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_goods (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 收藏表
CREATE TABLE IF NOT EXISTS tb_favorite (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL                 COMMENT '用户id',
    goods_id    BIGINT   NOT NULL                 COMMENT '商品id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_goods (user_id, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 站内消息表
CREATE TABLE IF NOT EXISTS tb_message (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    from_user_id BIGINT       NOT NULL                COMMENT '发送者id，0=系统',
    to_user_id   BIGINT       NOT NULL                COMMENT '接收者id',
    type         TINYINT      NOT NULL                COMMENT '类型 1下单 2评论 3关注 4评价',
    content      VARCHAR(500) DEFAULT NULL            COMMENT '消息内容',
    goods_id     BIGINT       DEFAULT NULL            COMMENT '关联商品id',
    is_read      TINYINT      DEFAULT 0               COMMENT '是否已读 0否1是',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_to_user (to_user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ============================================================
-- 初始化测试数据（可选，便于开发调试）
-- ============================================================
INSERT INTO tb_user (student_no, nickname, school, credit)
VALUES ('20210001', '测试同学', '计算机学院', 0);

INSERT INTO tb_goods (title, description, category, price, status, seller_id, longitude, latitude, location, condition_desc, stock, is_hot)
VALUES
('高等数学教材', '同济版高数第七版，九成新，无笔记', '书籍', 15.00, 1, 1, 118.7900, 31.9388, '将军路校区', '九成新', 1, 0),
('iPad 2021', 'iPad 9代 64G WiFi，功能正常，送保护壳', '数码', 1800.00, 1, 1, 118.7900, 31.9388, '将军路校区', '九成新', 1, 0),
('通勤自行车', '通勤自行车，车况良好，可试骑', '生活用品', 200.00, 1, 1, 118.7900, 31.9388, '将军路校区', '八成新', 1, 0),
('机械键盘', 'Cherry 红轴机械键盘，按键灵敏', '数码', 150.00, 1, 1, 118.7900, 31.9388, '将军路校区', '九成新', 5, 1);
