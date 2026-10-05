# Chapter05 关联映射综合总结

本文对照项目中的 XML 和注解实现，记录一对一/多对一、一对多、多对多映射的实现位置、验证方式和 SQL 次数。映射使用员工 `emp`、部门 `dept`、技能 `skill` 及关系表 `emp_skill`。

## 实现对照

| 关系 | XML 实现 | 注解实现 | 映射特点 |
|---|---|---|---|
| 一对一 / 多对一：员工 → 部门 | `Chapter05EmpMapper.xml` 的 `empWithDeptMap` 用 JOIN + `<association>`；`empWithDeptSelectMap` 用 `<association select="..." column="deptno">` | `Chapter05EmpMapper` 中 `@One(select = "...Chapter05DeptMapper.findById")` | 员工对象持有单个 `Dept`。JOIN 结果中员工部门号使用 `emp_deptno` 别名，避免与部门主键 `deptno` 冲突。 |
| 一对多：部门 → 员工 | `Chapter05DeptMapper.xml` 用 JOIN + `<collection>` | `Chapter05DeptMapper.one2manyByAnn` 用 `@Many(select = "...Chapter05EmpMapper.selectListByDeptno")` | `Dept.emps` 是员工集合。JOIN 结果里部门与员工各自的主键都用 `<id>`，让 MyBatis 正确合并重复行。 |
| 多对多：员工 → 技能 | `Chapter05EmpMapper.xml` 经 `emp_skill` 两次 JOIN，并用 `<collection>` | `Chapter05EmpMapper.many2manyByAnn` 用 `@Many` 调用 `selectSkillsByEmpno` | `Emp.skills` 是技能集合。中间表保存关系；员工主键用于归并，技能列用别名映射，SQL 使用 `DISTINCT`。 |
| 多对多反向：技能 → 员工 | `Chapter05SkillMapper.xml` 经 `emp_skill` JOIN 员工，并映射 `<collection property="emps">` | `Chapter05SkillMapper.findByIdWithEmployeesByAnnotation` 用 `@Many` 调用 `findEmployeesBySkillId` | `Skill.emps` 表示掌握该技能的员工集合；员工子查询不再加载技能集合，避免双向递归。 |

## SQL 次数实测

次数由测试专用 `SqlStatementCounter` 拦截 MyBatis 的 `StatementHandler.prepare` 统计。嵌套查询只有在访问关联属性时才会触发延迟加载；以下比较都访问了关联数据：

| 查询场景 | JOIN / 嵌套结果 | 嵌套查询 | 当前测试数据的解释 |
|---|---:|---:|---|
| 查询一个员工及其部门 | 1 | 2 | 嵌套查询为员工查询 1 次 + 部门查询 1 次。 |
| 查询全部员工及其部门 | 1 | 1 + D | `D` 是结果中不同的部门编号数量。当前数据 `D = 3`，所以为 4 次；同一 SqlSession 的本地缓存复用了相同部门查询。 |
| 查询一个部门及其员工 | 1 | 2 | 嵌套查询为部门查询 1 次 + 员工集合查询 1 次。 |
| 查询一个员工及其技能 | 1 | 2 | 嵌套查询为员工查询 1 次 + 技能集合查询 1 次。 |

上述次数是当前查询形态和测试数据下的实测值，不是所有数据规模下的固定常数。尤其多对一列表嵌套查询的次数随不同部门数量变化；更换数据或 SqlSession 缓存策略后，结果也可能不同。

## 代码与验证入口

- 关联 Mapper：`src/main/java/com/example/chapter05/mapper/Chapter05EmpMapper.java`、`Chapter05DeptMapper.java`、`Chapter05SkillMapper.java`。
- XML 映射：`src/main/resources/chapter05/mapper/`。
- MyBatis 延迟加载配置：`src/main/resources/chapter05/mybatis-config.xml` 中 `lazyLoadingEnabled=true`、`aggressiveLazyLoading=false`。
- 结果一致性和 SQL 次数测试：`src/test/java/com/example/chapter05/Chapter05Test.java`。
- SQL 统计拦截器：`src/test/java/com/example/chapter05/SqlStatementCounter.java`。

运行全部测试：

```powershell
.\mvnw.cmd test
```

## 选择建议

- 需要列表一次取回关联数据、希望避免 N+1 查询时，优先考虑 JOIN 嵌套结果。
- 只有访问关联属性时才需要关联数据、关联对象不总被使用时，可以考虑嵌套查询与延迟加载；应结合日志或统计确认查询次数。
- 多对多必须经中间表连接。JOIN 映射需处理重复行与主键归并；嵌套查询更易拆分职责，但批量读取时要留意查询放大。
- XML 和注解应按团队/课程要求统一风格；本章保留两种实现用于学习与结果对照。
