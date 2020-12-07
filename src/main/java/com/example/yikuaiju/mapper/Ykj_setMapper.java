package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_set;

public interface Ykj_setMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_set record);

    int insertSelective(Ykj_set record);

    Ykj_set selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_set record);

    int updateByPrimaryKey(Ykj_set record);
}