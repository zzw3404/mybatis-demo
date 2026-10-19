package study.userapi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.datasource.url=jdbc:h2:mem:user_http_test;MODE=MySQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.sql.init.mode=always"})
class UserHttpTest {
    @Autowired TestRestTemplate http;
    @Test void readsUseAllThreeReferenceUrls(){assertEquals(10,http.getForObject("/user/findAll",UserRecord[].class).length);assertEquals("demo1",http.getForObject("/user/1",UserRecord.class).username);assertEquals(1,http.getForObject("/user/search?username=demo1",UserRecord.class).id);assertNull(http.getForObject("/user/99999",UserRecord.class));}
    @Test void createUpdateDeleteRoundTrip(){
        String name="http_"+UUID.randomUUID();var input=Map.of("username",name,"password","Demo123","email",name+"@example.com");
        var created=http.postForEntity("/user",input,String.class);assertEquals(HttpStatus.OK,created.getStatusCode());assertEquals("添加成功",created.getBody());
        UserRecord row=http.getForObject("/user/search?username={name}",UserRecord.class,name);assertNotNull(row);assertNotNull(row.createTime);
        try{
            var changed=http.exchange("/user",HttpMethod.PUT,new HttpEntity<>(Map.of("id",row.id,"email","changed_"+row.email)),String.class);
            assertEquals("更新成功",changed.getBody());UserRecord after=http.getForObject("/user/{id}",UserRecord.class,row.id);assertEquals(name,after.username);assertEquals("Demo123",after.password);assertEquals("changed_"+row.email,after.email);
        }finally{assertEquals("删除成功",http.exchange("/user/{id}",HttpMethod.DELETE,HttpEntity.EMPTY,String.class,row.id).getBody());}
        assertNull(http.getForObject("/user/{id}",UserRecord.class,row.id));assertEquals("删除失败",http.exchange("/user/{id}",HttpMethod.DELETE,HttpEntity.EMPTY,String.class,row.id).getBody());
    }
    @Test void validationAndDuplicateConflict(){assertEquals(HttpStatus.BAD_REQUEST,http.postForEntity("/user",Map.of("username","missing"),String.class).getStatusCode());assertEquals(HttpStatus.CONFLICT,http.postForEntity("/user",Map.of("username","demo1","password","x","email","different@example.com"),String.class).getStatusCode());assertEquals(HttpStatus.BAD_REQUEST,http.exchange("/user",HttpMethod.PUT,new HttpEntity<>(Map.of("email","missing@example.com")),String.class).getStatusCode());}
}
