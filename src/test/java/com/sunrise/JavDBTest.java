package com.sunrise;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sunrise.javbusbot.spider.JavbusSpider.getJavdbSearchReqHeader;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : JavDBTest
 * @software: IntelliJ IDEA
 * @time : 2022/10/22 11:56 AM
 */

public class JavDBTest {
    public static void main(String[] args) {
        String queryStr = "三上";
        String queryUrl = "https://javdb.com/search?q=$s&f=actor";
        queryUrl = queryUrl.replace("$s", queryStr);
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS);

        okHttpClient = builder.build();
        Request request = new Request.Builder().url(queryUrl).get().headers(Headers.of(getJavdbSearchReqHeader(queryStr, ""))).build();


        try (Response response = okHttpClient.newCall(request).execute(); ResponseBody responseBody = response.body()) {
            if (response.code() != 200) {
                System.out.println("当前查询失败: " + response.request().url());
                return;
            }
            String result = Objects.requireNonNull(responseBody).string();
            System.out.println(result);
            Document document = Jsoup.parse(result);
            Elements elements = document.selectXpath("//*[@id=\"actors\"]/div/a/strong");
            List<String> strings = elements.stream().map(e -> e.text()).collect(Collectors.toList());
            System.out.println(strings);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
