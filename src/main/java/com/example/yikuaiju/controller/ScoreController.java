package com.example.yikuaiju.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.yikuaiju.bean.*;
import com.example.yikuaiju.bean.common.CommonBean;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IGameService;
import com.example.yikuaiju.service.IUserService;
import com.example.yikuaiju.util.BeanMapConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("score")
@Transactional
public class ScoreController {

    private static final Logger logger = LoggerFactory.getLogger(ScoreController.class);

    @Autowired
    private ICommonService commonService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGameService gameService;


    //记一局,创建一局
    @RequestMapping(value = "/startSet",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean startSet(Integer gameid, String unionkey, Date ts, boolean cancel) {
        CommonBean commonBean = new CommonBean();
        if(gameid == null ){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏id不能为空");
            return commonBean;
        }
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey不能为空");
            return commonBean;
        }
        if(ts == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏时间戳ts不能为空");
            return commonBean;
        }
        try {
            /*1、校验ts、游戏是否在进行中，若不在进行中或ts失效则return*/
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("gameid",gameid);
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct id, name, gstatus, ts from ykj_game " +
                    "where id=:gameid ", selParam);
            if(inGame != null && inGame.size()>0 && inGame.get(0)!= null){
                Integer gstatus = (Integer) inGame.get(0).get("gstatus");
                if(!gstatus.equals(Ykj_gstatus.gameing)){  //进行中
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏["+inGame.get(0).get("name")+"]"+inGame.get(0).get("gstatus")+"，不能记一局");
                    return commonBean;
                }
                Date db_ts = (Date) inGame.get(0).get("ts");
                if(db_ts.getTime() != (ts.getTime())){
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏已变更，请刷新");
                    return commonBean;
                }
            }else{
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏"+gameid+"不存在");
                return commonBean;
            }
            /*2、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*3、根据userid和gameid更新玩家表ykj_player的玩家状态playstatus=游戏中或正在编辑*/
            Integer playstatus = Ykj_playerstatus.editing;       //"正在编辑"
            if(cancel)//取消记一局
                playstatus = Ykj_playerstatus.playing;      //"游戏中"
            commonService.execute("update ykj_player " +
                    "set playstatus='"+playstatus+"', ts=sysdate() where userid='"+user.getId()+"' and gameid='"+gameid+"' ");
            /*5、查询游戏是否有未计分的局，若没有则创建一局ykj_set，局次、局次创建时间、游戏场id*/
            List<Map<String, Object>> nullset = commonService.select("select se.id, se.setnumber " +
                    "from ykj_set se where " +
                    "       se.id not in (select distinct sc.setid from ykj_score sc ) " +
                    "       and se.gameid="+gameid+" and se.creator="+user.getId()+" ", null);
            Integer setid = null;
            if(cancel){//取消记一局
                //删除未计分的局
                commonService.execute("delete from ykj_set " +
                        "where " +
                        "       id not in (select distinct sc.setid from ykj_score sc ) " +
                        "       and gameid="+gameid+"");
            }else{//记一局
                //如果有未计分的局，直接用
                if(nullset != null && nullset.size()>0 && nullset.get(0)!= null){
                    setid = (Integer) nullset.get(0).get("id");
                }else{//没有未计分的局，就创建一局
                    Ykj_set ykj_set = gameService.createSet(gameid, user.getId());
                    setid = ykj_set.getId();
                }
            }
            /*6、更新游戏的时间戳ts、最新登记时间modifytime、局次gcount*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id",gameid);
            updateParam.put("nowtime",new Date());
            String updateSql = "update ykj_game set " +
                    "ts=:nowtime, " +
                    "modifytime=:nowtime, " +
                    "gcount=(select count(se.id) from ykj_set se where se.gameid=:id) " +
                    "where id=:id ";
            Ykj_game game = commonService.update(Ykj_game.class, updateSql, updateParam);
            /*7、查询同游戏是否有60秒内的不同玩家创建的局*/
            boolean needConfirm = false;
            long setCount = commonService.count("select count(*) from ykj_set " +
                    "where creationtime>DATE_SUB(sysdate(),interval 60 SECOND) " +
                    "and creator<>" +user.getId()+" "+
                    "and id<>" + setid, null);
            if(setCount>0)
                needConfirm = true;
            HashMap<String,Object> setMap = new HashMap<String,Object>();
            setMap.put("gameid",gameid);
            setMap.put("setid",setid);
            setMap.put("ts",game.getTs());
            setMap.put("needConfirm",needConfirm); //确认是否重复操作
            commonBean.setSuccess(true);
            commonBean.setMessage("记一局");
            commonBean.setData(setMap);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("记一局失败！"+e.getMessage());
            return commonBean;
        }
    }

    //确认计分
    @RequestMapping(value = "/saveOneSet",method = RequestMethod.POST, headers = {"content-type=application/json"})
    @ResponseBody   //代表返回json
    public CommonBean saveOneSet(@RequestBody JSONObject jsonObject) {
        CommonBean commonBean = new CommonBean();
        try {
            Integer gameid = jsonObject.getInteger("gameid");
            Integer setid = jsonObject.getInteger("setid");
            String ts_str = jsonObject.getString("ts");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date ts = formatter.parse(ts_str.replace("/", "-"));
            JSONArray jsonScores = jsonObject.getJSONArray("jsonScores");
            if(gameid == null ){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏id不能为空");
                return commonBean;
            }
            if(setid == null ){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏局setid不能为空");
                return commonBean;
            }
            if(jsonScores == null || jsonScores.size() == 0){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏分数记录不能为空");
                return commonBean;
            }
            if(ts == null){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏ts不能为空");
                return commonBean;
            }

            /*1、校验ts、游戏是否在进行中，若不在进行中或ts失效则return*/
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("gameid",gameid);
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct id, name, gstatus, ts from ykj_game " +
                    "where id=:gameid ", selParam);
            if(inGame != null && inGame.size()>0 && inGame.get(0)!= null){
                Integer gstatus = (Integer) inGame.get(0).get("gstatus");
                if(!gstatus.equals(Ykj_gstatus.gameing)){  //进行中
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏["+inGame.get(0).get("name")+"]"+inGame.get(0).get("gstatus")+"，不能确认计分");
                    return commonBean;
                }
                Date db_ts = (Date) inGame.get(0).get("ts");
                if(db_ts.getTime() != (ts.getTime())){
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏已变更，请刷新");
                    return commonBean;
                }
            }else{
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏"+gameid+"不存在");
                return commonBean;
            }
            /*2、创建一个score、用户id、游戏局id、是否胜、分数*/
            List<Ykj_score> scoreList = JSONObject.parseArray(jsonScores.toJSONString(), Ykj_score.class);
            for(Ykj_score score : scoreList){
                score.setSetid(setid);
                score.setTs(new Date());
            }
            commonService.batchAddRecords(Ykj_score.class, BeanMapConvertUtil.beanListToMapList(scoreList));
            List<Ykj_score> fakeScoreList = commonService.select(Ykj_score.class,
                    "select p.id playerid, se.id setid, false iswin, ifnull(sc.score, 0) score, sc.id, sysdate() ts " +
                            "from ykj_player p " +
                            "inner join ykj_set se on p.gameid = se.gameid " +
                            "left join ykj_score sc on sc.setid = se.id and sc.playerid = p.id " +
                            "where sc.id is null and p.playstatus <>" + Ykj_playerstatus.quit +" "+
                            "and p.gameid='"+gameid+"' ", null);
            if(fakeScoreList != null && fakeScoreList.size()>0 && fakeScoreList.get(0) != null) {
                commonService.batchAddRecords(Ykj_score.class, BeanMapConvertUtil.beanListToMapList(fakeScoreList));
            }
            /*3、根据gameid更新玩家表ykj_player的玩家状态playstatus=游戏中、ts、gamerank*/
            String updateSql = "update ykj_player player set " +
                    "playstatus= "+Ykj_playerstatus.playing+", " +  //'游戏中'
                    "gamerank=( " +
                            "case when player.isshare=true " +
                        "then 0  " +
                        "else " +
                            "(select gp.gamerank from ( " +
                                    " (select ROW_NUMBER() over(order by sum(gs.score) desc) gamerank, gs.playerid " +
                                        "from ( select se.gameid, sc.playerid, sc.score, sc.setid,p.playername " +
                                                    "from ykj_score sc, ykj_set se, ykj_player p " +
                                                "where se.id =sc.setid " +
                                                    "and p.id = sc.playerid " +
                                                    "and se.gameid = p.gameid " +
                                                    "and se.gameid=player.gameid " +
                                                    "and p.isshare =false " +   //不是公摊玩家
                                        ") gs  " +
                                    "group by gs.gameid, gs.playerid) " +
                                ") gp " +
                            "where gp.playerid = player.id) " +
                        "end), " +
                    "ts=sysdate() " +
                    "where gameid='"+gameid+"' " +
                    "   and player.playstatus <>" + Ykj_playerstatus.quit ;  //未退出的玩家
            commonService.execute(updateSql);
            /*4、更新游戏ykj_game的ts和modifytime*/
            HashMap<String, Object> updateParams = new HashMap<>();
            updateParams.put("id",gameid);
            updateParams.put("nowtime", new Date());
            String gameSql = "update ykj_game set " +
                    "ts=:nowtime, " +
                    "modifytime=:nowtime " +
                    "where id=:id ";
            Ykj_game ykj_game = commonService.update(Ykj_game.class, gameSql, updateParams);
            /*5、返回游戏小结和对局明细*/
            HashMap<String,Object> returnMap = new HashMap<String,Object>();
            returnMap.put("setid",setid);
            returnMap.put("gameid",gameid);
            returnMap.put("ts",ykj_game.getTs());
            commonBean.setSuccess(true);
            commonBean.setMessage("游戏计分成功！");
            commonBean.setData(returnMap);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏计分失败！"+e.getMessage());
            return commonBean;
        }
    }
}
