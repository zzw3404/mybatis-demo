package study.console;
import org.apache.ibatis.jdbc.SQL;
import java.util.*;
/** Names come exclusively from these table definitions, never from user SQL fragments. */
public final class CatalogSql {
    public enum Kind {
        USER("acl_user", "username:username,password:password,realName:real_name,email:email,phone:phone,status:status"),
        ROLE("acl_role", "name:role_name,description:description,status:status"),
        RESOURCE("acl_resource", "parentId:parent_id,name:resource_name,code:code,type:kind,url:url,sort:sort_order,status:status"),
        ACCOUNT("login_account", "loginName:login_name,secret:secret,mailbox:mailbox,staffId:staff_id,state:state,createdAt:created_at,updatedAt:updated_at");
        final String table; final Map<String,String> fields=new LinkedHashMap<>();
        Kind(String table,String spec) { this.table=table; for(String pair:spec.split(",")){String[] p=pair.split(":");fields.put(p[0],p[1]);} }
    }
    public static String select(Map<String,Object> arguments) {
        Kind kind=(Kind)arguments.get("kind");
        @SuppressWarnings("unchecked") Map<String,Object> filter=(Map<String,Object>)arguments.get("filter");
        SQL q=new SQL().SELECT("id AS \"id\"").FROM(kind.table);
        kind.fields.forEach((key,column)->q.SELECT(column+" AS \""+key+"\""));
        filter.forEach((key,value)->{
            boolean like=key.endsWith("Like");
            String field=like ? key.substring(0,key.length()-4) : key;
            String column="id".equals(field) ? "id" : kind.fields.get(field);
            if(column==null) throw new IllegalArgumentException("不支持的筛选字段："+key);
            q.WHERE(value==null ? column+" IS NULL" : like ? column+" LIKE CONCAT('%',#{filter."+key+"},'%')" : column+" = #{filter."+key+"}");
        });
        q.ORDER_BY("id");
        String result=q.toString();
        if(arguments.get("limit")!=null) result+=" LIMIT #{limit} OFFSET #{offset}";
        return result;
    }
    public static String insert(Map<String,Object> arguments) {
        Kind kind=(Kind)arguments.get("kind");
        @SuppressWarnings("unchecked") Map<String,Object> row=(Map<String,Object>)arguments.get("row");
        SQL q=new SQL().INSERT_INTO(kind.table);
        if(row.containsKey("id")) q.VALUES("id","#{row.id}");
        kind.fields.forEach((key,column)->{if(row.containsKey(key)) q.VALUES(column,"#{row."+key+"}");});
        return q.toString();
    }
    public static String update(Map<String,Object> arguments) {
        Kind kind=(Kind)arguments.get("kind");
        @SuppressWarnings("unchecked") Map<String,Object> row=(Map<String,Object>)arguments.get("row");
        SQL q=new SQL().UPDATE(kind.table); int count=0;
        for(var pair:kind.fields.entrySet()) if(row.containsKey(pair.getKey()) && row.get(pair.getKey())!=null) {
            q.SET(pair.getValue()+" = #{row."+pair.getKey()+"}"); count++;
        }
        if(count==0 || row.get("id")==null) throw new IllegalArgumentException("更新需要主键及至少一个字段");
        return q.WHERE("id = #{row.id}").toString();
    }
    public static String delete(Map<String,Object> arguments) {
        return new SQL().DELETE_FROM(((Kind)arguments.get("kind")).table).WHERE("id = #{id}").toString();
    }
    public static String link(Map<String,Object> args) {
        return "INSERT INTO "+relation(args)+"(left_id,right_id) VALUES(#{left},#{right})";
    }
    public static String unlink(Map<String,Object> args) {
        SQL q=new SQL().DELETE_FROM(relation(args));
        if(args.get("left")!=null) q.WHERE("left_id=#{left}");
        if(args.get("right")!=null) q.WHERE("right_id=#{right}");
        if(args.get("left")==null && args.get("right")==null) throw new IllegalArgumentException("需要关系条件");
        return q.toString();
    }
    public static String links(Map<String,Object> args) {
        SQL q=new SQL().SELECT("left_id AS \"leftId\", right_id AS \"rightId\"").FROM(relation(args));
        if(args.get("left")!=null) q.WHERE("left_id=#{left}");
        if(args.get("right")!=null) q.WHERE("right_id=#{right}");
        return q.ORDER_BY("left_id,right_id").toString();
    }
    private static String relation(Map<String,Object> args) {
        return switch((String)args.get("relation")) {case "membership"->"acl_member";case "grant"->"acl_grant";default->throw new IllegalArgumentException("未知关系");};
    }
}
