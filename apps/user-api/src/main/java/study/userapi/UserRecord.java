package study.userapi;
import java.time.LocalDateTime;
/** Public data fields give MyBatis and Jackson a single, small transport model. */
public class UserRecord {
    public Integer id;
    public String username,password,email;
    public LocalDateTime createTime,updateTime;
}
