package com.sunrise.javbusbot.tgbot;

import com.sunrise.javbusbot.spider.*;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.sunrise.javbusbot.spider.JavbusSpider.getJavdbSearchReqHeader;

/**
 * @description: tg bot 信息推送
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 12:55 PM
 */
public class JavbusInfoPushBot extends TelegramLongPollingBot {
    public static final Logger logging = LoggerFactory.getLogger(JavbusInfoPushBot.class);

    // TODO chatid 有状态 如果不同的频道使用，那么会导致消息错乱
    // 预留给forward功能
    public static String chatId = "";

    // private String chatId = "-493244777";

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

        if (update.hasEditedMessage()) {
            logging.info("----------------------> recieve message from bot place");
            // 判断是否开启了forward chat
            if (TgBotConfig.FORWARD_MESSAGE_OPTION) {
                chatId = TgBotConfig.FORWARD_MESSAGE_OPTION_CHATID;
            } else {
                chatId = update.getEditedMessage().getChatId().toString();
            }
            // 文本消息
            if (update.getEditedMessage().hasText()) {
                String text = update.getEditedMessage().getText();
                String messageChatId = update.getEditedMessage().getChatId().toString();
                sendWaitingForQuery(text, messageChatId);
                doWithCommand(text, messageChatId);
                return;
            }
        }
        // 设置chatId
        if (update.hasMessage()) {
            logging.info("----------------------> recieve message from bot place");
            // 判断是否开启了forward chat
            if (TgBotConfig.FORWARD_MESSAGE_OPTION) {
                chatId = TgBotConfig.FORWARD_MESSAGE_OPTION_CHATID;
            } else {
                chatId = update.getMessage().getChatId().toString();
            }
            // 文本消息
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                String messageChatId = update.getMessage().getChatId().toString();
                sendWaitingForQuery(text, messageChatId);
                doWithCommand(text, messageChatId);
                return;
            }
        }
        // channel post
        // post 消息频率约束比bot严格
        if (update.hasChannelPost()) {
            logging.info("----------------------> recieve message from channel place");
            if (TgBotConfig.FORWARD_MESSAGE_OPTION) {
                chatId = TgBotConfig.FORWARD_MESSAGE_OPTION_CHATID;
            } else {
                chatId = update.getChannelPost().getChatId().toString();
            }
            // channel post
            if (update.getChannelPost().hasText()) {
                String text = update.getChannelPost().getText();
                String messageChatId = update.getEditedMessage().getChatId().toString();
                sendWaitingForQuery(text, messageChatId);
                doWithCommand(text, messageChatId);
            }
        }


    }

    /**
     * 发送查询出的演员列表
     *
     * @param starNames
     * @param messageChatId
     */
    private void sendStarNameList(List<String> starNames, String messageChatId) {
        StringBuilder builder = new StringBuilder();
        builder.append("已为您找到如下演员(包含曾用名): \n");
        builder.append("-----------------------------------------------------\n");
        for (int i = 1; i <= starNames.size(); i++) {
            builder.append(i + ". " + starNames.get(i - 1) + "\n");
        }
        builder.append("-----------------------------------------------------\n");
        builder.append("请选择需要查询的演员，并重新输入命令(/command xxxx)");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(messageChatId);
        sendMessage.setText(builder.toString());
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdown(false);
        sendMessage.enableNotification();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logging.error("发送消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送查询等待消息
     *
     * @param text
     * @param messageChatId
     */
    private void sendWaitingForQuery(String text, String messageChatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(messageChatId);
        sendMessage.setText("正在拼命查询: " + text + ", 请稍后...");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logging.warn("发送等待消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据查询指令执行相应功能
     *
     * @param text
     * @param messageChatId
     */
    private void doWithCommand(String text, String messageChatId) {
        if (text.trim().startsWith("/code")) {
            String[] strings = text.split(" ");
            if (strings.length == 2) {
                SpiderJob.trigerJavbusCodeTask(JavbusHelper.normalCode(strings[1].trim()), messageChatId);
                logging.info("触发推Javbus任务, 查询 " + strings[1]);
                return;
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(messageChatId);
                message.setText(text + " 人家查询不到啦,请重新输入(/code xxxx)" + "❤️❤️❤️");

                try {
                    // Call method to send the message
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        // 推送演员最新的一部作品 可能是没有mag的
        // latest
        if (text.trim().startsWith("/latest")) {
            String[] queryStrs = text.split(" ");
            if (queryStrs.length == 2) {
                // 获取演员的正确名字
                List<String> starNames = this.fixStarName(queryStrs[1].trim());
                if (starNames.size() > 1) {
                    this.sendStarNameList(starNames, messageChatId);
                    return;
                }
                JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(starNames.get(0));
                javbusDataItem.setMessageChatId(messageChatId);
                List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                logging.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                return;
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(messageChatId);
                message.setText(text + "人家查询不到啦,请重新输入(/latest xxxx)" + "❤️❤️❤️");

                try {
                    // Call method to send the message
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        // 推送最新一部有磁力的作品
        if (text.trim().startsWith("/maglatest")) {
            String[] queryStrs = text.split(" ");
            if (queryStrs.length == 2) {
                // 获取演员的正确名字
                List<String> starNames = this.fixStarName(queryStrs[1].trim());
                if (starNames.size() > 1) {
                    this.sendStarNameList(starNames, messageChatId);
                    return;
                }
                JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestMagFilmInfoByName(starNames.get(0));
                javbusDataItem.setMessageChatId(messageChatId);
                List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                logging.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                return;
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(messageChatId);
                message.setText(text + "人家查询不到啦,请重新输入(/maglatest xxxx)" + "❤️❤️❤️");

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
            // 查询所有
            if (text.trim().startsWith("/starall")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询所有" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replaceAll("/starall", "").trim();
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询所有" + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(messageChatId);
                    message.setText(text + "人家查询不到啦,请重新输入(/starall xxxx)" + "❤️❤️❤️");

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            // 查询已有磁力
            if (text.trim().startsWith("/starmag")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询所有含有磁力" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replace("/starmag", "").trim();
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询所有含有磁力" + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(messageChatId);
                    message.setText(text + "人家查询不到啦,请重新输入(/starmag xxxx)" + "❤️❤️❤️");

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            // 查询个人信息
            if (text.trim().startsWith("/starinfo")) {
                String[] strings = text.split(" ");
                if (strings.length == 2) {
                    logging.info("触发推InfoJavbus任务, 查询个人信息" + strings[1]);
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    JavbusStarInfoItem JavbusStarInfoItem = JavbusSpider.fetchStarInfoByName(starNames.get(0));
                    JavbusStarInfoItem.setMessageChatId(messageChatId);
                    StartInfoSpiderJob.trigerStarInfoJob(JavbusStarInfoItem);

                    return;
                }

                if (strings.length >= 3) {
                    String starName = text.replace("/starinfo", "").trim();
                    logging.info("触发推InfoJavbus任务, 查询个人信息" + starName);
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    JavbusStarInfoItem JavbusStarInfoItem = JavbusSpider.fetchStarInfoByName(starNames.get(0));
                    JavbusStarInfoItem.setMessageChatId(messageChatId);
                    StartInfoSpiderJob.trigerStarInfoJob(JavbusStarInfoItem);

                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(messageChatId);
                    message.setText(text + "人家查询不到啦,请重新输入(/starinfo xxxx)" + "❤️❤️❤️");

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            // 查询首页最多30个作品
            if (text.trim().startsWith("/star")) {
                String[] queryStrs = text.split(" ");
                if (queryStrs.length == 2) {
                    List<String> starNames = this.fixStarName(queryStrs[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                    return;
                }

                if (queryStrs.length >= 3) {
                    String starName = text.replaceAll("/star", "").trim();
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logging.info("触发推StarJavbus任务, 查询 " + starName);
                    return;
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(messageChatId);
                    message.setText(text + "人家查询不到啦,请重新输入(/star xxxx)" + "❤️❤️❤️");

                    try {
                        // Call method to send the message
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }


        // 直接返回无法处理的消息命令
        logging.info(TgBotConfig.JAVBUS_BOT_NAME + " 收到消息： " + text);
        // Create a SendMessage object with mandatory fields
        SendMessage message = new SendMessage();
        message.setChatId(messageChatId);
        message.setText("人家不认识这个指令： " + text + " " + "\uD83D\uDE35\uD83D\uDE35\uD83D\uDE35");

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
        JobExcutor.doTgJob(() -> this.startJavbusPushTask());
        JobExcutor.doDelayPushImgJob(() -> this.startDelaySamplePushJob());
        JobExcutor.doJavbusStarInfoItemJob(() -> this.startJavbusStarInfoItemPushTask());
    }

    public void startJavbusStarInfoItemPushTask() {
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

    public void startJavbusPushTask() {
        ConcurrentLinkedDeque<JavbusDataItem> linkedDeque = JobExcutor.javbusDataItemConcurrentLinkedDeque;
        while (true) {
            // Response{protocol=http/1.1, code=200, message=OK, url=https://api.telegram.org/bot1795760*6173:AAGqnMBVoBohuWzv0fsQGbclZ3N_nYOIW_o/sendMessage?chat_id=@sunrisechannel_8888&text=hello}
            //{"ok":true,"result":{"message_id":38,"sender_chat":{"id":-1001371132897,"title":"Q&A","username":"sunrisechannel_8888","type":"channel"},"chat":{"id":-1001371132897,"title":"Q&A","username":"sunrisechannel_8888","type":"channel"},"date":1619242901,"text":"hello"}}
            try {
                if (!linkedDeque.isEmpty()) {
                    JavbusDataItem javbusDataItem = linkedDeque.pollFirst();
                    Runnable tgPushTask = new JavbusPushInfoJob(javbusDataItem);
                    // JavbusPushInfoPipelineJob tgPushTask = new JavbusPushInfoPipelineJob(javbusDataItem);
                    JobExcutor.doTgJob(tgPushTask);
                } else {
                    logging.info("--------------------------------当前爬虫数据已经推送完毕--------------------------------");
                }
                TimeUnit.SECONDS.sleep(5);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (logging.isDebugEnabled()) {
                logging.debug("--------------------------------睡眠5秒--------------------------------" + System.currentTimeMillis());
                logging.debug("--------------------------------当前还有" + linkedDeque.size() + "个任务没有被推入执行器--------------------------------");
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
                    sendMessage.setChatId(javbusDataItem.getMessageChatId());
                    sendMessage.setText(prettyStr);
                    sendMessage.enableHtml(true);
                    sendMessage.enableMarkdown(false);
                    sendMessage.enableNotification();
                    try {
                        executeAsync(sendMessage).whenCompleteAsync((message, throwable) -> logging.info("推送简介完成：" + javbusDataItem.getCode()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                CompletableFuture<Message> stage2 = CompletableFuture.supplyAsync(() -> {
                    String magnetStrs = javbusDataItem.toPrettyMagnetStrs();
                    SendMessage magnetMessage = new SendMessage();
                    magnetMessage.setChatId(javbusDataItem.getMessageChatId());
                    magnetMessage.setText(magnetStrs);
                    magnetMessage.enableHtml(true);
                    magnetMessage.enableMarkdown(false);
                    try {
                        executeAsync(magnetMessage).whenCompleteAsync((message, throwable) -> logging.info("推送磁力链接完成：" + javbusDataItem.getCode()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;

                });

                CompletableFuture<Message> stage3 = CompletableFuture.supplyAsync(() -> {
                    List<String> sampleImgs = javbusDataItem.getSampleImgs();
                    List<List<String>> listList = javbusDataItem.sliceSampleImgUrlForupload();
                    if (null != sampleImgs && !sampleImgs.isEmpty()) {
                        // 发送图片组
                        for (List<String> strings : listList) {
                            List<InputMedia> inputMediaPhotoList = new ArrayList<>();
                            boolean hasSetTag = true;
                            // for (String sampleImg : strings) {
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

                            CompletableFuture[] completableFutures = strings.stream().map(el -> {
                                CompletableFuture<Object[]> inputStreamCompletableFuture = CompletableFuture.supplyAsync(() -> {
                                    // 下载图片
                                    OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(60 * 6, TimeUnit.SECONDS) // 连接超时
                                            .readTimeout(60 * 6, TimeUnit.SECONDS) // 读取超时
                                            .writeTimeout(60 * 6, TimeUnit.SECONDS) // 写超时
                                            .build();
                                    // 获取请求对象
                                    Request request = new Request.Builder().url(el.trim()).build();
                                    // 获取响应体
                                    Response response = null;
                                    ResponseBody body = null;
                                    try {
                                        response = client.newCall(request).execute();
                                        body = response.body();
                                    } catch (IOException exception) {
                                        if (null != body) {
                                            body.close();
                                            logging.warn("当前请求响应失败");
                                        }
                                        exception.printStackTrace();
                                        logging.error("当前请求地址: " + request.url());
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
                                } catch (InterruptedException | ExecutionException e) {
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
                            sendMediaGroup.setChatId(javbusDataItem.getMessageChatId());
                            sendMediaGroup.setMedias(inputMediaPhotoList);
                            executeAsync(sendMediaGroup).whenCompleteAsync((message, throwable) -> logging.info("推送样品图完成：" + javbusDataItem.getCode()));
                        }

                    }
                    return new Message();
                }).exceptionally(throwable -> {
                    logging.info("推送样品图出现异常：" + throwable.getMessage());
                    return null;
                });

                CompletableFuture<Void> all = CompletableFuture.allOf(stage1, stage2, stage3);
                // 等待所有任务完成
                all.join();

            } catch (Exception e) {
                // e.printStackTrace();
                logging.info("推送作品信息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 延迟队列推送样品图
     */
    public void startDelaySamplePushJob() {
        DelayQueue<DelaySampleImgPush> delaySampleImgPushes = JobExcutor.delaySampleImgPushes;
        assert delaySampleImgPushes != null;

        while (true) {
            try {
                DelaySampleImgPush delaySampleImgPush = delaySampleImgPushes.take();

                JavbusDataItem javbusDataItem = delaySampleImgPush.getJavbusDataItem();
                logging.info("延迟队列到期，正在处理中：" + javbusDataItem.getCode());

                pushSampleImagesInfo(javbusDataItem);


            } catch (InterruptedException | TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void pushJavbusStarInfoItem(JavbusStarInfoItem javbusStarInfoItem) {
        try {
            String javStarInfo = javbusStarInfoItem.toPrettyStr();
            SendMessage selfInfoMessage = new SendMessage();
            selfInfoMessage.setChatId(javbusStarInfoItem.getMessageChatId());
            selfInfoMessage.setText(javStarInfo);
            selfInfoMessage.enableHtml(true);
            selfInfoMessage.enableMarkdown(false);

            executeAsync(selfInfoMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logging.info("个人信息推送完成： " + javbusStarInfoItem.getStarName());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logging.info("推送个人信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logging.info("推送个人信息出现异常：" + e.getMessage());
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
            if (null == this.javbusDataItem.getVisitUrl() || "".equals(this.javbusDataItem.getVisitUrl())) {
                pushNotFoundResult();
                return;
            }
            try {
                logging.info("当前作品地址为: " + javbusDataItem.getVisitUrl());
                pushIntroduceInfo(javbusDataItem);
                pushVideoPreview(javbusDataItem);
                pushMagnentInfo(javbusDataItem);
                logging.info("正在推送样品图延迟任务: " + javbusDataItem.getCode());
                JobExcutor.doDelayPushImgEnqueue(javbusDataItem);

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        public void pushNotFoundResult() {
            try {
                pushCodeNotFundMsg(this.javbusDataItem);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 将输入的演员名字 从javdb查询出正确的名字
     *
     * @param starName
     * @return
     */
    private List<String> fixStarName(String starName) {
        String queryUrl = TgBotConfig.JAVDB_BASE_URL + "search?q=$s&f=actor";
        queryUrl = queryUrl.replace("$s", starName);
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true).addInterceptor(new RetryInterceptor(2))
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS);

        okHttpClient = builder.build();
        Request request = new Request.Builder().url(queryUrl).get().headers(Headers.of(getJavdbSearchReqHeader(starName, ""))).build();

        List<String> results = Collections.emptyList();
        try (Response response = okHttpClient.newCall(request).execute(); ResponseBody responseBody = response.body()) {
            if (response.code() != 200) {
                logging.warn("当前查询失败: " + response.request().url());
                return results;
            }
            String result = Objects.requireNonNull(responseBody).string();
            System.out.println(result);
            Document document = Jsoup.parse(result);
            Elements elements = document.selectXpath("//*[@id=\"actors\"]/div/a/strong");
            results = elements.stream().map(e -> e.text()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            logging.warn("当前查询出现错误: " + e.getMessage());
        }
        return results;
    }

    /**
     * 当查询单一番号作品时 推送视频预览
     *
     * @param javbusDataItem
     */
    private void pushVideoPreview(JavbusDataItem javbusDataItem) {
        try {
            CompletableFuture<Object[]> completableFuture = CompletableFuture.supplyAsync(() -> {
                String videoPreviewUrl = javbusDataItem.getVideoPreviewUrl();
                // 下载视频
                // 这里没有做网络代理设置，可能会出现无法访问的情况
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new RetryInterceptor(2)).retryOnConnectionFailure(true).connectTimeout(60 * 6, TimeUnit.SECONDS) // 连接超时
                        .readTimeout(60 * 6, TimeUnit.SECONDS) // 读取超时
                        .writeTimeout(60 * 6, TimeUnit.SECONDS) // 写超时
                        .build();
                // 获取请求对象
                Request request = new Request.Builder().url(videoPreviewUrl.trim()).build();
                // 获取响应体
                ResponseBody body = null;
                Response execute = null;
                try {
                    logging.info("开始请求视频地址: " + request.url());
                    execute = client.newCall(request).execute();
                    body = execute.body();
                } catch (IOException exception) {
                    if (null != body) {
                        body.close();
                        logging.warn("当前请求响应失败");
                    }
                    exception.printStackTrace();
                    logging.error("当前请求地址: " + request.url());
                }
                String bigImgUrl = javbusDataItem.getBigImgUrl();
                Request thumbRequest = new Request.Builder().get().url(bigImgUrl.trim()).build();
                ResponseBody thumbResponse = null;
                try {
                    Response response = client.newCall(thumbRequest).execute();
                    thumbResponse = response.body();
                } catch (IOException e) {
                    if (null != body) {
                        body.close();
                        logging.warn("当前请求响应失败");
                    }
                    e.printStackTrace();
                    logging.error("当前请求地址: " + request.url());
                }
                Object[] objects = new Object[3];
                objects[0] = body;
                objects[1] = thumbResponse;
                objects[2] = videoPreviewUrl.trim();
                return objects;
            });
            CompletableFuture.allOf(completableFuture).join();

            StringBuilder stringBuilder = new StringBuilder();
            String code = javbusDataItem.getCode();
            stringBuilder.append("#").append(code.replaceAll("-", "_"));
            if (null != javbusDataItem.getMainStarPageUrl() && null != javbusDataItem.getMainStarPageUrl().getStartPageUrl()) {
                stringBuilder.append(" ").append("#").append(javbusDataItem.getStars());
            }
            Object[] objects = completableFuture.get();
            ResponseBody responseBody = (ResponseBody) objects[0];
            if (responseBody == null) {
                logging.warn("当前视频预览请求失败,已跳过");
                return;
            }
            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(javbusDataItem.getMessageChatId());
            InputStream inputStream = responseBody.byteStream();

            ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                tempStream.write(buffer, 0, len);
            }
            tempStream.flush();

            if ((tempStream.size() / (1000 * 1000)) > 50) {
                logging.warn("预览视频大于50MB,无法使用Bot发送");
                return;
            }
            InputStream fileInputStream = new ByteArrayInputStream(tempStream.toByteArray());
            InputStream getMetaDataStream = new ByteArrayInputStream(tempStream.toByteArray());
            // telegram 对bot发送文件单个文件最大不超过50Mb
            String videoPreviewUrl = (String) objects[2];
            InputFile inputFile = new InputFile(fileInputStream, videoPreviewUrl.substring(videoPreviewUrl.lastIndexOf("/")));
            sendVideo.setVideo(inputFile);
            sendVideo.setCaption(stringBuilder.toString());
            sendVideo.setParseMode("html");

            ArrayList<Integer> videoMetaData = VideoPreviewUtils.getVideoMetaData(getMetaDataStream, javbusDataItem.getVideoPreviewUrl());
            sendVideo.setDuration(videoMetaData.get(2));
            sendVideo.setHeight(videoMetaData.get(0));
            sendVideo.setWidth(videoMetaData.get(1));
            sendVideo.setSupportsStreaming(true);
            // 获取缩略图
            ResponseBody thumbResponse = (ResponseBody) objects[1];
            InputStream thumbInputStream = thumbResponse.byteStream();
            String bigImgUrl = javbusDataItem.getBigImgUrl();
            sendVideo.setThumb(new InputFile(thumbInputStream, bigImgUrl.substring(bigImgUrl.lastIndexOf("/"))));
            CompletableFuture<Message> messageFuture = executeAsync(sendVideo);

            messageFuture.whenCompleteAsync((message, throwable) -> {
                // 主动关闭
                ResponseBody rs = (ResponseBody) objects[0];
                rs.close();
                logging.info("推送预览视频完成：" + javbusDataItem.getCode());
            }).exceptionally(throwable -> {
                logging.info("推送预览视频CompleteFuture出现异常：" + throwable.getMessage());
                // 尝试重新加入延迟队列的最末端
                logging.info("正在尝试重新加入延迟队列......");
                if (javbusDataItem.getFetchRetry() >= 1) {
                    logging.info("推送预览视频尝试次数超过限制(2次),丢弃：" + javbusDataItem.getCode());
                } else {
                    int fetchCount = javbusDataItem.getFetchRetry() + 1;
                    javbusDataItem.setFetchRetry(fetchCount);
                    JobExcutor.doTgJob(() -> this.pushVideoPreview(javbusDataItem));
                }
                return null;
            });
            messageFuture.join();
            logging.info("推送视频预览成功: " + code);
        } catch (Exception e) {
            logging.error("推送视频预览Try出现异常：" + e.getMessage());
            e.printStackTrace();
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
                // 发送图片组
                for (List<String> strings : listList) {
                    List<InputMedia> inputMediaPhotoList = new ArrayList<>();
                    boolean hasSetTag = true;
                    // for (String sampleImg : strings) {
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

                    CompletableFuture[] completableFutures = strings.stream().parallel().map(el -> {
                        CompletableFuture<Object[]> inputStreamCompletableFuture = CompletableFuture.supplyAsync(() -> {
                            // 下载图片
                            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new RetryInterceptor(2)).retryOnConnectionFailure(true).connectTimeout(60 * 6, TimeUnit.SECONDS) // 连接超时
                                    .readTimeout(60 * 6, TimeUnit.SECONDS) // 读取超时
                                    .writeTimeout(60 * 6, TimeUnit.SECONDS) // 写超时
                                    .build();
                            // 获取请求对象
                            Request request = new Request.Builder().url(el.trim()).build();
                            // 获取响应体
                            ResponseBody body = null;
                            Response execute = null;
                            try {
                                logging.info("开始请求图片地址: " + request.url());
                                execute = client.newCall(request).execute();
                                body = execute.body();
                            } catch (IOException exception) {
                                if (null != body) {
                                    body.close();
                                    logging.warn("当前请求响应失败");
                                }
                                exception.printStackTrace();
                                logging.error("当前请求地址: " + request.url());
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
                        if (responseBody == null) {
                            logging.warn("当前样品图片请求失败,已跳过");
                            continue;
                        }
                        InputStream inputStream = responseBody.byteStream();
                        String sampleImg = (String) objects[1];

                        inputMediaPhoto.setMedia(inputStream, sampleImg.substring(sampleImg.lastIndexOf("/")));
                        // Markdown模式会对下划线 中划线敏感
                        inputMediaPhoto.setParseMode("Html");
                        inputMediaPhotoList.add(inputMediaPhoto);
                    }

                    SendMediaGroup sendMediaGroup = new SendMediaGroup();
                    sendMediaGroup.setChatId(javbusDataItem.getMessageChatId());
                    sendMediaGroup.setMedias(inputMediaPhotoList);
                    CompletableFuture<List<Message>> listCompletableFuture = executeAsync(sendMediaGroup);

                    listCompletableFuture.whenCompleteAsync((message, throwable) -> {
                        // 主动关闭
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
                        logging.info("推送样品图完成：" + javbusDataItem.getCode());
                    }).exceptionally(throwable -> {
                        logging.info("推送样品图CompleteFuture出现异常：" + throwable.getMessage());
                        // 尝试重新加入延迟队列的最末端
                        logging.info("正在尝试重新加入延迟队列......");
                        if (javbusDataItem.getFetchRetry() >= 2) {
                            logging.info("推送样品图尝试次数超过限制(3次),丢弃：" + javbusDataItem.getCode());
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
            logging.info("推送样品图Try出现异常：" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void pushCodeNotFundMsg(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(javbusDataItem.getMessageChatId());
            sendMessage.setText("该番号未找到!\uD83D\uDE37\uD83D\uDE37\uD83D\uDE37");
            sendMessage.enableHtml(true);
            sendMessage.enableMarkdown(false);
            sendMessage.enableNotification();
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logging.info(javbusDataItem.getCode() + " 番号查询未找到结果,消息推送完毕");
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logging.info("推送番号未找到消息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logging.info("推送番号未找到消息出现异常：" + e.getMessage());
        }

    }

    private void pushIntroduceInfo(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            String prettyStr = javbusDataItem.toPrettyStr();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(javbusDataItem.getMessageChatId());
            sendMessage.setText(prettyStr);
            sendMessage.enableHtml(true);
            sendMessage.enableMarkdown(false);
            sendMessage.enableNotification();
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logging.info("推送简介完成：" + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logging.info("推送简介出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logging.info("推送简介出现异常：" + e.getMessage());
            e.printStackTrace();
            // try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            // pushSampleImagesInfo(javbusDataItem);
        }

    }

    private void pushMagnentInfo(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            String magnetStrs = javbusDataItem.toPrettyMagnetStrs();
            SendMessage magnetMessage = new SendMessage();
            magnetMessage.setChatId(javbusDataItem.getMessageChatId());
            magnetMessage.setText(magnetStrs);
            magnetMessage.enableMarkdown(false);
            executeAsync(magnetMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logging.info("磁力信息推送完成： " + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logging.info("推送磁力信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logging.info("推送磁力信息出现异常：" + e.getMessage());
            // try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            // pushMagnentInfo(javbusDataItem);
        }
    }
}
