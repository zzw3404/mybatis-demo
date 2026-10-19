package study.userapi.persistence;
import study.userapi.UserRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface XmlUsers {
    List<UserRecord> browse();
    List<UserRecord> browseUsingXml();
    UserRecord get(int key);
    UserRecord named(String login);
    List<UserRecord> containing(String fragment);
    int save(UserRecord draft);
    int patch(UserRecord changes);
    int erase(int key);
}
