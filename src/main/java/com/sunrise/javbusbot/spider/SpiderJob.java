package com.sunrise.javbusbot.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.javbusbot.storege.MongodbStorege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @description: 爬取番号详情线程
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 12:40 AM
 */
public class SpiderJob implements Runnable {
    public static final Logger logging = LoggerFactory.getLogger(SpiderJob.class);


    private String filmCode;

    private ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque;

    public SpiderJob(String filmCode, ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusDataItem Queue cant be null");
        this.filmCode = filmCode;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        JavbusDataItem javbusDataItem = JavbusSpider.fetchFilmInFoByCode(filmCode);
        // 在push消息那边判断
        // if (null == javbusDataItem.getVisitUrl() || "".equals(javbusDataItem.getVisitUrl())) {
        //     return;
        // }
        this.concurrentLinkedDeque.offer(javbusDataItem);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(javbusDataItem);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (MongodbStorege.isMongoDatabaseAvailable.get()) {
            MongodbStorege.storeInfo(jsonStr, "javbus", "javFilm");
        } else {
            logging.warn("Warn! No mongoDB online, skip for local store：" + jsonStr);
        }


    }

    public static void trigerJavbusTask(String code) {
        SpiderJob spiderJob = new SpiderJob(code, JobExcutor.javbusDataItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(spiderJob);
    }
}
