package study.console;
import org.junit.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import static org.junit.Assert.*;
public class OrganizationHttpTest {
    private Database db;private OrganizationHttp server;private String base;private final HttpClient client=HttpClient.newHttpClient();
    @Before public void start()throws Exception{db=Database.demo();server=new OrganizationHttp(db,0);base="http://127.0.0.1:"+server.port()+"/api";}
    @After public void close(){server.close();db.close();}
    private HttpResponse<String> request(String method,String path,Object body)throws Exception{
        var builder=HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/json");
        builder.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(JsonCodec.encode(body)));
        return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
    private JsonNode json(HttpResponse<String> response)throws Exception{return JsonCodec.MAPPER.readTree(response.body());}
    @Test public void healthStatsAndDepartmentReads()throws Exception{
        var health=request("GET","/health",null);assertEquals(200,health.statusCode());assertEquals("ok",json(health).get("status").asText());assertEquals(6,json(health).get("departments").asInt());
        assertEquals(18,json(request("GET","/stats/pay",null)).get("staff_total").asInt());
        assertEquals(6,json(request("GET","/departments",null)).size());assertEquals(1,json(request("GET","/departments?city="+URLEncoder.encode("南京",java.nio.charset.StandardCharsets.UTF_8),null)).size());
        var nested=json(request("GET","/departments/1101",null));var joined=json(request("GET","/departments/1101?mode=join",null));
        assertEquals(3,nested.get("memberCount").asInt());assertEquals(nested.get("members"),joined.get("members"));assertEquals("join",joined.get("loading").asText());
    }
    @Test public void departmentsCrudAndForeignKeyConflict()throws Exception{
        var created=request("POST","/departments",Map.of("deptId",1200,"deptCode","TEST","deptName","HTTP部门","city","天津"));assertEquals(201,created.statusCode());
        assertEquals(200,request("PUT","/departments/1200",Map.of("deptName","改名部门")).statusCode());
        assertEquals("改名部门",json(request("GET","/departments/1200",null)).get("deptName").asText());
        assertEquals(200,request("DELETE","/departments/1200",null).statusCode());assertEquals(404,request("GET","/departments/1200",null).statusCode());
        assertEquals(409,request("DELETE","/departments/1101",null).statusCode());
    }
    @Test public void staffWritesSearchAndTransfer()throws Exception{
        assertEquals(3,json(request("GET","/staff?deptId=1101&onJob=1&orderBy=pay",null)).size());
        var hire=request("POST","/staff",Map.of("staffName","HTTP新员工","deptId",1101,"basePay",12000.75));assertEquals(201,hire.statusCode());long id=json(hire).get("staffId").asLong();
        assertEquals("N7019",json(hire).get("staffNo").asText());assertEquals(12000.75,json(hire).get("basePay").asDouble(),0.001);
        assertEquals(200,request("PUT","/staff/"+id,Map.of("post","测试经理")).statusCode());
        assertEquals(200,request("PUT","/staff/"+id+"/transfer",Map.of("deptId",1102)).statusCode());
        assertEquals(1102,json(request("GET","/staff/"+id,null)).get("department").get("deptId").asInt());
        assertEquals(200,request("DELETE","/staff/"+id,null).statusCode());
        assertEquals(0,json(request("GET","/staff/"+id,null)).get("onJob").asInt());
    }
    @Test public void accountLifecycleAndLoginStatus()throws Exception{
        var list=request("GET","/accounts",null);assertEquals(10,json(list).size());assertFalse(list.body().contains("Demo@123"));
        assertEquals(200,request("POST","/login",Map.of("loginName","staff1","secret","Demo@123")).statusCode());
        assertEquals(401,request("POST","/login",Map.of("loginName","staff1","secret","bad")).statusCode());
        assertEquals(400,request("POST","/login",Map.of("loginName","staff1")).statusCode());
        var body=Map.of("loginName","http.account","secret","PrivateDemo","mailbox","http@example.com","staffId",7001);
        var created=request("POST","/accounts",body);assertEquals(201,created.statusCode());assertFalse(created.body().contains("PrivateDemo"));long id=json(created).get("accountId").asLong();
        assertEquals(409,request("POST","/accounts",body).statusCode());
        assertEquals(200,request("PUT","/accounts/"+id,Map.of("state",0,"mailbox","changed@example.com")).statusCode());
        assertEquals(401,request("POST","/login",Map.of("loginName","http.account","secret","PrivateDemo")).statusCode());
        assertEquals(200,request("DELETE","/accounts/"+id,null).statusCode());assertEquals(404,request("DELETE","/accounts/"+id,null).statusCode());
    }
    @Test public void invalidRoutesMethodsAndPayloads()throws Exception{
        assertEquals(404,request("GET","/unknown",null).statusCode());assertEquals(405,request("POST","/health",Map.of()).statusCode());
        assertEquals(404,request("GET","/staff/99999",null).statusCode());assertEquals(404,request("GET","/departments/99999",null).statusCode());
        assertEquals(400,request("POST","/staff",Map.of("deptId",1101)).statusCode());
        assertEquals(400,request("GET","/staff?deptId=not-a-number",null).statusCode());
        var bad=HttpRequest.newBuilder(URI.create(base+"/accounts")).POST(HttpRequest.BodyPublishers.ofString("{invalid-json")).build();
        assertEquals(400,client.send(bad,HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
