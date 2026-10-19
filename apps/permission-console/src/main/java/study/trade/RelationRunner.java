package study.trade;
import study.console.Database;
import study.trade.RelationModels.*;
public final class RelationRunner {
 public static void main(String[] args){try(Database db=Database.configured(false)){run(db);}}
 public static void run(Database db){
  System.out.println("交易系统 · 一对一、一对多与两组多对多映射实验");
  TradeDatabase database=new TradeDatabase(db,true);
  try(var session=database.open()){
   TradeRelationMapper mapper=session.getMapper(TradeRelationMapper.class);
   heading("1. 一对一 association：客户1与会员档案");
   var customer=mapper.findCustomerWithProfile(1);System.out.println(customer);System.out.println("  └ "+customer.profile);
   heading("2. 一对一：全部客户与可选会员档案");
   mapper.findAllCustomersWithProfiles().forEach(c->{System.out.println(c);System.out.println("  └ "+(c.profile==null?"尚未建立会员档案":c.profile));});
   heading("3. 一对多 collection：客户1及其订单");
   customer=mapper.findCustomerWithOrders(1);System.out.println(customer);customer.orders.forEach(o->System.out.println("  └ "+o));
   heading("4. 一对多：全部客户及各自订单（JOIN结果按主键聚合）");
   mapper.findAllCustomersWithOrders().forEach(c->{System.out.println(c+"，订单数="+c.orders.size());c.orders.forEach(o->System.out.println("  └ "+o));});
   heading("5. 多对多第一组：订单1 → 订单明细关系表 → 商品");
   var order=mapper.findOrderWithProducts(1);System.out.println(order);order.products.forEach(p->System.out.println("  └ "+p));
   heading("6. 多对多第一组反向：商品1 → 订单明细关系表 → 订单");
   var product=mapper.findProductWithOrders(1);System.out.println(product);product.orders.forEach(o->System.out.println("  └ "+o));
   heading("7. 多对多第二组：供应商1 → 供货关系表 → 商品");
   var supplier=mapper.findSupplierWithProducts(1);System.out.println(supplier);supplier.products.forEach(p->System.out.println("  └ "+p));
   heading("8. 多对多第二组反向：商品1 → 供货关系表 → 供应商");
   product=mapper.findProductWithSuppliers(1);System.out.println(product);product.suppliers.forEach(v->System.out.println("  └ "+v));
  }
  System.out.println("\n===== 四组关联映射实验结束 =====");
 }
 private static void heading(String title){System.out.println("\n===== "+title+" =====");}
}
