package com.sunrise.tgbot;

import com.sunrise.spider.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @description: tg bot 信息推送
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 12:55 PM
 */
public class JavbusInfoPushBot extends TelegramLongPollingBot {
    //Q&A private chatid
    //TODO chatid 有状态 如果不同的频道使用，那么会导致消息错乱
    private String chatId = "-1001371132897";

    //private String chatId = "-493244777";


    public JavbusInfoPushBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotUsername() {
        return TgBotConfig.JAVBUS_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TgBotConfig.JAVBUS_BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        //设置chatId
        if (update.hasMessage()) {
            System.out.println("----------------------> recieve message from bot place");
            chatId = update.getMessage().getChatId().toString();
            //文本消息
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                doWithCommand(text);
                return;
            }
        }
        //channel post
        //post 消息频率约束比bot严格
        if (update.hasChannelPost()) {
            System.out.println("----------------------> recieve message from channel place");
            chatId = update.getChannelPost().getChatId().toString();
            //channel post
            if (update.getChannelPost().hasText()) {
                String text = update.getChannelPost().getText();
                doWithCommand(text);
            }
        }


    }

    private void doWithCommand(String text) {
        if (text.trim().startsWith("/code")) {
            String[] strings = text.split(" ");
            if (strings.length == 2) {
                SpiderJob.trigerJavbusTask(JavbusHelper.normalCode(strings[1].trim()));
                System.out.println("触发推Javbus任务, 查询 " + strings[1]);
                return;
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("'" + text + "无效查询<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

                try {
                    // Call method to send the message
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        if (text.trim().startsWith("/star")) {
            //查询所有
            if (text.trim().startsWith("/starall")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(strings[1].trim());
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询所有" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replaceAll("/starall", "").trim();
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(starName);
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询所有" + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("'" + text + "无效查询<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            //查询已有磁力
            if (text.trim().startsWith("/starmag")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(strings[1].trim());
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询所有含有磁力" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replace("/starmag", "").trim();
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(starName);
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询所有含有磁力" + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("'" + text + "无效查询<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            //查询个人信息
            if (text.trim().startsWith("/starinfo")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    System.out.println("触发推InfoJavbus任务, 查询个人信息" + strings[1]);

                    JavbusStarInfoItem JavbusStarInfoItem = JavbusSpider.fetchStarInfoByName(strings[1].trim());
                    StartInfoSpiderJob.trigerStarInfoJob(JavbusStarInfoItem);

                    return;
                }

                if (strings.length >= 3) {
                    String starName = text.replace("/starinfo", "").trim();
                    System.out.println("触发推InfoJavbus任务, 查询个人信息" + starName);

                    JavbusStarInfoItem JavbusStarInfoItem = JavbusSpider.fetchStarInfoByName(starName);
                    StartInfoSpiderJob.trigerStarInfoJob(JavbusStarInfoItem);

                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("'" + text + "无效查询<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            //查询首页最多30个作品
            if (text.trim().startsWith("/star")) {
                String[] queryStrs = text.split(" ");
                if (queryStrs.length == 2) {
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(queryStrs[1].trim());
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                    return;
                }

                if (queryStrs.length >= 3) {
                    String starName = text.replaceAll("/star", "").trim();
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(starName);
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    System.out.println("触发推StarJavbus任务, 查询 " + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("'" + text + "无效查询<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            //starnewall

            //starnewmag
        }


        // 直接返回无法处理的消息命令
        System.out.println(TgBotConfig.JAVBUS_BOT_NAME + " 收到消息： " + text);
        // Create a SendMessage object with mandatory fields
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Warn！无法处理： '" + text + "<<<<<-'" + TgBotConfig.JAVBUS_BOT_NAME);

        try {
            // Call method to send the message
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRegister() {
        super.onRegister();
        JobExcutor.doTgJob(() -> this.startJavbusPushTask(chatId));
        JobExcutor.doDelayPushImgJob(() -> this.startDelaySamplePushJob(chatId));
        JobExcutor.doJavbusStarInfoItemJob(() -> this.startJavbusStarInfoItemPushTask(chatId));
    }

    public void startJavbusStarInfoItemPushTask(String chatId) {
        ConcurrentLinkedDeque<JavbusStarInfoItem> linkedDeque = JobExcutor.JavbusStarInfoItemConcurrentLinkedDeque;

        while (true) {
            try {
                if (!linkedDeque.isEmpty()) {
                    JavbusStarInfoItem javbusDataItem = linkedDeque.pollFirst();
                    Runnable tgPushTask = new JavbusStarInfoItemJob(javbusDataItem);
                    JobExcutor.doTgJob(tgPushTask);
                }
                TimeUnit.SECONDS.sleep(5);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startJavbusPushTask(String chatId) {
        ConcurrentLinkedDeque<JavbusDataItem> linkedDeque = JobExcutor.javbusDataItemConcurrentLinkedDeque;

        while (true) {
            //Response{protocol=http/1.1, code=200, message=OK, url=https://api.telegram.org/bot1795760173:AAGqnMBVoBohuWzv0fsQGbclZ3N_nYOIW_o/sendMessage?chat_id=@sunrisechannel_8888&text=hello}
            //{"ok":true,"result":{"message_id":38,"sender_chat":{"id":-1001371132897,"title":"Q&A","username":"sunrisechannel_8888","type":"channel"},"chat":{"id":-1001371132897,"title":"Q&A","username":"sunrisechannel_8888","type":"channel"},"date":1619242901,"text":"hello"}}
            System.out.println("--------------------------------睡眠5秒--------------------------------" + System.currentTimeMillis());
            System.out.println("--------------------------------当前还有" + linkedDeque.size() + "个任务没有被推入执行器--------------------------------");
            try {

                if (!linkedDeque.isEmpty()) {
                    JavbusDataItem javbusDataItem = linkedDeque.pollFirst();
                    Runnable tgPushTask = new JavbusPushInfoJob(javbusDataItem);

                    //JavbusPushInfoPipelineJob tgPushTask = new JavbusPushInfoPipelineJob(javbusDataItem);

                    JobExcutor.doTgJob(tgPushTask);

                    //SendPhoto.SendPhotoBuilder builder = SendPhoto.builder();
                    //builder.caption("cccccc");
                    //builder.photo(new InputFile(new FileInputStream("abc.jpg"),"ceshi"));
                    //builder.chatId("-1001371132897");
                    //builder.parseMode("Markdown");
                    //SendPhoto sendPhoto = builder.build();

                    //execute(sendPhoto);
                } else {
                    System.out.println("--------------------------------当前爬虫数据已经推送完毕--------------------------------");
                }
                TimeUnit.SECONDS.sleep(5);

                //SendDocument.SendDocumentBuilder builder = SendDocument.builder();
                //builder.chatId("-1001371132897");
                //builder.caption("这是一个标题");
                //builder.parseMode("Markdown");
                //InputFile inputFile = new InputFile();
                //inputFile.setMedia(new FileInputStream("abc.jpg"),"测试图票");
                //builder.thumb(inputFile);
                //
                //InputFile inputFile2 = new InputFile();
                //inputFile2.setMedia(new FileInputStream("abc.jpg"),"测试图票");
                //builder.document(inputFile2);
                //SendDocument sendDocument = builder.build();
                //execute(sendDocument);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class JavbusPushInfoPipelineJob implements Runnable {
        private JavbusDataItem javbusDataItem = null;


        public JavbusPushInfoPipelineJob(JavbusDataItem javbusDataItem) {
            this.javbusDataItem = javbusDataItem;
        }

        @Override
        public void run() {
            try {

                CompletableFuture<Message> stage1 = CompletableFuture.supplyAsync(() -> {
                    String prettyStr = javbusDataItem.toPrettyStr();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(prettyStr);
                    sendMessage.enableHtml(true);
                    sendMessage.enableMarkdown(false);
                    sendMessage.enableNotification();
                    try {
                        executeAsync(sendMessage).whenCompleteAsync((message, throwable) -> System.out.println("推送简介完成：" + javbusDataItem.getCode()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                CompletableFuture<Message> stage2 = CompletableFuture.supplyAsync(() -> {
                    String magnetStrs = javbusDataItem.toPrettyMagnetStrs();
                    SendMessage magnetMessage = new SendMessage();
                    magnetMessage.setChatId(chatId);
                    magnetMessage.setText(magnetStrs);
                    magnetMessage.enableHtml(true);
                    magnetMessage.enableMarkdown(false);
                    try {
                        executeAsync(magnetMessage).whenCompleteAsync((message, throwable) -> System.out.println("推送磁力链接完成：" + javbusDataItem.getCode()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;

                });

                CompletableFuture<Message> stage3 = CompletableFuture.supplyAsync(() -> {
                    List<String> sampleImgs = javbusDataItem.getSampleImgs();
                    List<List<String>> listList = javbusDataItem.sliceSampleImgUrlForupload();
                    if (null != sampleImgs && !sampleImgs.isEmpty()) {
                        //发送图片组
                        for (List<String> strings : listList) {
                            List<InputMedia> inputMediaPhotoList = new ArrayList<>();
                            boolean hasSetTag = true;
                            //for (String sampleImg : strings) {
                            //    InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
                            //    if (hasSetTag) {
                            //        inputMediaPhoto.setCaption("#" + javbusDataItem.getCode().replace("-", ""));
                            //        hasSetTag = false;
                            //    }
                            //    //下载图片
                            //    OkHttpClient client = new OkHttpClient();
                            //    //获取请求对象
                            //    Request request = new Request.Builder().url(sampleImg.trim()).build();
                            //    //获取响应体
                            //    ResponseBody body = null;
                            //    try {
                            //        body = client.newCall(request).execute().body();
                            //    } catch (IOException e) {
                            //        e.printStackTrace();
                            //    }
                            //    //获取流
                            //    InputStream in = body.byteStream();
                            //    inputMediaPhoto.setMedia(in, sampleImg.substring(sampleImg.lastIndexOf("/")));
                            //    inputMediaPhoto.setParseMode("Markdown");
                            //    inputMediaPhotoList.add(inputMediaPhoto);
                            //}

                            CompletableFuture[] completableFutures = strings.stream()
                                    .map(el -> {
                                        CompletableFuture<Object[]> inputStreamCompletableFuture = CompletableFuture.supplyAsync(() -> {
                                            //下载图片
                                            OkHttpClient client = new OkHttpClient.Builder()
                                                    .retryOnConnectionFailure(true)
                                                    .connectTimeout(60, TimeUnit.SECONDS) //连接超时
                                                    .readTimeout(60, TimeUnit.SECONDS) //读取超时
                                                    .writeTimeout(60, TimeUnit.SECONDS) //写超时
                                                    .build();
                                            //获取请求对象
                                            Request request = new Request.Builder().url(el.trim()).build();
                                            //获取响应体
                                            ResponseBody body = null;
                                            try {
                                                body = client.newCall(request).execute().body();
                                            } catch (IOException exception) {
                                                body.close();
                                                exception.printStackTrace();
                                            }
                                            Object[] objects = new Object[2];
                                            objects[0] = body;
                                            objects[1] = el.trim();
                                            return objects;
                                        });

                                        return inputStreamCompletableFuture;
                                    }).toArray(CompletableFuture[]::new);

                            CompletableFuture.allOf(completableFutures).join();

                            for (int i = 0; i < completableFutures.length; i++) {
                                InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
                                if (hasSetTag) {
                                    inputMediaPhoto.setCaption("#" + javbusDataItem.getCode());
                                    hasSetTag = false;
                                }
                                CompletableFuture completableFuture = completableFutures[i];
                                Object[] objects = new Object[0];
                                try {
                                    objects = (Object[]) completableFuture.get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                ResponseBody responseBody = (ResponseBody) objects[0];
                                InputStream inputStream = responseBody.byteStream();
                                String sampleImg = (String) objects[1];

                                inputMediaPhoto.setMedia(inputStream, sampleImg.substring(sampleImg.lastIndexOf("/")));
                                inputMediaPhoto.setParseMode("Html");
                                inputMediaPhotoList.add(inputMediaPhoto);
                            }
                            SendMediaGroup sendMediaGroup = new SendMediaGroup();
                            sendMediaGroup.setChatId(chatId);
                            sendMediaGroup.setMedias(inputMediaPhotoList);
                            executeAsync(sendMediaGroup).whenCompleteAsync((message, throwable) -> System.out.println("推送样品图完成：" + javbusDataItem.getCode()));
                        }

                    }
                    return new Message();
                }).exceptionally(throwable -> {
                    System.out.println("推送样品图出现异常：" + throwable.getMessage());
                    return null;
                });

                CompletableFuture<Void> all = CompletableFuture.allOf(stage1, stage2, stage3);
                //等待所有任务完成
                all.join();

            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("推送作品信息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 延迟队列推送样品图
     */
    public void startDelaySamplePushJob(String chatId) {
        DelayQueue<DelaySampleImgPush> delaySampleImgPushes = JobExcutor.delaySampleImgPushes;
        assert delaySampleImgPushes != null;

        while (true) {
            try {
                DelaySampleImgPush delaySampleImgPush = delaySampleImgPushes.take();

                JavbusDataItem javbusDataItem = delaySampleImgPush.getJavbusDataItem();
                System.out.println("延迟队列到期，正在处理中：" + javbusDataItem.getCode());

                pushSampleImagesInfo(javbusDataItem);


            } catch (InterruptedException | TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void pushJavbusStarInfoItem(JavbusStarInfoItem JavbusStarInfoItem) {
        try {
            String javStarInfo = JavbusStarInfoItem.toPrettyStr();
            SendMessage selfInfoMessage = new SendMessage();
            selfInfoMessage.setChatId(chatId);
            selfInfoMessage.setText(javStarInfo);
            selfInfoMessage.enableHtml(true);
            selfInfoMessage.enableMarkdown(false);

            executeAsync(selfInfoMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("个人信息推送完成： " + JavbusStarInfoItem.getStarName());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.out.println("推送个人信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("推送个人信息出现异常：" + e.getMessage());
        }
    }

    /**
     * 推送个人信息任务
     */
    class JavbusStarInfoItemJob implements Runnable {

        private JavbusStarInfoItem JavbusStarInfoItem;

        public JavbusStarInfoItemJob(JavbusStarInfoItem JavbusStarInfoItem) {
            this.JavbusStarInfoItem = JavbusStarInfoItem;
        }

        @Override
        public void run() {
            try {
                pushJavbusStarInfoItem(JavbusStarInfoItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class JavbusPushInfoJob implements Runnable {
        private JavbusDataItem javbusDataItem;

        public JavbusPushInfoJob(JavbusDataItem javbusDataItem) {
            this.javbusDataItem = javbusDataItem;
        }

        @Override
        public void run() {
            try {
                pushIntroduceInfo(javbusDataItem);
                pushMagnentInfo(javbusDataItem);
                JobExcutor.doDelayPushImgEnqueue(javbusDataItem);

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * #ABW016 11张回出错
     * Number of media should be between 2 and 10 in method: SendMediaGroup
     *
     * @param javbusDataItem
     * @throws TelegramApiException
     */
    private void pushSampleImagesInfo(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            List<String> sampleImgs = javbusDataItem.getSampleImgs();

            List<List<String>> listList = javbusDataItem.sliceSampleImgUrlForupload();

            if (null != sampleImgs && !sampleImgs.isEmpty()) {
                //发送图片组
                for (List<String> strings : listList) {
                    List<InputMedia> inputMediaPhotoList = new ArrayList<>();
                    boolean hasSetTag = true;
                    //for (String sampleImg : strings) {
                    //    InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
                    //    if (hasSetTag) {
                    //        inputMediaPhoto.setCaption("#" + javbusDataItem.getCode().replace("-", ""));
                    //        hasSetTag = false;
                    //    }
                    //
                    //    //下载图片
                    //    OkHttpClient client = new OkHttpClient();
                    //    //获取请求对象
                    //    Request request = new Request.Builder().url(sampleImg.trim()).build();
                    //    //获取响应体
                    //    ResponseBody body = null;
                    //    try {
                    //        body = client.newCall(request).execute().body();
                    //    } catch (IOException e) {
                    //        e.printStackTrace();
                    //    }
                    //    //获取流
                    //    InputStream in = body.byteStream();
                    //    inputMediaPhoto.setMedia(in, sampleImg.substring(sampleImg.lastIndexOf("/")));
                    //    inputMediaPhoto.setParseMode("Markdown");
                    //    inputMediaPhotoList.add(inputMediaPhoto);
                    //}

                    CompletableFuture[] completableFutures = strings.stream().parallel()
                            .map(el -> {
                                CompletableFuture<Object[]> inputStreamCompletableFuture = CompletableFuture.supplyAsync(() -> {
                                    //下载图片
                                    OkHttpClient client = new OkHttpClient.Builder()
                                            .retryOnConnectionFailure(true)
                                            .connectTimeout(60, TimeUnit.SECONDS) //连接超时
                                            .readTimeout(60, TimeUnit.SECONDS) //读取超时
                                            .writeTimeout(60, TimeUnit.SECONDS) //写超时
                                            .build();
                                    //获取请求对象
                                    Request request = new Request.Builder().url(el.trim()).build();
                                    //获取响应体
                                    ResponseBody body = null;
                                    Response execute = null;
                                    try {
                                        execute = client.newCall(request).execute();
                                        body = execute.body();
                                    } catch (IOException exception) {
                                        if (null != body) {
                                            body.close();
                                        }
                                        exception.printStackTrace();
                                    }
                                    Object[] objects = new Object[2];
                                    objects[0] = body;
                                    objects[1] = el.trim();
                                    return objects;
                                });

                                return inputStreamCompletableFuture;
                            }).toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(completableFutures).join();

                    for (int i = 0; i < completableFutures.length; i++) {
                        InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
                        if (hasSetTag) {
                            StringBuilder stringBuilder = new StringBuilder();
                            String code = javbusDataItem.getCode();
                            stringBuilder.append("#").append(code.replaceAll("-", "_"));
                            if (null != javbusDataItem.getMainStarPageUrl() && null != javbusDataItem.getMainStarPageUrl().getStartPageUrl()) {
                                stringBuilder.append(" ").append("#").append(javbusDataItem.getStars());
                            }
                            inputMediaPhoto.setCaption(stringBuilder.toString());
                            hasSetTag = false;
                        }
                        CompletableFuture completableFuture = completableFutures[i];
                        Object[] objects = (Object[]) completableFuture.get();
                        ResponseBody responseBody = (ResponseBody) objects[0];
                        InputStream inputStream = responseBody.byteStream();
                        String sampleImg = (String) objects[1];

                        inputMediaPhoto.setMedia(inputStream, sampleImg.substring(sampleImg.lastIndexOf("/")));
                        //Markdown模式会对下划线 中划线敏感
                        inputMediaPhoto.setParseMode("Html");
                        inputMediaPhotoList.add(inputMediaPhoto);
                    }

                    SendMediaGroup sendMediaGroup = new SendMediaGroup();
                    sendMediaGroup.setChatId(chatId);
                    sendMediaGroup.setMedias(inputMediaPhotoList);
                    CompletableFuture<List<Message>> listCompletableFuture = executeAsync(sendMediaGroup);

                    listCompletableFuture.whenCompleteAsync((message, throwable) -> {
                        //主动关闭
                        for (CompletableFuture completableFuture : completableFutures) {
                            Object[] objects = new Object[0];
                            try {
                                objects = (Object[]) completableFuture.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            ResponseBody responseBody = (ResponseBody) objects[0];
                            responseBody.close();
                        }
                        System.out.println("推送样品图完成：" + javbusDataItem.getCode());
                    }).exceptionally(throwable -> {
                        System.out.println("推送样品图CompleteFuture出现异常：" + throwable.getMessage());
                        //尝试重新加入延迟队列的最末端
                        System.out.println("正在尝试重新加入延迟队列......");
                        if (javbusDataItem.getFetchRetry() >= 2) {
                            System.out.println("推送样品图尝试次数超过限制(3次),丢弃：" + javbusDataItem.getCode());
                            return null;
                        }
                        int fetchCount = javbusDataItem.getFetchRetry() + 1;
                        javbusDataItem.setFetchRetry(fetchCount);
                        JobExcutor.doDelayPushImgEnqueue(javbusDataItem);
                        return null;
                    });
                    listCompletableFuture.join();
                }
            }
        } catch (Exception e) {
            System.out.println("推送样品图Try出现异常：" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void pushIntroduceInfo(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            String prettyStr = javbusDataItem.toPrettyStr();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(prettyStr);
            sendMessage.enableHtml(true);
            sendMessage.enableMarkdown(false);
            sendMessage.enableNotification();
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("推送简介完成：" + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.out.println("推送简介出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("推送简介出现异常：" + e.getMessage());
            e.printStackTrace();
            //try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            //pushSampleImagesInfo(javbusDataItem);
        }

    }

    private void pushMagnentInfo(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            String magnetStrs = javbusDataItem.toPrettyMagnetStrs();
            SendMessage magnetMessage = new SendMessage();
            magnetMessage.setChatId(chatId);
            magnetMessage.setText(magnetStrs);
            magnetMessage.enableMarkdown(false);
            executeAsync(magnetMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("磁力信息推送完成： " + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.out.println("推送磁力信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("推送磁力信息出现异常：" + e.getMessage());
            //try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            //pushMagnentInfo(javbusDataItem);
        }
    }
}
