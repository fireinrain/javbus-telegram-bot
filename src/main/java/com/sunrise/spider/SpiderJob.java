package com.sunrise.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrise.storege.MongodbStorege;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @description: 爬取番号详情线程
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 12:40 AM
 */
public class SpiderJob implements Runnable{

    private String filmCode;

    private ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque;

    public SpiderJob(String filmCode,ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque,"JavbusDataItem Queue cant be null");
        this.filmCode = filmCode;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        JavbusDataItem javbusDataItem = JavbusSpider.fetchFilmInFoByCode(filmCode);
        if (null == javbusDataItem.getVisitUrl()){
            return;
        }
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
            // TODO log no store db for skip
            System.out.println("Warn! No mongoDB online, skip for local store：" + jsonStr);
        }


    }

    public static void trigerJavbusTask(String code){
        SpiderJob spiderJob = new SpiderJob(code,JobExcutor.javbusDataItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(spiderJob);
    }
}
