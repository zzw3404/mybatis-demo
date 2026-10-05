package com.example.chapter05;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/** Counts SQL statements prepared by MyBatis during chapter05 tests. */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
        args = {Connection.class, Integer.class}))
public class SqlStatementCounter implements Interceptor {

    private final AtomicInteger count = new AtomicInteger();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        count.incrementAndGet();
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // No configuration properties are required.
    }

    public int getCount() {
        return count.get();
    }

    public void reset() {
        count.set(0);
    }
}
