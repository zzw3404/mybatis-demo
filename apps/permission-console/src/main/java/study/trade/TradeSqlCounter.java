package study.trade;
import java.sql.Connection;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
@Intercepts(@Signature(type=StatementHandler.class,method="prepare",args={Connection.class,Integer.class}))
public final class TradeSqlCounter implements Interceptor {
 private final ThreadLocal<Integer> count=ThreadLocal.withInitial(()->0);
 public int value(){return count.get();}public void reset(){count.set(0);}
 public Object intercept(Invocation call)throws Throwable{Object result=call.proceed();if(((StatementHandler)call.getTarget()).getBoundSql().getSql().stripLeading().toUpperCase(java.util.Locale.ROOT).startsWith("SELECT"))count.set(count.get()+1);return result;}
}
