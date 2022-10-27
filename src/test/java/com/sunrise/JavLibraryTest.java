package com.sunrise;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sunrise.javbusbot.spider.JavbusSpider.getJavLibraryReqHeader;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : JavLibraryTest
 * @software: IntelliJ IDEA
 * @time : 2022/10/27 8:45 PM
 */

public class JavLibraryTest {
    public static void main(String[] args) {
        String queryUrl = "https://www.javlibrary.com/cn/star_mostfav.php";
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS);

        okHttpClient = builder.build();
        Request request = new Request.Builder().url(queryUrl).get().headers(Headers.of(getJavLibraryReqHeader(queryUrl))).build();

        List<String> results = Collections.emptyList();
        try (Response response = okHttpClient.newCall(request).execute(); ResponseBody responseBody = response.body()) {
            if (response.code() != 200) {
                System.out.println("当前查询失败: " + response.request().url());
                return;
            }
            String result = Objects.requireNonNull(responseBody).string();
            System.out.println(result);
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

            String join = String.join("\n", results);
            System.out.println(join);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
