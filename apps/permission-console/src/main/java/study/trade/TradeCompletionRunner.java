package study.trade;
import study.console.Database;
import study.trade.TradeModels.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;import java.util.*;
public final class TradeCompletionRunner {
 public static void main(String[] args){try(Database db=Database.configured(false)){run(db);}}
 public static void run(Database db){
  System.out.println("交易系统 · 报告补充实验（真实SQL）");var trade=new TradeDatabase(db,true);
  heading("1. 品牌外键关联与聚合统计");
  try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);m.productsWithBrands().forEach(p->System.out.println(p+" → "+p.brandInfo));m.brandSummary().forEach(System.out::println);}
  heading("2. trim + if + foreach + choose：品牌IN、价格区间、降序");
  try(var s=trade.open()){var q=new ProductQuery();q.brandIds=List.of(1L,2L);q.minimum=new BigDecimal("200");q.maximum=new BigDecimal("5000");q.priceDesc=true;q.validate();s.getMapper(TradeMapper.class).findAdvanced(q).forEach(System.out::println);}
  heading("3. LambdaQueryWrapper条件分页");
  try(var s=trade.openPlus()){var p=s.getMapper(ProductPlusMapper.class).selectPage(new Page<Product>(2,2),new LambdaQueryWrapper<Product>().eq(Product::getCategory,"外设").ge(Product::getPrice,100).orderByDesc(Product::getPrice).orderByAsc(Product::getId));System.out.println("总数="+p.getTotal()+"，页数="+p.getPages()+"，当前第"+p.getCurrent()+"页");p.getRecords().forEach(System.out::println);}
  long id=-Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)-100;
  heading("4. foreach批量新增与trim局部更新（回滚）");
  try(var s=trade.open()){var m=s.getMapper(TradeMapper.class);System.out.println("批量新增="+m.insertBatch(List.of(draft(id),draft(id-1))));var patch=new Product();patch.id=id;patch.price=new BigDecimal("188.00");System.out.println("局部更新="+m.updateSelective(patch));System.out.println(m.findById(id));s.rollback(true);System.out.println("批量数据已回滚");}
  heading("5. 自动填充与乐观锁（回滚）");
  try(var s=trade.openPlus()){var m=s.getMapper(ProductPlusMapper.class);m.insert(draft(id));var original=m.selectById(id);var stale=draft(id);stale.version=original.version;original.stock=15;System.out.println("首次更新="+m.updateById(original));stale.stock=99;System.out.println("过期版本更新="+m.updateById(stale));var p=m.selectById(id);System.out.println("库存="+p.stock+"，version="+p.version+"，create_time="+p.createTime+"，update_time="+p.updateTime);s.rollback(true);}
  heading("6. 二级缓存：跨SqlSession查询，实际SQL计数");
  trade.clearProductCache();trade.sqlCounter().reset();
  try(var s=trade.open()){System.out.println("首次查询："+s.getMapper(TradeMapper.class).findById(1));s.commit();}
  try(var s=trade.open()){System.out.println("另一个会话查询："+s.getMapper(TradeMapper.class).findById(1));s.commit();}
  if(trade.sqlCounter().value()!=1)throw new IllegalStateException("二级缓存未命中");System.out.println("跨会话查询2次，实际SELECT="+trade.sqlCounter().value()+"，验证通过");
  heading("7. MyBatis-Plus修改后XML二级缓存失效（仅操作临时商品）");
  try{
   try(var s=trade.openPlus()){s.getMapper(ProductPlusMapper.class).insert(draft(id));s.commit();}
   trade.sqlCounter().reset();try(var s=trade.open()){s.getMapper(TradeMapper.class).findById(id);s.commit();}
   try(var s=trade.openPlus()){var patch=new Product();patch.id=id;patch.stock=27;s.getMapper(ProductPlusMapper.class).updateById(patch);s.commit();}
   try(var s=trade.open()){var p=s.getMapper(TradeMapper.class).findById(id);if(p.stock!=27)throw new IllegalStateException("缓存返回旧库存");System.out.println("XML重查库存="+p.stock);s.commit();}
   if(trade.sqlCounter().value()!=2)throw new IllegalStateException("缓存未正确失效");System.out.println("实际SELECT="+trade.sqlCounter().value()+"，修改后重查数据库，验证通过");
  }finally{try(var s=trade.openPlus()){s.getMapper(ProductPlusMapper.class).deleteById(id);s.commit();}}
  System.out.println("补充实验完成；临时商品已清理。");
 }
 private static Product draft(long id){var p=new Product();p.id=id;p.productName="报告补充实验商品";p.brand="星河";p.brandId=1L;p.category="外设";p.price=new BigDecimal("199.00");p.stock=10;p.saleStatus="在售";return p;}
 private static void heading(String s){System.out.println("\n===== "+s+" =====");}
}
