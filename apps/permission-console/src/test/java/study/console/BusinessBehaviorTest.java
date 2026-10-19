package study.console;
import org.junit.*;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.Assert.*;
import static study.console.Database.Intent.*;
import static study.console.CatalogSql.Kind.*;
public class BusinessBehaviorTest {
    private Database db; private AccessControl acl;private Organization org;
    @Before public void start(){db=Database.demo();acl=new AccessControl(db);org=new Organization(db);}
    @After public void close(){db.close();}
    @Test public void permissionUnionsAndLogin(){
        assertEquals(8,acl.permissions(1L).size());assertEquals(2,acl.permissions(2L).size());
        assertTrue(acl.allowed(1L,"system:user:delete"));assertFalse(acl.allowed(2L,"system:user:delete"));
        assertNotNull(acl.authenticate("admin","demo123"));assertNull(acl.authenticate("admin","wrong"));
        assertNull(acl.authenticate("admin",null));assertNull(acl.authenticate(null,"demo123"));
        assertTrue(acl.permissions(999L).isEmpty());assertFalse(acl.allowed(null,"system"));assertFalse(acl.allowed(1L,null));
    }
    @Test public void userCrudFilteringAndPaging(){
        var draft=acl.create(USER,Map.of("username","testing","password","pass","realName","独立测试","status",1));long key=((Number)draft.get("id")).longValue();
        assertEquals("testing",acl.find(USER,key).get("username"));assertTrue(acl.patch(USER,key,Map.of("email","changed@example.com")));
        assertEquals(1,acl.query(USER,Map.of("usernameLike","est"),null,null).size());
        assertEquals(4,acl.total(USER,Map.of()));assertEquals(2,acl.query(USER,Map.of(),1,2).size());
        assertTrue(acl.delete(USER,key));assertNull(acl.find(USER,key));assertFalse(acl.delete(USER,key));
    }
    @Test public void roleAndResourceCrud(){
        var role=acl.create(ROLE,Map.of("name","临时角色","description","测试","status",1));long r=((Number)role.get("id")).longValue();
        var resource=acl.create(RESOURCE,Map.of("parentId",2,"name","临时资源","code","temporary","type",3,"status",1));long p=((Number)resource.get("id")).longValue();
        assertTrue(acl.patch(ROLE,r,Map.of("description","已修改")));assertTrue(acl.patch(RESOURCE,p,Map.of("name","新资源")));
        assertEquals(1,acl.query(ROLE,Map.of("nameLike","临时"),null,null).size());
        assertEquals(1,acl.query(RESOURCE,Map.of("code","temporary"),null,null).size());
        acl.bind("grant",r,p);assertEquals(1,((List<?>)acl.roleGraph(r).get("permissions")).size());
        assertTrue(acl.delete(RESOURCE,p));assertTrue(acl.links("grant",r,null).isEmpty());assertTrue(acl.delete(ROLE,r));
    }
    @Test public void relationCrudBothDirections(){
        acl.bind("membership",2,3);assertEquals(2,acl.links("membership",2L,null).size());
        assertEquals(3,acl.links("membership",null,3L).size());assertTrue(acl.allowed(2L,"system:user:edit"));
        assertEquals(1,acl.unbind("membership",2L,3L));assertFalse(acl.allowed(2L,"system:user:edit"));
        assertEquals(2,acl.unbind("grant",2L,null));assertTrue(acl.links("grant",2L,null).isEmpty());
        assertFalse(acl.allowed(2L,"system:user:list"));
    }
    @Test public void disabledActorsDoNotAuthorize(){
        acl.patch(USER,1,Map.of("status",0));assertNull(acl.authenticate("admin","demo123"));assertTrue(acl.permissions(1L).isEmpty());
        acl.patch(ROLE,2,Map.of("status",0));assertTrue(acl.permissions(2L).isEmpty());
    }
    @Test public void treeAndUserGraphs(){
        var tree=acl.permissionTree();assertEquals(1,tree.size());assertEquals(3,((List<?>)tree.get(0).get("children")).size());
        assertEquals(3,((List<?>)acl.userGraph(1).get("roles")).size());assertEquals("******",acl.userGraph(1).get("password"));
        assertNull(acl.userGraph(999));assertEquals(4,acl.query(RESOURCE,Map.of("type",1),null,null).size());
    }
    @Test public void invalidColumnCannotBecomeSql(){
        assertThrows(RuntimeException.class,()->acl.query(USER,Map.of("id OR 1=1","x"),null,null));
        assertTrue(acl.query(USER,Map.of("username","' OR 1=1 --"),null,null).isEmpty());
        assertThrows(RuntimeException.class,()->acl.unbind("grant",null,null));
    }
    @Test public void nestedAndJoinedMappingsAgree(){
        for(var t:org.teams(Map.of())){
            var nested=org.department(t.deptId,false);var joined=org.department(t.deptId,true);
            assertEquals(3,nested.members.size());assertEquals(nested.members.stream().map(x->x.staffId).toList(),joined.members.stream().map(x->x.staffId).toList());
            for(var person:nested.members){assertEquals(t.deptId,org.employee(person.staffId,false).department.deptId);assertEquals(t.deptId,org.employee(person.staffId,true).department.deptId);}
        }
        assertNull(org.employee(9999,true));assertNull(org.department(9999,false));
    }
    @Test public void filtersForeachChooseAndPayroll(){
        assertEquals(18,org.search(Map.of()).size());assertEquals(16,org.search(Map.of("onJob",1)).size());
        assertEquals(3,org.search(Map.of("deptId",1101)).size());assertEquals(2,org.search(Map.of("ids",List.of(7001,7003))).size());
        var pay=org.search(Map.of("orderBy","pay"));assertTrue(pay.get(0).monthlyPay().compareTo(pay.get(1).monthlyPay())>=0);
        assertEquals(1,org.teams(Map.of("city","南京")).size());assertEquals(1,org.teams(Map.of("keyword","技术")).size());
        assertEquals(18,((Number)org.pay().get("staff_total")).intValue());assertEquals(6,org.overview().size());
    }
    @Test public void departmentLifecycleAndRestrictedDeletion(){
        Models.Team t=new Models.Team();t.deptId=1200L;t.deptCode="QA";t.deptName="质量组";t.city="天津";
        assertEquals(t.deptId,org.addDepartment(t).deptId);t.deptName="质量保障组";assertTrue(org.editDepartment(t));assertEquals("质量保障组",org.department(1200,false).deptName);
        assertTrue(org.removeDepartment(1200));assertNull(org.department(1200,false));
        assertThrows(RuntimeException.class,()->org.removeDepartment(1101));assertNotNull(org.department(1101,false));
    }
    @Test public void hiringPatchingTransferringLeaving(){
        Models.Person p=new Models.Person();p.staffName="新员工";p.deptId=1101L;p.basePay=new BigDecimal("12500.50");
        p=org.hire(p);assertEquals(Long.valueOf(7019),p.staffId);assertEquals("N7019",p.staffNo);
        assertEquals(new BigDecimal("12500.50"),p.monthlyPay());p.post="技术顾问";assertTrue(org.editPerson(p));
        assertTrue(org.move(p.staffId,1102));assertEquals(Long.valueOf(1102),org.employee(p.staffId,true).department.deptId);
        assertTrue(org.offDuty(p.staffId));assertEquals(Integer.valueOf(0),org.employee(p.staffId,true).onJob);
        assertEquals(16,org.search(Map.of("onJob",1)).size());
    }
    @Test public void accountsLifecycleAndMasking(){
        assertEquals(10,org.accounts(false).size());assertEquals(9,org.activeAccounts());assertEquals(10,org.accountsWithSuffix("example.com").size());
        assertNotNull(org.login("staff1","Demo@123"));assertNull(org.login("staff10","Demo@123"));assertNull(org.login("staff1","bad"));
        var record=org.register(Map.of("loginName","new.login","secret","Secret123","mailbox","new@example.com","staffId",7001));long id=((Number)record.get("accountId")).longValue();
        assertEquals("******",record.get("secret"));assertEquals("顾清和",record.get("staffName"));
        assertTrue(org.patchAccount(id,Map.of("state",0,"mailbox","changed@example.com")));assertNull(org.login("new.login","Secret123"));
        assertTrue(org.eraseAccount(id));assertNull(org.account(id));assertFalse(org.eraseAccount(id));
    }
    @Test public void duplicateAccountIsAtomic(){
        assertThrows(RuntimeException.class,()->org.register(Map.of("loginName","staff1","secret","x","mailbox","new@example.com")));
        assertEquals(10,org.accounts(false).size());
        assertThrows(IllegalArgumentException.class,()->org.register(Map.of("loginName","missing")));
    }
    @Test public void transactionRehearsalsAndOrganizationReset(){
        assertEquals(true,org.rehearseAccount().get("rolledBack"));assertEquals(10,org.accounts(false).size());
        org.offDuty(7001);org.reset();assertEquals(Integer.valueOf(1),org.employee(7001,false).onJob);assertEquals(10,org.accounts(false).size());
    }
    @Test public void salesRelationshipQueriesAndScopedReset(){
        db.nativeWork(READ,s->{var m=s.getMapper(TradeMapper.class);assertEquals(3,m.vendors().size());assertEquals(3,m.goods().size());assertEquals(3,m.buyers().size());
            assertEquals(6,m.catalog().size());assertEquals(6,m.inventory().size());assertEquals(4,m.sold(Map.of()).size());
            assertEquals(2,m.sold(Map.of("productId",10)).size());assertEquals(1,m.sold(Map.of("supplierId",1,"customerId",1)).size());
            assertEquals(2,m.profile(1).products.size());assertEquals(2,m.profile(1).customers.size());assertEquals(2,m.profile(1).services.size());
            assertEquals(2,m.serviceClients(1).size());assertEquals(1,m.serviceClients(2).size());assertNull(m.profile(99));return null;});
        db.runScript("database/reset-sales.sql");assertEquals(18,org.search(Map.of()).size());
    }
}
