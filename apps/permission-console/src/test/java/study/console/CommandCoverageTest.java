package study.console;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
/** Every supported command is exercised, including aliases and fractional scores. */
public class CommandCoverageTest {
    private static final class StubApi extends UserGateway {
        @Override public Object call(String method,String path,Map<String,Object> fields){return Map.of("method",method,"path",path);}
        @Override public void close(){ }
    }
    @Test public void executeAllCommandHandlers() throws Exception {
        try(Database db=Database.demo()){
            try(Commands commands=new Commands(db,new StubApi())){Set<String> tested=new HashSet<>();
            Map<String,List<String>> args=new HashMap<>();
            for(String c:List.of("user","supplier","buyers","served","services","soldproducts","mp-student","mp-expel"))args.put(c,List.of("1"));
            args.put("check",List.of("1","system:user:delete"));args.put("login",List.of("admin","demo123"));args.put("org-dept",List.of("1101","join"));args.put("org-staff",List.of("7001"));args.put("org-login",List.of("staff1","Demo@123"));
            args.put("org-hire",List.of("命令测试","1101","测试专员","10000.50"));args.put("org-transfer",List.of("7001","1102"));args.put("mp-score",List.of("1","95.25"));args.put("mp-count",List.of("计算机"));args.put("mp-one",List.of("赵明川"));args.put("mp-exact",List.of("计算机","19"));args.put("mp-raise",List.of("计算机","90","92.5"));

            args.put("user-add",List.of("console.user","demo123","控制台测试","console@example.com"));
            args.put("role-add",List.of("控制台角色","测试说明"));args.put("perm-add",List.of("2","控制台权限","console:permission","3"));
            args.put("user-role-add",List.of("2","3"));args.put("user-role-remove",List.of("2","3"));args.put("role-perm-add",List.of("2","7"));args.put("role-perm-remove",List.of("2","7"));
            args.put("role-detail",List.of("1"));args.put("org-dept-join",List.of("1101"));args.put("org-staff-nested",List.of("7001"));args.put("org-staff-join",List.of("7001"));
            args.put("org-dept-add",List.of("1200","QA","控制台部门","天津"));args.put("org-dept-edit",List.of("1200","修改部门"));args.put("org-dept-delete",List.of("1200"));
            args.put("org-staff-edit",List.of("7001","顾问","12000","700"));args.put("org-leave",List.of("7001"));args.put("org-account-suffix",List.of("example.com"));
            args.put("org-account-add",List.of("console.account","test123","console.account@example.com","7001"));
            args.put("mp-enroll",List.of("控制台新学生","20","计算机","88.75"));args.put("mp-expel-batch",List.of("1,2"));args.put("mp-name",List.of("韩","start"));args.put("mp-in",List.of("计算机,软件工程"));
            for(String command:List.of("user-role-list","role-user-list","role-perm-list","perm-role-list","org-account-detail"))args.put(command,List.of("1"));
            args.put("api-user",List.of("1"));args.put("api-search",List.of("demo1"));args.put("api-add",List.of("console.api","demo123","console.api@example.com"));args.put("api-edit",List.of("1","changed.api@example.com"));args.put("api-delete",List.of("1"));
            Map<String,Object> results=new HashMap<>();
            for(String command:commands.names()){
                if(command.equals("user-edit")||command.equals("user-delete")){var row=(Map<?,?>)results.get("user-add");String id=row.get("id").toString();args.put(command,command.equals("user-edit")?List.of(id,"改名测试","updated.console@example.com"):List.of(id));}
                if(command.equals("role-edit")||command.equals("role-delete")){var row=(Map<?,?>)results.get("role-add");String id=row.get("id").toString();args.put(command,command.equals("role-edit")?List.of(id,"改名角色"):List.of(id));}
                if(command.equals("perm-edit")||command.equals("perm-delete")){var row=(Map<?,?>)results.get("perm-add");String id=row.get("id").toString();args.put(command,command.equals("perm-edit")?List.of(id,"改名权限"):List.of(id));}
                if(command.equals("org-account-edit")||command.equals("org-account-disable")||command.equals("org-account-delete")){var row=(Map<?,?>)results.get("org-account-add");String id=row.get("accountId").toString();args.put(command,command.equals("org-account-edit")?List.of(id,"updated.account@example.com"):List.of(id));}

                Object result=commands.execute(command,args.getOrDefault(command,List.of()));
                JsonCodec.encode(result);results.put(command,result);tested.add(command);
            }
            assertEquals(new HashSet<>(commands.names()),tested);assertTrue(tested.size()>=50);
            assertThrows(IllegalArgumentException.class,()->commands.execute("check",List.of()));
            assertThrows(IllegalArgumentException.class,()->commands.execute("no-such-command",List.of()));
            assertEquals(List.of("org-hire","带 空格姓名","1101"),Launcher.split("org-hire '带 空格姓名' 1101"));
            }
        }
    }
}
