package com.example.yikuaiju.serviceImpl;

import com.example.yikuaiju.bean.*;
import com.example.yikuaiju.bean.resultbean.Group;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IGameService;
import com.example.yikuaiju.service.IGroupService;
import com.example.yikuaiju.util.BeanMapConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class GroupServiceImpl implements IGroupService {

    @Autowired
    private ICommonService commonService;

    @Autowired
    private IGameService gameService;


    @Override
    public Ykj_group createNewGroup(Ykj_game game, String nameFromGroup) throws Exception {
        if(game != null && game.getId() != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("gameid",game.getId());
            /*1、查询这个游戏是否有存在的群组或群组关联*/
            String checkSql = "SELECT  " +
                    "    *" +
                    "FROM " +
                    "    ykj_group g " +
                    "        LEFT JOIN " +
                    "    ykj_relation r ON g.id = r.groupid " +
                    "WHERE " +
                    "    (g.gameid = :gameid OR r.gameid = :gameid) ";
            List<Map<String, Object>> groups = commonService.select(checkSql, params);
            if(groups != null && groups.size()>0)
                return (Ykj_group)BeanMapConvertUtil.mapToBean(Ykj_group.class, groups.get(0));
            /*2、生成一个群组和群组关联, 群组名称命名规则  游戏创建人等4人*/
            Ykj_user user = commonService.selectOne(Ykj_user.class, "select *from ykj_user where id=" + game.getCreator(), null);
            String groupname = user.getNickname() + "等" + game.getPersonnum().intValue() + "人";
            if(!StringUtils.isEmptyOrWhitespace(nameFromGroup)){
                groupname = "(新)"+nameFromGroup;
            }
            Ykj_group Ykj_group = new Ykj_group();
            Ykj_group.setName(groupname);
            Ykj_group.setGameid(game.getId());
            Ykj_group.setCreationtime(game.getCreationtime());
            Ykj_group.setPersonnum(game.getPersonnum());
            Ykj_group = commonService.addOneRecord(Ykj_group.class, BeanMapConvertUtil.beanToMap(Ykj_group));

            Ykj_relation relation = relateGroup(game.getId(), Ykj_group.getId(), game.getCreationtime());
            return Ykj_group;
        }
        return null;
    }

    @Override
    public Ykj_relation relateGroup(Integer gameid, Integer groupid, Date relateDate)throws Exception {
        Ykj_relation Ykj_relation = new Ykj_relation();
        Ykj_relation.setGameid(gameid);
        Ykj_relation.setGroupid(groupid);
        Ykj_relation.setCreationtime(relateDate);
        Ykj_relation = commonService.addOneRecord(Ykj_relation.class,  BeanMapConvertUtil.beanToMap(Ykj_relation));
        return Ykj_relation;
    }

    @Override
    public List<Group> getRelatedGroups(Integer userid) {
        List<Integer> fiGameIds = gameService.selectFinishGame(userid);
        if(fiGameIds != null && fiGameIds.size()>0) {
            StringBuilder pks = new StringBuilder();
            for(Integer id : fiGameIds) {
                pks.append(" '"+id+"',");
            }
            pks = pks.deleteCharAt(pks.length()-1);
            String sql = "SELECT distinct  " +
                    "    gp.id groupid, " +
                    "    gp.gameid, " +
                    "    gp.name gamename, " +
                    "    gp.creationtime, " +
                    "    gp.personnum, " +
                    "    pa.playername, " +
                    "    pa.isshare," +
                    "    u.avatarUrl, " +
                    "    u.id userid " +
                    "FROM " +
                    "    ykj_relation re " +
                    "        INNER JOIN " +
                    "    ykj_group gp ON gp.id = re.groupid " +
                    "        INNER JOIN " +
                    "    ykj_game ga ON gp.gameid = ga.id " +
                    "        INNER JOIN " +
                    "    ykj_player pa ON ga.id = pa.gameid " +
                    "                       and pa.playstatus<>"+ Ykj_playerstatus.quit +" " +
                    "        LEFT JOIN " +
                    "    ykj_user u ON pa.userid = u.id " +
                    "WHERE " +
                    "    re.gameid IN ("+pks+")";
            List<Map<String, Object>> mapList = commonService.select(sql, null);
            if(mapList != null && mapList.size()>0){
                HashMap<Integer, Group> groupMap = new HashMap<Integer,Group>();
                for(Map<String,Object> map:mapList){
                    Integer groupid = (Integer) map.get("groupid");
                    List<Map<String, Object>> userMap = new ArrayList<>();
                    userMap.add(map);
                    if(groupMap.containsKey(groupid)){
                        Group groupBean = groupMap.get(groupid);
                        userMap.addAll(groupBean.getUserInfo());
                        groupBean.setUserInfo(userMap);
                        groupMap.put(groupid,groupBean);
                    }else{
                        Group groupBean = new Group();
                        groupBean.setCreationtime((Date) map.get("creationtime"));
                        groupBean.setGameid((Integer) map.get("gameid"));
                        groupBean.setGamename((String) map.get("gamename"));
                        groupBean.setGroupid((Integer) map.get("groupid"));
                        groupBean.setPersonnum((Integer) map.get("personnum"));
                        groupBean.setUserInfo(userMap);
                        groupMap.put(groupid,groupBean);
                    }

                }
                List<Group> groupList = new ArrayList<Group>();
                groupList.addAll(groupMap.values());
                return groupList;
            }
        }
        return null;
    }

    @Override
    public Ykj_group createGroupByGroup(Ykj_game game) throws Exception {
        /*1、查询这个游戏玩家是否比群组玩家多*/
        HashMap<String,Object> params = new HashMap<>();
        params.put("gameid",game.getId());
        String groupcheckSql = "SELECT  " +
                "    distinct a.groupid " +
                "FROM " +
                "    (SELECT  " +
                "        p1.userid, g1.id gameid, r1.groupid " +
                "    FROM " +
                "        ykj_game g1, ykj_player p1, ykj_relation r1 " +
                "    WHERE " +
                "        g1.id = p1.gameid AND g1.id = r1.gameid " +
                "            AND p1.userid IS NOT NULL) a " +       //当前游戏及关联群组
                "        LEFT JOIN " +
                "    (SELECT  " +
                "        p2.userid, gp.id groupid, g2.id gameid " +
                "    FROM " +
                "        ykj_game g2, ykj_player p2, ykj_group gp " +
                "    WHERE " +
                "        g2.id = gp.gameid AND g2.id = p2.gameid) b " +  //群组游戏
                "   ON a.userid = b.userid " +
                "        AND a.groupid = b.groupid " +
                "WHERE " +
                "    b.userid IS NULL and a.gameid=:gameid ";
        List<Integer> groupids = commonService.selectSingleColumn(groupcheckSql, params, Integer.class);
        if(groupids != null && groupids.size()>0) {
            /*2、如果多，则解除原来游戏关联，新增群组，新建群组关联*/
            /*2.1、解除原来游戏关联*/
            String sql =" delete from ykj_relation where gameid="+game.getId();
            commonService.execute(sql);
            /*2.2、新增群组，新建群组关联*/
            List<Ykj_group> groups = commonService.selectById(Ykj_group.class, groupids);
            if(groups != null && groups.size()>0) {
                createNewGroup(game, groups.get(0).getName());
            }
        }
        return null;
    }
}
