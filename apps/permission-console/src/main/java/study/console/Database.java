package study.console;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import java.io.Reader;
import java.sql.Connection;
import java.util.function.Function;

/** An application owns its two factories and closes both connection pools. */
public final class Database implements AutoCloseable {
    public enum Intent { READ, COMMIT, REHEARSE }
    private final PooledDataSource source;
    private final boolean projectTables;
    private final SqlSessionFactory ordinary, enhanced;
    public Database(String driver, String url, String user, String password, boolean sql) {
        this(driver,url,user,password,sql,false);
    }
    public Database(String driver,String url,String user,String password,boolean sql,boolean existingProject){
        projectTables=existingProject;
        source = new PooledDataSource(driver, url, user, password);
        try {
            if(projectTables){
                var existing=ProjectData.existing(source);
                runScript("database/project-schema.sql");ProjectData.initialize(this,source,existing);
            }else{
                runScript("database/schema.sql");
                try(Connection c=source.getConnection();var statement=c.createStatement();var row=statement.executeQuery("SELECT COUNT(*) FROM acl_user")){
                    row.next();if(row.getLong(1)==0)runScript("database/seed.sql");
                }
            }
            Configuration nativeConfig = new Configuration(environment());
            setup(nativeConfig, sql);
            nativeConfig.addMapper(CatalogMapper.class);
            for (String file : (projectTables?new String[]{"mapping/ProjectDirectoryMapper.xml","mapping/ProjectTradeMapper.xml"}:new String[]{"mapping/DirectoryMapper.xml","mapping/TradeMapper.xml"})) {
                try (var in = Resources.getResourceAsStream(file)) {
                    new XMLMapperBuilder(in, nativeConfig, file, nativeConfig.getSqlFragments()).parse();
                }
            }
            ordinary = new SqlSessionFactoryBuilder().build(nativeConfig);
            MybatisConfiguration mp = new MybatisConfiguration();
            mp.setEnvironment(environment()); setup(mp, sql);
            GlobalConfigUtils.getGlobalConfig(mp).setMetaObjectHandler(new AuditClock());
            MybatisPlusInterceptor pipeline = new MybatisPlusInterceptor();
            if(projectTables){DynamicTableNameInnerInterceptor names=new DynamicTableNameInnerInterceptor();names.setTableNameHandler((statement,table)->table.equals("learner")?"mp_student":table);pipeline.addInnerInterceptor(names);}
            pipeline.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            pipeline.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
            pipeline.addInnerInterceptor(new BlockAttackInnerInterceptor());
            mp.addInterceptor(pipeline); mp.addMapper(LearnerMapper.class);
            enhanced = new MybatisSqlSessionFactoryBuilder().build(mp);
        } catch (Exception e) { source.forceCloseAll(); throw new IllegalStateException("应用数据库初始化失败", e); }
    }
    public static Database demo() {
        return new Database("org.h2.Driver", "jdbc:h2:mem:app_" + java.util.UUID.randomUUID()
            + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "", false);
    }
    public static Database configured(boolean sql) {
        var p=ProjectConnection.settings();return new Database(p.getProperty("jdbc.driver","com.mysql.cj.jdbc.Driver"),p.getProperty("jdbc.url"),p.getProperty("jdbc.username"),p.getProperty("jdbc.password"),sql,true);
    }
    public boolean usesProjectTables(){return projectTables;}
    public String description(){
        try(Connection c=source.getConnection()){return c.getMetaData().getDatabaseProductName()+" / "+c.getCatalog()+(projectTables?"；员工 emp.empno，部门 dept.deptno，学生 mp_student":"；自动化测试库");}
        catch(Exception e){throw new IllegalStateException("读取数据库信息失败",e);}
    }
    public java.util.Map<String,Object> studentStatus(){
        String table=projectTables?"mp_student":"learner";
        try(Connection c=source.getConnection();var query=c.createStatement();var row=query.executeQuery("SELECT COUNT(*) AS total,SUM(CASE WHEN deleted=0 THEN 1 ELSE 0 END) AS active,SUM(CASE WHEN deleted=1 THEN 1 ELSE 0 END) AS removed FROM "+table)){
            row.next();return java.util.Map.of("学生数据表",table,"全部记录",row.getLong("total"),"未删除记录",row.getLong("active"),"已逻辑删除记录",row.getLong("removed"));
        }catch(Exception e){throw new IllegalStateException("学生数据状态查询失败",e);}
    }
    public java.util.Map<String,Object> status(){
        var result=new java.util.LinkedHashMap<String,Object>();
        try(Connection c=source.getConnection()){
            result.put("数据库类型",c.getMetaData().getDatabaseProductName());result.put("数据库名称",c.getCatalog());
            String[] tables=projectTables?new String[]{"emp","dept","mp_student","supplier","product","customer","purchase","product_service","console_product_item","acl_user","acl_role","acl_resource","login_account"}:new String[]{"person","team","learner","vendor","goods","buyer","purchase","service_event","acl_user","acl_role","acl_resource","login_account"};
            for(String table:tables)try(var query=c.createStatement();var row=query.executeQuery("SELECT COUNT(*) FROM "+table)){row.next();result.put(table+" 记录数",row.getLong(1));}
            if(projectTables){boolean exists=false;try(var tablesFound=c.getMetaData().getTables(c.getCatalog(),null,"user",new String[]{"TABLE"})){exists=tablesFound.next();}
                if(exists)try(var query=c.createStatement();var row=query.executeQuery("SELECT COUNT(*) FROM `user`")){row.next();result.put("user 记录数",row.getLong(1));}
            }
            return result;
        }catch(Exception e){throw new IllegalStateException("数据库状态查询失败",e);}
    }
    public Environment tradeEnvironment(){return environment();}
    Environment cacheEnvironment(){return environment();}
    private Environment environment() { return new Environment("application", new JdbcTransactionFactory(), source); }
    private void setup(Configuration c, boolean sql) {
        c.setMapUnderscoreToCamelCase(true); c.setCacheEnabled(false);
        c.setLazyLoadingEnabled(true); c.setAggressiveLazyLoading(false);
        c.setLogImpl(sql ? StdOutImpl.class : NoLoggingImpl.class);
    }
    public <T> T nativeWork(Intent intent, Function<SqlSession,T> action) { return transact(ordinary, intent, action); }
    public <T> T plusWork(Intent intent, Function<LearnerMapper,T> action) {
        return transact(enhanced, intent, session -> action.apply(session.getMapper(LearnerMapper.class)));
    }
    private <T> T transact(SqlSessionFactory factory, Intent intent, Function<SqlSession,T> action) {
        try (SqlSession session = factory.openSession(false)) {
            try {
                T result=action.apply(session);
                if (intent == Intent.COMMIT) session.commit(); else session.rollback();
                return result;
            } catch (RuntimeException e) { session.rollback(); throw e; }
        }
    }
    public void runScript(String resource) {
        try (Connection c=source.getConnection(); Reader r=Resources.getResourceAsReader(resource)) {
            ScriptRunner script=new ScriptRunner(c); script.setAutoCommit(true); script.setLogWriter(null);
            script.setStopOnError(true); script.runScript(r);
        } catch (Exception e) { throw new IllegalStateException("执行脚本失败："+resource, e); }
    }
    public void close() { source.forceCloseAll(); }
}
