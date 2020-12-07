package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_group;

public interface Ykj_groupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_group record);

    int insertSelective(Ykj_group record);

    Ykj_group selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_group record);

    int updateByPrimaryKey(Ykj_group record);
}