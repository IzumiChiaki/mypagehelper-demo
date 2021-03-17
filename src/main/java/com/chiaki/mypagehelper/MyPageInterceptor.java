package com.chiaki.mypagehelper;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 分页拦截器实现
 * @author chenliang258
 * @date 2021/3/17 17:30
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class MyPageInterceptor implements Interceptor {

    private Field field;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor)invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement)args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds)args[2];
        ResultHandler resultHandler = (ResultHandler)args[3];
        CacheKey cacheKey;
        BoundSql boundSql;
        // 4个参数
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        }
        // 6个参数
        else {
            cacheKey = (CacheKey)args[4];
            boundSql = (BoundSql)args[5];
        }
        // 判断是否需要分页
        MyPage myPage = MyPageHelper.geyMyPageThreadLocal();
        // 不执行分页
        if (myPage.getPageNum() <= 0) {
            return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }
        // count查询
        MappedStatement countMs = newCountMappedStatement(ms);
        String sql = boundSql.getSql();
        String countSql = "select count(1) from (" + sql + ") _count";
        BoundSql countBoundSql =
                new BoundSql(ms.getConfiguration(), countSql, boundSql.getParameterMappings(), parameter);
        Map<String, Object> additionalParameters = (Map<String, Object>) field.get(boundSql);
        for (Map.Entry<String, Object> additionalParameter : additionalParameters.entrySet()) {
            countBoundSql.setAdditionalParameter(additionalParameter.getKey(), additionalParameter.getValue());
        }
        CacheKey countCacheKey = executor.createCacheKey(countMs, parameter, rowBounds, countBoundSql);
        Object countResult = executor.query(countMs, parameter, RowBounds.DEFAULT, resultHandler, countCacheKey, countBoundSql);
        Long count = (Long)((List)countResult).get(0);
        myPage.setTotal(count);
        // 分页查询
        String pageSql = sql + " limit " + myPage.getStartIndex() + "," + myPage.getPageSize();
        BoundSql pageBoundSql = new BoundSql(ms.getConfiguration(), pageSql, boundSql.getParameterMappings(), parameter);
        for (Map.Entry<String, Object> additionalParameter : additionalParameters.entrySet()) {
            pageBoundSql.setAdditionalParameter(additionalParameter.getKey(), additionalParameter.getValue());
        }
        CacheKey pageCacheKey = executor.createCacheKey(ms, parameter, rowBounds, pageBoundSql);
        List listResult = executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, pageCacheKey, pageBoundSql);
        myPage.addAll(listResult);
        // 清空线程局部变量分页信息
        MyPageHelper.clearMyPageThreadLocal();
        return myPage;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        try {
            field = BoundSql.class.getDeclaredField("additionalParameters");
            field.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建count的MappedStatement
     *
     * @param ms 原始MappedStatement
     * @return 新的带有分页信息的MappedStatement
     */
    private MappedStatement newCountMappedStatement(MappedStatement ms) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "_count",
                ms.getSqlSource(), ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        // count查询返回值int
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId() + "_count", Long.class,
                new ArrayList<>(0)).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }
}
