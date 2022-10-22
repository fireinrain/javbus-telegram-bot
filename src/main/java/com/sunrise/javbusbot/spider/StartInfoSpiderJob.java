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

    private JavbusStarInfoItem JavbusStarInfoItem;

    private ConcurrentLinkedDeque<JavbusStarInfoItem> concurrentLinkedDeque;

    public StartInfoSpiderJob(JavbusStarInfoItem JavbusStarInfoItem, ConcurrentLinkedDeque<JavbusStarInfoItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusStarInfoItem Queue cant be null");
        this.JavbusStarInfoItem = JavbusStarInfoItem;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        if (null == JavbusStarInfoItem || (null != JavbusStarInfoItem && JavbusStarInfoItem.getStarName().equals(""))) {
            return;
        }
        this.concurrentLinkedDeque.offer(JavbusStarInfoItem);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(JavbusStarInfoItem);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (MongodbStorege.isMongoDatabaseAvailable.get()) {
            MongodbStorege.storeInfo(jsonStr, "javbus", "javStarInfo");
        } else {
            logger.warn("Warn! No mongoDB online, skip for local store：" + jsonStr);

        }

    }

    public static void trigerStarInfoJob(JavbusStarInfoItem JavbusStarInfoItem) {
        StartInfoSpiderJob startInfoSpiderJob = new StartInfoSpiderJob(JavbusStarInfoItem, JobExcutor.JavbusStarInfoItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(startInfoSpiderJob);
    }
}