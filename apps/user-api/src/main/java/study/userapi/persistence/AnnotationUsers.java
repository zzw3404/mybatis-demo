package study.userapi.persistence;
import org.apache.ibatis.annotations.*;
import study.userapi.UserRecord;
import java.util.*;
public interface AnnotationUsers {
    @Results(id="columns",value={@Result(column="id",property="id",id=true),@Result(column="username",property="username"),
        @Result(column="password",property="password"),@Result(column="email",property="email"),
        @Result(column="created_at",property="createTime"),@Result(column="updated_at",property="updateTime")})
    @Select("SELECT id,username,password,email,created_at,updated_at FROM `user` ORDER BY id")
    List<UserRecord> browse();
    @ResultMap("columns") @Select("SELECT * FROM `user` WHERE id=#{key}") UserRecord get(int key);
    @ResultMap("columns") @Select("SELECT * FROM `user` WHERE username=#{login}") UserRecord named(String login);
    @ResultMap("columns") @Select("SELECT * FROM `user` WHERE username LIKE CONCAT('%',#{fragment},'%') ORDER BY id") List<UserRecord> containing(String fragment);
    @ResultMap("columns") @SelectProvider(type=QueryPlans.class,method="where") List<UserRecord> matching(Map<String,Object> fields);
    @ResultMap("columns") @Select("SELECT * FROM `user` WHERE username=#{login} AND password=#{secret}") UserRecord authenticate(@Param("login") String login,@Param("secret") String secret);
    @Insert("INSERT INTO `user`(username,password,email,created_at,updated_at) VALUES(#{username},#{password},#{email},COALESCE(#{createTime},CURRENT_TIMESTAMP),COALESCE(#{updateTime},CURRENT_TIMESTAMP))")
    @Options(useGeneratedKeys=true,keyProperty="id") int save(UserRecord row);
    @UpdateProvider(type=QueryPlans.class,method="edit") int patch(UserRecord row);
    @Delete("DELETE FROM `user` WHERE id=#{key}") int erase(int key);
}
