package study.console;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class Launcher {
    private Launcher() { }
    public static void main(String[] supplied){
        var arguments=new ArrayList<>(Arrays.asList(supplied));boolean sql=arguments.remove("--sql");boolean json=arguments.remove("--json");
        try(Database db=Database.configured(sql);Commands router=new Commands(db)){
            if(!arguments.isEmpty()&&arguments.get(0).equals("web")){
                int port=arguments.size()>1?Integer.parseInt(arguments.get(1)):8090;
                try(var server=new OrganizationHttp(db,port)){System.out.println("组织接口：http://127.0.0.1:"+server.port()+"/api/health；Ctrl+C停止");new java.util.concurrent.CountDownLatch(1).await();}
                return;
            }
            if(!arguments.isEmpty()){
                Object result=router.execute(arguments.get(0),arguments.subList(1,arguments.size()));
                if(json)System.out.println(JsonCodec.encode(result));else new ChineseDisplay(System.out).show(router.help().get(arguments.get(0)),result);
                return;
            }
            var input=new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));new ChineseMenus(router,input,System.out).run();
        }catch(Exception failure){System.err.println("运行失败："+failure.getMessage());System.exit(2);}
    }
    static List<String> split(String line){
        List<String> parts=new ArrayList<>();StringBuilder word=new StringBuilder();char quote=0;boolean started=false;
        for(int i=0;i<line.length();i++){
            char current=line.charAt(i);
            if(quote!=0){if(current==quote)quote=0;else word.append(current);started=true;}
            else if(current==34||current==39){quote=current;started=true;}
            else if(Character.isWhitespace(current)){if(started){parts.add(word.toString());word.setLength(0);started=false;}}
            else{word.append(current);started=true;}
        }
        if(quote!=0)throw new IllegalArgumentException("引号没有闭合");
        if(started)parts.add(word.toString());return parts;
    }
}
