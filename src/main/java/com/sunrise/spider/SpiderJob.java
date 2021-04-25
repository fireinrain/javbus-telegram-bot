package com.sunrise.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.sunrise.storege.MongodbStorege;
import org.bson.Document;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @description:
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
        MongoCollection<Document> mongoCollection = MongodbStorege.createCollectionIfNotExist("javbus", "javFilm");

        org.bson.Document document = org.bson.Document.parse(jsonStr);

        mongoCollection.insertOne(document);

        System.out.println("插入成功：" + jsonStr);

    }

    public static void trigerJavbusTask(String code){
        SpiderJob spiderJob = new SpiderJob(code,JobExcutor.concurrentLinkedDeque);
        JobExcutor.doTgJob(spiderJob);
    }
}
