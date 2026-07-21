package com.example.orchardcommon.interceptor;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 * SQL 执行耗时拦截器
 */
@Log4j2
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, org.apache.ibatis.session.ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
@Component
public class SqlCostInterceptor implements Interceptor {

    private static final org.apache.logging.log4j.Logger SQL_LOGGER = 
            org.apache.logging.log4j.LogManager.getLogger("SQL_LOG");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        
        // 获取 SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        
        // 获取 Mapper 方法
        String mapperId = "";
        try {
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            mapperId = mappedStatement.getId();
        } catch (Exception e) {
            // ignore
        }
        
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        String params = parameterObject != null ? parameterObject.toString() : "null";
        
        try {
            Object result = invocation.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            
            // 记录 SQL 执行耗时
            if (costTime > 1000) {
                // 超过 1 秒的 SQL 记录为警告
                SQL_LOGGER.warn("[SQL] {} | 耗时: {}ms | SQL: {} | 参数: {}", mapperId, costTime, sql, params);
            } else if (costTime > 500) {
                // 超过 500ms 的 SQL 记录为信息
                SQL_LOGGER.info("[SQL] {} | 耗时: {}ms | SQL: {} | 参数: {}", mapperId, costTime, sql, params);
            } else {
                SQL_LOGGER.debug("[SQL] {} | 耗时: {}ms | SQL: {} | 参数: {}", mapperId, costTime, sql, params);
            }
            
            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            SQL_LOGGER.error("[SQL] {} | 耗时: {}ms | SQL: {} | 参数: {} | 异常: {}", 
                    mapperId, costTime, sql, params, e.getMessage());
            throw e;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可配置属性
    }
}
