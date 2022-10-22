package com.sunrise.javbusbot.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @description: 爬虫执行器
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 12:35 AM
 */
public class JobExcutor {
    public static final Logger logger = LoggerFactory.getLogger(JobExcutor.class);

    public static volatile ThreadPoolExecutor spiderExcutorService = null;

    public static volatile ThreadPoolExecutor tgBotExcutorService = null;

    public static volatile ConcurrentLinkedDeque<JavbusDataItem> javbusDataItemConcurrentLinkedDeque = null;

    public static volatile ConcurrentLinkedDeque<JavbusStarInfoItem> javbusStarInfoItemConcurrentLinkedDeque = null;

    public static volatile DelayQueue<DelaySampleImgPush> delaySampleImgPushes = null;

    static {
        spiderExcutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        tgBotExcutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        javbusDataItemConcurrentLinkedDeque = new ConcurrentLinkedDeque<>();
        javbusStarInfoItemConcurrentLinkedDeque = new ConcurrentLinkedDeque<>();
        delaySampleImgPushes = new DelayQueue<>();
    }

    public static void doSpiderJob(Runnable runnable) {
        spiderExcutorService.submit(runnable);
    }

    public static void doTgJob(Runnable runnable) {
        tgBotExcutorService.submit(runnable);
    }

    public static void doJavbusStarInfoItemJob(Runnable runnable) {
        tgBotExcutorService.submit(runnable);
    }

    public static void doDelayPushImgEnqueue(JavbusDataItem javbusDataItem){
        int size = JobExcutor.delaySampleImgPushes.size();
        delaySampleImgPushes.put(new DelaySampleImgPush(javbusDataItem.getCode(),javbusDataItem,15*(size+1)*1000));
    }

    public static void doDelayPushImgJob(Runnable runnable){
        tgBotExcutorService.submit(runnable);
    }

    public static synchronized int getTaskQueue(){
        return tgBotExcutorService.getQueue().size();
    }


    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info(Thread.currentThread().getName() + "----" + finalI);
                        BlockingQueue<Runnable> queue = tgBotExcutorService.getQueue();
                        logger.info("-----当前队列：" + queue.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            tgBotExcutorService.submit(runnable);

        }

        for (int j = 0; j < 20; j++) {
            BlockingQueue<Runnable> queue = tgBotExcutorService.getQueue();
            logger.info("当前队列：" + queue.size());
        }
    }


}
