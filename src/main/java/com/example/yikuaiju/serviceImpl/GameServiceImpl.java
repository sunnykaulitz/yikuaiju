package com.example.yikuaiju.serviceImpl;

import com.example.yikuaiju.bean.*;
import com.example.yikuaiju.bean.resultbean.Game;
import com.example.yikuaiju.bean.resultbean.GameSummary;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IGameService;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class GameServiceImpl implements IGameService {

    @Autowired
    private ICommonService commonService;

    @Override
    public Ykj_game selectById(Integer gameid) throws Exception {
        if(gameid != null) {
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("id", gameid);
            Ykj_game game = commonService.selectOne(Ykj_game.class, "select *from ykj_game where id=:id", paramMap);
            return game;
        }
        return null;
    }

    @Override
    public Ykj_game selectByUserid(Integer gameid, Integer userid) throws Exception {
        HashMap<String, Object> selParam = new HashMap<String, Object>();
        selParam.put("userid",userid);
        selParam.put("gameid",gameid);
        List<Ykj_game> games = commonService.select(Ykj_game.class,"select " +
                "distinct b.name, b.gstatus, b.creator from ykj_player a, ykj_game b " +
                "where a.gameid=b.id " +
                "and a.playstatus <> "+Ykj_playerstatus.quit+" " +      //退出游戏
                "and a.userid=:userid " +
                "and b.invalid=false " +
                "and b.id=:gameid ", selParam);
        if(games != null && games.size()>0)
            return games.get(0);
        return null;
    }

    /*
      * 返回数据示例
      * [{game:{name:游戏1,address:杭州,personnum:3,gcount:1,gstatus:进行中,isshare:true, modifytime:2020-11-18}, gamesummary:[{gameid: 1, userid:123,playerid:1,playername:李飞,
      * avatarUrl:XXX，playstatus:游戏中,gstatus:游戏中,score:25,wincount:5,setcount:2,gamerank:1}]}]
      */
    @Override
    public List<Game> selectGamesummary(List<Integer> gameids) throws Exception {
        if(gameids != null && gameids.size()>0) {
            Map<Integer, Game> resultMap = new HashMap<Integer, Game>();
            List<GameSummary> summaryList = commonService.select(GameSummary.class, summarySql(gameids), null);
            for (int i = 0; i < summaryList.size(); i++) {
                Integer gameid = (Integer) summaryList.get(i).getGameid();
                List<GameSummary> list = new ArrayList<GameSummary>();
                list.add(summaryList.get(i));
                Game game = new Game();
                //game主表已经查询
                if (resultMap.containsKey(gameid)) {
                    game = resultMap.get(gameid);
                    list.addAll(game.getGameSummary());
                } else {
                    ArrayList<Integer> idList = new ArrayList<Integer>();
                    idList.add(gameid);
                    List<Ykj_game> Ykj_games = commonService.selectById(Ykj_game.class, idList);
                    game.setGame(Ykj_games.get(0));
                }
                game.setGameSummary(list);
                resultMap.put(gameid, game);
            }
            ArrayList<Game> gameList = new ArrayList<Game>();
            gameList.addAll(resultMap.values());
            return gameList;
        }
        return null;
    }

    /*
     * 返回数据示例
     * [{gameid:1, gamename: sss, userid:123,playerid:1,playername:李飞,
     * avatarUrl:XXX，playstatus:游戏中,gstatus:游戏中,set1score:25,set2score:20,set3score:12,gamerank:1}]
     */
    @Override
    public List<Map<String, Object>> selectGameDetail(Integer gameid) throws Exception {
        if(gameid != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("gameid",gameid);
            /*1、查询游戏是否有局，没有局则返回空*/
            long setCount = commonService.count("select count(id) from ykj_set where gameid=:gameid ", params);
            if(setCount == 0) {
                return null;
            }
//            List<Map<String, Object>> gameDetail = commonService.select(" call Score_QueryData(:gameid) ", params);
            String gameDetailSql = "SELECT  " +
                    "    game.id gameid, " +
                    "    game.name gamename, " +
                    "    player.userid, " +
                    "    player.id playerid, " +
                    "    CASE " +
                    "        WHEN us.nickName IS NULL THEN player.playername " +
                    "        ELSE us.nickName " +
                    "    END playername, " +
                    "    player.playstatus, " +
                    "    us.avatarUrl, " +
                    "    game.gstatus, " +
                    "    SUM(sc.score) totalscore, " +
                    "    player.gamerank, " +
                    "    player.isshare, " +
                    "    GROUP_CONCAT(sc.score " +
                    "        ORDER BY se.setnumber ASC) scores " +
                    "FROM " +
                    "    ykj_score sc " +
                    "        LEFT JOIN " +
                    "    ykj_set se ON se.id = sc.setid " +
                    "        LEFT JOIN " +
                    "    ykj_player player ON sc.playerid = player.id " +
                    "        LEFT JOIN " +
                    "    ykj_game game ON se.gameid = game.id " +
                    "        AND player.gameid = game.id " +
                    "        LEFT JOIN " +
                    "    ykj_user us ON player.userid = us.id " +
                    "WHERE " +
                    "    game.invalid = FALSE " +
                    "        AND player.playstatus <> 2 " +
                    "        AND game.id = "+gameid+" " +
                    "GROUP BY game.id , game.name , player.userid , player.id , CASE " +
                    "    WHEN us.nickName IS NULL THEN player.playername " +
                    "    ELSE us.nickName " +
                    "END , player.playstatus , us.avatarUrl , game.gstatus , player.gamerank";
            List<Map<String, Object>> gameDetail = commonService.select(gameDetailSql, null);
            if(gameDetail != null && gameDetail.size()>0){
                for(Map<String, Object> map:gameDetail){
                    String scores = (String) map.get("scores");
                    if(scores != null && scores.length()>0){
                        String[] strArr = scores.split(",");
                        BigDecimal[] scoreArray = (BigDecimal[]) ConvertUtils.convert(strArr, BigDecimal.class);
                        map.put("scores", scoreArray);
                    }
                }
            }
            return gameDetail;
        }
        return null;
    }

    @Override
    public Ykj_set createSet(Integer gameid, Integer userid){
        HashMap<String, Object> insertParam = new HashMap<String, Object>();
                        insertParam.put("gameid",gameid);
                        insertParam.put("userid", userid);
        Ykj_set ykj_set = commonService.insert(Ykj_set.class,
                "insert into ykj_set (id,gameid,setnumber, creationtime,creator,ts) " +
                        "values(:id, :gameid, (select gcount+1 from ykj_game where id=:gameid ), sysdate(), :userid, sysdate())",
                insertParam);
        return ykj_set;
    }

    @Override
    public List<Map<String, Object>> selectSet(Integer setid) {
        HashMap<String, Object> setParam = new HashMap<String, Object>();
        setParam.put("setid",setid);
        List<Map<String, Object>> setMap = commonService.select("select " +
                "p.gameid, " +
                "p.id playerid, " +
                "p.isshare, " +
                "case when u.id is not null then p.playername else u.nickName end playername, " +
                "u.avatarUrl, " +
                "se.id setid, " +
                "se.setnumber, " +
                "sc.score, " +
                "sc.iswin " +
                "from ykj_set se " +
                "inner join ykj_player p on se.gameid = p.gameid " +
                "left join ykj_user u on u.id = p.userid " +
                "left join ykj_score sc on se.id = sc.setid " +
                "       and p.id = sc.playerid " +
                "where se.id=:setid and p.playstatus<>"+Ykj_playerstatus.quit+ " ", setParam);
        return setMap;
    }


    @Override
    public List<Integer> selectUnfinishGame(Integer userid) {
        String sql = "select p.gameid from ykj_player p " +
                "inner join ykj_game g on p.gameid = g.id " +
                "where g.invalid=false " +
                "   and p.playstatus<>"+Ykj_playerstatus.quit+" " +     //不是退出游戏的玩家
                "   and g.gstatus<>"+ Ykj_gstatus.gameend +     //未结束
                "   and p.userid='"+userid+"' ";
        return commonService.selectSingleColumn(sql, null, Integer.class);
    }

    @Override
    public List<Integer> selectFinishGame(Integer userid) {
        String sql = "select p.gameid from ykj_player p " +
                "inner join ykj_game g on p.gameid = g.id " +
                "where g.invalid=false " +
                "   and p.playstatus<>"+Ykj_playerstatus.quit+" " +     //不是退出游戏的玩家
                "   and g.gstatus = "+ Ykj_gstatus.gameend +" " +
                "   and p.userid='"+userid+"' ";
        return commonService.selectSingleColumn(sql, null, Integer.class);
    }

    @Override
    public Ykj_game endGame(Integer gameid, Integer userid) throws Exception {
        /*3、更新游戏ykj_game的ts和modifytime、endtime、finisher，游戏状态改为已结束*/
        HashMap<String, Object> updateParam = new HashMap<String, Object>();
        updateParam.put("userid",userid);
        updateParam.put("id",gameid);
        updateParam.put("gstatus", Ykj_gstatus.gameend);  //已结束
        updateParam.put("nowtime",new Date());
        String updateSql = "update ykj_game set " +
                "endtime=:nowtime, " +
                "taketime = date_format(date_sub(from_unixtime(UNIX_TIMESTAMP(:nowtime)-UNIX_TIMESTAMP(creationtime)), INTERVAL 8 HOUR),'%H小时%i分钟%s秒'), " +
                "ts=:nowtime, " +
                "modifytime=:nowtime, " +
                "finisher=:userid, " +
                "gstatus=:gstatus where id=:id";
        Ykj_game game = commonService.update(Ykj_game.class, updateSql, updateParam);
        return game;
    }

    @Override
    public List<Integer> getInactiveGame() throws Exception {
        return commonService.selectSingleColumn("select g.id from ykj_game g " +
                "where g.gstatus<>"+ Ykj_gstatus.gameend +" " +  //已结束
                "and DATE_ADD(g.modifytime, INTERVAL 1 DAY)<sysdate()",
                null, Integer.class);
    }

    @Override
    public int[] autoEndGameTask() throws Exception {
        List<Integer> gameids = getInactiveGame();
        if(gameids != null && gameids.size()>0) {
            List<Map<String, Object>> updateParam = new ArrayList<Map<String, Object>>();
            for(Integer gameid: gameids){
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("id", gameid);
                params.put("gstatus", Ykj_gstatus.gameend);  //已结束
                params.put("nowtime", new Date());
                updateParam.add(params);
            }
            String updateSql = "update ykj_game set " +
                        "endtime=:nowtime, " +
                        "ts=:nowtime, " +
                        "modifytime=:nowtime, " +
                        "gstatus=:gstatus " +
                    "where id=:id";
            return commonService.batchUpdate(updateSql,updateParam);
        }
        return new int[0];
    }

    @Override
    public void createPlayerByGroup(Integer groupid, Integer gameid) throws Exception {
        /*1、新建从未加入过此游戏的玩家*/
        String  sql = "SELECT  " +
                "    '"+gameid+"' gameid, " +
                "    pa.userid, " +
                "    pa.playername, " +
                "    SYSDATE() jointime, " +
                "    pa.playstatus, " +
                "    SYSDATE() ts, " +
                "    pa.isshare, " +
                "    pa.gamerank " +
                "FROM " +
                "    ykj_group gp " +
                "        INNER JOIN " +
                "    ykj_game ga ON gp.gameid = ga.id " +
                "        INNER JOIN " +
                "    ykj_player pa ON ga.id = pa.gameid " +
                "                       and pa.playstatus<>" + Ykj_playerstatus.quit+" "+   //不计算退出玩家
                "WHERE " +
                "    gp.id = '"+groupid+"' " +
                "    and pa.isshare = false " +     //不是分摊玩家
                "        AND pa.userid NOT IN (SELECT  " +
                "            userid " +
                "        FROM " +
                "            ykj_player " +
                "        WHERE " +
                "            gameid = '"+gameid+"')";
        List<Map<String, Object>> palyerMap = commonService.select(sql, null);
        commonService.batchAddRecords(Ykj_player.class, palyerMap);
        /*2、更新加入过此游戏又退出,并且在快捷群里的玩家的状态*/
        commonService.execute("update ykj_player set playstatus="+Ykj_playerstatus.playing+" " +
                "where playstatus="+Ykj_playerstatus.quit+" " +
                "       and gameid="+gameid+" " +
                "       and id in (" +
                "           SELECT  " +
                "               p.id " +
                "           FROM " +
                "               ykj_group gp, " +
                "               ykj_game game, " +
                "               ykj_player p " +
                "           WHERE " +
                "               gp.gameid = game.id " +
                "                   AND game.id = p.gameid) ");
    }

    private String getSql(boolean isshare, List<Integer> gameids){
        String selectsql = "select " +
                "game.id gameid, " +
                "us.id userid, " +
                "case when us.id is not null and us.unionkey is null then true else false end virtualuser, " +
                "player.id playerid, " +
                "case when us.nickName is null then player.playername else us.nickName end playername, " +
                "player.playstatus, " +
                "player.isshare, " +
                "us.avatarUrl, " +
                "player.gamerank, " +
                "se.id setid, " +
                "se.setnumber, " +
                "sc.score, " +
                "sc.iswin " +
                "from ykj_game game " +
                "inner join ykj_player player on game.id = player.gameid " +
                "left join ykj_set se on game.id = se.gameid " +
                "left join ykj_score sc on se.id = sc.setid and player.id = sc.playerid " +
                "left join ykj_user us on player.userid = us.id " +
                "where game.invalid=false and player.playstatus<>"+Ykj_playerstatus.quit+" " +
                "and player.isshare ="+isshare+" ";

        if(gameids != null && gameids.size()>0) {
            StringBuilder pks = new StringBuilder();
            for(Integer id : gameids) {
                pks.append(" '"+id+"',");
            }
            pks = pks.deleteCharAt(pks.length()-1);
            selectsql = selectsql + "and game.id in (" + pks + ") ";
        }

        selectsql = selectsql + " order by game.modifytime asc ";
        return selectsql;
    }

    private String summarySql(List<Integer> gameid){
        String summarySql = "select " +
                "gameid, " +
                "userid, " +
                "virtualuser, " +
                "playerid, " +
                "playername, " +
                "isshare, " +
                "playstatus, " +
                "avatarUrl, " +
                "sum(score) score, " +
                "sum(iswin) wincount, " +
                "count(distinct setid)setcount, " +
                "0 gamerank " +
            "from ("+getSql(true, gameid)+") gamesum1 " +
                "group by " +
                    "gameid, " +
                    "userid, " +
                    "virtualuser, " +
                    "playerid, " +
                    "playername, " +
                    "isshare, " +
                    "playstatus, " +
                    "avatarUrl " +
        "union all "+
                "select " +
                "gameid, " +
                "userid, " +
                "virtualuser, " +
                "playerid, " +
                "playername, " +
                "isshare, " +
                "playstatus, " +
                "avatarUrl, " +
                "sum(score) score, " +
                "sum(iswin) wincount, " +
                "count(distinct setid)setcount, " +
                "ROW_NUMBER() over(order by sum(score) desc) gamerank " +
            "from ("+getSql(false, gameid)+") gamesum2 " +
                "group by " +
                    "gameid, " +
                    "userid, " +
                    "virtualuser, " +
                    "playerid, " +
                    "playername, " +
                    "isshare, " +
                    "playstatus, " +
                    "avatarUrl " ;
        return summarySql;
    }


    private String procedure_Score_QueryData(){
        StringBuilder columnSwitchSql = new StringBuilder();
        columnSwitchSql.append("DELIMITER &&    " +
                "                drop procedure if exists Score_QueryData;  " +
                "                Create Procedure Score_QueryData(IN gameid int)  " +
                "                READS SQL DATA   " +
                "                BEGIN  " +
                " " +
                "                SET @sql = NULL;  " +
                "                SET @columnSql = NULL;  " +
                "                SET @gameid = gameid;  " +
                "SELECT  " +
                "    GROUP_CONCAT(DISTINCT CONCAT('MAX(IF(se.setnumber = ''', " +
                "                se.setnumber, " +
                "                ''', sc.score, 0)) AS ''第', " +
                "                se.setnumber, " +
                "                '局''')) " +
                "INTO @columnSql FROM " +
                "    ykj_set se;  " +
                " " +
                "                  " +
                "                SET @sql = CONCAT('select game.id gameid, game.name gamename, player.userid, player.id playerid, case when us.nickName is null then player.playername else us.nickName end playername, player.playstatus, us.avatarUrl, game.gstatus, sum(sc.score) score, player.gamerank, player.isshare, ', @columnSql,   " +
                "                                        ' from ykj_score sc left join ykj_set se on se.id = sc.setid left join ykj_player player on sc.playerid = player.id left join ykj_game game on se.gameid= game.id and player.gameid = game.id left join ykj_user us on player.userid = us.id where game.invalid=false and player.playstatus<>2 ');                          " +
                "                IF @gameid is not null and @gameid != 0 then  " +
                "                SET @sql = CONCAT(@sql, ' and game.id = ''', @gameid, '''');  " +
                "                END IF;      " +
                "        " +
                "                SET @sql = CONCAT(@sql, ' group by game.id, game.name , player.userid, player.id , case when us.nickName is null then player.playername else us.nickName end , player.playstatus, us.avatarUrl, game.gstatus, player.gamerank ');  " +
                " " +
                "                 " +
                "                PREPARE stmt FROM @sql;  " +
                "                EXECUTE stmt;  " +
                "                DEALLOCATE PREPARE stmt;  " +
                "           " +
                "                END &&    " +
                "           " +
                "                DELIMITER ;");
        return columnSwitchSql.toString();
    }
}
