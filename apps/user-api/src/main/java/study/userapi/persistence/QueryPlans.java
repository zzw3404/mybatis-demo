package study.userapi.persistence;
import study.userapi.UserRecord;
import org.apache.ibatis.jdbc.SQL;
import java.util.Map;
public final class QueryPlans {
    private QueryPlans() { }
    public static String where(Map<String,Object> criteria) {
        SQL statement=new SQL().SELECT("id,username,password,email,created_at,updated_at").FROM("`user`");
        for(var field:Map.of("username","username","password","password","email","email").entrySet())
            if(criteria.get(field.getKey())!=null)statement.WHERE(field.getValue()+" = #{"+field.getKey()+"}");
        return statement.ORDER_BY("id").toString();
    }
    public static String edit(UserRecord row) {
        SQL statement=new SQL().UPDATE("`user`").SET("updated_at=CURRENT_TIMESTAMP");
        if(row.username!=null)statement.SET("username=#{username}");
        if(row.password!=null)statement.SET("password=#{password}");
        if(row.email!=null)statement.SET("email=#{email}");
        return statement.WHERE("id=#{id}").toString();
    }
}
