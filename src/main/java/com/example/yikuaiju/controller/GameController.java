package com.example.yikuaiju.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.yikuaiju.bean.*;
import com.example.yikuaiju.bean.common.CommonBean;
import com.example.yikuaiju.bean.resultbean.Game;
import com.example.yikuaiju.bean.resultbean.Group;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IGameService;
import com.example.yikuaiju.service.IGroupService;
import com.example.yikuaiju.service.IUserService;
import com.example.yikuaiju.util.BeanMapConvertUtil;
import com.example.yikuaiju.util.WeChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("game")
@Transactional
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private ICommonService commonService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IGroupService groupService;


    //创建游戏
    @RequestMapping(value = "/createGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean saveGame(String unionkey, String address) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        if(address == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("地址address参数不能为空");
            return commonBean;
        }
        try {
            /*1、根据unionkey查询用户表，如果不存在，则抛出异常让用户授权*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、校验用户是否在进行的游戏中，并且是游戏的创建者，若在游戏中则提示*/
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("userid",user.getId());
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct b.name, b.gstatus, b.creator from ykj_player a, ykj_game b " +
                    "where a.gameid=b.id " +
                    "and b.gstatus in("+Ykj_gstatus.gameing+","+Ykj_gstatus.joining+") " +   //组队中、进行中
                    "and a.playstatus <>"+ Ykj_playerstatus.quit+" " +   //不是退出游戏的玩家
                    "and b.invalid=false " +
                    "and a.userid=:userid ", selParam);
            if(inGame != null && inGame.size()>0 && inGame.get(0)!= null ){
                Integer creator = (Integer) inGame.get(0).get("creator");
                if(creator.equals(user.getId())) {  //游戏创建者并且游戏正在进行中或者组队中
                    commonBean.setSuccess(false);
                    commonBean.setMessage("玩家已经创建游戏[" + inGame.get(0).get("name") + "]，不能重复创建");
                    return commonBean;
                }
            }
            /*3、默认活动标题“年/月/日 第n场”，默认计公摊，游戏成员默认为公摊和操作者,创建游戏*/
            /*3.1、获取活动标题*/
            long gamecount = commonService.count("select count( a.gameid) gamecount  from ykj_player a " +
                    "where a.userid=:userid " +
                    "       and to_days(jointime) = to_days(now()) " +
                    "       and a.playstatus <> "+Ykj_playerstatus.quit,    //不计算退出的游戏
                    selParam);
            gamecount = gamecount+1;
            Ykj_game gameBean = new Ykj_game();
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH)+1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String gamename = year + "年/" + month + "月/" + day + "/日 第" + gamecount + "场";
            /*3.2、创建游戏*/
            gameBean.setName(gamename);
            gameBean.setCreationtime(new Date());
            gameBean.setModifytime(new Date());
            gameBean.setEndtime(null);
            gameBean.setCreator(user.getId());
            gameBean.setPersonnum(1);
            gameBean.setAddress(address);
            gameBean.setGcount(0);
            gameBean.setGstatus(Ykj_gstatus.joining);   //组队中
            gameBean.setInvalid((byte) 0);
            gameBean.setIsshare((byte) 1);
            gameBean.setTs(new Date());
            Ykj_game game = commonService.addOneRecord(Ykj_game.class, BeanMapConvertUtil.beanToMap(gameBean));
            /*3.3、创建游戏玩家*/
            List<Map<String, Object>> playerList = new ArrayList<Map<String, Object>>();
            Ykj_player player1 = new Ykj_player();
            player1.setGameid(game.getId());
            player1.setUserid(user.getId());
            player1.setPlayername(user.getNickname());
            player1.setJointime(new Date());
            player1.setPlaystatus(Ykj_playerstatus.playing);   //"游戏中"
            player1.setIsshare((byte) 0);
            player1.setTs(new Date());
            commonService.addOneRecord(Ykj_player.class, BeanMapConvertUtil.beanToMap(player1));
            Ykj_player sharePlayer = getSharePlayer(game.getId());
            commonService.addOneRecord(Ykj_player.class, BeanMapConvertUtil.beanToMap(sharePlayer));
            /*4、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
//            ArrayList<Integer> idList = new ArrayList<Integer>();
//            idList.add(game.getId());
//            List<Game> gameSummary = gameService.selectGamesummary(idList);
            commonBean.setSuccess(true);
            commonBean.setMessage("游戏创建成功！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏创建失败！"+e.getMessage());
            return commonBean;
        }
    }


    //修改游戏
    @RequestMapping(value = "/updateGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean updateGame(String unionkey, Integer gameid, String address, Date ts, String name, boolean isshare) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        if(ts == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏ts参数不能为空");
            return commonBean;
        }
        try {
            /*1、根据unionkey查询用户表，如果不存在，则抛出异常让用户授权*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、校验游戏的ts是否与数据库中一致， 校验用户是否在此游戏中并游戏正在组队中，否则提示*/
            boolean db_isshare;
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("userid",user.getId());
            selParam.put("gameid",gameid);
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct b.name, b.gstatus, b.ts, b.isshare, b.address " +
                    "from ykj_player a, ykj_game b " +
                    "where a.gameid=b.id " +
                    "and b.gstatus in("+Ykj_gstatus.joining+") " +  //组队中
                    "and a.playstatus <> "+Ykj_playerstatus.quit+" " +  //玩家没有退出
                    "and b.invalid=false " +  //非作废游戏
                    "and a.userid=:userid and b.id=:gameid ", selParam);
            if(inGame != null && inGame.size()>0 && inGame.get(0)!= null){
                Date db_ts = (Date) inGame.get(0).get("ts");
                db_isshare = (Integer)inGame.get(0).get("isshare") == 1 ? true: false;
                if(db_ts.getTime() != (ts.getTime())){
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏已变更，请刷新");
                    return commonBean;
                }
            }else{
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏不在组队中或者用户已退出游戏");
                return commonBean;
            }
            /*3、若修改是否公摊，修改游戏的isshare状态，true时校验玩家ykj_player中是否有公摊玩家，若没有则增加一个，false时将公摊玩家的玩家状态设置成退出*/
            if(db_isshare != isshare){
                if(isshare){/*3.1、增加公摊玩家*/
                    Ykj_player sharePlayer = getSharePlayer(gameid);
                    commonService.addOneRecord(Ykj_player.class,BeanMapConvertUtil.beanToMap(sharePlayer));
                }else{/*3.2、清空公摊玩家*/
                    commonService.execute("delete from ykj_player where isshare=true and gameid='"+gameid+"' ");
                }
            }
            /*4、更新游戏的是否公摊isshare、时间戳ts、最新登记时间modifytime、游戏人数personnum*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id",gameid);
            updateParam.put("isshare",isshare);
            updateParam.put("ts",new Date());
            updateParam.put("modifytime",new Date());

            String updateSql = "update ykj_game set ";
            if(!StringUtils.isEmptyOrWhitespace(name)) {
                updateParam.put("name", name);
                updateSql = updateSql + "name=:name, ";
            }
            if(!StringUtils.isEmptyOrWhitespace(address)) {
                updateParam.put("address", address);
                updateSql = updateSql + "address=:address, ";
            }
            updateSql = updateSql +
                    "isshare=:isshare, " +
                    "ts=:ts, " +
                    "modifytime=:modifytime, " +
                    "personnum=(" +
                    "       select count(player.id) " +
                    "   from ykj_player player " +
                    "       where player.gameid=:id " +
                    "           and player.isshare=false " +
                    "           and player.playstatus<> "+Ykj_playerstatus.quit+" ), " +
                    "gcount=(select count(se.id) from ykj_set se where se.gameid=:id) " +
                "where id=:id ";
            Ykj_game game = commonService.update(Ykj_game.class, updateSql, updateParam);
            /*5、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
//            ArrayList<Integer> idList = new ArrayList<Integer>();
//            idList.add(gameid);
//            List<Game> gameSummary = gameService.selectGamesummary(idList);
            commonBean.setSuccess(true);
            commonBean.setMessage("游戏修改成功！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏修改失败！"+e.getMessage());
            return commonBean;
        }
    }


     /*
      * @author lifei
      * @Params
      * @return
      * @description: 授权并加入游戏（扫码或链接）
      * @date 2020/11/16 22:50
      */
    @RequestMapping(value = "/wechatJoinGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean wechatJoinGame(String unionkey, Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }

            /*2、校验游戏是否已经结束，若结束则提示*/
            HashMap<String, Object> gameParam = new HashMap<String, Object>();
            gameParam.put("id",gameid);
            Ykj_game game = commonService.selectOne(Ykj_game.class, "select *from ykj_game where id=:id ", gameParam);
            if(game == null || (game != null && (game.getGstatus().equals(Ykj_gstatus.gameend)|| game.getInvalid() == 1)) ){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏["+game.getName()+"]已结束，不能加入");
                commonBean.setData(game);
                return commonBean;
            }
            /*3、校验用户是否在当前游戏中，若在游戏中则直接返回信息*/
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("userid",user.getId());
            selParam.put("gameid",gameid);
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct a.id playerid, b.name, b.gstatus from ykj_player a, ykj_game b " +
                    "where a.gameid=b.id " +
                    "and b.gstatus in("+Ykj_gstatus.gameing+","+Ykj_gstatus.joining+") " +
                    "and b.invalid=false " +
                    "and a.userid=:userid and b.id=:gameid ", selParam);
            /*4、玩家不在当前游戏中，则创建玩家，若玩家已经在游戏中，则修改玩家状态为游戏中*/
            if(inGame == null || inGame.size()==0 || inGame.get(0)== null){
                /*4.1、玩家不在当前游戏中，则创建玩家*/
                Ykj_player player1 = new Ykj_player();
                player1.setGameid(game.getId());
                player1.setUserid(user.getId());
                player1.setPlayername(user.getNickname());
                player1.setJointime(new Date());
                player1.setPlaystatus(Ykj_playerstatus.playing);   //"游戏中"
                player1.setIsshare((byte) 0);
                player1.setTs(new Date());
                commonService.addOneRecord(Ykj_player.class, BeanMapConvertUtil.beanToMap(player1));

            }else{
                /*4.2在当前游戏中，更新玩家状态为游戏中*/
                Integer playerid = (Integer) inGame.get(0).get("playerid");
                commonService.execute("update ykj_player " +
                        "set playstatus = "+ Ykj_playerstatus.playing +"where id='"+playerid+"'");
            }
            /*5、更新游戏的时间戳ts、最新登记时间modifytime、游戏人数personnum、局次gcount*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id", gameid);
            updateParam.put("ts", new Date());
            updateParam.put("modifytime", new Date());
            String updateSql = "update ykj_game set " +
                    "ts=:ts, " +
                    "modifytime=:modifytime, " +
                    "personnum=(" +
                    "       select count(player.id) " +
                    "   from ykj_player player " +
                    "       where player.gameid=:id " +
                    "           and player.isshare=false " +
                    "           and player.playstatus<> " + Ykj_playerstatus.quit + " ), " +
                    "gcount=(select count(se.id) from ykj_set se where se.gameid=:id) " +
                    "where id=:id ";
            game = commonService.update(Ykj_game.class, updateSql, updateParam);
            /*6、返回游戏*/
