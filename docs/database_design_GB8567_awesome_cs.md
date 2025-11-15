# 数据库设计说明书（GB8567——88）

## 引言

### 编写目的

说明本说明书用于描述 AwesomeCS 系统所用数据库（awesome_cs）的结构、约定、使用与维护要求，供开发、测试、运维与维护人员参考。预期读者包括后端开发人员、DBA、测试人员和运维人员。

### 背景

1. 待开发数据库名称：`awesome_cs`。
2. 使用该数据库的软件系统名称：AwesomeCS（包含博客、点赞、评论、付费咨询、模拟面试等模块）。
3. 项目提出者、用户与部署环境：项目需求方、系统最终用户及将安装该软件和数据库的计算站（开发/测试/生产环境），具体部署由运维团队负责。

### 定义

- JSON 字段：数据库中以 JSON 类型或文本形式存储的复合数据结构（如 tags、domains、user_data）。
- relation：咨询关系项（专家发布的可提供咨询的条目）。
- consultation：咨询会话或订单。
- mock_interview：模拟面试记录。

### 参考资料

- 项目需求文档
- MySQL 官方手册（建议 MySQL 5.7+ 以支持 JSON）
- 团队编码与数据库设计规范

## 外部设计

### 标识符和状态

- 数据库标识：`awesome_cs`。
- 部署状态：可在开发、测试、生产环境分别部署，需在文档中注明有效时间范围与版本。

### 使用它的程序

- AwesomeCS 后端服务（Spring Boot，按项目中声明的版本）
- WebSocket 服务模块（用于实时聊天）
- 后台任务、定时任务及管理后台

### 约定

- 表与字段使用下划线风格（snake_case）；后端实体使用驼峰风格。
- JSON 字段应存储标准 JSON，写入时保持小写与去重（视业务需要）。
- 日期时间统一使用 DATETIME/TIMESTAMP，API 层格式化输出。

### 专门指导

- 数据导入格式：确保文本与 JSON 字段的格式正确；字符集使用 utf8mb4。
- 数据库操作规范：通过后端 Service 层或专用脚本执行批量导入/更新，生产环境操作需经过变更控制流程。

### 支持软件

- 建议 DBMS：MySQL 5.7+ 或 MariaDB 支持 JSON 的版本。
- 存储引擎：InnoDB。
- 字符集：utf8mb4。
- 备份/恢复工具：mysqldump、Percona XtraBackup 等。

## 结构设计

### 概念结构设计

本数据库反映的现实世界实体与关系包括：

- 用户（users）
- 博客文章（posts）及标签、分类
- 文章点赞（post_likes）
- 文章评论（comments）及回复
- 咨询关系（consultation_relation）— 专家发布的咨询项
- 咨询会话（consultations）— 具体的咨询订单/会话
- 咨询支付流水（consultation_payments）
- 咨询消息（consultation_messages）— 可选的聊天消息持久化
- 模拟面试记录（mock_interviews）
- 学习路径推荐（study_path_recommendations）

对于每一实体，字段类型、约束与说明见下层数据字典条目。

### 逻辑结构设计

- 关键字段与关系：

  - `posts.user_id` → `users.id`
  - `post_likes.post_id` → `posts.id`
  - `comments.post_id` → `posts.id`, `comments.parent_id` → `comments.id`
  - `consultation_relation.user_id` → `users.id`
  - `consultations.expert_id` / `consultations.seeker_id` → `users.id`
  - `consultation_payments.consultation_id` → `consultations.id`
  - `consultation_messages.consultation_id` → `consultations.id`

- 视图与 API：后端通过 DTO/响应类控制对外数据结构，数据库作为持久化层。

### 物理结构设计

- 内存与缓冲：数据库参数（innodb_buffer_pool_size 等）应根据数据量与并发调整。
- 外存组织：使用 InnoDB 页与事务日志，考虑分表/分库策略用于水平扩展（未来需求）。
- 访问方式：主从复制用于读写分离；关键写操作（支付、点赞）通过事务或幂等设计保证一致性。

## 运用设计

### 数据字典设计（概要）

以下为主要表的字段说明（精简版）：

