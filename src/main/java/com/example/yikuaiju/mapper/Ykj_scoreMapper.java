package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_score;

public interface Ykj_scoreMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_score record);

    int insertSelective(Ykj_score record);

    Ykj_score selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_score record);

    int updateByPrimaryKey(Ykj_score record);
}