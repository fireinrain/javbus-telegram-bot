package com.sunrise;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.sunrise.javbusbot.spider.JavbusSpider.getJavLibraryReqHeader;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : JavLibraryTest
 * @software: IntelliJ IDEA
 * @time : 2022/10/27 8:45 PM
 */

public class JavLibraryTest2 {
    public static void main(String[] args) {
        String queryUrl = "https://www.javlibrary.com/cn/vl_bestrated.php?list&mode=&page=1";
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

                System.out.println(tdTags);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
