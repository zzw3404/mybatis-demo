package study.console;
import org.junit.*;
import static org.junit.Assert.*;
import study.trade.*;
import study.trade.TradeMapper;
import study.trade.TradeModels.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
public class TradeLabTest {
 private Database db;private TradeDatabase trade;
 @Before public void start(){db=Database.demo();trade=new TradeDatabase(db,false);}
 @After public void finish(){db.close();}
 @Test public void queriesAndFilters(){try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);assertEquals(10,m.selectAll().size());assertEquals("流光无线鼠标",m.findById(3).productName);assertEquals(2,m.selectByCondition("外设",new BigDecimal("150"),null).size());assertEquals(2,m.selectByCondition(null,null,"星河").size());assertNull(m.findById(999));}}
 @Test public void bothPaginationPaths(){try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);assertEquals(10,m.countProducts());assertEquals(Long.valueOf(4),m.page(3,3).get(0).id);assertTrue(m.page(99,3).isEmpty());}try(var s=trade.openPlus()){var p=s.getMapper(ProductPlusMapper.class).selectPage(new Page<Product>(1,2),new QueryWrapper<Product>().eq("category","外设").ge("price",100).orderByDesc("price").orderByAsc("id"));assertEquals(3,p.getTotal());assertEquals(2,p.getPages());assertEquals(Long.valueOf(2),p.getRecords().get(0).id);}}
 @Test public void joinsAndNestedMappings(){try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);assertEquals(14,m.selectTradeDetails(null,null).size());assertEquals(2,m.selectTradeDetails("已完成","陈").size());var o=m.findOrderWithLines(1);assertEquals("陈晓",o.customerName);assertEquals(2,o.lines.size());assertEquals(new BigDecimal("4499.00"),o.lines.get(0).subtotal);assertEquals(2,m.ordersForCustomer(1).size());assertEquals(4,m.categorySummary().size());}}
 @Test public void crudRollsBack(){try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);var p=new Product();p.id=-1L;p.productName="测试商品";p.brand="测试品牌";p.category="外设";p.price=new BigDecimal("1.00");p.stock=2;p.saleStatus="在售";assertEquals(1,m.insert(p));assertEquals(1,m.updateStock(-1,8));assertEquals(Integer.valueOf(8),m.findById(-1).stock);assertEquals(1,m.delete(-1));assertNull(m.findById(-1));s.rollback(true);}try(var s=trade.open()){assertEquals(10,s.getMapper(TradeMapper.class).countProducts());assertNull(s.getMapper(TradeMapper.class).findById(-1));}}
 @Test public void rerunPreservesExistingChanges(){try(var s=trade.open()){s.getMapper(TradeMapper.class).updateStock(1,99);s.commit();}var again=new TradeDatabase(db,false);try(var s=again.open()){assertEquals(10,s.getMapper(TradeMapper.class).countProducts());assertEquals(Integer.valueOf(99),s.getMapper(TradeMapper.class).findById(1).stock);}}
}
