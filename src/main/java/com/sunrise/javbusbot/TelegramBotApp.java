package com.sunrise.javbusbot;

import com.sunrise.javbusbot.storege.MongodbStorege;
import com.sunrise.javbusbot.tgbot.JavbusInfoPushBot;
import com.sunrise.javbusbot.tgbot.TgBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import static com.sunrise.javbusbot.tgbot.TgBotConfig.*;

/**
 * Main App
 *
 * @author sunrise
 */
public class TelegramBotApp {
    public static final TgBotConfig tgBotConfig = new TgBotConfig();

    public static final Logger logger = LoggerFactory.getLogger(TelegramBotApp.class);


    public static void main(String[] args) {
        try {
            // check mongodb is ok
            MongodbStorege.isMongoDatabaseAvailable();
            // Create the Authenticator that will return auth's parameters for proxy authentication
            if (TgBotConfig.ENABLE_TG_PROXY) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(PROXY_USER, PROXY_PASS.toCharArray());
                    }
                });
            }
            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Set up Http proxy
            DefaultBotOptions botOptions = new DefaultBotOptions();
            botOptions.setGetUpdatesTimeout(6 * 60);

            if (TgBotConfig.ENABLE_TG_PROXY) {
                botOptions.setProxyHost(PROXY_HOST);
                botOptions.setProxyPort(PROXY_PORT);

                // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
                botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            }

            // Register your newly created AbilityBot
            JavbusInfoPushBot pushInfoBot = new JavbusInfoPushBot(botOptions);

            //"KAWD-552", "KAWD-563", "KAWD-573", "KAWD-774", "KAWD-692"
            // List<String> strings = Arrays.asList("mide-433");
            // List<String> strings = JavbusHelper.getStarAllCodeNrFanHao("https://www.nrfanhao.com/nvyou/yingyouluo.html");
            // List<String> strings = JavbusHelper.getStarAllCodeNrFanHao("https://www.nrfanhao.com/nvyou/kuisi.html");
            // List<SpiderJob> spiderJobs = strings.stream()
            //        .map(e -> new SpiderJob(e, JobExcutor.concurrentLinkedDeque))
            //        .collect(Collectors.toList());
            // spiderJobs.forEach(spiderJob -> {
            //    JobExcutor.doSpiderJob(spiderJob);
            //});

            logger.info("Javbus Bot 已经启动");
            botsApi.registerBot(pushInfoBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
