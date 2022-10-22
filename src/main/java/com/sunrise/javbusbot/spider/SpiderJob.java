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
    public static final Logger logger = LoggerFactory.getLogger(SpiderJob.class);


    private String filmCode;

    private String messageChatId;

    private ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque;

    public SpiderJob(String filmCode, String messageChatId, ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusDataItem Queue cant be null");
        this.filmCode = filmCode;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
        this.messageChatId = messageChatId;
    }

    @Override
    public void run() {
        JavbusDataItem javbusDataItem = JavbusSpider.fetchFilmInFoByCode(filmCode);
        javbusDataItem.setMessageChatId(messageChatId);
        // 如果有访问链接
        if (null != javbusDataItem.getVisitUrl() && !"".equals(javbusDataItem.getVisitUrl())) {
            // 获取视频预览地址
            String filmPreviewUrl = VideoPreviewUtils.getFilmPreviewUrl(javbusDataItem);
            javbusDataItem.setVideoPreviewUrl(filmPreviewUrl);
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
            logger.warn("Warn! No mongoDB online, skip for local store：" + jsonStr);
        }


    }

    public static void trigerJavbusCodeTask(String code, String messageChatId) {
        SpiderJob spiderJob = new SpiderJob(code, messageChatId, JobExcutor.javbusDataItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(spiderJob);
    }
}
