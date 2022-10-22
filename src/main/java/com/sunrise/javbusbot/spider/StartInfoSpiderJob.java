package com.sunrise.javbusbot.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.javbusbot.storege.MongodbStorege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 爬取演员信息爬虫
 *
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/5/1 6:45 PM
 */
public class StartInfoSpiderJob implements Runnable {
    public static final Logger logger = LoggerFactory.getLogger(StartInfoSpiderJob.class);

    private JavbusStarInfoItem javbusStarInfoItem;

    private ConcurrentLinkedDeque<JavbusStarInfoItem> concurrentLinkedDeque;

    public StartInfoSpiderJob(JavbusStarInfoItem javbusStarInfoItem, ConcurrentLinkedDeque<JavbusStarInfoItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusStarInfoItem Queue cant be null");
        this.javbusStarInfoItem = javbusStarInfoItem;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        // if (null == javbusStarInfoItem || (null != javbusStarInfoItem && javbusStarInfoItem.getStarName().equals(""))) {
        //     return;
        // }
        this.concurrentLinkedDeque.offer(javbusStarInfoItem);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(javbusStarInfoItem);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (MongodbStorege.isMongoDatabaseAvailable.get()) {
            MongodbStorege.storeInfo(jsonStr, "javbus", "javStarInfo");
        } else {
            logger.warn("Warn! No mongoDB online, skip for local store：" + jsonStr);

        }

    }

    public static void trigerStarInfoJob(JavbusStarInfoItem javbusStarInfoItem) {
        StartInfoSpiderJob startInfoSpiderJob = new StartInfoSpiderJob(javbusStarInfoItem, JobExcutor.javbusStarInfoItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(startInfoSpiderJob);
    }
}