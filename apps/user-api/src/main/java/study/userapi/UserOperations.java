package study.userapi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.userapi.persistence.XmlUsers;
import java.util.List;
@Service
public class UserOperations {
    private final XmlUsers users;
    public UserOperations(XmlUsers users){this.users=users;}
    public List<UserRecord> all(){return users.browse();}
    public UserRecord byId(int id){return users.get(id);}
    public UserRecord byName(String username){return users.named(username);}
    @Transactional public boolean add(UserRecord user){
        if(blank(user.username)||blank(user.password)||blank(user.email))throw new IllegalArgumentException("用户名、密码和邮箱必填");
        return users.save(user)==1;
    }
    @Transactional public boolean edit(UserRecord user){if(user.id==null)throw new IllegalArgumentException("更新需要id");return users.patch(user)==1;}
    @Transactional public boolean delete(int id){return users.erase(id)==1;}
    private static boolean blank(String value){return value==null||value.isBlank();}
}
