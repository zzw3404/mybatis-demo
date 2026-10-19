package study.console;

import java.sql.Connection;
import java.util.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.*;

/** SQL is recorded at JDBC prepare time, so a cache hit cannot create a fake SQL entry. */
public final class LocalCacheLab {
    public interface Rows {
        @SelectProvider(type=Sql.class,method="select")
        List<Map<String,Object>> find(@Param("table") String table,@Param("floor") long floor);
        @UpdateProvider(type=Sql.class,method="update")
        int untouched(@Param("table") String table);
    }
    public static final class Sql {
        private static String table(Map<String,Object> p){
            String t=(String)p.get("table");
            if(!Set.of("mp_student","learner").contains(t))throw new IllegalArgumentException("Invalid table");
            return t;
        }
        public static String select(Map<String,Object> p){return "SELECT id, name, age, major, score, deleted FROM "+table(p)+" WHERE id >= #{floor} ORDER BY id LIMIT 3";}
        public static String update(Map<String,Object> p){return "UPDATE "+table(p)+" SET score = score WHERE 1 = 0";}
    }
    @Intercepts(@Signature(type=StatementHandler.class,method="prepare",args={Connection.class,Integer.class}))
    public static final class SqlTrace implements Interceptor {
        final List<String> sql=new ArrayList<>();
        public Object intercept(Invocation invocation)throws Throwable{
            var bound=((StatementHandler)invocation.getTarget()).getBoundSql();
            Object result=invocation.proceed();
            sql.add(bound.getSql()+" ; 参数="+bound.getParameterObject());return result;
        }
    }
    public record Outcome(String scenario,int actualSql,boolean expected,String log) { }
    public static void main(String[] args){try(Database db=Database.configured(false)){System.out.println("当前连接："+db.description());System.out.println(new LocalCacheLab(db).display());}}
    private final Database db;
    public LocalCacheLab(Database db){this.db=db;}
    public List<Outcome> run(){
        List<Outcome> report=new ArrayList<>();
        for(String scenario:List.of("同一会话重复查询","clearCache后重查","commit后重查","rollback(true)后重查","执行UPDATE后重查","查询参数不同","不同SqlSession","STATEMENT作用域")){
            SqlTrace trace=new SqlTrace();Configuration config=new Configuration(db.cacheEnvironment());
            config.setCacheEnabled(false);config.setLocalCacheScope(scenario.equals("STATEMENT作用域")?LocalCacheScope.STATEMENT:LocalCacheScope.SESSION);
            config.setLogImpl(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
            config.addInterceptor(trace);config.addMapper(Rows.class);
            var factory=new SqlSessionFactoryBuilder().build(config);String table=db.usesProjectTables()?"mp_student":"learner";
            StringBuilder log=new StringBuilder();boolean same=false;
            try(SqlSession session=factory.openSession(false)){
                Rows mapper=session.getMapper(Rows.class);var first=mapper.find(table,0);
                log.append("第1次返回：").append(first).append('\n');
                switch(scenario){
                    case "clearCache后重查"->session.clearCache();
                    case "commit后重查"->session.commit();
                    case "rollback(true)后重查"->session.rollback(true);
                    case "执行UPDATE后重查"->log.append("UPDATE影响行数：").append(mapper.untouched(table)).append("（固定空条件，不修改数据）\n");
                    default-> { }
                }
                List<Map<String,Object>> second;
                if(scenario.equals("不同SqlSession")){
                    try(SqlSession other=factory.openSession(false)){second=other.getMapper(Rows.class).find(table,0);}
                }else second=mapper.find(table,scenario.equals("查询参数不同")?1:0);
                same=first==second;
                log.append("第2次返回：").append(second).append("\n两次结果列表为同一引用：").append(same).append('\n');
                session.rollback(true);
            }
            int expected=scenario.equals("同一会话重复查询")?1:scenario.equals("执行UPDATE后重查")?3:2;
            boolean passed=trace.sql.size()==expected && (scenario.equals("同一会话重复查询")?same:!same);
            log.append("实际提交到JDBC的SQL（缓存命中不会产生此日志）：\n");
            for(int i=0;i<trace.sql.size();i++)log.append(i+1).append(". ").append(trace.sql.get(i)).append('\n');
            log.append("预期SQL次数=").append(expected).append("，实测=").append(trace.sql.size()).append("，验证").append(passed?"通过":"失败");
            report.add(new Outcome(scenario,trace.sql.size(),passed,log.toString()));
        }
        return report;
    }
    public String display(){
        StringBuilder text=new StringBuilder("一级缓存实验：SqlSession作用域，二级缓存已关闭。\n读取学生原始行（含deleted=1），用于观察缓存；不恢复或删除数据。\n");
        for(var result:run())text.append("\n【").append(result.scenario()).append("】\n").append(result.log()).append('\n');
        return text.toString();
    }
}
