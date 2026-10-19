package study.console;
import org.junit.*;
import static org.junit.Assert.*;
import study.trade.*;
import org.apache.ibatis.session.SqlSession;
import java.util.*;
public class TradeRelationMapperTest {
 private Database db;private SqlSession session;private TradeRelationMapper mapper;
 @Before public void init(){db=Boolean.getBoolean("trade.real")?Database.configured(false):Database.demo();session=new TradeDatabase(db,true).open();mapper=session.getMapper(TradeRelationMapper.class);}
 @After public void close(){if(session!=null)session.close();if(db!=null)db.close();}
 @Test public void testOneToOneCustomerWithProfile(){System.out.println("===== 一对一：客户与会员档案 =====");var c=mapper.findCustomerWithProfile(1);assertNotNull(c);assertEquals("陈晓",c.customerName);assertNotNull(c.profile);assertEquals(c.id,c.profile.customerId);assertEquals("VIP001",c.profile.memberNo);System.out.println(c);System.out.println("  └ "+c.profile);}
 @Test public void testAllCustomersWithProfiles(){System.out.println("===== 一对一：全部客户 =====");var rows=mapper.findAllCustomersWithProfiles();assertEquals(5,rows.size());assertEquals(3,rows.stream().filter(c->c.profile!=null).count());assertNull(mapper.findCustomerWithProfile(4).profile);rows.forEach(c->{System.out.println(c);System.out.println("  └ "+c.profile);});}
 @Test public void testOneToManyCustomerWithOrders(){System.out.println("===== 一对多：客户及其订单 =====");var c=mapper.findCustomerWithOrders(1);assertEquals(2,c.orders.size());assertEquals(List.of(1L,3L),c.orders.stream().map(o->o.id).toList());System.out.println(c);c.orders.forEach(System.out::println);}
 @Test public void testAllCustomersWithOrders(){System.out.println("===== 一对多：全部客户 =====");var rows=mapper.findAllCustomersWithOrders();assertEquals(5,rows.size());assertEquals(8,rows.stream().mapToInt(c->c.orders.size()).sum());rows.forEach(c->{System.out.println(c);c.orders.forEach(System.out::println);});}
 @Test public void testManyToManyOrderWithProducts(){System.out.println("===== 多对多一：订单到商品 =====");var o=mapper.findOrderWithProducts(1);assertEquals(List.of(1L,2L),o.products.stream().map(p->p.id).toList());System.out.println(o);o.products.forEach(System.out::println);}
 @Test public void testManyToManyProductWithOrders(){System.out.println("===== 多对多一反向：商品到订单 =====");var p=mapper.findProductWithOrders(1);assertEquals(List.of(1L,7L),p.orders.stream().map(o->o.id).toList());assertTrue(mapper.findProductWithOrders(8).orders.isEmpty());System.out.println(p);p.orders.forEach(System.out::println);}
 @Test public void testManyToManySupplierWithProducts(){System.out.println("===== 多对多二：供应商到商品 =====");var s=mapper.findSupplierWithProducts(1);assertEquals(List.of(1L,2L,4L),s.products.stream().map(p->p.id).toList());System.out.println(s);s.products.forEach(System.out::println);}
 @Test public void testManyToManyProductWithSuppliers(){System.out.println("===== 多对多二反向：商品到供应商 =====");var p=mapper.findProductWithSuppliers(1);assertEquals(List.of(1L,2L),p.suppliers.stream().map(s->s.id).toList());assertTrue(mapper.findProductWithSuppliers(8).suppliers.isEmpty());assertNull(mapper.findSupplierWithProducts(999));System.out.println(p);p.suppliers.forEach(System.out::println);}
}