- users

  - id BIGINT PK
  - phone VARCHAR(20) UNIQUE
  - nickname VARCHAR(50) UNIQUE
  - password VARCHAR(1024)（加密存储，例如 bcrypt/argon2）
  - avatar VARCHAR(255)
  - bio VARCHAR(500)
  - user_data JSON
  - status TINYINT DEFAULT 1
  - created_at, updated_at

- posts

  - id BIGINT PK
  - user_id BIGINT
  - category VARCHAR(64)
  - tags JSON
  - title VARCHAR(200)
  - content LONGTEXT
  - summary VARCHAR(500)
  - status TINYINT DEFAULT 0
  - view_count INT DEFAULT 0
  - created_at, updated_at
  - 索引建议：user_id、status；若需全文搜索，使用 FULLTEXT 或 Elasticsearch

- post_likes

  - id BIGINT PK
  - user_id BIGINT
  - post_id BIGINT
  - created_at
  - 索引：(post_id)、(user_id)

- comments

  - id BIGINT PK
  - post_id BIGINT
  - user_id BIGINT
  - parent_id BIGINT NULL
  - content TEXT
  - created_at

- consultation_relation

  - id BIGINT PK
  - user_id BIGINT NOT NULL
  - price DOUBLE NOT NULL
  - domains VARCHAR(1024) 或 JSON
  - created_at

- consultations

  - id BIGINT PK
  - expert_id BIGINT
  - seeker_id BIGINT
  - status TINYINT DEFAULT 0
  - created_at
  - 索引：expert_id、seeker_id、status

- consultation_payments

  - id BIGINT PK AUTO_INC
  - consultation_id BIGINT
  - amount DECIMAL(10,2)
  - status TINYINT DEFAULT 0 (0-pending,1-success,2-failed)
  - provider VARCHAR(64)
  - transaction_id VARCHAR(128)
  - created_at
  - 唯一约束：UNIQUE(provider, transaction_id)

- consultation_messages

  - id BIGINT PK AUTO_INC
  - consultation_id BIGINT
  - sender_id BIGINT
  - content TEXT
  - message_type VARCHAR(32) DEFAULT 'text'
  - created_at
  - 索引：consultation_id

- mock_interviews

  - id BIGINT PK
  - user_id BIGINT
  - domain VARCHAR(32)
  - style VARCHAR(32)
  - recording_url VARCHAR(1024)
  - created_at

- study_path_recommendations
  - id BIGINT PK
  - user_id BIGINT
  - content TEXT
  - created_at

### 安全保密设计

- 访问控制：按角色分配数据库账号权限，原则最小权限。
- 敏感数据：密码使用不可逆加密存储（bcrypt/argon2）；生产环境启用 TLS 连接。
- 审计日志：记录支付回调、订单状态变化等关键日志以便追溯。

## 物理设计与索引建议（详细）

- 引擎：InnoDB
- 字符集：utf8mb4
- 索引建议：
  - posts(user_id), posts(status), posts(created_at)
  - post_likes(post_id), post_likes(user_id)
  - comments(post_id), comments(parent_id)
  - consultation_payments(consultation_id)
  - consultation_messages(consultation_id)
- tags/domains 查询建议：
  - 使用 JSON 类型并配合 JSON_CONTAINS 查询，或在写入时同步生成标准化字段并为其建立索引
  - 若频繁模糊搜索，考虑引入搜索引擎（Elasticsearch）

## 事务与并发控制

- 点赞：使用事务保证插入/删除与计数一致；高并发下使用 Redis 缓存计数并定时落盘。
- 支付流程：支付回调必须幂等处理，使用 consultation_payments 的 (provider, transaction_id) 唯一约束保证幂等性，并在事务中更新 consultations 状态。
- view_count：高并发场景下使用 Redis 累积并周期性同步到数据库。

## 运维与专门指导

- 备份策略：定期全量备份 + binlog 增量；灾备演练。
- 数据迁移：使用有序的 SQL 迁移脚本（Flyway 或 Liquibase）。
- 日志与监控：监控慢查询、连接数、IO、buffer pool 使用率；设置慢查询阈值并优化。

## 数据字典维护建议

- 建议维护机器可读的数据字典（CSV/Excel）包含：表名、字段名、类型、是否主键、是否允许空、默认值、注释、索引、外键说明。

## 安全与保密（补充）

- 加密：对敏感字段进行加密存储或在应用层加密。
- 访问日志：对支付、提现、数据导出等敏感操作记录操作人、时间与变更内容。
