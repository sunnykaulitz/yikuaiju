package com.example.yikuaiju.service;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;


/**
 * @author lifei
 *
 */
public interface ICommonService {

    /**
     * 执行指定的SQL语句，返回指定对象集合。该分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param type 对象类型
     * @param limitSql SQL查询语句
     * @param params 查询语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param total 总记录条数
     * @param <T> 返回对象类型
     * @return 查询结果
     */
    <T> Page<T> select(Class<T> type, String limitSql, Map<String, ?> params,
                       int pageNum, int pageSize, long total);

    /**
     * 执行指定的SQL语句，返回指定对象集合。该分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param type 对象类型
     * @param limitSql SQL查询语句
     * @param countSql 统计数据总数的SQL
     * @param params 查询语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param <T> 返回对象类型
     * @return 查询结果
     */
    <T> Page<T> select(Class<T> type, String limitSql, String countSql,
                       Map<String, ?> params,
                       int pageNum, int pageSize);

    /**
     * 执行指定的SQL语句，返回指定对象集合。该分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param type 对象类型
     * @param limitSql SQL查询语句
     * @param countSql 统计数据总数的SQL
     * @param params 查询语句参数
     * @param countParams 统计数据总数语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param <T> 返回对象类型
     * @return 查询结果
     */
    <T> Page<T> select(Class<T> type, String limitSql, String countSql,
                       Map<String, ?> params,
                       Map<String, ?> countParams, int pageNum,
                       int pageSize);

    /**
     * 执行指定的SQL语句，返回指定Map集合。分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param limitSql SQL查询语句
     * @param params 查询语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param total 总记录条数
     * @return 查询结果
     */
    Page<Map<String, Object>> select(String limitSql, Map<String, ?> params,
                                     int pageNum, int pageSize, long total);

    /**
     * 执行指定的SQL语句，返回指定Map集合。分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param limitSql SQL查询语句
     * @param countSql 统计数据总数的SQL
     * @param params 查询语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 查询结果
     */
    Page<Map<String, Object>> select(String limitSql, String countSql,
                                     Map<String, ?> params,
                                     int pageNum, int pageSize);

    /**
     * 执行指定的SQL语句，返回指定Map集合。分页查询需传入limit语句，如下：
     * SELECT user.first_name, user.last_name FROM user LIMIT :offset, :pageSize，
     * 其中:offset，:pageSize为固定参数名称。
     * 若SQL语句中不含LIMIT语句，则该方法不会进行分页
     * @param limitSql SQL查询语句
     * @param countSql 统计数据总数的SQL
     * @param params 查询语句参数
     * @param countParams 统计数据总数语句参数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 查询结果
     */
    Page<Map<String, Object>> select(String limitSql, String countSql,
                                     Map<String, ?> params,
                                     Map<String, ?> countParams, int pageNum,
                                     int pageSize);

    /**
     * 执行指定的SQL语句，返回统计的数据数
     * @param sql SELECT COUNT语句
     * @param params 统计语句参数
     * @return 统计的数据数
     */
    long count(String sql, Map<String, ?> params);

    /**
     * 执行指定的SQL语句，返回指定对象集合
     * @param type 对象类型
     * @param sql SQL查询语句
     * @param params 查询语句参数
     * @param <T> 返回对象类型
     * @return 查询结果
     */
    <T> List<T> select(Class<T> type, String sql, Map<String, ?> params);

    /**
     * 执行指定的SQL语句，以Map形式返回数据，其中Map Key是列名称，Value是列值
     * @param sql SQL查询语句
     * @param params 查询语句参数
     * @return 查询结果
     */
    List<Map<String, Object>> select(
            String sql, Map<String, ?> params);

    /**
     * 执行指定SQL语句，返回指定的对象，对记录不存在，则返回null，
     * 该方法要求sql执行结果返回0或1条，若返回超过1条记录，将抛出org.springframework.dao.IncorrectResultSizeDataAccessException
     * 该方法可用于仅返回一行一列的查询，如：Integer/Long/Double/Datetime/Boolean/String等返回值类型
     * @param type 返回值类型
     * @param sql SQL查询语句
     * @param params 查询语句参数
     * @param <T>
     * @return 查询结果
     */
    <T> T selectOne(Class<T> type, String sql, Map<String, ?> params);

    <T> T insert(Class<T> type, String insertSql, Map<String, ?> params);

    <T> T addOneRecord(Class<T> type, Map<String, ?> params) throws Exception;

    <T> List<T> batchAddRecords(Class<T> type, List<Map<String, Object>> params) throws Exception;

    <T> T update(Class<T> type, Map<String, ?> params) throws Exception;

    <T> int[] batchUpdate(String updateSql, List<Map<String, Object>> params) throws Exception;

    <T> T update(Class<T> type, String updateSql, Map<String, ?> params) throws Exception;

    <T> int delete(Class<T> type, int id);

    <T> int execute(String sql);

    <T> List<T> selectById(Class<T> type, List<Integer> ids);

    <T> List<T> selectSingleColumn(String sql, Map<String, ?> params, Class<T> type);
}