//            ArrayList<Integer> idList = new ArrayList<Integer>();
//            idList.add(gameid);
//            List<Game> gameSummary = gameService.selectGamesummary(idList);
            commonBean.setSuccess(true);
            commonBean.setMessage("成功加入游戏！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("加入游戏失败！"+e.getMessage());
            return commonBean;
        }
    }


    /*
     * @author lifei
     * @Params
     * @return
     * @description: 自定义用户加入游戏
     * @date 2020/11/16 22:50
     */
    @RequestMapping(value = "/customJoinGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean customJoinGame(String playername, Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(playername == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("玩家playername参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验游戏是否已经结束，若结束则提示*/
            HashMap<String, Object> gameParam = new HashMap<String, Object>();
            gameParam.put("id",gameid);
            Ykj_game game = commonService.selectOne(Ykj_game.class, "select *from ykj_game where id=:id ", gameParam);
            if(game == null || (game != null && (game.getGstatus().equals(Ykj_gstatus.gameend) || game.getInvalid() == 1)) ){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏["+game.getName()+"]已结束，不能加入");
                return commonBean;
            }
            /*2、创建用户、创建玩家*/
            /*2.1、创建用户*/
            String userid = WeChatUtil.getUUID();
            Ykj_user user = new Ykj_user();
            user.setId(Integer.parseInt(userid));
            user.setOpenid(userid);
            user.setNickname(playername);
            user.setAvatarurl(userService.getImgUrl());
            user.setSessionkey(userid);
            user.setCreationtime(new Date());
            user.setModifytime(new Date());
            user = commonService.addOneRecord(Ykj_user.class, BeanMapConvertUtil.beanToMap(user));
            /*2.2、创建玩家*/
            Ykj_player player1 = new Ykj_player();
            player1.setGameid(game.getId());
            player1.setUserid(user.getId());
            player1.setPlayername(user.getNickname());
            player1.setJointime(new Date());
            player1.setPlaystatus(Ykj_playerstatus.playing);   //"游戏中"
            player1.setIsshare((byte) 0);
            player1.setTs(new Date());
            commonService.addOneRecord(Ykj_player.class, BeanMapConvertUtil.beanToMap(player1));
            /*5、更新游戏的时间戳ts、最新登记时间modifytime、游戏人数personnum、局次gcount*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id",gameid);
            updateParam.put("ts",new Date());
            updateParam.put("modifytime",new Date());
            String updateSql = "update ykj_game set " +
                    "ts=:ts, " +
                    "modifytime=:modifytime, " +
                    "personnum=(" +
                    "       select count(player.id) " +
                    "   from ykj_player player " +
                    "       where player.gameid=:id " +
                    "           and player.isshare=false " +
                    "           and player.playstatus<> "+Ykj_playerstatus.quit+"), " +
                    "gcount=(select count(se.id) from ykj_set se where se.gameid=:id) " +
                    "where id=:id ";
            game = commonService.update(Ykj_game.class, updateSql, updateParam);
            /*6、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
//            ArrayList<Integer> idList = new ArrayList<Integer>();
//            idList.add(gameid);
//            List<Game> gameSummary = gameService.selectGamesummary(idList);
            commonBean.setSuccess(true);
            commonBean.setMessage("成功加入游戏！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("加入游戏失败！"+e.getMessage());
            return commonBean;
        }
    }


     /*
      * @author lifei
      * @Params
      * @return
      * @description: 获取二维码
      * @date 2020/11/17 14:33
      */
    @RequestMapping(value = "/getQrTwo",method = RequestMethod.POST)
    public void getminiqrQrTwo(String gameid, String page, HttpServletResponse response) {
        try {
            JSONObject jsonObject = WeChatUtil.getAccessToken();
            String access_token = (String) jsonObject.get("access_token");
            WeChatUtil.getminiqrQrTwo(gameid, page, access_token, response);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/getGamesByUser",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getRecords(String unionkey) {
        CommonBean commonBean = new CommonBean();
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、查询ykj_player表中本用户的所有游戏id*/
            List<Integer> unfiGameIds = gameService.selectUnfinishGame(user.getId());
            List<Integer> fiGameIds = gameService.selectFinishGame(user.getId());
            /*6、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
            List<Game> unFiGameSummary = null;
            List<Game> fiGameSummary = null;
            if(unfiGameIds != null && unfiGameIds.size()>0) {
                unFiGameSummary = gameService.selectGamesummary(unfiGameIds);
            }
            if(fiGameIds != null && fiGameIds.size()>0) {
                fiGameSummary = gameService.selectGamesummary(fiGameIds);
            }
            Map<String, Object> records = new HashMap<>();
            records.put("finished",fiGameSummary);
            records.put("unfinished",unFiGameSummary);
            commonBean.setSuccess(true);
            commonBean.setMessage("成功获取数据！");
            commonBean.setData(records);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取数据失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/getUnfiGamesByUser",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getUnfiGames(String unionkey) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、查询ykj_player表中本用户的所有未结束游戏id*/
            List<Integer> unFiGameIds = gameService.selectUnfinishGame(user.getId());
            /*6、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
            List<Game> unFiGameSummary = null;
            if(unFiGameIds != null && unFiGameIds.size()>0) {
                unFiGameSummary = gameService.selectGamesummary(unFiGameIds);
            }
            commonBean.setSuccess(true);
            commonBean.setMessage("成功获取数据！");
            commonBean.setData(unFiGameSummary);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取数据失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/getfiGamesByUser",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getFiGames(String unionkey) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、查询ykj_player表中本用户的所有结束的游戏id*/
            List<Integer> fiGameIds = gameService.selectFinishGame(user.getId());
            /*6、返回游戏ykj_game、玩家ykj_player、用户ykj_user、游戏计分ykj_score的组合数据*/
            List<Game> fiGameSummary = null;
            if(fiGameIds != null && fiGameIds.size()>0) {
                fiGameSummary = gameService.selectGamesummary(fiGameIds);
            }
            commonBean.setSuccess(true);
            commonBean.setMessage("成功获取数据！");
            commonBean.setData(fiGameSummary);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取数据失败！"+e.getMessage());
            return commonBean;
        }
    }

    //退出游戏
    @RequestMapping(value = "/quitGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean quitGame(String unionkey, Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、校验游戏是否在组队中，若不在组队中则return*/
            Ykj_game game = gameService.selectByUserid(gameid, user.getId());
            if(game != null && !game.getGstatus().equals(Ykj_gstatus.joining)){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏["+game.getName()+"]正在进行中或已结束，不能退出");
                return commonBean;
            }
            /*3、修改玩家状态为退出*/
            commonService.execute("update ykj_player set playstatus="+Ykj_playerstatus.quit+" where userid='"+user.getId()+"' and gameid='"+gameid+"' ");
//            commonService.execute("delete from ykj_player where userid='"+user.getId()+"' and gameid='"+gameid+"' ");
            /*4、查询userid是否是游戏创建者creator，如果是创建者，则作废游戏invalid=true、endtime、finisher、taketime、gstatus、
            否则只更新ts、modifytime、游戏人数*/
            Integer creator = game.getCreator();
            if(user.getId().equals(creator)){
                HashMap<String, Object> updateParam = new HashMap<String, Object>();
                updateParam.put("userid",user.getId());
                updateParam.put("id",gameid);
                updateParam.put("gstatus",Ykj_gstatus.gameend);  //已结束
                updateParam.put("nowtime",new Date());
                String updateSql = "update ykj_game set " +
                        "invalid=true, " +
                        "endtime=:nowtime, " +
                        "ts=:nowtime, " +
                        "modifytime=:nowtime, " +
                        "finisher=:userid, " +
                        "gstatus=:gstatus where id=:id";
                game = commonService.update(Ykj_game.class, updateSql, updateParam);
            }else{
                HashMap<String, Object> updateParam = new HashMap<String, Object>();
                updateParam.put("id",gameid);
                updateParam.put("nowtime",new Date());
                String updateSql = "update ykj_game set " +
                        "personnum=(" +
                        "       select count(player.id) " +
                        "   from ykj_player player " +
                        "       where player.gameid=:id " +
                        "           and player.isshare=false " +
                        "           and player.playstatus<> "+Ykj_playerstatus.quit+"), " +
                        "ts=:nowtime, " +
                        "modifytime=:nowtime " +
                        "where id=:id";
                game = commonService.update(Ykj_game.class, updateSql, updateParam);
            }
            commonBean.setSuccess(true);
            commonBean.setMessage("退出游戏！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("退出游戏失败！"+e.getMessage());
            return commonBean;
        }
    }


    //退出游戏
    @RequestMapping(value = "/removePlayer",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean removePlayer(Integer playerid, Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(playerid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("玩家playerid参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验游戏是否在组队中，若不在组队中则return*/
            Ykj_game game = gameService.selectById(gameid);
            if(game != null && !game.getGstatus().equals(Ykj_gstatus.joining)){
                commonBean.setSuccess(false);
                commonBean.setMessage("游戏["+game.getName()+"]正在进行中或已结束，不能退出");
                return commonBean;
            }
            /*2、判断玩家是否虚拟玩家*/
            List<Map<String, Object>> customPlayer = commonService.select("SELECT  " +
                    "    player.id, us.id, us.unionkey, player.playername " +
                    "FROM " +
                    "    ykj_player player " +
                    "        LEFT JOIN " +
                    "    ykj_user us ON player.userid = us.id " +
                    "WHERE " +
                    "    unionkey IS NULL AND player.id = '" + playerid + "'", null);
            if(customPlayer == null || customPlayer.size() ==0 || customPlayer.get(0) == null){
                commonBean.setSuccess(false);
                commonBean.setMessage("玩家["+customPlayer.get(0).get("playername")+"]不是虚拟玩家，不能移除");
                return commonBean;
            }
            /*3、修改玩家状态为退出*/
            commonService.execute("update ykj_player set playstatus="+Ykj_playerstatus.quit+" where id='"+playerid+"' ");
//            commonService.execute("delete from ykj_player where id='"+playerid+"' ");
            /*4、查询userid是否是游戏创建者creator，如果是创建者，则作废游戏invalid=true、endtime、finisher、taketime、gstatus、
            否则只更新ts、modifytime、游戏人数*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id",gameid);
            updateParam.put("nowtime",new Date());
            String updateSql = "update ykj_game set " +
                    "personnum=(" +
                    "       select count(player.id) " +
                    "   from ykj_player player " +
                    "       where player.gameid=:id " +
                    "           and player.isshare=false " +
                    "           and player.playstatus<> "+Ykj_playerstatus.quit+"), " +
                    "ts=:nowtime, " +
                    "modifytime=:nowtime " +
                    "where id=:id";
            game = commonService.update(Ykj_game.class, updateSql, updateParam);
            commonBean.setSuccess(true);
            commonBean.setMessage("退出游戏！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("退出游戏失败！"+e.getMessage());
            return commonBean;
        }
    }


    @RequestMapping(value = "/startGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean startGame(Integer gameid, Date ts, Integer groupid) {
        CommonBean commonBean = new CommonBean();
        if(ts == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏ts参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验ts、游戏是否在组队中，若不在组队中或ts失效则return*/
            Ykj_game game = gameService.selectById(gameid);
            if(game != null ){
                if(!game.getGstatus().equals(Ykj_gstatus.joining)) {//不在组队中
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏[" + game.getName() + "]正在进行中或已结束，不能开始");
                    return commonBean;
                }
                Date db_ts = game.getTs();
                if(db_ts.getTime() != (ts.getTime())){
                    commonBean.setSuccess(false);
                    commonBean.setMessage("游戏已变更，请刷新");
                    return commonBean;
                }
            }
            /*2、创建群组并关联， 如果是群组id空，则创建一个群组并关联，否则，新建玩家，关联群组*/
            if(groupid == null){
                /*创建群组并关联, 群组命名规则 游戏创建者姓名+等几人*/
                groupService.createNewGroup(game,null);
            }else{
                /*新建玩家，关联群组*/
                gameService.createPlayerByGroup(groupid, game.getId());
                groupService.relateGroup(game.getId(), groupid, new Date());
            }
            /*3、更新游戏的gstatus、ts、modifytime、组队方式formtype、游戏人数personnum*/
            HashMap<String, Object> updateParam = new HashMap<String, Object>();
            updateParam.put("id",gameid);
            updateParam.put("nowtime",new Date());
            updateParam.put("gstatus",Ykj_gstatus.gameing); //进行中
            if(groupid != null) {
                updateParam.put("formtype", Ykj_formtype.quickjoin);       //"快捷邀请"
            }else{
                updateParam.put("formtype", Ykj_formtype.spotjoin);    //"现场邀请"
            }
            String updateSql = "update ykj_game set " +
                    "personnum=(" +
                    "       select count(player.id) " +
                    "   from ykj_player player " +
                    "       where player.gameid=:id " +
                    "           and player.isshare=false " +
                    "           and player.playstatus<> "+Ykj_playerstatus.quit+" ), " +
                    "formtype=:formtype, " +
                    "gstatus=:gstatus, " +
                    "ts=:nowtime, " +
                    "modifytime=:nowtime " +
                    "where id=:id";
            game = commonService.update(Ykj_game.class, updateSql, updateParam);
//            ArrayList<Integer> pks = new ArrayList<Integer>();
//            pks.add(gameid);
//            List<Game> gamesummarys = gameService.selectGamesummary(pks);
            commonBean.setSuccess(true);
            commonBean.setMessage("开始游戏！");
            commonBean.setData(game);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("开始游戏失败！"+e.getMessage());
            return commonBean;
        }
    }

    //退出游戏
    @RequestMapping(value = "/endGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean endGame(String unionkey, Integer gameid, Date ts) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        if(ts == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏ts参数不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if(user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、校验游戏是否已结束，若结束则return*/
            HashMap<String, Object> selParam = new HashMap<String, Object>();
            selParam.put("userid",user.getId());
            selParam.put("gameid",gameid);
            List<Map<String, Object>> inGame = commonService.select("select " +
                    "distinct b.name, b.gstatus, b.ts from ykj_player a, ykj_game b " +
                    "where a.gameid=b.id " +
                    "and a.userid=:userid " +
                    "and b.invalid=false " +
                    "and b.id=:gameid ", selParam);
            if(inGame != null && inGame.size()>0 && inGame.get(0)!= null){
                Integer gstatus = (Integer) inGame.get(0).get("gstatus");
                if(gstatus.equals(Ykj_gstatus.gameend)){  //已结束
                    commonBean.setSuccess(true);
                    commonBean.setMessage("游戏["+inGame.get(0).get("name")+"]已经结束");
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
                commonBean.setMessage("用户不在游戏"+gameid+"中");
                return commonBean;
            }
            /*3、更新游戏ykj_game的ts和modifytime、endtime、finisher、taketime，游戏状态改为已结束*/
            Ykj_game game = gameService.endGame(gameid, user.getId());
            /*4、更新群组，如果本游戏玩家与关联的群组玩家有出入，则新建群组及关联*/
            groupService.createGroupByGroup(game);
            /*5、返回游戏和是否需要命名群组*/
            /*5.1、如果群组由该游戏生成，则去需要确认是否重新命名*/
            boolean rename = false;
            List<Map<String, Object>> group = commonService.select("select * from ykj_group where gameid = " + gameid, null);
            if(group != null && group.size()>0 && group.get(0) != null)
                rename = true;
            HashMap<String,Object> resultMap = new HashMap<>();
            resultMap.put("game",game);
            resultMap.put("rename", rename);
            resultMap.put("groupid", group.get(0).get("id"));
            commonBean.setSuccess(true);
            commonBean.setMessage("结束游戏!");
            commonBean.setData(resultMap);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("结束游戏失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/getGameSummary",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getGameSummary(Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try{
            /*返回游戏小结*/
            ArrayList<Integer> pks = new ArrayList<Integer>();
            pks.add(gameid);
            List<Game> gamesummarys = gameService.selectGamesummary(pks);
            commonBean.setSuccess(true);
            commonBean.setMessage("获取游戏小结!");
            commonBean.setData(gamesummarys);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取游戏小结失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/getGameDetail",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getGameDetail(Integer gameid) {
        CommonBean commonBean = new CommonBean();
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try{
            /*返回游戏对局明细*/
            List<Map<String, Object>> gamedetail = gameService.selectGameDetail(gameid);
            commonBean.setSuccess(true);
            commonBean.setMessage("获取游戏对局明细!");
            commonBean.setData(gamedetail);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取游戏对局明细失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/getGameGroup",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean getGameGroup(String unionkey) {
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey参数不能为空");
            return commonBean;
        }
        try{
            /*1、查询用户*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            /*2、返回游戏群组*/
            List<Group> groupList = groupService.getRelatedGroups(user.getId());
            commonBean.setSuccess(true);
            commonBean.setMessage("成功获取游戏群组!");
            commonBean.setData(groupList);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取游戏群组失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/shareGame",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean shareGame(String gameid) {
        CommonBean commonBean = new CommonBean();
        if(gameid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("游戏gameid参数不能为空");
            return commonBean;
        }
        try{
            /*1、修改游戏分享次数*/
            String sql = "update ykj_game set sharetimes = sharetimes+1 where id='"+gameid+"' ";
            commonService.execute(sql);
            commonBean.setSuccess(true);
            commonBean.setMessage("分享成功!");
            commonBean.setData(null);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("分享失败！"+e.getMessage());
            return commonBean;
        }
    }

    @RequestMapping(value = "/updateGroupname",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean updateGroupname(String groupid, String groupname) {
        CommonBean commonBean = new CommonBean();
        if(groupid == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("群组groupid参数不能为空");
            return commonBean;
        }
        if(groupname == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("群组groupname参数不能为空");
            return commonBean;
        }
        try{
            /*1、更新游戏群组名称*/
            String sql = "update ykj_group set name = '"+groupname+"' where id='"+groupid+"' ";
            commonService.execute(sql);
            commonBean.setSuccess(true);
            commonBean.setMessage("群组命名成功!");
            commonBean.setData(null);
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("群组命名失败！"+e.getMessage());
            return commonBean;
        }
    }


    private Ykj_player getSharePlayer(Integer gameid){
        Ykj_player sharePlayer = new Ykj_player();
        sharePlayer.setGameid(gameid);
        sharePlayer.setUserid(null);
        sharePlayer.setPlayername("公摊");
        sharePlayer.setJointime(new Date());
        sharePlayer.setPlaystatus(Ykj_playerstatus.playing);    //"游戏中"
        sharePlayer.setIsshare((byte) 1);
        sharePlayer.setTs(new Date());
        return sharePlayer;
    }


}
