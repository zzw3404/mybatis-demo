# mybatis-demo

课程 MyBatis 学习项目，按《第1节 Mybatis的组件_详细版.html》建立。

## 环境

- JDK 17（Maven 编译目标为 Java 8）
- Maven Wrapper 3.9.9
- MyBatis 3.5.19
- MySQL Connector/J 8.4.0
- JUnit 4.13.2

## 第一次使用

1. 复制 `src/main/resources/db.properties.example` 到 `src/main/resources/common/db.properties`，填写本机 MySQL 账号密码。
2. 在 MySQL 中按需执行 `sql/User_db.sql`、`sql/chapter02_dept_emp.sql` 和 `sql/chapter05_schema.sql`。chapter05 脚本会补建技能关系表及供应商、商品、客户业务表，不会删除已有数据。
3. 在项目根目录执行 `./mvnw.cmd test`（Windows）或 `./mvnw test`（macOS/Linux）。

## 章节

- `chapter01`：MyBatis 基础配置、XML CRUD、日志和注解。
- `chapter02`：`resultType`/`resultMap`、VO、员工与部门关系映射。
- `chapter05`：员工/部门一对一、多对一、一对多和员工/技能多对多；另含截图要求的供应商/商品/客户采购及售后查询。

chapter05 的配置文件为 `src/main/resources/chapter05/mybatis-config.xml`。数据库脚本为 `sql/chapter05_schema.sql`。

如果终端提示 `JAVA_HOME` 未设置，可在 Windows 用户环境变量中设置：
`JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot`，并将 `%JAVA_HOME%\bin` 加入 `Path`。

`db.properties` 已被 `.gitignore` 排除，不要把真实密码提交到 Git。
