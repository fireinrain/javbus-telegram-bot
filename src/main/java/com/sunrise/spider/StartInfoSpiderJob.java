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
 * @date: 2021/5/1 6:45 PM
 */
public class StartInfoSpiderJob implements Runnable {
    private JavbusStarInfo javbusStarInfo;

    private ConcurrentLinkedDeque<JavbusStarInfo> concurrentLinkedDeque;

    public StartInfoSpiderJob(JavbusStarInfo javbusStarInfo, ConcurrentLinkedDeque<JavbusStarInfo> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusStarInfo Queue cant be null");
        this.javbusStarInfo = javbusStarInfo;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        if (null == javbusStarInfo) {
            return;
        }
        this.concurrentLinkedDeque.offer(javbusStarInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(javbusStarInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        MongoCollection<Document> mongoCollection = MongodbStorege.createCollectionIfNotExist("javbus", "javStarInfo");

        org.bson.Document document = org.bson.Document.parse(jsonStr);

        mongoCollection.insertOne(document);

        System.out.println("插入成功：" + jsonStr);

    }

    public static void trigerStarInfoJob(JavbusStarInfo javbusStarInfo) {
        StartInfoSpiderJob startInfoSpiderJob = new StartInfoSpiderJob(javbusStarInfo, JobExcutor.javbusStarInfoConcurrentLinkedDeque);
        JobExcutor.doTgJob(startInfoSpiderJob);
    }
}