package study.trade;
import study.console.Database;
import study.trade.TradeModels.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.util.*;
public final class ConsoleRunner {
 public static void main(String[] args){try(Database db=Database.configured(false)){run(db);}}
 public static void run(Database db){
  System.out.println("数码商品交易系统 · MyBatis综合实验");
  System.out.println("数据库连接："+db.description());
  System.out.println("本实验表：lab_trade_product / lab_trade_customer / lab_trade_order / lab_trade_order_line");
  System.out.println("演示品牌与交易数据为虚构数据。CRUD在事务中试跑并回滚。\n");
  TradeDatabase trade=new TradeDatabase(db,true);
  try(var s=trade.open()){
   TradeMapper m=s.getMapper(TradeMapper.class);
   heading(1,"全部商品");print(m.selectAll());
   heading(2,"按编号查商品（编号3）");System.out.println(m.findById(3));
   heading(3,"多条件查询（外设、价格150元以上，按价格降序）");print(m.selectByCondition("外设",new BigDecimal("150"),null));
   heading(4,"名称模糊查询（关键词：星河）");print(m.selectByCondition(null,null,"星河"));
   heading(5,"原生MyBatis分页（第2页，每页3条）");
   long count=m.countProducts();System.out.println("当前页=2，每页=3，总记录="+count+"，总页数="+((count+2)/3));print(m.page(3,3));
  }
  heading(6,"MyBatis-Plus条件分页（外设、价格100元以上，第1页，每页2条）");
  try(var s=trade.openPlus()){
   Page<Product> page=s.getMapper(ProductPlusMapper.class).selectPage(new Page<>(1,2),new QueryWrapper<Product>().eq("category","外设").ge("price",100).orderByDesc("price").orderByAsc("id"));
   System.out.println("当前页="+page.getCurrent()+"，每页="+page.getSize()+"，符合条件总数="+page.getTotal()+"，总页数="+page.getPages());print(page.getRecords());
  }
  try(var s=trade.open()){
   TradeMapper m=s.getMapper(TradeMapper.class);
   heading(7,"四表JOIN联查：订单明细＋订单＋商品＋客户");print(m.selectTradeDetails(null,null));
   heading(8,"JOIN高级筛选（已完成、客户姓名含陈）");print(m.selectTradeDetails("已完成","陈"));
   heading(9,"一对多嵌套查询：订单1及其全部明细");order(m.findOrderWithLines(1));
   heading(10,"客户到订单再到明细：客户1的交易档案");m.ordersForCustomer(1).forEach(ConsoleRunner::order);
   heading(11,"分组统计：已完成订单按商品分类汇总");print(m.categorySummary());
  }
  heading(12,"新增、修改、删除（真实SQL，事务回滚）");
  long trial=-Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)-1;
  try(var s=trade.open()){
   TradeMapper m=s.getMapper(TradeMapper.class);Product row=new Product();row.id=trial;row.productName="事务实验扩展坞";row.category="外设";row.brand="实验品牌";row.price=new BigDecimal("199.00");row.stock=10;row.saleStatus="在售";
   System.out.println("新增影响行数="+m.insert(row));System.out.println(m.findById(trial));
   System.out.println("修改库存影响行数="+m.updateStock(trial,15));System.out.println(m.findById(trial));
   System.out.println("删除影响行数="+m.delete(trial));System.out.println("删除后查询="+m.findById(trial));s.rollback(true);System.out.println("事务已回滚。");
  }
  try(var s=trade.open()){System.out.println("新会话复核实验商品="+s.getMapper(TradeMapper.class).findById(trial));}
  System.out.println("\n===== 数码商品交易系统实验结束 =====");
 }
 private static void heading(int n,String text){System.out.println("\n===== "+n+". "+text+" =====");}
 private static void print(List<?> rows){rows.forEach(System.out::println);System.out.println("本次返回 "+rows.size()+" 条记录。");}
 private static void order(Order order){if(order==null){System.out.println("未找到订单");return;}System.out.println(order);order.lines.forEach(x->System.out.println("  └ "+x));}
}
