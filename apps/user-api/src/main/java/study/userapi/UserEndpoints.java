package study.userapi;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** Preserve the reference application's six public URLs, backed by a new service and schema. */
@RestController
@RequestMapping("/user")
public class UserEndpoints {
    private final UserOperations service;
    @org.springframework.beans.factory.annotation.Autowired private javax.sql.DataSource source;
    public UserEndpoints(UserOperations service){this.service=service;}
    @GetMapping("/findAll") public List<UserRecord> list(){return service.all();}
    @GetMapping("/{id}") public UserRecord detail(@PathVariable("id") int id){return service.byId(id);}
    @GetMapping("/search") public UserRecord search(@RequestParam("username") String name){return service.byName(name);}
    @PostMapping public String create(@RequestBody UserRecord input){return service.add(input)?"添加成功":"添加失败";}
    @PutMapping public String change(@RequestBody UserRecord input){return service.edit(input)?"更新成功":"更新失败";}
    @DeleteMapping("/{id}") public String remove(@PathVariable("id") int id){return service.delete(id)?"删除成功":"删除失败";}
    @GetMapping("/database") public java.util.Map<String,String> database() throws java.sql.SQLException {
        try(var connection=source.getConnection()){return java.util.Map.of("engine",connection.getMetaData().getDatabaseProductName(),"database",connection.getCatalog(),"table","user");}
    }
}
