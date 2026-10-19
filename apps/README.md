> 最新功能补充与运行方法见《交易系统报告补充说明.md》；本文的初版测试数量和功能范围以补充说明为准。

# 中文控制台 MySQL 运行说明

控制台及 Spring Boot 用户接口现在默认使用原项目的 MySQL 连接，读取 E:/mybatis-demo/src/main/resources/common/db.properties。实际已连接到 mybatis_db。启动时不再默认使用 H2；H2 仅用于自动化测试。

## 启动

先退出之前运行的旧控制台和旧版用户接口，再在 PowerShell 中运行：

```powershell
cd E:\mybatis-demo
.\run-apps.cmd
```

当前 JAR 已重新编译，可以直接启动。首页会显示当前数据库，例如：

```text
当前连接：MySQL / mybatis_db；员工 emp.empno，部门 dept.deptno，学生 mp_student
所有查询与维护使用当前数据库，修改提交后会持久保存。
```

需要 JDK 17。修改代码后用 `.\build-apps.cmd` 重新构建并执行测试。脚本不会将数据库密码写入命令行或新增配置文件。

## 实际使用的表

| 功能 | MySQL 表 |
|---|---|
| 员工、部门、岗位、工资、佣金与组织联查 | emp、dept |
| 学生分页、条件构造器、乐观锁和逻辑删除 | mp_student |
| 用户接口 XML/注解 CRUD | user，使用 created_at、updated_at 时间字段 |
| 供应商、商品与供应目录 | supplier、product、supplier_product |
| 客户、采购及售后服务 | customer、purchase、product_service |
| 用户角色与权限 | acl_user、acl_role、acl_resource、acl_member、acl_grant |
| 员工登录账号 | login_account，staff_id 关联 emp.empno |
| 采购记录对应的实物扩展 | console_product_item |

原来控制台中的 person/team/learner/vendor 演示映射不再用于正常运行。运行时直接使用原项目的表和真实编号。实体内部仍采用统一的视图字段名，staffId 的值来自 emp.empno，deptId 的值来自 dept.deptno。

## 缺少数据库或表

连接 URL 自动启用 createDatabaseIfNotExist=true。配置指向的数据库不存在时，会使用配置中的数据库账号自动创建，再建立缺少的表；账号需要具有对应的建库、建表权限。

初始化使用 CREATE TABLE IF NOT EXISTS。只为本次新建的表补充初始数据，不会以新的演示数据覆盖已有表内容。已有 emp/dept、user、学生、销售数据会直接使用。真实业务模式下，旧的演示重置入口已从菜单移除，相应重置命令也会拒绝清空原数据。

原销售表没有逐件实物表，因此新增 console_product_item：按 purchase.quantity 建立逐件关联记录，P采购编号-序号是系统生成的记录标识，不是从外部采集的设备序列号。原采购记录不改动。

## 主菜单

```text
1. 多表联查与关联映射
2. 用户、角色与权限管理
3. 部门、员工与登录账号
4. 供应商、销售与售后
5. 学生管理与事务实验
6. 条件构造器查询实验
7. Spring Boot 用户接口
8. 数据库状态与全部概况
9. 命令帮助
0. 退出
```

查询部门或员工之前会列出当前数据库中的真实编号及名称。菜单序号不是数据主键。

## 与当前数据对应的例子

- 主菜单 1 → 2：输入部门 1，查询财务管理部及员工 7、9、14。
- 主菜单 1 → 4：输入员工 10，查询凯申，所属部门为 3 市场运营部。
- 主菜单 1 → 7：三个可选条件全部回车，查询真实采购、商品、供应商和客户关联数据。
- 主菜单 8：查看当前连接的数据库、各表记录数和全部模块概况。

也可以直接运行命令：

```powershell
.\run-apps.cmd org-dept-join 1
.\run-apps.cmd org-staff-join 10
.\run-apps.cmd sold
.\run-apps.cmd db-status
.\run-apps.cmd mp-status
.\run-apps.cmd --json help
```

默认输出中文表格；宽表按字段分组，并用重复主键对应同一记录。`--sql` 显示 SQL，`--json` 用于脚本读取。

## 分页与高级查询

学生分页位于主菜单 5 → 1，可以设置页码与每页条数。权限用户筛选分页位于主菜单 2 → 2。学生高级查询位于主菜单 6，支持专业、分数范围、姓名、排序、括号 OR、IN、部分字段、Map、聚合及批量条件更新。

当前 MySQL 的 mp_student 有 11 条记录，但全部 deleted=1，所以普通查询和分页显示 0 条。这些记录仍在数据库中。主菜单 5 的“查看学生数据与逻辑删除状态”或 mp-status 可以核对。录入新的学生会持久写入 mp_student，并正常参与分页及条件查询；本次没有批量恢复或重写已有学生。

## 用户与组织接口

主菜单 7 通过真实 HTTP 调用 Spring Boot 的 6 个用户接口，读取同一个 mybatis_db.user。首次使用会启动随项目打包的用户应用，并核对它的数据库身份，避免复用连接到旧 H2 数据的服务。退出控制台会关闭它自行启动的服务；MySQL 数据会保留。

用户接口可单独运行：

```powershell
.\run-apps.cmd user-api
```

默认地址 http://127.0.0.1:8080 。GET /user/database 返回数据库类型、名称和 user 表标识，不包含账号密码。原有 /user/findAll、/user/{id}、/user/search、POST /user、PUT /user、DELETE /user/{id} 均保留。

组织人事接口可从组织菜单启动，也可运行 `.\run-apps.cmd web 8090`；同样使用现有 emp/dept 与关联账号表，保留 18 个组织接口。

## 连接配置

直接使用原项目 common/db.properties，不需要再重复填写。需要指定其他配置文件时，可设置 PROJECT_DB_CONFIG。PERMISSION_DB_URL/DRIVER/USERNAME/PASSWORD 可显式覆盖控制台连接；用户接口默认沿用同一配置，USER_DB_* 可以显式覆盖。如果用户接口连接的数据库与控制台不一致，控制台会提示关闭旧服务或修正配置，不会悄悄使用另一套数据。

## 验证

已通过 60 个自动化测试，并在真实 MySQL 中验证员工与部门联查、跨连接保存员工、修改、调岗、逻辑删除、销售关联、学生查询，以及全部 6 个用户接口的增删改查。临时验证记录已清理。

数据库不存在的自动创建流程也已经实测；临时验证数据库已清理。原有 12 张业务表在验证前后的全部行内容指纹一致。
