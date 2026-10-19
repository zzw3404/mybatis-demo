package study.console;
import org.junit.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import static org.junit.Assert.*;
import static study.console.Database.Intent.*;
public class ProjectSchemaTest {
    private String url;private Database db;private Organization org;
    @Before public void start(){url="jdbc:h2:mem:project_"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1";db=new Database("org.h2.Driver",url,"sa","",false,true);org=new Organization(db);}
    @After public void stop(){db.close();}
    @Test public void employeeAndDepartmentUseActualPrimaryKeys(){
        assertTrue(db.usesProjectTables());assertEquals(14,org.search(Map.of()).size());assertEquals(4,org.teams(Map.of()).size());
        assertNotNull(org.employee(10,true));assertNull(org.employee(7010,true));assertNotNull(org.department(1,true));assertNull(org.department(1101,true));
        assertEquals(org.employee(10,false).deptId,org.employee(10,true).department.deptId);
        assertEquals(org.department(1,false).members.stream().map(x->x.staffId).toList(),org.department(1,true).members.stream().map(x->x.staffId).toList());
    }
    @Test public void noParallelDemoBusinessTablesAreCreated()throws Exception{
        try(Connection c=DriverManager.getConnection(url,"sa","");ResultSet r=c.getMetaData().getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})){
            Set<String> names=new HashSet<>();while(r.next())names.add(r.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
            assertTrue(names.containsAll(List.of("emp","dept","mp_student","supplier","product","customer","purchase","console_product_item")));
            assertFalse(names.contains("person"));assertFalse(names.contains("team"));assertFalse(names.contains("learner"));assertFalse(names.contains("vendor"));
        }
    }
    @Test public void reopeningPreservesExistingEmployeeAndStudentRows()throws Exception{
        try(Connection c=DriverManager.getConnection(url,"sa","");Statement s=c.createStatement()){
            s.executeUpdate("UPDATE emp SET ename='已有员工',comm=NULL WHERE empno=10");s.executeUpdate("UPDATE mp_student SET name='已有学生' WHERE id=1");
        }
        try(Database second=new Database("org.h2.Driver",url,"sa","",false,true)){
            var directory=new Organization(second);assertEquals("已有员工",directory.employee(10,true).staffName);assertNull(directory.employee(10,true).meritPay);
            assertEquals("已有学生",new StudentLab(second).find(1).getName());assertEquals(14,directory.search(Map.of()).size());
        }
    }
    @Test public void originalEmpCrudAndLogicalDeletionPersist()throws Exception{
        Models.Person p=new Models.Person();p.staffName="接入测试";p.deptId=1L;p.basePay=new BigDecimal("9000");p=org.hire(p);assertEquals(Long.valueOf(15),p.staffId);
        assertTrue(org.move(p.staffId,2));p=org.employee(p.staffId,true);assertEquals(Long.valueOf(2),p.department.deptId);p.post="顾问";p.meritPay=new BigDecimal("500");assertTrue(org.editPerson(p));
        assertTrue(org.offDuty(p.staffId));assertNull(org.employee(p.staffId,true));
        try(Connection c=DriverManager.getConnection(url,"sa","");Statement s=c.createStatement();ResultSet row=s.executeQuery("SELECT deleted FROM emp WHERE empno=15")){assertTrue(row.next());assertEquals(1,row.getInt(1));}
    }
    @Test public void departmentCrudUsesDeptFields(){
        Models.Team t=new Models.Team();t.deptId=5L;t.deptName="新增部门";t.city="杭州";assertEquals("新增部门",org.addDepartment(t).deptName);
        t.deptName="修改部门";assertTrue(org.editDepartment(t));assertEquals("修改部门",org.department(5,true).deptName);assertTrue(org.removeDepartment(5));
    }
    @Test public void studentWrappersOperateOnMpStudent(){
        StudentLab lab=new StudentLab(db);assertEquals(8,lab.count());var before=lab.find(1);assertTrue(lab.changeScore(1,95.25,before.getVersion()));assertEquals(Double.valueOf(95.25),lab.find(1).getScore());
        assertEquals(8,lab.page(1,3).getTotal());assertEquals(3,lab.grouped().size());assertEquals(3,lab.stats().size());assertEquals(2,lab.top(70,2).size());
        long count=lab.count();assertEquals(true,lab.rehearse().get("rolledBack"));assertEquals(count,lab.count());
    }
    @Test public void originalSalesAndDerivedPhysicalItemsAreLinked(){
        db.nativeWork(READ,s->{var trade=s.getMapper(TradeMapper.class);assertEquals(3,trade.vendors().size());assertEquals(3,trade.goods().size());assertEquals(5,trade.inventory().size());assertEquals(5,trade.sold(Map.of()).size());assertEquals(3,trade.sold(Map.of("supplierId",2)).size()+trade.sold(Map.of("supplierId",1,"customerId",1)).size());assertEquals(2,trade.profile(1).products.size());return null;});
    }
    @Test public void accountAssociationUsesEmpNotDemoPerson(){
        var account=org.register(Map.of("loginName","existing.emp","secret","pass","mailbox","emp@example.com","staffId",10));assertEquals(org.employee(10,false).staffName,account.get("staffName"));
        assertEquals(14,((Number)db.status().get("emp 记录数")).intValue());
        assertThrows(IllegalArgumentException.class,org::reset);assertThrows(IllegalArgumentException.class,()->new StudentLab(db).reset());
    }
}
