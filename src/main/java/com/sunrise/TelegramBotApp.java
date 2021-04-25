package com.sunrise;

import com.sunrise.spider.JobExcutor;
import com.sunrise.spider.SpiderJob;
import com.sunrise.tgbot.ReplyMessageBot;
import com.sunrise.tgbot.JavbusInfoPushBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.sunrise.tgbot.TgBotConfig.*;

/**
 * Hello world!
 *
 * @author sunrise
 */
public class TelegramBotApp {



    public static void main(String[] args) {
        try {
            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Set up Http proxy
            DefaultBotOptions botOptions = new DefaultBotOptions();

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);


            // Register your newly created AbilityBot
            ReplyMessageBot replyMessageBot = new ReplyMessageBot(botOptions);
            JavbusInfoPushBot pushInfoBot = new JavbusInfoPushBot(botOptions);


            //"KAWD-552", "KAWD-563", "KAWD-573", "KAWD-774", "KAWD-692"
            //List<String> strings = Arrays.asList("mide-433");

            //List<String> strings = JavbusHelper.getStarAllCodeNrFanHao("https://www.nrfanhao.com/nvyou/yingyouluo.html");
            //List<String> strings = JavbusHelper.getStarAllCodeNrFanHao("https://www.nrfanhao.com/nvyou/kuisi.html");

            //List<SpiderJob> spiderJobs = strings.stream()
            //        .map(e -> new SpiderJob(e, JobExcutor.concurrentLinkedDeque))
            //        .collect(Collectors.toList());
            //spiderJobs.forEach(spiderJob -> {
            //    JobExcutor.doSpiderJob(spiderJob);
            //});

            botsApi.registerBot(replyMessageBot);
            botsApi.registerBot(pushInfoBot);



        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
