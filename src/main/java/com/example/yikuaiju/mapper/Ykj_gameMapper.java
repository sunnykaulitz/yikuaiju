package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_game;
import com.example.yikuaiju.bean.resultbean.operation.OperatorGame;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Ykj_gameMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_game record);

    int insertSelective(Ykj_game record);

    Ykj_game selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_game record);

    int updateByPrimaryKey(Ykj_game record);


    //运维查询
    @Select("<script>"+"SELECT  " +
            "    game.id, " +
            "    game.creationtime, " +
            "    game.creator, " +
            "    game.name, " +
            "    gs.gstatus, " +
            "    ft.formtype, " +
            "    g.name groupname, " +
            "    ifnull(pu.pcount,0) pcount, " +
            "    ifnull(pu.wechatpcount,0) wechatpcount, " +
            "    ifnull(gs.setcount,0) setcount, " +
            "    case when game.finisher is null then concat( game.endtime, '(系统)') else game.endtime end endtime , " +
            "    game.sharetimes " +
            "FROM " +
            "    ykj_game game " +
            "        INNER JOIN " +
            "    ykj_gstatus gs ON gs.id = game.gstatus " +
            "        LEFT JOIN " +
            "    ykj_formtype ft ON ft.id = game.formtype " +
            "        LEFT JOIN " +
            "    ykj_relation rl ON rl.gameid = game.id " +
            "        LEFT JOIN " +
            "    ykj_group g ON g.id = rl.groupid " +
            "        LEFT JOIN " +
            "    (SELECT  " +
            "        player.gameid, " +
            "            COUNT(us.id) pcount, " +
            "            COUNT(us.unionkey) wechatpcount " +
            "    FROM " +
            "        ykj_player player, ykj_user us " +
            "    WHERE " +
            "        player.userid = us.id " +
            "    GROUP BY player.gameid) pu ON pu.gameid = game.id " +
            "        LEFT JOIN " +
            "    (SELECT  " +
            "        se.gameid, COUNT(se.id) setcount " +
            "    FROM " +
            "        ykj_set se " +
            "    GROUP BY se.gameid) gs ON gs.gameid = game.id " +
            "   where 1=1 " +
            "<if test='name!=null'> " +
            "           and (" +
            "               game.name like CONCAT('%',#{name},'%') " +
            "   <if test='groupname!=null'> " +
            "               or g.name like CONCAT('%',#{groupname},'%') " +
            "   </if> " +
            "   <if test='id!=null'> " +
            "               or game.id like CONCAT('%',#{id},'%') " +
            "   </if> " +
            "           ) " +
            "</if> " +
            "<if test='formtype!=null'> " +
            "           and game.formtype like CONCAT('%',#{formtype},'%') " +
            "</if> " +
            "order by game.creationtime desc "
            +"</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "creationtime", column = "creationtime"),
            @Result(property = "creator",column = "creator"),
            @Result(property = "name",column = "name"),
            @Result(property = "gstatus",column = "gstatus"),
            @Result(property = "formtype",column = "formtype"),
            @Result(property = "groupname",column = "groupname"),
            @Result(property = "pcount",column = "pcount"),
            @Result(property = "wechatpcount",column = "wechatpcount"),
            @Result(property = "setcount",column = "setcount"),
            @Result(property = "endtime",column = "endtime"),
            @Result(property = "sharetimes",column = "sharetimes")
    })
    public List<OperatorGame> selectOperatorGame(OperatorGame game);

}