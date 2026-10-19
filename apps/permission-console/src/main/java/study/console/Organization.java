package study.console;
import study.console.Models.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import static study.console.Database.Intent.*;
import static study.console.CatalogSql.Kind.ACCOUNT;
public final class Organization {
    private final Database db; private final AccessControl records;
    public Organization(Database db) { this.db=db;records=new AccessControl(db); }
    private <T>T read(Function<DirectoryMapper,T> f){return db.nativeWork(READ,s->f.apply(s.getMapper(DirectoryMapper.class)));}
    private <T>T write(Function<DirectoryMapper,T> f){return db.nativeWork(COMMIT,s->f.apply(s.getMapper(DirectoryMapper.class)));}
    public List<Map<String,Object>> overview(){return read(DirectoryMapper::totals);}
    public List<Team> teams(Map<String,Object> filter){return read(m->m.teams(filter));}
    public Team department(long id,boolean joined){return read(m->joined ? m.teamJoin(id) : m.team(id));}
    public Person employee(long id,boolean joined){return read(m->joined ? m.personJoin(id) : m.person(id));}
    public List<Person> search(Map<String,Object> filters){return read(m->m.search(filters));}
    public List<Person> staffIn(long department){return read(m->m.members(department));}
    public Map<String,Object> pay(){return read(DirectoryMapper::payroll);}
    public Team addDepartment(Team team){
        if(team.deptId==null || (!db.usesProjectTables() && blank(team.deptCode)) || blank(team.deptName))throw new IllegalArgumentException("部门编号、代码与名称必填");
        write(m->m.addTeam(team)); return department(team.deptId,false);
    }
    public boolean editDepartment(Team team){if(team.deptId==null)throw new IllegalArgumentException("缺少部门编号");return write(m->m.editTeam(team))==1;}
    public boolean removeDepartment(long id){return write(m->m.dropTeam(id))==1;}
    public Person hire(Person employee){
        if(blank(employee.staffName) || employee.deptId==null) throw new IllegalArgumentException("姓名与部门必填");
        if(employee.post==null)employee.post="待分配";
        if(employee.basePay==null)employee.basePay=BigDecimal.ZERO;
        if(employee.meritPay==null)employee.meritPay=BigDecimal.ZERO;
        if(employee.hiredOn==null)employee.hiredOn=LocalDate.now();
        if(employee.onJob==null)employee.onJob=1;
        write(m->m.addPerson(employee));return employee(employee.staffId,true);
    }
    public boolean editPerson(Person person){if(person.staffId==null)throw new IllegalArgumentException("缺少员工编号");return write(m->m.editPerson(person))==1;}
    public boolean move(long person,long department){return write(m->m.transfer(person,department))==1;}
    public boolean offDuty(long id){return write(m->m.leave(id))==1;}
    private static boolean blank(String x){return x==null || x.isBlank();}
    public List<Map<String,Object>> accounts(boolean active){return records.query(ACCOUNT,active ? Map.of("state",1):Map.of(),null,null).stream().map(this::mask).toList();}
    public Map<String,Object> account(long id){var row=records.find(ACCOUNT,id);return row==null ? null : mask(row);}
    public long activeAccounts(){return records.total(ACCOUNT,Map.of("state",1));}
    public List<Map<String,Object>> accountsWithSuffix(String suffix){return accounts(false).stream().filter(x->String.valueOf(x.get("mailbox")).endsWith(suffix)).toList();}
    public Map<String,Object> login(String name,String secret){
        if(name==null || secret==null)return null;
        return records.query(ACCOUNT,Map.of("loginName",name,"secret",secret,"state",1),null,null).stream().findFirst().map(this::mask).orElse(null);
    }
    public Map<String,Object> register(Map<String,Object> fields){
        if(blank((String)fields.get("loginName")) || blank((String)fields.get("secret")) || blank((String)fields.get("mailbox")))throw new IllegalArgumentException("登录名、口令与邮箱必填");
        return mask(records.create(ACCOUNT,fields));
    }
    public boolean patchAccount(long id,Map<String,Object> fields){var change=new LinkedHashMap<>(fields);change.put("updatedAt",LocalDateTime.now());return records.patch(ACCOUNT,id,change);}
    public boolean eraseAccount(long id){return records.delete(ACCOUNT,id);}
    private Map<String,Object> mask(Map<String,Object> row){var safe=new LinkedHashMap<>(row);safe.put("secret","******");safe.put("accountId",safe.remove("id"));Object staff=safe.get("staffId");
        if(staff instanceof Number number){Person p=employee(number.longValue(),false);if(p!=null)safe.put("staffName",p.staffName);}return safe;}
    public Map<String,Object> rehearseAccount(){
        return db.nativeWork(REHEARSE,s->{var m=s.getMapper(CatalogMapper.class);var row=new LinkedHashMap<String,Object>();
            row.put("loginName","trial_"+UUID.randomUUID());row.put("secret","Trial@123");row.put("mailbox",row.get("loginName")+"@example.com");
            int inserted=m.create(ACCOUNT,row);long key=((Number)row.get("id")).longValue();row.put("mailbox","changed_"+key+"@example.com");row.put("state",0);
            int updated=m.change(ACCOUNT,row);int removed=m.remove(ACCOUNT,key);return Map.of("inserted",inserted,"updated",updated,"removed",removed,"rolledBack",true);});
    }
    public void reset(){if(db.usesProjectTables())throw new IllegalArgumentException("当前使用真实 emp/dept，不能用演示重置覆盖现有数据");db.runScript("database/reset-organization.sql");}
}
