package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_player;

public interface Ykj_playerMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_player record);

    int insertSelective(Ykj_player record);

    Ykj_player selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_player record);

    int updateByPrimaryKey(Ykj_player record);
}