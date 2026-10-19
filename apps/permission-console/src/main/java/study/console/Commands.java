package study.console;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import study.console.Models.*;
import static study.console.Database.Intent.READ;
import static study.console.CatalogSql.Kind.*;

/** A registry replaces an intertwined menu/command switch; each operation has one handler. */
public final class Commands implements AutoCloseable {
    private final UserGateway userApi;
    private record Operation(String usage,Function<List<String>,Object> action) { }
    private final Map<String,Operation> registry=new LinkedHashMap<>();
    private final Database db; private final AccessControl acl;private final Organization org;private final StudentLab lab;
    public Commands(Database database){this(database,new UserGateway());}
    public Commands(Database database,UserGateway gateway){
        userApi=gateway;db=database;acl=new AccessControl(db);org=new Organization(db);lab=new StudentLab(db);
        add("users","用户清单",a->acl.list(USER).stream().map(row->{row.put("password","******");return row;}).toList());
        add("roles","角色清单",a->acl.list(ROLE));add("perms","权限清单",a->acl.list(RESOURCE));alias("permissions","perms");
        add("tree","权限树",a->acl.permissionTree());add("user","<用户ID> 用户角色及权限",a->acl.userGraph(requiredId(a,0)));
        add("check","<用户ID> <权限代码>",a->Map.of("allowed",acl.allowed(requiredId(a,0),required(a,1))));
        add("login","<用户名> <密码>",a->Map.of("authenticated",acl.authenticate(required(a,0),required(a,1))!=null));
        add("suppliers","供应商、商品与客户清单",a->trade(m->Map.of("suppliers",m.vendors(),"products",m.goods(),"customers",m.buyers())));alias("basics","suppliers");
        add("items","含未售出实物",a->trade(TradeMapper::inventory));add("catalog","供应商与商品目录",a->trade(TradeMapper::catalog));
        add("sold","[商品ID] [供应商ID] [客户ID]",a->{var f=new LinkedHashMap<String,Object>();put(f,"productId",id(a,0));put(f,"supplierId",id(a,1));put(f,"customerId",id(a,2));return trade(m->m.sold(f));});
        add("supplier","<供应商ID> 销售及服务明细",a->trade(m->m.profile(requiredId(a,0))));
        add("buyers","<供应商ID> 采购客户",a->trade(m->m.purchasingClients(requiredId(a,0))));
        add("served","<供应商ID> 服务客户",a->trade(m->m.serviceClients(requiredId(a,0))));
        add("services","<供应商ID> 服务记录",a->trade(m->m.serviceRows(requiredId(a,0))));
        add("soldproducts","<供应商ID> 已售商品种类",a->trade(m->m.soldKinds(requiredId(a,0))));
        add("reload","重置销售演示数据",a->{if(db.usesProjectTables())throw new IllegalArgumentException("当前使用真实销售表，不能用演示重置覆盖已有数据");db.runScript("database/reset-sales.sql");return "销售数据已重置";});
        add("org-depts","部门汇总",a->org.overview());alias("org-overview","org-depts");
        add("org-dept","<部门ID> [join]",a->org.department(requiredId(a,0),"join".equals(text(a,1))));
        add("org-staff","<员工ID>",a->org.employee(requiredId(a,0),true));
        add("org-search","[关键字] [岗位] [部门ID] [在职1/0]",a->{var f=new LinkedHashMap<String,Object>();put(f,"keyword",text(a,0));put(f,"post",text(a,1));put(f,"deptId",id(a,2));put(f,"onJob",id(a,3));return org.search(f);});
        add("org-pay","薪酬聚合统计",a->org.pay());add("org-accounts","账号清单",a->org.accounts(false));
        add("org-login","<登录名> <口令>",a->org.login(required(a,0),required(a,1)));
        add("org-demo","账号生命周期事务回滚",a->org.rehearseAccount());
        add("org-hire","<姓名> <部门ID> [岗位] [工资]",a->{Person p=new Person();p.staffName=required(a,0);p.deptId=requiredId(a,1);p.post=text(a,2);p.basePay=decimal(a,3);return org.hire(p);});
        add("org-transfer","<员工ID> <部门ID>",a->org.move(requiredId(a,0),requiredId(a,1)));
        add("org-reset","重置组织人事数据",a->{org.reset();return "组织数据已重置";});
        add("cache-local","一级缓存实测与实际执行SQL",a->new LocalCacheLab(db).display());
        add("mp-page","[页码] [每页条数] [专业] [最低分] [最高分] [姓名片段] [排序id/score/age/major] [or]",a->lab.page(integer(a,0,1),integer(a,1,5),criteria(a.size()>2?a.subList(2,a.size()):List.of()),true));
        add("mp-students","[页码] [每页条数]",a->lab.page(integer(a,0,1),integer(a,1,5)));
        add("mp-student","<学生ID>",a->lab.find(requiredId(a,0)));add("mp-stats","专业统计SQL",a->lab.stats());
        add("mp-top","[最低分] [人数]",a->lab.top(number(a,0,80),integer(a,1,5)));
        add("mp-score","<学生ID> <新分数>",a->lab.changeScore(requiredId(a,0),Double.parseDouble(required(a,1)),null));
        add("mp-expel","<学生ID>",a->lab.expel(List.of(requiredId(a,0))));
        add("mp-demo","主键、自动填充、锁与删除事务演示",a->lab.rehearse());
        add("mp-reset","重置学生数据",a->{lab.reset();return "学生数据已重置";});
        add("mp-filter","<专业> [最低分] [最高分] [姓名] [排序] [or]",a->lab.query(criteria(a),false));
        add("mp-lambda","同mp-filter，使用Lambda",a->lab.query(criteria(a),true));
        add("mp-compare","比较两种条件构造器",a->{var f=criteria(a);var x=lab.query(f,false);var y=lab.query(f,true);return Map.of("equal",x.data().stream().map(Learner::getId).toList().equals(y.data().stream().map(Learner::getId).toList()),"columnQuery",x,"lambdaQuery",y);});
        add("mp-count","<专业>",a->lab.count(required(a,0)));add("mp-one","<姓名>",a->{var result=new LinkedHashMap<String,Object>();result.put("count",lab.occurrences(required(a,0)));result.put("result",lab.unique(required(a,0)));return result;});
        add("mp-slim","条件查询部分字段",a->lab.slim(criteria(a)));add("mp-maps","条件查询Map结果",a->lab.maps(criteria(a)));add("mp-group","Wrapper分组统计",a->lab.grouped());
        add("mp-exact","<专业> <年龄>",a->{var f=new LinkedHashMap<String,Object>();put(f,"major",text(a,0));put(f,"age",id(a,1));return lab.exact(f);});
        add("mp-bulk","条件更新、字段策略与回滚演示",a->lab.rehearseWrappers());alias("mp-wrapper-demo","mp-bulk");
        add("mp-raise","<专业> <低于分> <目标分>",a->lab.raise(required(a,0),Double.parseDouble(required(a,1)),Double.parseDouble(required(a,2))));
        add("mp-wrappers","只读条件构造器巡览",a->Map.of("columns",lab.query(new StudentLab.Criteria(),false),"lambda",lab.query(new StudentLab.Criteria(),true),"slim",lab.slim(new StudentLab.Criteria()),"maps",lab.maps(new StudentLab.Criteria()),"groups",lab.grouped()));
        add("all","各模块概况",a->Map.of("organization",org.overview(),"permissions",acl.permissionTree(),"sales",trade(m->m.sold(Map.of())),"students",lab.page(1,5)));

        add("users-filter","[用户名片段] [状态] [页码] [每页条数]",a->{var f=new LinkedHashMap<String,Object>();put(f,"usernameLike",text(a,0));put(f,"status",id(a,1));int page=integer(a,2,1),size=integer(a,3,5);if(page<1||size<1)throw new IllegalArgumentException("分页参数无效");return acl.query(USER,f,(page-1)*size,size).stream().map(row->{row.put("password","******");return row;}).toList();});
        add("user-add","<用户名> <密码> <姓名> [邮箱]",a->{var f=new LinkedHashMap<String,Object>();f.put("username",required(a,0));f.put("password",required(a,1));f.put("realName",required(a,2));f.put("status",1);put(f,"email",text(a,3));var row=acl.create(USER,f);row.put("password","******");return row;});
        add("user-edit","<用户ID> <姓名> [邮箱] [状态]",a->{var f=new LinkedHashMap<String,Object>();f.put("realName",required(a,1));put(f,"email",text(a,2));put(f,"status",id(a,3));return acl.patch(USER,requiredId(a,0),f);});
        add("user-delete","<用户ID>",a->acl.delete(USER,requiredId(a,0)));
        add("role-detail","<角色ID> 关联权限",a->acl.roleGraph(requiredId(a,0)));
        add("role-add","<名称> [说明]",a->{var f=new LinkedHashMap<String,Object>();f.put("name",required(a,0));f.put("status",1);put(f,"description",text(a,1));return acl.create(ROLE,f);});
        add("role-edit","<角色ID> <名称> [说明]",a->{var f=new LinkedHashMap<String,Object>();f.put("name",required(a,1));put(f,"description",text(a,2));return acl.patch(ROLE,requiredId(a,0),f);});
        add("role-delete","<角色ID>",a->acl.delete(ROLE,requiredId(a,0)));
        add("perm-add","<父ID> <名称> <权限代码> <类型1/2/3>",a->acl.create(RESOURCE,Map.of("parentId",requiredId(a,0),"name",required(a,1),"code",required(a,2),"type",requiredId(a,3),"status",1)));
        add("perm-edit","<权限ID> <名称> [状态]",a->{var f=new LinkedHashMap<String,Object>();f.put("name",required(a,1));put(f,"status",id(a,2));return acl.patch(RESOURCE,requiredId(a,0),f);});
        add("perm-delete","<权限ID>",a->acl.delete(RESOURCE,requiredId(a,0)));
        add("user-role-add","<用户ID> <角色ID>",a->acl.bind("membership",requiredId(a,0),requiredId(a,1)));
        add("user-role-remove","<用户ID> [角色ID]（空表示该用户全部角色）",a->acl.unbind("membership",requiredId(a,0),id(a,1)));
        add("role-perm-add","<角色ID> <权限ID>",a->acl.bind("grant",requiredId(a,0),requiredId(a,1)));
        add("role-perm-remove","<角色ID> [权限ID]（空表示该角色全部权限）",a->acl.unbind("grant",requiredId(a,0),id(a,1)));
        add("org-dept-join","<部门ID> JOIN装载成员",a->org.department(requiredId(a,0),true));
        add("org-staff-nested","<员工ID> 嵌套查询部门",a->org.employee(requiredId(a,0),false));
        add("org-staff-join","<员工ID> JOIN装载部门",a->org.employee(requiredId(a,0),true));
        add("org-dept-add",db.usesProjectTables()?"<部门ID> <名称> [位置]":"<部门ID> <代码> <名称> [城市] [成本中心]",a->{Team t=new Team();t.deptId=requiredId(a,0);
            if(db.usesProjectTables()){t.deptName=required(a,1);t.city=text(a,2);}else{t.deptCode=required(a,1);t.deptName=required(a,2);t.city=text(a,3);t.costCenter=text(a,4);}return org.addDepartment(t);});
        add("org-dept-edit","<部门ID> <名称> [城市]",a->{Team t=new Team();t.deptId=requiredId(a,0);t.deptName=required(a,1);t.city=text(a,2);return org.editDepartment(t);});
        add("org-dept-delete","<部门ID>",a->org.removeDepartment(requiredId(a,0)));
        add("org-staff-edit","<员工ID> <岗位> [工资] [佣金]",a->{Person p=new Person();p.staffId=requiredId(a,0);p.post=required(a,1);p.basePay=text(a,2)==null?null:decimal(a,2);p.meritPay=text(a,3)==null?null:decimal(a,3);return org.editPerson(p);});
        add("org-leave","<员工ID> 离职",a->org.offDuty(requiredId(a,0)));
        add("org-active-accounts","可用账号",a->org.accounts(true));
        add("org-account-suffix","<邮箱后缀>",a->org.accountsWithSuffix(required(a,0)));
        add("org-account-add","<登录名> <口令> <邮箱> [员工ID]",a->{var f=new LinkedHashMap<String,Object>();f.put("loginName",required(a,0));f.put("secret",required(a,1));f.put("mailbox",required(a,2));put(f,"staffId",id(a,3));return org.register(f);});
        add("org-account-edit","<账号ID> <邮箱> [状态]",a->{var f=new LinkedHashMap<String,Object>();f.put("mailbox",required(a,1));put(f,"state",id(a,2));return org.patchAccount(requiredId(a,0),f);});
        add("org-account-disable","<账号ID>",a->org.patchAccount(requiredId(a,0),Map.of("state",0)));
        add("org-account-delete","<账号ID>",a->org.eraseAccount(requiredId(a,0)));
        add("mp-enroll","<姓名> <年龄> <专业> <分数> [邮箱]",a->{Learner l=new Learner();l.setName(required(a,0));l.setAge(Integer.valueOf(required(a,1)));l.setMajor(required(a,2));l.setScore(Double.valueOf(required(a,3)));l.setEmail(text(a,4));return lab.enroll(l);});
        add("mp-expel-batch","<学生ID列表，以逗号分隔>",a->lab.expel(Arrays.stream(required(a,0).split(",")).map(Long::valueOf).toList()));
        add("mp-name","<姓名片段> [contains/start/end]",a->{var f=new StudentLab.Criteria();f.name=required(a,0);f.pattern=switch(text(a,1)==null?"contains":text(a,1)){case "start"->StudentLab.Pattern.STARTS_WITH;case "end"->StudentLab.Pattern.ENDS_WITH;case "contains"->StudentLab.Pattern.CONTAINS;default->throw new IllegalArgumentException("匹配方式无效");};return lab.query(f,true);});
        add("mp-in","<专业列表，以逗号分隔>",a->{var f=new StudentLab.Criteria();f.majors=Arrays.asList(required(a,0).split(","));return lab.query(f,true);});


        add("roles-filter","[角色名称片段] [状态]",a->{var f=new LinkedHashMap<String,Object>();put(f,"nameLike",text(a,0));put(f,"status",id(a,1));return acl.query(ROLE,f,null,null);});
        add("perms-filter","[权限名称片段] [父ID] [类型]",a->{var f=new LinkedHashMap<String,Object>();put(f,"nameLike",text(a,0));put(f,"parentId",id(a,1));put(f,"type",id(a,2));return acl.query(RESOURCE,f,null,null);});
        add("user-role-list","<用户ID>",a->acl.links("membership",requiredId(a,0),null));
        add("role-user-list","<角色ID>",a->acl.links("membership",null,requiredId(a,0)));
        add("role-perm-list","<角色ID>",a->acl.links("grant",requiredId(a,0),null));
        add("perm-role-list","<权限ID>",a->acl.links("grant",null,requiredId(a,0)));
        add("org-account-detail","<账号ID>",a->org.account(requiredId(a,0)));
        add("api-users","全部用户接口",a->userApi.call("GET","/user/findAll",null));
        add("api-user","<用户ID>",a->userApi.call("GET","/user/"+requiredId(a,0),null));
        add("api-search","<用户名>",a->userApi.call("GET","/user/search?username="+UserGateway.escaped(required(a,0)),null));
        add("api-add","<用户名> <密码> <邮箱>",a->userApi.call("POST","/user",Map.of("username",required(a,0),"password",required(a,1),"email",required(a,2))));
        add("api-edit","<用户ID> <邮箱>",a->userApi.call("PUT","/user",Map.of("id",requiredId(a,0),"email",required(a,1))));
        add("api-delete","<用户ID>",a->userApi.call("DELETE","/user/"+requiredId(a,0),null));

        add("db-status","当前数据库、数据表及记录数量",a->db.status());
        add("mp-status","查看学生总数、未删除数和逻辑删除数",a->db.studentStatus());
        add("help","命令用法",a->help());
    }
    private <T>T trade(Function<TradeMapper,T> action){return db.nativeWork(READ,s->action.apply(s.getMapper(TradeMapper.class)));}
    private void add(String name,String usage,Function<List<String>,Object> action){registry.put(name,new Operation(usage,action));}
    private void alias(String name,String target){registry.put(name,registry.get(target));}
    public Object execute(String command,List<String> args){Operation op=registry.get(command);if(op==null)throw new IllegalArgumentException("未知命令："+command);return op.action.apply(args);}
    public void close(){userApi.close();}
    Database database(){return db;}
    public List<String> names(){return new ArrayList<>(registry.keySet());}
    public Map<String,String> help(){var entries=new LinkedHashMap<String,String>();registry.forEach((key,value)->entries.put(key,value.usage));entries.put("web","[端口] 启动组织人事HTTP接口，默认8090");return entries;}
    private static String text(List<String>a,int i){return i>=a.size() || a.get(i).isBlank() || a.get(i).equals("-")?null:a.get(i);}
    private static String required(List<String>a,int i){String x=text(a,i);if(x==null)throw new IllegalArgumentException("缺少第"+(i+1)+"个参数");return x;}
    private static Long id(List<String>a,int i){String x=text(a,i);return x==null?null:Long.valueOf(x);}
    private static long requiredId(List<String>a,int i){return Long.parseLong(required(a,i));}
    private static int integer(List<String>a,int i,int defaultValue){Long x=id(a,i);return x==null?defaultValue:Math.toIntExact(x);}
    private static Double number(List<String>a,int i,double defaultValue){String x=text(a,i);return x==null?defaultValue:Double.valueOf(x);}
    private static BigDecimal decimal(List<String>a,int i){String x=text(a,i);return x==null?BigDecimal.ZERO:new BigDecimal(x);}
    private static void put(Map<String,Object> map,String key,Object value){if(value!=null)map.put(key,value);}
    private static StudentLab.Criteria criteria(List<String>a){
        var f=new StudentLab.Criteria();f.major=text(a,0);f.minimum=text(a,1)==null?null:Double.valueOf(text(a,1));f.maximum=text(a,2)==null?null:Double.valueOf(text(a,2));f.name=text(a,3);
        if(text(a,4)!=null)f.sort=text(a,4);if("or".equals(text(a,5)))f.alternativeAge=21;return f;
    }
}
