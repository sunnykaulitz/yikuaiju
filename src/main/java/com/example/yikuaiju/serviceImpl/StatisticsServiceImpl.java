package com.example.yikuaiju.serviceImpl;

import com.example.yikuaiju.bean.Ykj_gstatus;
import com.example.yikuaiju.bean.Ykj_playerstatus;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private ICommonService commonService;

    @Override
    public BigDecimal totalScore(Integer userid, Integer monthInterval) {
        monthInterval = monthInterval-1;
        String sql = "SELECT  " +
//                "    p.userid, " +
                "    SUM(IFNULL(sc.score, 0)) gamescore " +
                "FROM " +
                "    ykj_game g " +
                "        LEFT JOIN " +
                "    ykj_player p ON g.id = p.gameid " +
                "        LEFT JOIN " +
                "    ykj_set se ON g.id = se.gameid " +
                "        LEFT JOIN " +
                "    ykj_score sc ON p.id = sc.playerid AND se.id = sc.setid " +
                "WHERE " +
                "    DATE_FORMAT(g.modifytime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(g.modifytime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND p.userid = '"+userid+"' " +
                "        and g.gstatus="+Ykj_gstatus.gameend+" " +  //已结束
                "GROUP BY p.userid";
        List<BigDecimal> totalScore = commonService.selectSingleColumn(sql, null, BigDecimal.class);
        if(totalScore != null && totalScore.size()>0)
            return totalScore.get(0);
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> bestMonth(Integer userid, Integer monthInterval) {
        monthInterval = monthInterval-1;
        String sql = "SELECT  " +
                "    p.userid, " +
                "    DATE_FORMAT(g.creationtime, '%Y-%m') yearmonth, " +
                "    sum(IFNULL(sc.score, 0)) gamescore " +
                "FROM " +
                "    ykj_game g " +
                "        LEFT JOIN " +
                "    ykj_player p ON g.id = p.gameid " +
                "        LEFT JOIN " +
                "    ykj_set se ON g.id = se.gameid " +
                "        LEFT JOIN " +
                "    ykj_score sc ON p.id = sc.playerid AND se.id = sc.setid " +
                "WHERE " +
                "    DATE_FORMAT(g.creationtime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(g.creationtime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND p.userid = '"+userid+"' " +
                "        and g.gstatus="+Ykj_gstatus.gameend+" " +  //已结束
                "        and g.invalid=false " +        //有效
                "group by p.userid, DATE_FORMAT(g.creationtime, '%Y-%m') order by sum(IFNULL(sc.score, 0)) desc limit 1";
        List<Map<String, Object>> bestMonth = commonService.select(sql, null);
        if(bestMonth != null && bestMonth.size()>0)
            return bestMonth.get(0);
        return null;
    }

    @Override
    public Map<String, Object> bestGame(Integer userid, Integer monthInterval) {
        monthInterval = monthInterval-1;
        String sql = "SELECT  " +
                "    g.id gameid, " +
                "    g.name gamename, " +
                "    g.modifytime, " +
                "    g.address, " +
                "    g.personnum, " +
                "    g.gcount, " +
                "    p.userid, " +
                "    SUM(IFNULL(sc.score, 0)) gamescore " +
                "FROM " +
                "    ykj_game g " +
                "        LEFT JOIN " +
                "    ykj_player p ON g.id = p.gameid " +
                "        LEFT JOIN " +
                "    ykj_set se ON g.id = se.gameid " +
                "        LEFT JOIN " +
                "    ykj_score sc ON p.id = sc.playerid AND se.id = sc.setid " +
                "WHERE " +
                "    DATE_FORMAT(g.creationtime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(g.creationtime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND p.userid = '"+userid+"' " +
                "        and g.gstatus="+ Ykj_gstatus.gameend + //已结束
                "        and g.invalid=false " +        //有效
                "GROUP BY g.id , p.userid " +
                "ORDER BY SUM(IFNULL(sc.score, 0)) DESC " +
                "LIMIT 1";
        List<Map<String, Object>> bestGame = commonService.select(sql, null);
        if(bestGame != null && bestGame.size()>0)
            return bestGame.get(0);
        return null;
    }

    /*战绩一览
     * @author lifei
     * @Params
     * @return setcount:3,"pie":[{"label":胜,"count":2},{"label":负,"count":2},{"label":平,"count":1}]
     * @description: 时间区间内游戏的胜，负，平的场次
     * @date 2020/11/28 19:53
     */
    @Override
    public Map<String, Object> pieData(Integer userid, Integer monthInterval) {
        String pieSql = "SELECT  " +
                "    userid, " +
                "    SUM(win) win, " +
                "    SUM(lost) lost, " +
                "    SUM(draw) draw, " +
                "    COUNT(gameid) gamecount, " +
                "    SUM(setcount) setcount " +
                "FROM " +
                "    (SELECT  " +
                "        player.gameid, " +
                "            player.userid, " +
                "            COUNT(se.id) setcount, " +
                "            CASE " +
                "                WHEN SUM(IFNULL(sc.score, 0)) > 0 THEN 1 " +
                "                ELSE 0 " +
                "            END win, " +
                "            CASE " +
                "                WHEN SUM(IFNULL(sc.score, 0)) < 0 THEN 1 " +
                "                ELSE 0 " +
                "            END lost, " +
                "            CASE " +
                "                WHEN SUM(IFNULL(sc.score, 0)) = 0 THEN 1 " +
                "                ELSE 0 " +
                "            END draw " +
                "    FROM " +
                "        ykj_player player " +
                "    INNER JOIN ykj_game game ON game.id = player.gameid " +
                "    INNER JOIN ykj_set se ON game.id = se.gameid " +
                "    INNER JOIN ykj_score sc ON sc.setid = se.id " +
                "        AND sc.playerid = player.id " +
                "    WHERE " +
                "    DATE_FORMAT(game.creationtime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(game.creationtime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND player.userid = "+userid+" " +
                "            and player.playstatus<>"+ Ykj_playerstatus.quit +" " +
                "            AND game.gstatus = "+ Ykj_gstatus.gameend + //已结束
                "            AND game.invalid = FALSE " +   //有效
                "    GROUP BY player.gameid , player.userid) pgame " +
                "GROUP BY userid ";
        List<Map<String, Object>> pieData = commonService.select(pieSql, null);
        if(pieData != null && pieData.size()>0){
            Map<String, Object> originMap = pieData.get(0);
            BigDecimal setcount = (BigDecimal) originMap.get("setcount");
            HashMap<String, Object> pieMap = new HashMap<String, Object>();
            pieMap.put("setcount", setcount);
            //构造饼图的json格式
            List<Map<String, Object>> pieList = new ArrayList<Map<String,Object>>();
            //胜局次数
            HashMap<String, Object> winMap = new HashMap<String, Object>();
            winMap.put("label","胜");
            winMap.put("count",originMap.get("win"));
            pieList.add(winMap);
            //败局次数
            HashMap<String, Object> lostMap = new HashMap<String, Object>();
            lostMap.put("label","负");
            lostMap.put("count",originMap.get("lost"));
            pieList.add(lostMap);
            //平局次数
            HashMap<String, Object> drawMap = new HashMap<String, Object>();
            drawMap.put("label","平");
            drawMap.put("count",originMap.get("draw"));
            pieList.add(drawMap);
            pieMap.put("pie", pieList);
            return pieMap;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> lineData(Integer userid, Integer monthInterval) {
        String lineSql = "SELECT  " +
                "    COUNT(player.gameid) gamecount, " +
                "    player.userid, " +
                "    DATE_FORMAT(game.creationtime, '%Y-%m') yearmonth, " +
                "    SUM(IFNULL(sc.score, 0)) gamescore " +
                "FROM " +
                "    ykj_player player " +
                "        INNER JOIN " +
                "    ykj_game game ON game.id = player.gameid " +
                "        INNER JOIN " +
                "    ykj_set se ON game.id = se.gameid " +
                "        INNER JOIN " +
                "    ykj_score sc ON sc.setid = se.id " +
                "        AND sc.playerid = player.id " +
                "WHERE " +
                "    DATE_FORMAT(game.creationtime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(game.creationtime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND player.userid = "+userid+" " +
                "        and player.playstatus<>"+ Ykj_playerstatus.quit +" " +
                "        AND game.gstatus = " + Ykj_gstatus.gameend + //已结束
                "        AND game.invalid = FALSE " +     //有效
                "GROUP BY player.userid , DATE_FORMAT(game.creationtime, '%Y-%m')";
        List<Map<String, Object>> lineData = commonService.select(lineSql, null);
        if(lineData != null && lineData.size()>0)
            return lineData;
        return null;
    }

    @Override
    public List<Map<String, Object>> barData(Integer userid, Integer monthInterval) {
        String barSql = "SELECT  " +
                "    userid, " +
                "    yearmonth, " +
                "    SUM(winscore) winscore, " +
                "    SUM(lostscore) lostscore, " +
                "    SUM(gamescore) totalscore " +
                "FROM " +
                "    (SELECT  " +
                "        player.gameid, " +
                "            DATE_FORMAT(game.creationtime, '%Y-%m') yearmonth, " +
                "            player.userid, " +
                "            COUNT(se.id) setcount, " +
                "            CASE " +
                "                WHEN SUM(IFNULL(sc.score, 0)) > 0 THEN SUM(IFNULL(sc.score, 0)) " +
                "                ELSE 0 " +
                "            END winscore, " +
                "            CASE " +
                "                WHEN SUM(IFNULL(sc.score, 0)) < 0 THEN SUM(IFNULL(sc.score, 0)) " +
                "                ELSE 0 " +
                "            END lostscore, " +
                "            SUM(IFNULL(sc.score, 0)) gamescore " +
                "    FROM " +
                "        ykj_player player " +
                "    INNER JOIN ykj_game game ON game.id = player.gameid " +
                "    INNER JOIN ykj_set se ON game.id = se.gameid " +
                "    INNER JOIN ykj_score sc ON sc.setid = se.id " +
                "        AND sc.playerid = player.id " +
                "    WHERE " +
                "    DATE_FORMAT(game.creationtime, '%Y-%m') >= DATE_FORMAT(DATE_ADD(NOW(), INTERVAL - "+monthInterval+" MONTH), " +
                "            '%Y-%m') " +
                "        AND DATE_FORMAT(game.creationtime, '%Y-%m') <= DATE_FORMAT(NOW(), '%Y-%m') " +
                "        AND player.userid = "+userid+" " +
                "        and player.playstatus<>"+ Ykj_playerstatus.quit +" " +
                "        AND game.gstatus = " + Ykj_gstatus.gameend + //已结束
                "        AND game.invalid = FALSE " +       //有效
                "    GROUP BY player.gameid , player.userid , DATE_FORMAT(game.creationtime, '%Y-%m')) pgame " +
                "GROUP BY userid , yearmonth ";
        List<Map<String, Object>> barData = commonService.select(barSql, null);
        if(barData != null && barData.size()>0)
            return barData;
        return null;
    }
}
