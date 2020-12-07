package com.example.yikuaiju.serviceImpl;

import java.util.*;

import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.util.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;


/** 
 * @author  lifei 
 * @date 创建时间：2017年10月10日 下午7:51:40 
 * @version 1.0 
 * @Description 通用接口
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CommonServiceImpl implements ICommonService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public <T> Page<T> select(Class<T> type, String limitSql, Map<String, ?> params,
                              int pageNum, int pageSize, long total) {
        List<T> list = pageQuery(type, limitSql, params, pageNum, pageSize);
        return Pages.create(list, pageNum, pageSize, total);
    }

    @Override
    public <T> Page<T> select(Class<T> type, String limitSql, String countSql,
                              Map<String, ?> params, int pageNum, int pageSize) {
        return select(type, limitSql, countSql, params, params, pageNum, pageSize);
    }

    @Override
    public <T> Page<T> select(Class<T> type, String limitSql, String countSql,
                              Map<String, ?> params, Map<String, ?> countParams,
                              int pageNum, int pageSize) {
        long total = count(countSql, countParams);
        if (total == 0L) {
            return Pages.create(new ArrayList<T>(0), pageNum, pageSize, total);
        }
        return select(type, limitSql, params, pageNum, pageSize, total);
    }


    @Override
    public Page<Map<String, Object>> select(String limitSql, Map<String, ?> params,
                                            int pageNum, int pageSize, long total) {
        if (total == 0L) {
            return Pages.create(new ArrayList<Map<String, Object>>(0), pageNum, pageSize, total);
        }
        //前置查询参数，total <= pageSize时虽然无需用到offset和pageSize分页参数，但sql中已有分页参数，没有加入该参数执行SQL会报错
        Map<String, Object> allParams = new HashMap<String, Object>(params);
        allParams.put("offset", pageSize * (pageNum - 1));
        allParams.put("pageSize", pageSize);
        if (total <= pageSize) {
            List<Map<String, Object>> content = select(limitSql, allParams);
            return Pages.create(content, pageNum, pageSize, total);
        }
        List<Map<String, Object>> content = select(limitSql, allParams);
        return Pages.create(content, pageNum, pageSize, total);
    }

    @Override
    public Page<Map<String, Object>> select(String limitSql, String countSql,
                                            Map<String, ?> params, int pageNum,
                                            int pageSize) {
        return select(limitSql, countSql, params, params, pageNum, pageSize);
    }

    @Override
    public Page<Map<String, Object>> select(String limitSql, String countSql,
                                            Map<String, ?> params,
                                            Map<String, ?> countParams,
                                            int pageNum, int pageSize) {
        long total = count(countSql, countParams);
        return select(limitSql, params, pageNum, pageSize, total);
    }

    @Override
    public <T> List<T> select(Class<T> type, String sql, Map<String, ?> params) {
        // return this.jdbcOperations.query(sql, params, new BeanPropertyRowMapper<T>(type));
        return this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<T>(type));
    }

    @Override
    public List<Map<String, Object>> select(String sql, Map<String, ?> params) {
        return this.jdbcTemplate.queryForList(sql, params);
    }

    @Override
    public <T> T selectOne(Class<T> type, String sql, Map<String, ?> params) {
        RowMapper<T> rm =BeanPropertyRowMapper.newInstance(type);
        return this.jdbcTemplate.queryForObject(sql, params, rm);
    }

    @Override
    public long count(String sql, Map<String, ?> params) {
        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        if(total == null) {
            return 0L;
        }
        return total;
    }
    private <T> List<T> pageQuery(Class<T> type, String sql,
                                  Map<String, ?> params, int pageNum, int pageSize) {
        Map<String, Object> allParams = new HashMap<String, Object>(params);
        Sort sort = Sort.by(Sort.Order.desc("create_date"));
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        allParams.put("offset", pageRequest.getOffset());
        allParams.put("pageSize", pageSize);
        return jdbcTemplate.query(sql, allParams,
                new BeanPropertyRowMapper<T>(type));
    }

    private static class Pages {
        public static <T> Page<T> create(List<T> content, int pageNum,
                                         int pageSize, long total) {
            return new PageImpl<T>(content, PageRequest.of(pageNum, pageSize),
                    total);
        }
    }


    @Override
    public <T> T insert(Class<T> type, String insertSql, Map<String, ?> params){
        Map<String, Object> allParams = new HashMap<String, Object>(params);
        Object id = allParams.get("id");
        if(id == null || id.equals("")) {
            id = WeChatUtil.getUUID();
            allParams.put("id", id);  //自动生成id
        }
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(allParams);  //插入的数据map
        this.jdbcTemplate.update(insertSql,mapSqlParameterSource);
        List<Integer> idList = new ArrayList<Integer>();
        idList.add(Integer.parseInt(id.toString()));
        return selectById(type, idList).get(0);
    }

    /*新增insert*/
    @Override
    public <T> T addOneRecord(Class<T> type, Map<String, ?> params) throws Exception {
        if(params != null && params.size()>0) {
            StringBuilder fields = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Iterator<? extends Map.Entry<String, ?>> iter = params.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String, ?> entry = iter.next();
                if(!entry.getKey().equals("id")) {
                    fields.append(entry.getKey() + ",");
                    values.append(":" + entry.getKey() + ",");
                }
            }
            fields = fields.deleteCharAt(fields.length()-1);
            values = values.deleteCharAt(values.length()-1);
            //sql语句
            String insertSql = "insert into " + type.getSimpleName() + "(id,"+fields+") values(:id,"+values+")";
            Map<String, Object> allParams = new HashMap<String, Object>(params);
            Object id = allParams.get("id");
            if(id == null || id.equals("")) {
                id = WeChatUtil.getUUID();
                allParams.put("id", id);  //自动生成id
            }
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(allParams);  //插入的数据map
            this.jdbcTemplate.update(insertSql,mapSqlParameterSource);
            List<Integer> idList = new ArrayList<Integer>();
            idList.add(Integer.parseInt(id.toString()));
            return selectById(type, idList).get(0);
        }else
            throw new Exception("参数为空，不能插入数据");
    }

    /*批量新增insert*/
    @Override
    public <T> List<T> batchAddRecords(Class<T> type, List<Map<String, Object>> params) throws Exception {
        if(params != null && params.size()>0 && params.get(0) != null) {
            StringBuilder fields = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Iterator<? extends Map.Entry<String, ?>> iter = params.get(0).entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String, ?> entry = iter.next();
                if(!entry.getKey().equals("id")) {
                    fields.append(entry.getKey() + ",");
                    values.append(":" + entry.getKey() + ",");
                }
            }
            fields = fields.deleteCharAt(fields.length()-1);
            values = values.deleteCharAt(values.length()-1);
            //sql语句
            String insertSql = "insert into " + type.getSimpleName() + "(id,"+fields+") values(:id,"+values+")";
            List<Integer> ids = new ArrayList<>();
            Map<String, Object>[] allParams = new HashMap[params.size()];
            for(int i=0; i<params.size(); i++) {
                String id = (String) params.get(i).get("id");
                if( id == null) {
                    id = WeChatUtil.getUUID();
                    params.get(i).put("id", id);  //自动生成id
                }
                ids.add(Integer.parseInt(id));
                allParams[i] = params.get(i);
            }
            this.jdbcTemplate.batchUpdate(insertSql, allParams);
            return selectById(type, ids);
        }else
            throw new Exception("参数为空，不能插入数据");
    }

    /*修改 update*/
    @Override
    public <T> T update(Class<T> type, Map<String, ?> params) throws Exception {
        if(params != null && params.size()>0 && params.get("id") != null) {
            StringBuilder setvalues = new StringBuilder();
            Iterator<? extends Map.Entry<String, ?>> iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ?> entry = iter.next();
                if(!entry.getKey().equals("id"))
                    setvalues.append(entry.getKey()+"=:" + entry.getKey() + ",");
            }
            setvalues = setvalues.deleteCharAt(setvalues.length() - 1);
            //更新sql
            String updateSql="update " + type.getSimpleName() + " set "+setvalues+" where id=:id";
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(params);  //修改的数据map
            this.jdbcTemplate.update(updateSql,mapSqlParameterSource);
            List<Integer> idList = new ArrayList<Integer>();
            idList.add((Integer)params.get("id"));
            return selectById(type, idList).get(0);
        }else
            throw new Exception("参数不包含ID，不能更新表");
    }

    @Override
    public <T> int[] batchUpdate(String updateSql, List<Map<String, Object>> params) throws Exception {
        if(params != null && params.size()>0 && params.get(0).get("id") != null) {
            Map<String, Object>[] allParams = new HashMap[params.size()];
            for(int i=0; i<params.size(); i++) {
                Integer id = (Integer) params.get(i).get("id");
                if( id == null) {
                    throw new Exception("参数不包含ID，不能更新表");
                }
                allParams[i] = params.get(i);
            }
            return this.jdbcTemplate.batchUpdate(updateSql,allParams);
        }else
            throw new Exception("参数不包含ID，不能更新表");
    }

    @Override
    public <T> T update(Class<T> type, String updateSql, Map<String, ?> params) throws Exception {
        if(params != null && params.size()>0 && params.get("id") != null) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(params);  //修改的数据map
            this.jdbcTemplate.update(updateSql,mapSqlParameterSource);
            List<Integer> idList = new ArrayList<Integer>();
            idList.add((Integer)params.get("id"));
            return selectById(type, idList).get(0);
        }else
            throw new Exception("参数不包含ID，不能更新表");
    }

    /*删除 delete*/
    @Override
    public <T> int delete(Class<T> type, int id) {
        String deleteSql="delete from " + type.getSimpleName() + " where id=:id";
        MapSqlParameterSource mapSqlParameterSource=new MapSqlParameterSource();
        mapSqlParameterSource.addValue("id", id);
        return this.jdbcTemplate.update(deleteSql,mapSqlParameterSource);
    }

    @Override
    public <T> int execute(String sql) {
        MapSqlParameterSource mapSqlParameterSource=new MapSqlParameterSource();
        return this.jdbcTemplate.update(sql,mapSqlParameterSource);
    }

    @Override
    public <T> List<T> selectSingleColumn(String sql, Map<String, ?> params, Class<T> type) {
        MapSqlParameterSource mapSqlParameterSource=new MapSqlParameterSource();
        return this.jdbcTemplate.queryForList(sql,params, type);
    }



    public <T> List<T> selectById(Class<T> type, List<Integer> ids){
        HashMap<String, Object> idMap = new HashMap<String, Object>();
        idMap.put("id", ids);
        return select(type,"select *from "+type.getSimpleName()+" where id IN(:id)", idMap);
    }


}
