package study.console;
import com.sun.net.httpserver.*;
import study.console.Models.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

/** Routes dispatch on HTTP method and segment count; the same service powers CLI and HTTP. */
public final class OrganizationHttp implements AutoCloseable {
    private final HttpServer listener;
    private final ExecutorService workers=Executors.newFixedThreadPool(4);
    private final Organization org;
    private record Reply(int status,Object body) { }
    public OrganizationHttp(Database db,int port) throws IOException {
        org=new Organization(db);
        listener=HttpServer.create(new InetSocketAddress("127.0.0.1",port),0);
        listener.setExecutor(workers);listener.createContext("/api",this::answer);listener.start();
    }
    public int port(){return listener.getAddress().getPort();}
    public void close(){listener.stop(0);workers.shutdownNow();}
    private void answer(HttpExchange exchange) throws IOException {
        Reply result;
        try{
            byte[] raw=exchange.getRequestBody().readNBytes(65537);
            if(raw.length>65536)result=new Reply(413,Map.of("error","请求体过大"));
            else result=dispatch(exchange.getRequestMethod(),exchange.getRequestURI(),JsonCodec.fields(raw));
        }catch(IllegalArgumentException e){result=new Reply(400,Map.of("error",e.getMessage()));}
        catch(Exception e){result=new Reply(conflict(e)?409:500,Map.of("error",conflict(e)?"数据冲突或关联记录仍存在":"处理失败"));}
        byte[] payload=JsonCodec.encode(result.body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        exchange.sendResponseHeaders(result.status,payload.length);
        try(var output=exchange.getResponseBody()){output.write(payload);}finally{exchange.close();}
    }
    private static boolean conflict(Throwable error){
        for(Throwable x=error;x!=null;x=x.getCause())if(x instanceof java.sql.SQLException sql && sql.getSQLState()!=null && sql.getSQLState().startsWith("23"))return true;
        return false;
    }
    private Reply dispatch(String method,URI uri,Map<String,Object> body){
        if(!uri.getPath().startsWith("/api/"))return new Reply(404,Map.of("error","接口不存在"));
        List<String> path=Arrays.stream(uri.getPath().substring(5).split("/")).filter(x->!x.isEmpty()).toList();
        Map<String,Object> query=new LinkedHashMap<>();
        if(uri.getRawQuery()!=null)for(String pair:uri.getRawQuery().split("&")){
            String[] bits=pair.split("=",2);query.put(URLDecoder.decode(bits[0],StandardCharsets.UTF_8),bits.length>1?URLDecoder.decode(bits[1],StandardCharsets.UTF_8):"");
        }
        if(path.isEmpty())return new Reply(404,Map.of("error","接口不存在"));
        String resource=path.get(0);Long id=path.size()>1 && !resource.equals("stats")?Long.valueOf(path.get(1)):null;
        if(resource.equals("health") && path.size()==1){
            if(!method.equals("GET"))return methodError();
            var state=new LinkedHashMap<String,Object>();state.put("status","ok");state.put("departments",org.teams(Map.of()).size());state.put("activeAccounts",org.activeAccounts());state.putAll(org.pay());return ok(state);
        }
        if(resource.equals("stats") && path.equals(List.of("stats","pay")))return method.equals("GET")?ok(org.pay()):methodError();
        if(resource.equals("login") && path.size()==1){
            if(!method.equals("POST"))return methodError();
            if(!body.containsKey("loginName") || !body.containsKey("secret"))throw new IllegalArgumentException("登录名与口令必填");
            var account=org.login(String.valueOf(body.get("loginName")),String.valueOf(body.get("secret")));
            return account==null?new Reply(401,Map.of("ok",false)):ok(Map.of("ok",true,"account",account));
        }
        if(resource.equals("departments") && path.size()<=2){
            if(method.equals("GET")){
                if(id==null)return ok(org.teams(query));
                boolean join="join".equals(query.get("mode"));Team team=org.department(id,join);
                if(team==null)return missing();
                var result=JsonCodec.MAPPER.convertValue(team,new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
                result.put("loading",join?"join":"nested-select");result.put("memberCount",team.members.size());return ok(result);
            }
            if(method.equals("POST") && id==null)return new Reply(201,org.addDepartment(JsonCodec.MAPPER.convertValue(body,Team.class)));
            if(id!=null && method.equals("PUT")){Team t=JsonCodec.MAPPER.convertValue(body,Team.class);t.deptId=id;return org.editDepartment(t)?ok(org.department(id,false)):missing();}
            if(id!=null && method.equals("DELETE"))return org.removeDepartment(id)?ok(Map.of("deleted",true)):missing();
            return methodError();
        }
        if(resource.equals("staff")){
            if(path.size()==3 && path.get(2).equals("transfer")){
                if(!method.equals("PUT"))return methodError();
                return org.move(id,number(body,"deptId"))?ok(org.employee(id,true)):missing();
            }
            if(path.size()>2)return missing();
            if(method.equals("GET")){
                if(id!=null)return nullable(org.employee(id,true));
                for(String key:List.of("deptId","onJob"))if(query.containsKey(key))query.put(key,Long.parseLong(query.get(key).toString()));
                if(query.containsKey("minPay"))query.put("minPay",new java.math.BigDecimal(query.get("minPay").toString()));
                return ok(org.search(query));
            }
            if(method.equals("POST") && id==null)return new Reply(201,org.hire(JsonCodec.MAPPER.convertValue(body,Person.class)));
            if(id!=null && method.equals("PUT")){Person p=JsonCodec.MAPPER.convertValue(body,Person.class);p.staffId=id;return org.editPerson(p)?ok(org.employee(id,true)):missing();}
            if(id!=null && method.equals("DELETE"))return org.offDuty(id)?ok(Map.of("offDuty",true)):missing();
            return methodError();
        }
        if(resource.equals("accounts") && path.size()<=2){
            if(method.equals("GET"))return id==null?ok(org.accounts(false)):nullable(org.account(id));
            if(method.equals("POST") && id==null)return new Reply(201,org.register(body));
            if(id!=null && method.equals("PUT"))return org.patchAccount(id,body)?ok(org.account(id)):missing();
            if(id!=null && method.equals("DELETE"))return org.eraseAccount(id)?ok(Map.of("purged",true)):missing();
            return methodError();
        }
        return missing();
    }
    private static long number(Map<String,Object> body,String key){Object n=body.get(key);if(n==null)throw new IllegalArgumentException("缺少"+key);return Long.parseLong(n.toString());}
    private static Reply ok(Object object){return new Reply(200,object);}
    private static Reply nullable(Object object){return object==null?missing():ok(object);}
    private static Reply missing(){return new Reply(404,Map.of("error","记录或接口不存在"));}
    private static Reply methodError(){return new Reply(405,Map.of("error","不支持的HTTP方法"));}
}
