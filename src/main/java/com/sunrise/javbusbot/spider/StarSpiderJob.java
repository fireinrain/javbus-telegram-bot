package com.sunrise.javbusbot.spider;

import com.sunrise.javbusbot.storege.MongodbStorege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * @description: 爬取演员主页信息爬虫
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/26 12:48 AM
 */
public class StarSpiderJob implements Runnable {
    public static final Logger logging = LoggerFactory.getLogger(StarSpiderJob.class);


    private List<JavbusDataItem> javbusDataItems;

    private ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque;

    public StarSpiderJob(List<JavbusDataItem> javbusDataItems, ConcurrentLinkedDeque<JavbusDataItem> concurrentLinkedDeque) {
        Objects.requireNonNull(concurrentLinkedDeque, "JavbusDataItem Queue cant be null");
        this.javbusDataItems = javbusDataItems;
        this.concurrentLinkedDeque = concurrentLinkedDeque;
    }

    @Override
    public void run() {
        if (javbusDataItems.size() <= 0) {
            return;
        }
        // 如果是列表只有一个 就进行视频预览地址查询
        if (javbusDataItems.size() == 1) {
            // 获取视频预览地址
            JavbusDataItem javbusDataItem = javbusDataItems.get(0);
            String filmPreviewUrl = VideoPreviewUtils.getFilmPreviewUrl(javbusDataItem);
            javbusDataItem.setVideoPreviewUrl(filmPreviewUrl);
        }
        List<JavbusDataItem> javbusDataItems = this.javbusDataItems.stream()
                .filter(e -> null != e.getVisitUrl())
                .collect(Collectors.toList());
        this.concurrentLinkedDeque.addAll(javbusDataItems);

        // for (JavbusDataItem javbusDataItem : this.javbusDataItems) {
        //    if (null == javbusDataItem.getVisitUrl()){
        //        continue;
        //    }
        //    this.concurrentLinkedDeque.offer(javbusDataItem);
        //}

        if (MongodbStorege.isMongoDatabaseAvailable.get()) {
            MongodbStorege.storeInfos(this.javbusDataItems, "javbus", "javFilm");
        } else {
            logging.warn("Warn! No mongoDB online, skip for local store：" + this.javbusDataItems.size());

        }


    }

    public static void trigerStarJavbusTask(List<JavbusDataItem> javbusDataItems){
        StarSpiderJob starSpiderJob = new StarSpiderJob(javbusDataItems,JobExcutor.javbusDataItemConcurrentLinkedDeque);
        JobExcutor.doTgJob(starSpiderJob);
    }
}
