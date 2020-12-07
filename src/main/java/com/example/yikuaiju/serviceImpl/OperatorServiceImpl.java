package com.example.yikuaiju.serviceImpl;

import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperatorServiceImpl implements IOperatorService {

    @Autowired
    private ICommonService commonService;

    @Override
    public Page<Map<String, Object>> userList(int pageNum, int pageSize) {
        long total = commonService.count("select count(id) from ykj_user ", null);
        String limitsql = userListSql() + "   LIMIT :offset, :pageSize ";
        Map<String, Object> params = new HashMap<String, Object>();
        Page<Map<String, Object>> page = commonService.select(limitsql, params, pageNum, pageSize, total);
        return page;
    }

    @Override
    public List<Map<String, Object>> userList() {
        String limitsql = userListSql();
        return commonService.select(limitsql, null);
    }

    private String userListSql(){
        String limitsql = "SELECT  " +
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
                "    gm.modifytime, " +
                "    gm.activedays, " +
                "    case  " +
                "    when us.source =1 then '小程序搜索'  " +
                "        else '活动扫码'  " +
                "  end source, " +
                "    us.mark, " +
                "    gm.groupcount " +
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
                "               order by us.creationtime desc";
        return limitsql;
    }

    @Override
    public Page<Map<String, Object>> gameList(int pageNum, int pageSize) {
        long total = commonService.count("select count(id) from ykj_game ", null);
        String limitSql = gameListSql() + "   LIMIT :offset, :pageSize";
        Map<String, Object> params = new HashMap<String, Object>();
        Page<Map<String, Object>> page = commonService.select(limitSql, params, pageNum, pageSize, total);
        return page;
    }

    @Override
    public List<Map<String, Object>> gameList(){
        String limitSql = gameListSql();
        return commonService.select(limitSql, null);
    }

    private String gameListSql() {
        String limitSql = "SELECT  " +
                "    game.id, " +
                "    game.creationtime, " +
                "    game.creator, " +
                "    game.name, " +
                "    gs.gstatus, " +
                "    ft.formtype, " +
                "    g.name groupname, " +
                "    pu.pcount, " +
                "    pu.wechatpcount, " +
                "    gs.setcount, " +
                "    game.endtime, " +
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
                "order by game.creationtime desc ";
        return limitSql;
    }
}
