package study.console;
import java.util.*;
import static study.console.Database.Intent.*;
import static study.console.CatalogSql.Kind.*;
public final class AccessControl {
    private final Database db;
    public AccessControl(Database db) { this.db=db; }
    public List<Map<String,Object>> query(CatalogSql.Kind kind,Map<String,Object> where,Integer offset,Integer limit) {
        if(offset!=null && offset<0 || limit!=null && limit<1) throw new IllegalArgumentException("分页参数无效");
        return db.nativeWork(READ,s->s.getMapper(CatalogMapper.class).rows(kind,where,offset,limit));
    }
    public List<Map<String,Object>> list(CatalogSql.Kind kind) { return query(kind,Map.of(),null,null); }
    public Map<String,Object> find(CatalogSql.Kind kind,long id) { return query(kind,Map.of("id",id),null,null).stream().findFirst().orElse(null); }
    public Map<String,Object> create(CatalogSql.Kind kind,Map<String,Object> fields) {
        Map<String,Object> record=new LinkedHashMap<>(fields);
        db.nativeWork(COMMIT,s->s.getMapper(CatalogMapper.class).create(kind,record));
        return find(kind,((Number)record.get("id")).longValue());
    }
    public boolean patch(CatalogSql.Kind kind,long id,Map<String,Object> fields) {
        Map<String,Object> data=new LinkedHashMap<>(fields);data.put("id",id);
        return db.nativeWork(COMMIT,s->s.getMapper(CatalogMapper.class).change(kind,data))==1;
    }
    public boolean delete(CatalogSql.Kind kind,long id) { return db.nativeWork(COMMIT,s->s.getMapper(CatalogMapper.class).remove(kind,id))==1; }
    public int bind(String relation,long left,long right) { return db.nativeWork(COMMIT,s->s.getMapper(CatalogMapper.class).connect(relation,left,right)); }
    public int unbind(String relation,Long left,Long right) { return db.nativeWork(COMMIT,s->s.getMapper(CatalogMapper.class).disconnect(relation,left,right)); }
    public List<Map<String,Object>> links(String relation,Long left,Long right) {
        return db.nativeWork(READ,s->s.getMapper(CatalogMapper.class).relations(relation,left,right));
    }
    public long total(CatalogSql.Kind kind,Map<String,Object> where) { return query(kind,where,null,null).size(); }
    public List<String> permissions(Long user) { return user==null ? List.of() : db.nativeWork(READ,s->s.getMapper(CatalogMapper.class).codes(user)); }
    public boolean allowed(Long user,String code) { return code!=null && permissions(user).contains(code); }
    public Map<String,Object> authenticate(String username,String password) {
        if(username==null || password==null) return null;
        return query(USER,Map.of("username",username,"password",password,"status",1),null,null).stream().findFirst().orElse(null);
    }
    public Map<String,Object> userGraph(long id) {
        Map<String,Object> user=find(USER,id); if(user==null) return null;
        user.put("roles",links("membership",id,null).stream().map(r->find(ROLE,((Number)r.get("rightId")).longValue())).toList());
        user.put("permissions",permissions(id)); user.put("password","******"); return user;
    }
    public Map<String,Object> roleGraph(long id) {
        Map<String,Object> role=find(ROLE,id);if(role==null)return null;
        role.put("permissions",links("grant",id,null).stream().map(r->find(RESOURCE,((Number)r.get("rightId")).longValue())).toList());return role;
    }
    public List<Map<String,Object>> permissionTree() {
        List<Map<String,Object>> rows=query(RESOURCE,Map.of("status",1),null,null);
        Map<Long,Map<String,Object>> nodes=new LinkedHashMap<>();
        for(var row:rows){row.put("children",new ArrayList<Map<String,Object>>());nodes.put(((Number)row.get("id")).longValue(),row);}
        List<Map<String,Object>> roots=new ArrayList<>();
        for(var row:rows){long parent=((Number)row.get("parentId")).longValue();var above=nodes.get(parent);
            if(above==null) roots.add(row); else { @SuppressWarnings("unchecked") List<Map<String,Object>> children=(List<Map<String,Object>>)above.get("children"); children.add(row); }
        } return roots;
    }
}
