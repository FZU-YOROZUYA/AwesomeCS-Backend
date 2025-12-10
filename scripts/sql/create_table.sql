create database `awesome_cs`;

use `awesome_cs`;

create table comments
(
    id         bigint unsigned                    not null
        primary key,
    post_id    bigint unsigned                    not null,
    user_id    bigint unsigned                    not null,
    parent_id  bigint unsigned                    null comment '父评论ID，用于实现回复功能',
    content    text                               not null,
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment '文章评论表';

create table consultation_messages
(
    id              bigint unsigned auto_increment
        primary key,
    consultation_id bigint unsigned                       not null,
    sender_id       bigint unsigned                       not null,
    content         text                                  not null,
    message_type    varchar(32) default 'text'            null comment 'text/image/…',
    created_at      datetime    default CURRENT_TIMESTAMP not null
)
    comment '咨询聊天消息（可选）';

create index idx_messages_consultation
    on consultation_messages (consultation_id);

create table consultation_payments
(
    id              bigint unsigned auto_increment
        primary key,
    consultation_id bigint unsigned                     not null,
    amount          decimal(10, 2)                      not null,
    status          tinyint   default 0                 not null comment '0-pending,1-success,2-failed',
    provider        varchar(64)                         null comment '支付渠道标识（例如 alipay/wechat）',
    transaction_id  varchar(128)                        null comment '第三方交易号，用于幂等校验',
    created_at      timestamp default CURRENT_TIMESTAMP not null,
    constraint uk_pay_transaction
        unique (provider, transaction_id)
)
    comment '咨询支付流水';

create index idx_payment_consultation
    on consultation_payments (consultation_id);

create table consultation_relation
(
    id         bigint                              not null
        primary key,
    user_id    bigint                              not null comment '创建者id',
    price      double                              not null,
    domains    varchar(1024)                       null comment '咨询领域',
    created_at timestamp default CURRENT_TIMESTAMP null
);

create table consultations
(
    id         bigint unsigned                    not null
        primary key,
    expert_id  bigint unsigned                    not null comment '专家（提供咨询的用户）ID',
    seeker_id  bigint unsigned                    not null comment '咨询者ID',
    status     tinyint  default 0                 null comment '状态：0-待支付，1-已预约，2-已完成，3-已取消',
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment '付费咨询表';

create table mock_interviews
(
    id            bigint unsigned                    not null
        primary key,
    user_id       bigint unsigned                    not null,
    domain        varchar(32)                        not null comment '面试领域',
    style         varchar(32)                        null comment '面试风格',
    recording_url varchar(1024)                      null comment '面试录音/录像存储URL',
    created_at    datetime default CURRENT_TIMESTAMP null
)
    comment '模拟面试记录表';

create table post_likes
(
    id         bigint unsigned                    not null
        primary key,
    user_id    bigint unsigned                    not null,
    post_id    bigint unsigned                    not null,
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment '文章点赞表';

create table posts
(
    id         bigint unsigned                        not null
        primary key,
    user_id    bigint unsigned                        not null,
    category   varchar(64)                            null comment '文章分类',
    tags       json                                   null comment 'tags',
    title      varchar(200)                           not null,
    content    longtext                               not null,
    summary    varchar(500)                           null comment '文章摘要',
    status     tinyint      default 0                 null comment '状态：0-草稿，1-已发布，2-已删除',
    view_count int unsigned default '0'               null comment '浏览量',
    created_at datetime     default CURRENT_TIMESTAMP null,
    updated_at datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '博客文章表';

create table study_path_recommendations
(
    id         bigint unsigned                    not null
        primary key,
    user_id    bigint unsigned                    not null,
    content    text                               not null comment 'AI推荐的学习内容',
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment 'AI学习路径推荐表';

create table users
(
    id         bigint unsigned                    not null
        primary key,
    phone      varchar(20)                        not null comment '手机号（唯一标识）',
    nickname   varchar(50)                        not null,
    password   varchar(1024)                      not null,
    avatar     varchar(255)                       null comment '用户头像URL',
    bio        varchar(500)                       null comment '个人简介',
    user_data  json                               null comment '存储兴趣、技术栈等扩展信息',
    status     tinyint  default 1                 null comment '账户状态：1-正常，0-禁用',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint nickname
        unique (nickname),
    constraint phone
        unique (phone)
)
    comment '用户基础信息表';

