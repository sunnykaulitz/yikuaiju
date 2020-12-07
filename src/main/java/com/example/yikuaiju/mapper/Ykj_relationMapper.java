package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_relation;

public interface Ykj_relationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_relation record);

    int insertSelective(Ykj_relation record);

    Ykj_relation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_relation record);

    int updateByPrimaryKey(Ykj_relation record);
}