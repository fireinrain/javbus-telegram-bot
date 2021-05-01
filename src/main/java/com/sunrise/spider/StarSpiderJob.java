package com.sunrise.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.sunrise.storege.MongodbStorege;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/26 12:48 AM
 */
public class StarSpiderJob implements Runnable{

    private List<JavbusDataItem> javbusDataItems;

    private ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque;

    public StarSpiderJob(List<JavbusDataItem> javbusDataItems,ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque,"JavbusDataItem Queue cant be null");
        this.javbusDataItems = javbusDataItems;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        if (javbusDataItems.size() <=0){
            return;
        }
        List<JavbusDataItem> javbusDataItems = this.javbusDataItems.stream()
                .filter(e -> null != e.getVisitUrl())
                .collect(Collectors.toList());
        this.concurrentLinkedDeque.addAll(javbusDataItems);

        //for (JavbusDataItem javbusDataItem : this.javbusDataItems) {
        //    if (null == javbusDataItem.getVisitUrl()){
        //        continue;
        //    }
        //    this.concurrentLinkedDeque.offer(javbusDataItem);
        //}
        List<Document> documents = this.javbusDataItems.stream()
                .map(e -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonStr = null;
                    try {
                        jsonStr = objectMapper.writeValueAsString(e);
                        Document document = Document.parse(jsonStr);
                        return document;
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                    }
                    return new Document();
                }).collect(Collectors.toList());


        MongoCollection<Document> mongoCollection = MongodbStorege.createCollectionIfNotExist("javbus", "javFilm");

        mongoCollection.insertMany(documents);

        System.out.println("插入批量数据成功：" + this.javbusDataItems.size());

    }

    public static void trigerStarJavbusTask(List<JavbusDataItem> javbusDataItems){
        StarSpiderJob starSpiderJob = new StarSpiderJob(javbusDataItems,JobExcutor.javbusDataItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(starSpiderJob);
    }
}
