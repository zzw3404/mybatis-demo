package study.console;
import java.io.*;
import java.util.*;
import java.util.regex.*;
/** Menu labels describe the business question; English command names remain optional shortcuts. */
public final class ChineseMenus {
    private record Choice(String name,String command) { }
    private final Commands commands;private final BufferedReader input;private final PrintStream out;private final ChineseDisplay view;
    public ChineseMenus(Commands commands,BufferedReader input,PrintStream out){this.commands=commands;this.input=input;this.out=out;view=new ChineseDisplay(out);}
    public void run()throws IOException{
        out.println("欢迎使用 MyBatis 综合实验控制台");out.println("当前连接："+commands.database().description());out.println("所有查询与维护使用当前数据库，修改提交后会持久保存。");
        boolean expanded=false;
        while(true){
            out.println("\n════════════ 主菜单 ════════════");
            if(expanded)out.println("1. 多表联查与关联映射\n2. 用户、角色与权限管理\n3. 部门、员工与登录账号\n4. 供应商、销售与售后\n5. 学生管理与事务实验\n6. 条件构造器查询实验\n7. Spring Boot 用户接口\n8. 数据库状态与全部概况\n9. 命令帮助\n10. 一级缓存实验与SQL日志\n11. 交易系统关联映射（一对一、一对多、两个多对多）\n12. 交易系统报告补充实验\nm. 收起更多功能\n0. 退出");
            else out.println("1. 多表联查\n5. 学生管理与分页\n6. 高级查询与条件分页\n8. 数据库状态\n10. 一级缓存实验与SQL日志\n11. 交易系统关联映射（一对一、一对多、两个多对多）\n12. 交易系统报告补充实验\nm. 展开全部功能\n0. 退出");
            String selected=ask("请选择编号（也可以直接输入命令）");if(selected==null||selected.equals("0")||selected.equals("exit"))return;
            if(selected.equalsIgnoreCase("m")){expanded=!expanded;continue;}
            try{switch(selected){
                case "1"->submenu("多表联查",List.of(
                    new Choice("部门 → 员工：一对多，嵌套查询","org-dept"),new Choice("部门 → 员工：一对多，JOIN联查","org-dept-join"),
                    new Choice("员工 → 部门：多对一，嵌套查询","org-staff-nested"),new Choice("员工 → 部门：多对一，JOIN联查","org-staff-join"),
                    new Choice("用户 → 角色 → 权限：多对多","user"),new Choice("角色 → 权限：多对多","role-detail"),
                    new Choice("销售明细：商品＋实物＋供应商＋客户","sold"),new Choice("供应商销售档案：商品＋客户＋售后","supplier"),
                    new Choice("供应商 → 商品：多对多供应目录","catalog"),new Choice("部门 → 员工 → 账号：人数与薪酬统计","org-depts"),
                    new Choice("供应商 → 购买客户","buyers"),new Choice("供应商 → 售后客户","served")));
                case "2"->submenu("权限管理",choices("users","users-filter","user","user-add","user-edit","user-delete","login","roles","roles-filter","role-detail","role-add","role-edit","role-delete","perms","perms-filter","tree","perm-add","perm-edit","perm-delete","user-role-add","user-role-remove","role-perm-add","role-perm-remove","user-role-list","role-user-list","role-perm-list","perm-role-list","check"));
                case "3"->submenu("组织人事",choices("org-depts","org-dept","org-dept-join","org-dept-add","org-dept-edit","org-dept-delete","org-search","org-staff","org-hire","org-staff-edit","org-transfer","org-leave","org-pay","org-accounts","org-account-detail","org-active-accounts","org-account-suffix","org-account-add","org-account-edit","org-account-disable","org-account-delete","org-login","org-demo","web"));
                case "4"->submenu("销售与售后",choices("suppliers","items","catalog","sold","supplier","soldproducts","buyers","served","services"));
                case "5"->submenu("学生管理",choices("mp-students","mp-student","mp-enroll","mp-score","mp-expel","mp-expel-batch","mp-top","mp-stats","mp-demo","mp-status"));
                case "6"->submenu("高级查询与条件分页",choices("mp-page","mp-filter","mp-lambda","mp-compare","mp-count","mp-one","mp-slim","mp-maps","mp-group","mp-exact","mp-name","mp-in","mp-bulk","mp-raise","mp-wrappers"));
                case "7"->{out.println("用户接口使用与控制台相同的MySQL连接，数据来自原项目user表。");submenu("Spring Boot 用户接口",choices("api-users","api-user","api-search","api-add","api-edit","api-delete"));}
                case "8"->{if(commands.database().usesProjectTables())submenu("数据库状态",choices("db-status","all"));else submenu("演示数据",choices("all","org-reset","reload","mp-reset"));}
                case "12"->study.trade.TradeCompletionRunner.run(commands.database());
                case "11"->study.trade.RelationRunner.run(commands.database());
                case "10"->execute("cache-local",List.of(),"一级缓存实测");
                case "9","help"->commands.help().forEach((key,value)->out.println(key+"  "+value));
                default->{var args=Launcher.split(selected);if(!args.isEmpty())execute(args.get(0),args.subList(1,args.size()),title(args.get(0)));}
            }}catch(Exception error){out.println("操作失败："+error.getMessage());}
        }
    }
    private List<Choice> choices(String... names){return Arrays.stream(names).map(n->new Choice(title(n),n)).toList();}
    private String title(String command){
        return switch(command){
            case "users"->"查看用户";case "users-filter"->"筛选与分页查询用户";case "user"->"联查用户的角色及权限";case "user-add"->"新增用户";case "user-edit"->"修改用户";case "user-delete"->"删除用户";case "login"->"验证用户登录";
            case "roles-filter"->"按名称与状态查询角色";case "perms-filter"->"按名称、父级与类型查询权限";case "user-role-list"->"查询用户与角色关系";case "role-user-list"->"反向查询角色下的用户";case "role-perm-list"->"查询角色与权限关系";case "perm-role-list"->"反向查询权限对应角色";case "org-account-detail"->"查询单个账号详情";case "roles"->"查看角色";case "role-detail"->"联查角色权限";case "role-add"->"新增角色";case "role-edit"->"修改角色";case "role-delete"->"删除角色";
            case "perms"->"查看全部权限";case "tree"->"查看权限树";case "perm-add"->"新增权限";case "perm-edit"->"修改权限";case "perm-delete"->"删除权限";case "check"->"检查用户是否有权限";
            case "user-role-add"->"给用户分配角色";case "user-role-remove"->"解除用户角色关系";case "role-perm-add"->"给角色授予权限";case "role-perm-remove"->"解除角色权限关系";
            case "org-depts"->"部门人数、账号及薪酬联查";case "org-dept"->"部门与员工嵌套查询";case "org-dept-join"->"部门与员工JOIN联查";case "org-staff","org-staff-join"->"员工与部门联查";case "org-search"->"多条件查询员工";case "org-dept-add"->"新增部门";case "org-dept-edit"->"修改部门";case "org-dept-delete"->"删除部门";case "org-hire"->"录入新员工";case "org-staff-edit"->"修改员工岗位与工资";case "org-transfer"->"员工调岗";case "org-leave"->"办理离职";case "org-pay"->"薪酬汇总";
            case "org-accounts"->"查看员工登录账号";case "org-active-accounts"->"查看可用账号";case "org-account-suffix"->"按邮箱后缀查询账号";case "org-account-add"->"注册账号";case "org-account-edit"->"修改账号";case "org-account-disable"->"停用账号";case "org-account-delete"->"删除账号";case "org-login"->"验证员工账号登录";case "org-demo"->"账号生命周期试跑并回滚";case "web"->"启动组织人事HTTP接口";
            case "suppliers"->"供应商、商品与客户基础清单";case "items"->"查询实物及销售状态";case "catalog"->"供应商与商品多对多目录";case "sold"->"联查商品、实物、供应商与客户";case "supplier"->"供应商销售与售后关联档案";case "soldproducts"->"供应商已售商品";case "buyers"->"供应商的购买客户";case "served"->"供应商的售后客户";case "services"->"供应商的售后记录";
            case "mp-status"->"查看学生数据与逻辑删除状态";case "mp-students"->"学生分页查询";case "mp-student"->"按编号查询学生";case "mp-enroll"->"录入学生";case "mp-score"->"修改学生分数（乐观锁）";case "mp-expel"->"学生退学（逻辑删除）";case "mp-expel-batch"->"批量退学";case "mp-top"->"高分学生榜";case "mp-stats"->"按专业统计学生";case "mp-demo"->"学生CRUD、乐观锁与回滚实验";
            case "mp-page"->"多条件筛选与分页查询";case "mp-filter"->"用字符串列名构建条件";case "mp-lambda"->"用Lambda构建条件";case "mp-compare"->"对比两种构造器的结果";case "mp-count"->"统计某专业人数";case "mp-one"->"姓名单条查询与重名检查";case "mp-slim"->"查询部分字段";case "mp-maps"->"以Map返回查询结果";case "mp-group"->"条件构造器分组统计";case "mp-exact"->"按专业与年龄等值查询";case "mp-name"->"姓名模糊、前缀与后缀匹配";case "mp-in"->"查询多个专业（IN）";case "mp-bulk"->"批量更新、空值策略与回滚";case "mp-raise"->"按条件批量调整分数";case "mp-wrappers"->"只读查询实验巡览";
            case "api-users"->"查询用户接口全部用户";case "api-user"->"按编号调用用户接口";case "api-search"->"按用户名调用用户接口";case "api-add"->"通过用户接口新增用户";case "api-edit"->"通过用户接口修改用户";case "api-delete"->"通过用户接口删除用户";
            case "db-status"->"查看当前数据库、表及记录数量";case "all"->"展示所有模块概况";case "org-reset"->"重置组织人事演示数据";case "reload"->"重置销售演示数据";case "mp-reset"->"重置学生演示数据";default->commands.help().getOrDefault(command,command);
        };
    }
    private void submenu(String section,List<Choice> operations)throws Exception{
        while(true){out.println("\n【"+section+"】");for(int i=0;i<operations.size();i++)out.println((i+1)+". "+operations.get(i).name);out.println("0. 返回主菜单");
            String value=ask("选择操作");if(value==null||value.equals("0"))return;
            try{int n=Integer.parseInt(value);if(n<1||n>operations.size())throw new IllegalArgumentException("请选择有效编号");Choice chosen=operations.get(n-1);
                showLookupIds(chosen.command);
                String usage=chosen.command.equals("web")?"[端口，默认8090]":commands.help().get(chosen.command);List<String> args=new ArrayList<>();
                if(Set.of("mp-filter","mp-lambda","mp-compare","mp-slim","mp-maps").contains(chosen.command))usage="[专业] [最低分] [最高分] [姓名片段] [排序id/score/age/major] [or：年龄超过21岁也匹配]";
                Matcher matcher=Pattern.compile("<([^>]+)>|\\[([^]]+)\\]").matcher(usage);
                while(matcher.find()){boolean required=matcher.group(1)!=null;String prompt=required?matcher.group(1):matcher.group(2);String raw=ask(prompt+(required?"（必填）":"（回车不限或使用默认值）"));if(raw==null)return;if(required&&raw.isBlank())throw new IllegalArgumentException(prompt+"不能为空");args.add(raw.isBlank()?"-":raw);}
                if(chosen.command.contains("delete")||chosen.command.contains("remove")||chosen.command.contains("reset")||chosen.command.equals("reload")||chosen.command.startsWith("mp-expel")){
                    if(!"确认".equals(ask("此操作会修改演示数据，输入“确认”继续，其他输入取消"))){out.println("已取消。");continue;}
                }
                execute(chosen.command,args,chosen.name);
            }catch(Exception error){out.println("操作失败："+error.getMessage());}
        }
    }
    private void execute(String name,List<String> args,String label)throws Exception{
        if(name.equals("web")){int port=args.isEmpty()||args.get(0).equals("-")?8090:Integer.parseInt(args.get(0));try(var server=new OrganizationHttp(commands.database(),port)){out.println("服务已启动：http://127.0.0.1:"+server.port()+"/api/health，回车关闭并返回");input.readLine();}return;}
        if(name.equals("sold"))out.println("关联链：销售记录 → 实物 → 商品；实物 → 供应商；销售记录 → 客户");
        if(name.equals("catalog"))out.println("关联链：供应商 ↔ 供应目录关系表 ↔ 商品（多对多）");
        if(name.equals("user"))out.println("关联链：用户 ↔ 用户角色关系 ↔ 角色 ↔ 角色权限关系 ↔ 权限");
        if(name.startsWith("org-dept"))out.println("关联链：部门编号 → 员工的所属部门编号（一对多）");
        if(name.startsWith("org-staff"))out.println("关联链：员工的所属部门编号 → 部门编号（多对一）");
        Object result=commands.execute(name,args);
        if(result==null && (departmentLookup(name)||employeeLookup(name))){
            out.println("【"+label+"】");out.println("未找到"+(departmentLookup(name)?"部门":"员工")+"编号 "+args.get(0)+" 对应的记录，请使用上方清单中的实际编号。");return;
        }
        if(name.startsWith("mp-")&&commands.database().usesProjectTables()){
            var counts=commands.database().studentStatus();long total=((Number)counts.get("全部记录")).longValue(),active=((Number)counts.get("未删除记录")).longValue();
            if(total>0&&active==0)out.println("当前mp_student有"+total+"条记录，但全部已逻辑删除（deleted=1）；常规查询只显示deleted=0，所以结果为空。可选“查看学生数据与逻辑删除状态”核对。");
        }
        view.show(label,result);
    }
    private static boolean departmentLookup(String command){return Set.of("org-dept","org-dept-join").contains(command);}
    private static boolean employeeLookup(String command){return Set.of("org-staff","org-staff-join","org-staff-nested").contains(command);}
    private void showLookupIds(String command){
        boolean team=departmentLookup(command);if(!team&&!employeeLookup(command))return;
        var rows=JsonCodec.MAPPER.valueToTree(commands.execute(team?"org-depts":"org-search",List.of()));
        out.println("当前数据库可用的"+(team?"部门":"员工")+"编号（菜单序号不是数据编号）：");
        if(rows.isEmpty()){out.println("当前没有"+(team?"部门":"员工")+"数据，请先录入或检查数据库连接。");return;}
        int shown=0;StringBuilder line=new StringBuilder();
        for(var row:rows){
            if(shown==30){out.println("其他记录请从组织人事菜单查询。");break;}
            line.append(row.path(team?"deptId":"staffId").asText()).append("（").append(row.path(team?"deptName":"staffName").asText()).append("）  ");shown++;
            if(shown%3==0){out.println(line);line.setLength(0);}
        }
        if(!line.isEmpty())out.println(line);
    }
    private String ask(String prompt)throws IOException{out.print(prompt+" > ");String line=input.readLine();return line==null?null:line.strip();}
}
