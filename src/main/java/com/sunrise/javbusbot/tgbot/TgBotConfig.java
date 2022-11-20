package com.sunrise.javbusbot.tgbot;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/11 5:01 AM
 */
public class TgBotConfig {
    private static final Logger logger = LoggerFactory.getLogger(TgBotConfig.class);
    /**
     * 自动回答bot的bot name
     */
    public static String REPLY_BOT_NAME;

    /**
     * 自动回答机器人的bot token
     */
    public static String REPLY_BOT_TOKEN;

    /**
     * javbus bot的名字
     */
    public static String JAVBUS_BOT_NAME;

    /**
     * javbus bot token,在tg的bot father上申请
     */
    public static String JAVBUS_BOT_TOKEN;


    /**
     * 代理地址
     */
    public static String PROXY_HOST;

    /**
     * 代理端口
     */
    public static Integer PROXY_PORT;
    /**
     * 是否开启代理
     */
    public static boolean ENABLE_PROXY;
    /**
     * 代理用户
     */
    public static String PROXY_USER;
    /**
     * 代理用户密码
     */
    public static String PROXY_PASS;
    /**
     * telegram Bot 是否使用代理
     */
    public static boolean ENABLE_TG_PROXY;

    /**
     * mongodb 链接
     */
    public static String MONGO_DB_URL;
    /**
     * sqlite链接
     */
    public static String SQLITE_DB_PATH;

    /**
     * javdb 链接
     */
    public static String JAVDB_BASE_URL;

    /**
     * 日本作品起始url
     */
    public static String SPIDER_BASE_URL;

    /**
     * 外国作品起始url
     */
    public static String SPIDER_FORGIEN_BASE_URL;

    /**
     * 是否开启消息转发到其他chat id
     */
    public static boolean FORWARD_MESSAGE_OPTION;


    /**
     * 转发到其他chat 的chat id
     */
    public static String FORWARD_MESSAGE_OPTION_CHATID;

    /**
     * 需要转发到channel的name
     */
    public static String FORWARD_MESSAGE_OPTION_CHANNEL_NAME;


    // 获取Bot Token的chat ID
    static {
        Properties properties = new Properties();
        try {
            properties.load(TgBotConfig.class.getClassLoader().getResourceAsStream("setting.properties"));
            REPLY_BOT_NAME = properties.getProperty("REPLY_BOT_NAME");
            REPLY_BOT_TOKEN = properties.getProperty("REPLY_BOT_TOKEN");
            // 优先使用环境变量
            String javbusBotName = System.getenv("JAVBUS_BOT_NAME");
            String javbusBotToken = System.getenv("JAVBUS_BOT_TOKEN");
            if (Strings.isNullOrEmpty(javbusBotName)) {
                logger.info("JAVBUS_BOT_NAME env not exist,use setting profile as default");
                JAVBUS_BOT_NAME = properties.getProperty("JAVBUS_BOT_NAME");
            } else {
                logger.info("JAVBUS_BOT_NAME env exist, use env value!");
                JAVBUS_BOT_NAME = javbusBotName;
            }
            if (Strings.isNullOrEmpty(javbusBotToken)) {
                logger.info("JAVBUS_BOT_TOKEN env not exist,use setting profile as default");
                JAVBUS_BOT_TOKEN = properties.getProperty("JAVBUS_BOT_TOKEN");

            } else {
                logger.info("JAVBUS_BOT_TOKEN env exist, use env value!");
                JAVBUS_BOT_TOKEN = javbusBotToken;
            }
            PROXY_HOST = properties.getProperty("PROXY_HOST");
            PROXY_PORT = Integer.parseInt(properties.getProperty("PROXY_PORT"));
            ENABLE_PROXY = Boolean.parseBoolean(properties.getProperty("ENABLE_PROXY"));
            ENABLE_TG_PROXY = Boolean.parseBoolean(properties.getProperty("ENABLE_TG_PROXY"));

            PROXY_USER = properties.getProperty("PROXY_USER");
            PROXY_PASS = properties.getProperty("PROXY_PASS");
            MONGO_DB_URL = properties.getProperty("MONGO_DB_URL");
            SQLITE_DB_PATH = properties.getProperty("SQLITE_DB_PATH");
            JAVDB_BASE_URL = properties.getProperty("JAVDB_BASE_URL");
            SPIDER_BASE_URL = properties.getProperty("SPIDER_BASE_URL");
            SPIDER_FORGIEN_BASE_URL = properties.getProperty("SPIDER_FORGIEN_BASE_URL");
            FORWARD_MESSAGE_OPTION = Boolean.parseBoolean(properties.getProperty("FORWARD_MESSAGE_OPTION"));
            FORWARD_MESSAGE_OPTION_CHATID = properties.getProperty("FORWARD_MESSAGE_OPTION_CHATID");
            FORWARD_MESSAGE_OPTION_CHANNEL_NAME = properties.getProperty("FORWARD_MESSAGE_OPTION_CHANNEL_NAME");

            if (FORWARD_MESSAGE_OPTION) {
                // use channel name to get chat id and set it as default chat_id
                String channelChatId = TgBotHelper.getChannelChatId(JAVBUS_BOT_TOKEN, FORWARD_MESSAGE_OPTION_CHANNEL_NAME);
                Objects.requireNonNull(channelChatId, "channelId 不能为空");
                FORWARD_MESSAGE_OPTION_CHATID = channelChatId;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(TgBotConfig.class.getClassLoader().getResourceAsStream("setting.properties"));
            System.out.println(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
