package com.example.yikuaiju.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*统计接口
  * @author lifei
  * @Params
  * @return
  * @description: 描述
  * @date 2020/11/25 15:29
  */
public interface IStatisticsService {

     /*荣誉总值
      * @author lifei
      * @Params
      * @return 总得分
      * @description: 根据月份间隔，查询出月份间隔中用户的游戏总得分
      * @date 2020/11/26 12:06
      */
    BigDecimal totalScore(Integer userid, Integer monthInterval);

     /*最佳月度
      * @author lifei
      * @Params
      * @return 返回月份、这个月份总得分
      * @description: 最佳月份，区间内得分最高的月份
      * @date 2020/11/26 12:16
      */
    Map<String,Object> bestMonth(Integer userid, Integer monthInterval);


     /*当场巅峰
      * @author lifei
      * @Params
      * @return 返回游戏id,游戏name,游戏最近登记时间，游戏人数，游戏得分
      * @description: 时间区间内玩的分数最高的一场游戏
      * @date 2020/11/26 12:21
      */
    Map<String,Object> bestGame(Integer userid, Integer monthInterval);

     /*战绩一览
      * @author lifei
      * @Params 
      * @return setcount:3,"pie":[{"label":胜,"count":2},{"label":负,"count":2},{"label":平,"count":1}]
      * @description: 时间区间内游戏的胜，负，平的场次
      * @date 2020/11/28 19:53
      */
    Map<String,Object> pieData(Integer userid, Integer monthInterval);

     /*荣耀之旅  折线图
      * @author lifei
      * @Params 
      * @return [{userid:1, yearmonth:2020-11, gamecount:3, gamescore:50}]
      * @description: 描述
      * @date 2020/11/29 11:24
      */
    List<Map<String,Object>> lineData(Integer userid, Integer monthInterval);

     /*胜负对照表  条形图
      * @author lifei
      * @Params
      * @return 
      * @description: [{userid:1, yearmonth:2020-11, winscore:3, lostscore:-50, totalscore:-47}]
      * @date 2020/11/29 11:38
      */
    List<Map<String,Object>> barData(Integer userid, Integer monthInterval);

}
