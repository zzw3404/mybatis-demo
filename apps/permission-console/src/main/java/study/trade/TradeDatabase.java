package study.trade;
import study.console.Database;
import org.apache.ibatis.session.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import com.baomidou.mybatisplus.core.*;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import java.nio.charset.StandardCharsets;
public final class TradeDatabase {
 private final SqlSessionFactory factory,plus;
 private final TradeSqlCounter sqlCounter=new TradeSqlCounter();
 public TradeSqlCounter sqlCounter(){return sqlCounter;}
 public void clearProductCache(){factory.getConfiguration().getCache(TradeMapper.class.getName()).clear();}
 public TradeDatabase(Database database,boolean log){
  database.runScript("database/trade-lab.sql");
  try(var c=database.tradeEnvironment().getDataSource().getConnection()){
   c.setAutoCommit(false);
   try{
    long rows=0;
    for(String table:new String[]{"lab_trade_product","lab_trade_customer","lab_trade_order","lab_trade_order_line"})
     try(var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM "+table)){r.next();rows+=r.getLong(1);}
    if(rows==0){
     String seed;try(var stream=Resources.getResourceAsStream("database/trade-lab-seed.sql")){seed=new String(stream.readAllBytes(),StandardCharsets.UTF_8);}
     for(String sql:seed.split(";"))if(!sql.isBlank())try(var s=c.createStatement()){s.executeUpdate(sql);}
    }
    try(var stream=Resources.getResourceAsStream("database/trade-relations-seed.sql")){
     String seed=new String(stream.readAllBytes(),StandardCharsets.UTF_8);
     for(String sql:seed.split(";"))if(!sql.isBlank())try(var statement=c.createStatement()){statement.executeUpdate(sql);}
    }
    c.commit();
   }catch(Exception e){c.rollback();throw e;}
   TradeSchema.upgrade(c);
   MybatisConfiguration config=new MybatisConfiguration();config.setEnvironment(database.tradeEnvironment());setup(config,log);
   com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils.getGlobalConfig(config).setMetaObjectHandler(new study.console.AuditClock());
   MybatisPlusInterceptor pipeline=new MybatisPlusInterceptor();
   pipeline.addInnerInterceptor(new com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor());
   pipeline.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));config.addInterceptor(pipeline);
   config.addInterceptor(sqlCounter);
   for(String path:new String[]{"mapping/TradeLabMapper.xml","mapping/TradeRelationMapper.xml"})
    try(var xml=Resources.getResourceAsStream(path)){new XMLMapperBuilder(xml,config,path,config.getSqlFragments()).parse();}
   config.addMapper(ProductPlusMapper.class);
   plus=new MybatisSqlSessionFactoryBuilder().build(config);factory=plus;
  }catch(Exception e){throw new IllegalStateException("交易实验库初始化失败",e);}
 }
 private static void setup(Configuration c,boolean log){c.setMapUnderscoreToCamelCase(true);c.setCacheEnabled(true);c.setLogImpl(log?StdOutImpl.class:org.apache.ibatis.logging.nologging.NoLoggingImpl.class);}
 public SqlSession open(){return factory.openSession(false);}public SqlSession openPlus(){return plus.openSession(false);}
}
