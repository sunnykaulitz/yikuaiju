package com.example.yikuaiju.task;

import com.example.yikuaiju.controller.GameController;
import com.example.yikuaiju.service.IGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class EndGameTask {

    private static final Logger logger = LoggerFactory.getLogger(EndGameTask.class);
    @Autowired
    private IGameService gameService;

    /**
     * @Scheduled :注解表示该方法成为一个任务调度方法
     * fixedDelay：属性表示任务执行频率：此处设置10分钟（10*60*1000）执行一次
     */
    @Scheduled(fixedDelay = 10*60*1000)
    public void execute() {
        //1、查询是否有24小时未操作的活动，自动结束
        try {
            logger.info(new Date()+"------定时任务开始-------结束不活跃游戏");
            gameService.autoEndGameTask();
            logger.info(new Date()+"------定时任务结束-------结束不活跃游戏");
        } catch (Exception e) {
            logger.error("定时任务[结束不活跃游戏]失败！"+e.getMessage());
            e.printStackTrace();
        }
    }
}
