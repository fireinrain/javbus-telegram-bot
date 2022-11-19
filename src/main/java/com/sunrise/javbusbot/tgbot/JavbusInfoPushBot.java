package com.sunrise.javbusbot.tgbot;

import com.google.common.base.Strings;
import com.sunrise.javbusbot.spider.*;
import com.sunrise.javbusbot.storege.QueryHistoryEntity;
import com.sunrise.javbusbot.storege.QueryStaticEntity;
import com.sunrise.javbusbot.storege.SqliteDbManager;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.sunrise.javbusbot.spider.JavbusSpider.getJavLibraryReqHeader;
import static com.sunrise.javbusbot.spider.JavbusSpider.getJavdbSearchReqHeader;

/**
 * @description: tg bot 信息推送
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 12:55 PM
 */
public class JavbusInfoPushBot extends TelegramLongPollingBot {
    public static final Logger logger = LoggerFactory.getLogger(JavbusInfoPushBot.class);

    // TODO chatid 有状态 如果不同的频道使用，那么会导致消息错乱
    // 预留给forward功能
    public static String chatId = "";

    // sqlite db
    public static Connection sqliteConnect = SqliteDbManager.getConnection();

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
        this.recordQueryHistory(update);
        if (update.hasEditedMessage()) {
            logger.info("----------------------> recieve message from bot place");
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
            logger.info("----------------------> recieve message from bot place");
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
            logger.info("----------------------> recieve message from channel place");
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
     * 记录查询统计
     *
     * @param update
     */
    private void recordQueryHistory(Update update) {
        if (update.hasEditedMessage()) {
            QueryHistoryEntity historyEntity = new QueryHistoryEntity();
            String text = update.getEditedMessage().getText();
            historyEntity.setQueryText(text);
            String[] query = text.split(" ");
            if (query.length >= 2) {
                historyEntity.setQueryCommand(query[0]);
                historyEntity.setQueryStr(query[1]);
                SqliteDbManager.insertQueryHistory(historyEntity);
            }
            return;
        }
        if (update.hasMessage()) {
            QueryHistoryEntity historyEntity = new QueryHistoryEntity();
            String text = update.getMessage().getText();
            historyEntity.setQueryText(text);
            String[] query = text.split(" ");
            if (query.length >= 2) {
                historyEntity.setQueryCommand(query[0]);
                historyEntity.setQueryStr(query[1]);
                SqliteDbManager.insertQueryHistory(historyEntity);
            }
            return;
        }
        if (update.hasChannelPost()) {
            QueryHistoryEntity historyEntity = new QueryHistoryEntity();
            String text = update.getChannelPost().getText();
            historyEntity.setQueryText(text);
            String[] query = text.split(" ");
            if (query.length >= 2) {
                historyEntity.setQueryCommand(query[0]);
                historyEntity.setQueryStr(query[1]);
                SqliteDbManager.insertQueryHistory(historyEntity);
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
        builder.append("-------------------------------------------\n");
        for (int i = 1; i <= starNames.size(); i++) {
            builder.append(i + ". " + starNames.get(i - 1) + "\n");
        }
        builder.append("-------------------------------------------\n");
        builder.append("请选择需要查询的演员，并重新输入命令(/command 序号-xxxx)来选择查询. /command 指你当前输入的查询命令. eg: /command 1-樱空桃");
        builder.append("如果输入关键字获得的模糊搜索无法获得结果，可以输入/command 1-你输入的关键字, 可以强制进行关键字查询");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(messageChatId);
        sendMessage.setText(builder.toString());
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdown(false);
        sendMessage.enableNotification();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("发送消息失败: " + e.getMessage());
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
            logger.warn("发送等待消息失败: " + e.getMessage());
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
        // 统计
        if (text.trim().startsWith("/states all")) {
            QueryStaticEntity queryStatic = SqliteDbManager.getQueryStatic();
            SendMessage message = new SendMessage();
            message.setChatId(messageChatId);
            message.setText(queryStatic.getPrettyTgMessage());
            try {
                // Call method to send the message
                executeAsync(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        // trending 数据来自javlibrary
        if (text.trim().startsWith("/trending")) {
            String[] strings = text.split(" ");
            if (strings.length == 2) {
                if (strings[1].trim().equals("star")) {
                    // 触发演员trending
                    this.pushTrendingStarInfo(messageChatId);
                    return;
                }
                if (strings[1].trim().equals("film")) {
                    // 触发作品trending
                    this.pushTrendingFilmInfo(messageChatId);
                    return;
                }
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(messageChatId);
                message.setText(text + " 人家无法识别命令啦,请重新输入(/trending xxxx)" + "❤️❤️❤️");

                try {
                    // Call method to send the message
                    executeAsync(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if (text.trim().startsWith("/code")) {
            String[] strings = text.split(" ");
            if (strings.length == 2) {
                SpiderJob.trigerJavbusCodeTask(JavbusHelper.normalCode(strings[1].trim()), messageChatId);
                logger.info("触发推Javbus任务, 查询 " + strings[1]);
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
                String queryStr = queryStrs[1].trim();
                String[] split = queryStr.split("-");
                if (split.length == 2 && isNumeric(split[0])) {
                    // 直接查询 不再进行javdb 演员名字查询
                    JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                    javbusDataItem.setMessageChatId(messageChatId);
                    List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                    return;
                }
                // 提示必须要两个字符
                boolean fixLength = this.warnQueryStrFixLength(queryStrs[1].trim(), messageChatId);
                if (fixLength) {
                    return;
                }
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
                logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
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
                String queryStr = queryStrs[1].trim();
                String[] split = queryStr.split("-");
                if (split.length == 2 && isNumeric(split[0])) {
                    // 直接查询 不再进行javdb 演员名字查询
                    JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                    javbusDataItem.setMessageChatId(messageChatId);
                    List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                    return;
                }
                // 提示必须要两个字符
                boolean fixLength = this.warnQueryStrFixLength(queryStrs[1].trim(), messageChatId);
                if (fixLength) {
                    return;
                }
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
                logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
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
                    String queryStr = strings[1].trim();
                    String[] split = queryStr.split("-");
                    if (split.length == 2 && isNumeric(split[0])) {
                        // 直接查询 不再进行javdb 演员名字查询
                        JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                        javbusDataItem.setMessageChatId(messageChatId);
                        List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                        StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                        logger.info("触发推StarJavbus任务, 查询 " + strings[1]);
                        return;
                    }
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(strings[1].trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询所有" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replaceAll("/starall", "").trim();
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(starName.trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameAll(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询所有" + starName);
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
                    String queryStr = strings[1].trim();
                    String[] split = queryStr.split("-");
                    if (split.length == 2 && isNumeric(split[0])) {
                        // 直接查询 不再进行javdb 演员名字查询
                        JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                        javbusDataItem.setMessageChatId(messageChatId);
                        List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                        StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                        logger.info("触发推StarJavbus任务, 查询 " + strings[1]);
                        return;
                    }
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(strings[1].trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询所有含有磁力" + strings[1]);
                    return;
                }
                if (strings.length >= 3) {
                    String starName = text.replace("/starmag", "").trim();
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(starName.trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchAllFilmsInfoByNameHasMagnent(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询所有含有磁力" + starName);
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
                    String queryStr = strings[1].trim();
                    String[] split = queryStr.split("-");
                    if (split.length == 2 && isNumeric(split[0])) {
                        // 直接查询 不再进行javdb 演员名字查询
                        JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                        javbusDataItem.setMessageChatId(messageChatId);
                        List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                        StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                        logger.info("触发推StarJavbus任务, 查询 " + strings[1]);
                        return;
                    }
                    logger.info("触发推InfoJavbus任务, 查询个人信息" + strings[1]);
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(strings[1].trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    // 获取演员的正确名字
                    List<String> starNames = this.fixStarName(strings[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    JavbusStarInfoItem javbusStarInfoItem = JavbusSpider.fetchStarInfoByName(starNames.get(0));
                    javbusStarInfoItem.setMessageChatId(messageChatId);
                    StartInfoSpiderJob.trigerStarInfoJob(javbusStarInfoItem);
                    return;
                }

                if (strings.length >= 3) {
                    String starName = text.replace("/starinfo", "").trim();
                    logger.info("触发推InfoJavbus任务, 查询个人信息" + starName);
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(starName.trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
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
                    String queryStr = queryStrs[1].trim();
                    String[] split = queryStr.split("-");
                    if (split.length == 2 && isNumeric(split[0])) {
                        // 直接查询 不再进行javdb 演员名字查询
                        JavbusDataItem javbusDataItem = JavbusSpider.fetchLatestFilmInfoByName(split[1].trim());
                        javbusDataItem.setMessageChatId(messageChatId);
                        List<JavbusDataItem> javbusDataItems = Collections.singletonList(javbusDataItem);
                        StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                        logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                        return;
                    }
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(queryStrs[1].trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    List<String> starNames = this.fixStarName(queryStrs[1].trim());
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询 " + queryStrs[1]);
                    return;
                }

                if (queryStrs.length >= 3) {
                    String starName = text.replaceAll("/star", "").trim();
                    // 提示必须要两个字符
                    boolean fixLength = this.warnQueryStrFixLength(starName.trim(), messageChatId);
                    if (fixLength) {
                        return;
                    }
                    List<String> starNames = this.fixStarName(starName);
                    if (starNames.size() > 1) {
                        this.sendStarNameList(starNames, messageChatId);
                        return;
                    }
                    List<JavbusDataItem> javbusDataItems = JavbusSpider.fetchFilmsInfoByName(starNames.get(0));
                    javbusDataItems.forEach(e -> e.setMessageChatId(messageChatId));
                    StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
                    logger.info("触发推StarJavbus任务, 查询 " + starName);
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
        logger.info(TgBotConfig.JAVBUS_BOT_NAME + " 收到消息： " + text);
        // Create a SendMessage object with mandatory fields
        SendMessage message = new SendMessage();
        message.setChatId(messageChatId);
        message.setText("人家不认识这个指令： " + text + " " + "\uD83D\uDE35\uD83D\uDE35\uD83D\uDE35");

        try {
            // Call method to send the message
            executeAsync(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送热门作品排行榜
     *
     * @param messageChatId
     */
    private void pushTrendingFilmInfo(String messageChatId) {
        logger.info("正在获取JavLibrary 热门女优排行榜......");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                    OkHttpClient okHttpClient;
                    OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                            .addInterceptor(new RetryInterceptor(2))
                            // 连接超时
                            .connectTimeout(60 * 6, TimeUnit.SECONDS)
                            // 读取超时
                            .readTimeout(60 * 6, TimeUnit.SECONDS)
                            // 写超时
                            .writeTimeout(60 * 6, TimeUnit.SECONDS);
                    okHttpClient = builder.build();
            String queryUrl = "https://www.javlibrary.com/tw/vl_bestrated.php?list&mode=&page=1";
            Request request = new Request.Builder().url(queryUrl).get().headers(Headers.of(getJavLibraryReqHeader(queryUrl))).build();
                    List<String> results = new ArrayList<>();
                    String filmTrendStr = "";
                    try (Response response = okHttpClient.newCall(request).execute(); ResponseBody responseBody = response.body()) {
                        if (response.code() != 200) {
                            System.out.println("当前查询失败: " + response.request().url());
                            return filmTrendStr;
                        }
                        String result = Objects.requireNonNull(responseBody).string();
                        Document document = Jsoup.parse(result);
                        Elements elements = document.selectXpath("//*[@id=\"rightcolumn\"]/table[2]/tbody/tr");
                        for (int i = 1; i < elements.size(); i++) {
                            Element element = elements.get(i);
                            Elements aTags = element.select("a");
                            String filmName = aTags.get(aTags.size() - 1).text();
                            int spaceIdex = filmName.indexOf(" ");
                            // 标题
                            String substring = filmName.substring(spaceIdex);
                            // 番号
                            String code = filmName.substring(0, spaceIdex);
                            Elements tdTags = element.select("td");
                            // 评分
                            String score = tdTags.get(tdTags.size() - 1).text();
                            String line = "#" + i + " " + score + " " + code + " " + substring;
                            results.add(line);
                        }
                        filmTrendStr = String.join("\n", results);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("访问javlibrary出现错误: " + e.getMessage());
                    }
                    return filmTrendStr;
                }
        ).whenComplete((result, error) -> {
            if (!Strings.isNullOrEmpty(result)) {
                StringBuilder builder = new StringBuilder();
                builder.append("已为您找到最新热门影片(序号-评分-番号-标题):\n");
                builder.append("-------------------------------------------\n");
                builder.append(result + "\n");
                builder.append("-------------------------------------------\n");
                builder.append("#trending ");
                builder.append("#film ");
                builder.append(" " + LocalDate.now() + " |");
                builder.append("数据来自: Javlibrary");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(messageChatId);
                sendMessage.setParseMode("html");
                sendMessage.setText(builder.toString());
                try {
                    executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(messageChatId);
                sendMessage.setParseMode("html");
                sendMessage.setText("很抱歉,该查询功能暂时无法为您提供服务,请尝试向管理员反馈.");
                try {
                    executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            logger.error("当前推送trending film 出现错误: " + e.getMessage());
            return "";
        });
        completableFuture.join();
    }

    /**
     * 推送热门演员排行榜
     *
     * @param messageChatId
     */
    private void pushTrendingStarInfo(String messageChatId) {
        logger.info("正在获取JavLibrary 热门女优排行榜......");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                    OkHttpClient okHttpClient;
                    OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                            .addInterceptor(new RetryInterceptor(2))
                            // 连接超时
                            .connectTimeout(60 * 6, TimeUnit.SECONDS)
                            // 读取超时
                            .readTimeout(60 * 6, TimeUnit.SECONDS)
                            // 写超时
                            .writeTimeout(60 * 6, TimeUnit.SECONDS);
                    okHttpClient = builder.build();
            String queryUrl = "https://www.javlibrary.com/tw/star_mostfav.php";
            Request request = new Request.Builder().url(queryUrl).get().headers(Headers.of(getJavLibraryReqHeader(queryUrl))).build();
                    List<String> results = Collections.emptyList();
                    String starTrendStr = "";
                    try (Response response = okHttpClient.newCall(request).execute(); ResponseBody responseBody = response.body()) {
                        if (response.code() != 200) {
                            System.out.println("当前查询失败: " + response.request().url());
                            return starTrendStr;
                        }
                        String result = Objects.requireNonNull(responseBody).string();
                        Document document = Jsoup.parse(result);
                        Elements elements = document.selectXpath("//*[@class=\"searchitem\"]");
                        results = elements.stream().map(e -> {
                            String text = e.text();
                            if (!text.contains("▲") && !text.contains("▼")) {
                                String[] temp = text.split(" ");
                                StringBuilder n = new StringBuilder();
                                n.append(temp[0]);
                                n.append(" ▬ ");
                                n.append(temp[1]);
                                return n.toString();
                            }
                            return text;
                        }).collect(Collectors.toList());
                        starTrendStr = String.join("\n", results);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("访问javlibrary出现错误: " + e.getMessage());
                    }
                    return starTrendStr;
                }
        ).whenComplete((result, error) -> {
            if (!Strings.isNullOrEmpty(result)) {
                StringBuilder builder = new StringBuilder();
                builder.append("已为您找到当月最新热门演员:\n");
                builder.append("-------------------------------------------\n");
                builder.append(result + "\n");
                builder.append("-------------------------------------------\n");
                builder.append("#trending ");
                builder.append("#star ");
                builder.append(" " + LocalDate.now() + " |");
                builder.append("数据来自: Javlibrary");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(messageChatId);
                sendMessage.setParseMode("html");
                sendMessage.setText(builder.toString());
                try {
                    executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(messageChatId);
                sendMessage.setParseMode("html");
                sendMessage.setText("很抱歉,该查询功能暂时无法为您提供服务,请尝试向管理员反馈.");
                try {
                    executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            logger.error("当前推送trending star 出现错误: " + e.getMessage());
            return "";
        });
        completableFuture.join();
    }


    @Override
    public void onRegister() {
        super.onRegister();
        JobExcutor.doTgJob(() -> this.startJavbusPushTask());
        JobExcutor.doDelayPushImgJob(() -> this.startDelaySamplePushJob());
        JobExcutor.doJavbusStarInfoItemJob(() -> this.startJavbusStarInfoItemPushTask());
    }

    /**
     * 提示最短查询字符长度
     *
     * @param queryStr
     * @param messageChatId
     * @return
     */
    private boolean warnQueryStrFixLength(String queryStr, String messageChatId) {
        if (queryStr.length() < 2) {
            SendMessage message = new SendMessage();
            message.setChatId(messageChatId);
            message.setText("亲, 模糊查询最少要两个字符长度： " + queryStr + " " + "\uD83D\uDE35\uD83D\uDE35\uD83D\uDE35");
            try {
                // Call method to send the message
                executeAsync(message);
                return true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 演员任务循环
     */
    public void startJavbusStarInfoItemPushTask() {
        ConcurrentLinkedDeque<JavbusStarInfoItem> linkedDeque = JobExcutor.javbusStarInfoItemConcurrentLinkedDeque;

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

    /**
     * 推送任务循环
     */
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
                    logger.info("--------------------------------当前爬虫数据已经推送完毕--------------------------------");
                }
                TimeUnit.SECONDS.sleep(5);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("--------------------------------睡眠5秒--------------------------------" + System.currentTimeMillis());
                logger.debug("--------------------------------当前还有" + linkedDeque.size() + "个任务没有被推入执行器--------------------------------");
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
                        executeAsync(sendMessage).whenCompleteAsync((message, throwable) -> logger.info("推送简介完成：" + javbusDataItem.getCode()));
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
                        executeAsync(magnetMessage).whenCompleteAsync((message, throwable) -> logger.info("推送磁力链接完成：" + javbusDataItem.getCode()));
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
                                            logger.warn("当前请求响应失败");
                                        }
                                        exception.printStackTrace();
                                        logger.error("当前请求地址: " + request.url());
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
                            executeAsync(sendMediaGroup).whenCompleteAsync((message, throwable) -> logger.info("推送样品图完成：" + javbusDataItem.getCode()));
                        }

                    }
                    return new Message();
                }).exceptionally(throwable -> {
                    logger.info("推送样品图出现异常：" + throwable.getMessage());
                    return null;
                });

                CompletableFuture<Void> all = CompletableFuture.allOf(stage1, stage2, stage3);
                // 等待所有任务完成
                all.join();

            } catch (Exception e) {
                // e.printStackTrace();
                logger.info("推送作品信息异常：" + e.getMessage());
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
                logger.info("延迟队列到期，正在处理中：" + javbusDataItem.getCode());

                pushSampleImagesInfo(javbusDataItem);


            } catch (InterruptedException | TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 推送演员信息
     *
     * @param javbusStarInfoItem
     */
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
                    logger.info("个人信息推送完成： " + javbusStarInfoItem.getStarName());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logger.info("推送个人信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("推送个人信息出现异常：" + e.getMessage());
        }
    }

    /**
     * 推送个人信息任务
     */
    class JavbusStarInfoItemJob implements Runnable {

        private JavbusStarInfoItem javbusStarInfoItem;

        public JavbusStarInfoItemJob(JavbusStarInfoItem javbusStarInfoItem) {
            this.javbusStarInfoItem = javbusStarInfoItem;
        }

        public void pushNotFoundResult() {
            try {
                pushCodeNotFundMsg(this.javbusStarInfoItem);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                if (null == this.javbusStarInfoItem.getStarName() || "".equals(this.javbusStarInfoItem.getStarName())) {
                    pushNotFoundResult();
                    return;
                }
                pushJavbusStarInfoItem(javbusStarInfoItem);
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
                logger.info("当前作品地址为: " + javbusDataItem.getVisitUrl());
                pushIntroduceInfo(javbusDataItem);
                pushVideoPreview(javbusDataItem);
                pushMagnentInfo(javbusDataItem);
                logger.info("正在推送样品图延迟任务: " + javbusDataItem.getCode());
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
        logger.info("查询演员名字: " + starName);
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
                logger.warn("当前查询失败: " + response.request().url());
                return results;
            }
            String result = Objects.requireNonNull(responseBody).string();
            // System.out.println(result);
            Document document = Jsoup.parse(result);
            Elements elements = document.selectXpath("//*[@id=\"actors\"]/div/a");
            results = elements.stream().map(e -> {
                String name = "";
                String title = e.attr("title");
                String[] split = title.split(", ");
                if (split.length >= 2) {
                    name = split[split.length - 1];
                } else {
                    name = split[0];
                }
                return name;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("当前查询出现错误: " + e.getMessage());
        }
        results = new ArrayList<>(new HashSet<>(results));
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
                if (Strings.isNullOrEmpty(videoPreviewUrl)) {
                    logger.warn("无法找到预览视频链接: " + javbusDataItem.getVisitUrl());
                    Object[] objects = new Object[3];
                    objects[0] = null;
                    objects[1] = null;
                    objects[2] = null;
                    return objects;
                }
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
                    logger.info("开始请求视频地址: " + request.url());
                    execute = client.newCall(request).execute();
                    body = execute.body();
                } catch (IOException exception) {
                    if (null != body) {
                        body.close();
                        logger.warn("当前请求响应失败");
                    }
                    exception.printStackTrace();
                    logger.error("当前请求地址: " + request.url());
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
                        logger.warn("当前请求响应失败");
                    }
                    e.printStackTrace();
                    logger.error("当前请求地址: " + request.url());
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
                logger.warn("当前视频预览请求失败,已跳过");
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
                logger.warn("预览视频大于50MB,无法使用Bot发送");
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
                logger.info("推送预览视频完成：" + javbusDataItem.getCode());
            }).exceptionally(throwable -> {
                logger.info("推送预览视频CompleteFuture出现异常：" + throwable.getMessage());
                // 尝试重新加入延迟队列的最末端
                logger.info("正在尝试重新加入延迟队列......");
                if (javbusDataItem.getFetchRetry() >= 1) {
                    logger.info("推送预览视频尝试次数超过限制(2次),丢弃：" + javbusDataItem.getCode());
                } else {
                    int fetchCount = javbusDataItem.getFetchRetry() + 1;
                    javbusDataItem.setFetchRetry(fetchCount);
                    JobExcutor.doTgJob(() -> this.pushVideoPreview(javbusDataItem));
                }
                return null;
            });
            messageFuture.join();
            logger.info("推送视频预览成功: " + code);
        } catch (Exception e) {
            logger.error("推送视频预览Try出现异常：" + e.getMessage());
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
                                logger.info("开始请求图片地址: " + request.url());
                                execute = client.newCall(request).execute();
                                body = execute.body();
                            } catch (IOException exception) {
                                if (null != body) {
                                    body.close();
                                    logger.warn("当前请求响应失败");
                                }
                                exception.printStackTrace();
                                logger.error("当前请求地址: " + request.url());
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
                            logger.warn("当前样品图片请求失败,已跳过");
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
                        logger.info("推送样品图完成：" + javbusDataItem.getCode());
                    }).exceptionally(throwable -> {
                        logger.info("推送样品图CompleteFuture出现异常：" + throwable.getMessage());
                        // 尝试重新加入延迟队列的最末端
                        logger.info("正在尝试重新加入延迟队列......");
                        if (javbusDataItem.getFetchRetry() >= 2) {
                            logger.info("推送样品图尝试次数超过限制(3次),丢弃：" + javbusDataItem.getCode());
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
            logger.info("推送样品图Try出现异常：" + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 推送演员无法查找到信息
     *
     * @param javbusStarInfoItem
     * @throws TelegramApiException
     */
    private void pushCodeNotFundMsg(JavbusStarInfoItem javbusStarInfoItem) throws TelegramApiException {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(javbusStarInfoItem.getMessageChatId());
            sendMessage.setText("对不起,该查询未找到!\uD83D\uDE37\uD83D\uDE37\uD83D\uDE37");
            sendMessage.enableHtml(true);
            sendMessage.enableMarkdown(false);
            sendMessage.enableNotification();
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logger.info(javbusStarInfoItem.getStarName() + " 演员查询未找到结果,消息推送完毕");
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logger.info("推送演员未找到消息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("推送演员未找到消息出现异常：" + e.getMessage());
        }

    }

    /**
     * 推送番号无法找到信息
     *
     * @param javbusDataItem
     * @throws TelegramApiException
     */
    private void pushCodeNotFundMsg(JavbusDataItem javbusDataItem) throws TelegramApiException {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(javbusDataItem.getMessageChatId());
            sendMessage.setText("对不起,该查询未找到!\uD83D\uDE37\uD83D\uDE37\uD83D\uDE37");
            sendMessage.enableHtml(true);
            sendMessage.enableMarkdown(false);
            sendMessage.enableNotification();
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    logger.info(javbusDataItem.getCode() + " 番号查询未找到结果,消息推送完毕");
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logger.info("推送番号未找到消息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("推送番号未找到消息出现异常：" + e.getMessage());
        }

    }

    /**
     * 推送作品简介信息
     *
     * @param javbusDataItem
     * @throws TelegramApiException
     */
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
                    logger.info("推送简介完成：" + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logger.info("推送简介出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("推送简介出现异常：" + e.getMessage());
            e.printStackTrace();
            // try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            // pushSampleImagesInfo(javbusDataItem);
        }

    }

    /**
     * 推送磁力链接信息
     *
     * @param javbusDataItem
     * @throws TelegramApiException
     */
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
                    logger.info("磁力信息推送完成： " + javbusDataItem.getCode());
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    logger.info("推送磁力信息出现异常：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            logger.info("推送磁力信息出现异常：" + e.getMessage());
            // try {
            //    Thread.sleep(2000);
            //} catch (InterruptedException interruptedException) {
            //    interruptedException.printStackTrace();
            //}
            // pushMagnentInfo(javbusDataItem);
        }
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str
     * @return bool
     */
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
