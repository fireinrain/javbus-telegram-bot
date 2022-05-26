package com.sunrise.tgbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 3:25 PM
 */
public class TgBotHelper {
    public static final Logger logging = LoggerFactory.getLogger(TgBotHelper.class);

    /**
     * 怎样获取private chatid？
     * 先将channle 设置为public 然后发送请求url 解析相应 然后再把channel设置
     * 回private 你就可以继续使用这个chatid 了
     *
     * @return
     */


    /**
     * 获取当前bottoken的chat id
     * @param botToken
     * @return
     */
    public static String getChatId(String botToken) {
        final String[] chatId = new String[1];
        OkHttpClient okHttpClient = new OkHttpClient();
        String getUpdate = "https://api.telegram.org/bot" + botToken + "/getMe";
        // 发送消息给特定的频道
        final Request request = new Request.Builder().url(getUpdate).addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String string = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                TgBotTokenResponse tgBotTokenResponse = mapper.readValue(string, TgBotTokenResponse.class);

                String s = String.valueOf(tgBotTokenResponse.getResult().getId());
                chatId[0] = s;
                logging.info("chat id: {}", s);
            }
        });
        try {
            //等待请求结果返回 并设置值 返回
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return chatId[0];

    }

    public static String getChannelChatId(String botToken, String channleName) {
        if (!channleName.startsWith("@")) {
            // do not append @header
            channleName = "@" + channleName;
        }
        final String[] chatId = new String[1];
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        // 发送消息给特定的频道
        RequestBody formBody = new FormBody.Builder()
                .add("chat_id", channleName)
                .add("text", ">>>request for channel chat id")
                .build();
        final Request request = new Request.Builder().url(url).post(formBody).addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String string = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                TgBotMessageSendResponse TgBotMessageSendResponse = mapper.readValue(string, TgBotMessageSendResponse.class);

                String s = String.valueOf(TgBotMessageSendResponse.getResult().getChat().getId());
                chatId[0] = s;
                logging.info("chat id: {}", s);
            }
        });
        try {
            // 等待请求结果返回 并设置值 返回
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return chatId[0];

    }

    public static void main(String[] args) {
        // OkHttpClient okHttpClient = new OkHttpClient();
        // String channelName = "@sunrisechannel_8888";
        // String chatId = "-1001371132897";
        // String url = "https://api.telegram.org/bot" + TgBotConfig.REPLY_BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + "hello";
        //
        // String getUpdate = "https://api.telegram.org/bot" + TgBotConfig.REPLY_BOT_TOKEN + "/getMe";
        // // 发送消息给特定的频道
        // final Request request = new Request.Builder().url(url).addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip, deflate").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36").build();
        //
        // okHttpClient.newCall(request).enqueue(new Callback() {
        //     @Override
        //     public void onFailure(@NotNull Call call, @NotNull IOException e) {
        //
        //     }
        //     @Override
        //     public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        //         logging.info(response.toString());
        //         logging.info(response.body().string());
        //     }
        // });

        // System.out.println(getChatId(TgBotConfig.JAVBUS_BOT_TOKEN));
        // System.out.println(getChannelChatId(TgBotConfig.JAVBUS_BOT_TOKEN, "sunrisechannel_8888"));
    }

}
