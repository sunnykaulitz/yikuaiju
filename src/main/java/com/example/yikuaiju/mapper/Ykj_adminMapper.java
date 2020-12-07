package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_admin;

public interface Ykj_adminMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_admin record);

    int insertSelective(Ykj_admin record);

    Ykj_admin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_admin record);

    int updateByPrimaryKey(Ykj_admin record);
}