package com.example.yikuaiju.controller;

import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.bean.common.CommonBean;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IStatisticsService;
import com.example.yikuaiju.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("statistics")
@Transactional
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/overview",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean overview(String unionkey, Integer monthInterval){
        CommonBean commonBean = new CommonBean();
        if(unionkey == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("用户unionkey不能为空");
            return commonBean;
        }
        if(monthInterval == null){
            commonBean.setSuccess(false);
            commonBean.setMessage("时间区间monthInterval不能为空");
            return commonBean;
        }
        try {
            /*1、校验用户是否存在*/
            Ykj_user user = userService.getUserByUnionkey(unionkey);
            if (user == null) {
                commonBean.setSuccess(false);
                commonBean.setMessage("用户未授权");
                return commonBean;
            }
            /*2、获取统计数据*/
            HashMap<String, Object> statisticsMap = new HashMap<String, Object>();
            BigDecimal totalScore = statisticsService.totalScore(user.getId(), monthInterval);
            Map<String, Object> bestGame = statisticsService.bestGame(user.getId(), monthInterval);
            Map<String, Object> bestMonth = statisticsService.bestMonth(user.getId(), monthInterval);
            Map<String, Object> pieData = statisticsService.pieData(user.getId(), monthInterval);
            List<Map<String, Object>> lineData = statisticsService.lineData(user.getId(), monthInterval);
            List<Map<String, Object>> barData = statisticsService.barData(user.getId(), monthInterval);
            statisticsMap.put("totalScore",totalScore);     //荣誉总值
            statisticsMap.put("bestGame",bestGame);     //单场巅峰
            statisticsMap.put("bestMonth",bestMonth);    //月度最佳
            statisticsMap.put("pie",pieData);    //饼图（战绩一览）
            statisticsMap.put("line",lineData);    //折线图（荣耀之旅）
            statisticsMap.put("bar",barData);    //正负条形图（胜负对照表）
            commonBean.setSuccess(true);
            commonBean.setMessage("成功");
            commonBean.setData(statisticsMap);
            return commonBean;
        }catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("获取统计数据失败！"+e.getMessage());
            return commonBean;
        }
    }

}
