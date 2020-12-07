package com.example.yikuaiju.service;

import com.example.yikuaiju.bean.Ykj_game;
import com.example.yikuaiju.bean.Ykj_group;
import com.example.yikuaiju.bean.Ykj_relation;
import com.example.yikuaiju.bean.resultbean.Group;

import java.util.Date;
import java.util.List;

public interface IGroupService {

     /*由现场邀请新建群组
      * @author lifei
      * @Params nameFromGroup 原群组名称
      * @return
      * @description: 描述
      * @date 2020/12/4 17:25
      */
    Ykj_group createNewGroup(Ykj_game game, String nameFromGroup) throws Exception;

    Ykj_relation relateGroup(Integer gameid, Integer groupid, Date relateDate)throws Exception;

    List<Group> getRelatedGroups(Integer userid);

     /*由快捷邀请新建群组
      * @author lifei
      * @Params
      * @return
      * @description: 描述
      * @date 2020/12/4 17:29
      */
    Ykj_group createGroupByGroup(Ykj_game game) throws Exception;
}
