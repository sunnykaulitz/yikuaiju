package com.example.yikuaiju.service;

import com.example.yikuaiju.bean.Ykj_game;
import com.example.yikuaiju.bean.Ykj_player;
import com.example.yikuaiju.bean.Ykj_set;
import com.example.yikuaiju.bean.resultbean.Game;

import java.util.List;
import java.util.Map;

public interface IGameService {

     /*
      * @author lifei
      * @Params gameid
      * @return
      * @description: 根据游戏id查询游戏vo
      * @date 2020/11/20 10:36
      */
    Ykj_game selectById(Integer gameid) throws Exception;

     /*根据用户和游戏查询是否在游戏中，如果在游戏中查出game,如果不在返回空
      * @author lifei
      * @Params
      * @return
      * @description: 描述
      * @date 2020/11/29 14:47
      */
    Ykj_game selectByUserid(Integer gameid, Integer userid) throws Exception;

     /*玩家、用户、胜局数、总分小结
      * @author lifei
      * @Params gameid
      * @return {game:{name:游戏1,address:杭州,personnum:3,gcount:1,gstatus:进行中,isshare:true, modifytime:2020-11-18}, gamesummary:[{gameid: 1, userid:123,playerid:1,playername:李飞,
      * avatarUrl:XXX，playstatus:游戏中,gstatus:游戏中,score:25,wincount:5,setcount:2,gamerank:1}
      * @description: 根据游戏id查询玩家ykj_player、用户ykj_user、游戏局ykj_set、游戏计分ykj_score,
      * 返回的mapkey为gameid, gamename, userid, playerid, playername, playstatus, avatarUrl, gstatus,score,wincount,setcount,gamerank
      * 公摊玩家为list的第一位gamerank=0，其余玩家排名
      * @date 2020/11/20 10:38
      */
     List<Game> selectGamesummary(List<Integer> gameids) throws Exception;


     /*玩家、用户、游戏局、计分明细
      * @author lifei
      * @Params gameid
      * @return [{gameid:1, gamename: sss, userid:123,playerid:1,playername:李飞,
      * avatarUrl:XXX，playstatus:游戏中,gstatus:游戏中,set1score:25,set2score:20,set3score:12,gamerank:1}]
      * @description: 根据游戏id查询玩家ykj_player、用户ykj_user、游戏局ykj_set、游戏计分ykj_score,
      * 返回的mapkey为gameid, gamename, userid, playerid, playername, playstatus, avatarUrl, gstatus、set1score(第一局分，不定列)、gamerank
      * 公摊玩家为list的第一位gamerank=0
      * @date 2020/11/20 11:05
      */
    List<Map<String,Object>> selectGameDetail(Integer gameid) throws Exception;

    Ykj_set createSet(Integer gameid, Integer userid);

    List<Map<String,Object>> selectSet(Integer setid);

     /*根据用户查询未结束的游戏
      * @author lifei
      * @Params
      * @return
      * @description: 描述
      * @date 2020/11/21 22:09
      */
    List<Integer> selectUnfinishGame(Integer userid);

    /*根据用户查询已结束的游戏
     * @author lifei
     * @Params
     * @return
     * @description: 描述
     * @date 2020/11/21 22:09
     */
    List<Integer> selectFinishGame(Integer userid);

     /*
      * @author lifei
      * @Params
      * @return
      * @description: 结束游戏
      * @date 2020/11/25 12:50
      */
     Ykj_game endGame(Integer gameid, Integer userid) throws Exception;

     /*
      * @author lifei
      * @Params
      * @return
      * @description: 查询24小时未操作且未结束的游戏
      * @date 2020/11/25 12:53
      */
    List<Integer> getInactiveGame() throws Exception;

     /*
      * @author lifei
      * @Params
      * @return
      * @description: 超过24小时未操作且未结束的游戏自动结束
      * @date 2020/11/25 13:13
      */
    int[] autoEndGameTask() throws Exception;

     /*根据群组创建玩家
      * @author lifei
      * @Params
      * @return
      * @description: 描述
      * @date 2020/11/26 14:32
      */
    void createPlayerByGroup(Integer groupid, Integer gameid) throws Exception;
}
