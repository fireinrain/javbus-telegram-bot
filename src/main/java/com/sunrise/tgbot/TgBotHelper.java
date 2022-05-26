package com.sunrise.tgbot;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 3:25 PM
 */
public class TgBotHelper {
    /**
     * 怎样获取private chatid？
     * 先将channle 设置为public 然后发送请求url 解析相应 然后再把channel设置
     * 回private 你就可以继续使用这个chatid 了
     * @return
     */
    public static String getChatId(){
        return null;

    }

    public static void main(String[] args) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String channelName = "@sunrisechannel_8888";
        String chatId = "-1001371132897";
        String url = "https://api.telegram.org/bot"+TgBotConfig.REPLY_BOT_TOKEN +
                "/sendMessage?chat_id="+chatId+"&text="+"hello";

        String getUpdate = "https://api.telegram.org/bot"+TgBotConfig.REPLY_BOT_TOKEN +"/getMe";
        //发送消息给特定的频道
        final Request request = new Request.Builder()
                .url(getUpdate)
                .addHeader("Accept","*/*")
                .addHeader("Accept-Encoding","gzip, deflate")
                .addHeader("Accept-Language","zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6")
                .addHeader("Cache-Control","no-cache")
                .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println(response.toString());
                System.out.println(response.body().string());
            }
        });
    }

}
