package com.example.yikuaiju.mapper;

import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.bean.resultbean.operation.OperatorUser;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Ykj_userMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Ykj_user record);

    int insertSelective(Ykj_user record);

    Ykj_user selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Ykj_user record);

    int updateByPrimaryKey(Ykj_user record);


    @Select("<script>"+"SELECT  " +
            "    us.id, " +
            "    us.nickName, " +
            "    CASE " +
            "        WHEN us.gender = 1 THEN '男' " +
            "        WHEN us.gender = 2 THEN '女' " +
            "        ELSE '未知' " +
            "    END gender, " +
            "    us.city, " +
            "    us.mobile, " +
            "    us.creationtime, " +
            "    case when gm.modifytime is null then us.modifytime else gm.modifytime end modifytime, " +
            "    case when gm.activedays is null then 1 else gm.activedays end activedays, " +
            "    case  " +
            "    when us.source =1 then '小程序搜索'  " +
            "        else '活动扫码'  " +
            "  end source, " +
            "    us.mark, " +
            "    gm.groupcount, " +
            "    us.status " +
            "FROM " +
            "    ykj_user us  " +
            "    LEFT JOIN  " +
            "             (  " +
            "            SELECT   " +
            "                player.userid,  " +
            "                game.modifytime,  " +
            "                row_number() over(partition by player.userid order by game.modifytime desc) rn,  " +
            "                count(date_format(game.modifytime,'%Y-%m-%d')) over(partition by player.userid ) activedays,  " +
            "                count(re.groupid) over(partition by player.userid) groupcount  " +
            "            FROM  " +
            "                ykj_game game  " +
            "                left join ykj_relation re on re.gameid = game.id  " +
            "                    INNER JOIN  " +
            "                ykj_player player ON game.id = player.gameid  " +
            "                where player.isshare=false ) gm on us.id = gm.userid  " +
            "                       and gm.rn=1  " +
            "            where us.unionkey is not null  " +
            "<if test='nickName!=null'> " +
            "           and (" +
            "               us.nickName like CONCAT('%',#{nickName},'%') " +
            "   <if test='id!=null'> " +
            "               or us.id like CONCAT('%',#{id},'%') " +
            "   </if> " +
            "   <if test='mobile!=null'> " +
            "               or us.mobile like CONCAT('%',#{mobile},'%') " +
            "   </if> " +
            "   <if test='mark!=null'> " +
            "               or us.mark like CONCAT('%',#{mark},'%') " +
            "   </if> " +
            "           ) " +
            "</if> " +
            "<if test='source!=null'> " +
            "           and us.source like CONCAT('%',#{source},'%') " +
            "</if> " +
            "               order by us.creationtime desc"
            +"</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "nickName", column = "nickName"),
            @Result(property = "gender",column = "gender"),
            @Result(property = "city",column = "city"),
            @Result(property = "mobile",column = "mobile"),
            @Result(property = "creationtime",column = "creationtime"),
            @Result(property = "modifytime",column = "modifytime"),
            @Result(property = "activedays",column = "activedays"),
            @Result(property = "source",column = "source"),
            @Result(property = "mark",column = "mark"),
            @Result(property = "groupcount",column = "groupcount")
    })
    public List<OperatorUser> selectOperatorUser(OperatorUser user);
}