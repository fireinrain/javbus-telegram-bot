package com.sunrise.javbusbot.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sunrise.javbusbot.tgbot.TgBotConfig;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: 爬虫类 实现爬取详情页 番号信息等
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 2:42 PM
 */
public class JavbusSpider {
    public static final Logger logging = LoggerFactory.getLogger(JavbusSpider.class);

    private static String proxyHost = TgBotConfig.PROXY_HOST;

    private static int proxyPort = TgBotConfig.PROXY_PORT;

    private static String baseUrl = TgBotConfig.SPIDER_BASE_URL;

    private static String foreignerBaseUrl = TgBotConfig.SPIDER_FORGIEN_BASE_URL;

    private static OkHttpClient okHttpClient;

    private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();


    static {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        okHttpClient = new OkHttpClient.Builder()
                .proxy(proxy)
                .retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS)
                .build();
    }

    public static JavbusStarInfoItem fetchStarInfoByName(String starName) {
        logging.info("正在查找信息：" + starName);
        JavbusStarInfoItem JavbusStarInfoItem = new JavbusStarInfoItem();
        List<JavbusDataItem> javbusDataItems = fetchFilmsInfoByName(starName);
        // 找到mainStarUrl为1的就是主演了
        if (null == javbusDataItems || javbusDataItems.size() <= 0) {
            return JavbusStarInfoItem;
        }
        JavbusDataItem javbusDataItem = javbusDataItems.stream()
                .filter(e -> null != e.getMainStarPageUrl())
                .findFirst().get();

        OkHttpClient okHttpClient = getCookiedOkHttpClient();

        String pageUrl = javbusDataItem.getMainStarPageUrl().getStartPageUrl();

        Request request = new Request.Builder()
                .url(pageUrl).get()
                .headers(Headers.of(getStarSearchReqHeader(pageUrl, true)))
                .build();

        Response execute = null;
        execute = getResponse(pageUrl, okHttpClient, request, execute);
        if (execute == null) {
            return null;
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execute.close();
        }
        logging.info("请求作品页，正在解析页面......");

        Document document = Jsoup.parse(result);

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        String allCounts = text1.trim().split(" ")[1].trim();
        JavbusStarInfoItem.setAllFilmNum(allCounts);

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnents = text2.trim().split(" ")[1].trim();
        JavbusStarInfoItem.setHasMagNum(haveMagnents);

        Elements elements = document.select("#waterfall");

        Element info = elements.get(0);

        Element infoEl = (Element) info.childNodes().get(1);

        Elements img = infoEl.select("img");
        String src = img.attr("src");
        JavbusStarInfoItem.setHeadPhoto(src);
        String title = img.attr("title");
        JavbusStarInfoItem.setStarName(title);

        Elements p = infoEl.select("p");

        for (Element element : p) {
            String text = element.text();
            String[] split = text.split(":");
            String key = split[0].trim();
            String value = split[1].trim();
            switch (key) {
                case "生日":
                    JavbusStarInfoItem.setBirthday(value);
                    break;
                case "年齡":
                    JavbusStarInfoItem.setAge(value);
                    break;
                case "身高":
                    JavbusStarInfoItem.setHeight(value);
                case "罩杯":
                    JavbusStarInfoItem.setCup(value);
                    break;
                case "胸圍":
                    JavbusStarInfoItem.setChestCircumference(value);
                    break;
                case "腰圍":
                    JavbusStarInfoItem.setWaistline(value);
                    break;
                case "臀圍":
                    JavbusStarInfoItem.setHips(value);
                    break;
                case "出生地":
                    JavbusStarInfoItem.setBirthPlace(value);
                    break;
                case "愛好":
                    JavbusStarInfoItem.setHobby(value);
                    break;
                default:
                    logging.info("无法抽取个人信息：" + key + " " + value);


            }
        }
        // logging.info(JavbusStarInfoItem.toPrettyStr());
        return JavbusStarInfoItem;
    }

    /**
     * 获取所有含有磁力链接的作品集合
     *
     * @param starName
     * @return
     */
    public static List<JavbusDataItem> fetchAllFilmsInfoByNameHasMagnent(String starName) {
        return fetchAllFilmsInfoByName(starName, true);
    }

    /**
     * 获取所有的作品集合
     *
     * @param starName
     * @return
     */
    public static List<JavbusDataItem> fetchAllFilmsInfoByNameAll(String starName) {
        return fetchAllFilmsInfoByName(starName, false);
    }

    /**
     * 获取所有作品信息
     *
     * @param starName
     * @param hasMagnentOrAll
     * @return
     */
    public static List<JavbusDataItem> fetchAllFilmsInfoByName(String starName, boolean hasMagnentOrAll) {
        String info = hasMagnentOrAll == true ? "(磁力)" : "";
        logging.info("正在查找： " + starName + "所有作品" + info + " ,请稍等......");
        List<JavbusDataItem> javbusDataItems = fetchFilmsInfoByName(starName);
        // 找到mainStarUrl为1的就是主演了
        if (null == javbusDataItems || javbusDataItems.size() <= 0) {
            return new ArrayList<>();
        }
        JavbusDataItem javbusDataItem = javbusDataItems.stream()
                .filter(e -> null != e.getMainStarPageUrl())
                .findFirst().get();

        logging.info("找到主演首页地址：" + javbusDataItem.getMainStarPageUrl());

        String[] filmsInfoByUrlPage = fetchFilmsCountsByUrlPage(javbusDataItem.getMainStarPageUrl().getStartPageUrl());

        List<JavbusDataItem> collects = null;
        // fetch has magnent films
        if (hasMagnentOrAll) {
            Integer hasMagnent = Integer.valueOf(filmsInfoByUrlPage[1]);
            return visitAllFilmsByPageNum(javbusDataItem, hasMagnent, true);
        }

        // fetch all
        Integer allFilms = Integer.valueOf(filmsInfoByUrlPage[0]);

        return visitAllFilmsByPageNum(javbusDataItem, allFilms, false);

    }

    /**
     * 按照计算好的分页依次访问页面爬取数据
     *
     * @param javbusDataItem
     * @param pageNum
     * @return
     */
    public static List<JavbusDataItem> visitAllFilmsByPageNum(JavbusDataItem javbusDataItem, int pageNum, boolean hasMagnentOrAll) {
        List<JavbusDataItem> collects = null;
        Integer hasMagnentCount = Integer.valueOf(pageNum);

        int[] caculatePageInfo = JavbusHelper.caculatePageInfo(hasMagnentCount);
        int totalPage = caculatePageInfo[0];
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            String reqUrl = javbusDataItem.getMainStarPageUrl().getStartPageUrl() + "/" + i;
            urls.add(reqUrl);
        }

        CompletableFuture[] completableFutures = urls.stream()
                .parallel()
                .map(e -> {
                    CompletableFuture<List<JavbusDataItem>> dataItemCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        return fetchFilmsInfoByEachPageUrl(e, hasMagnentOrAll);
                    });
                    return dataItemCompletableFuture;
                }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        collects = Arrays.stream(completableFutures).map(e -> {
            List<JavbusDataItem> javbusDataItemList = null;
            try {
                javbusDataItemList = (List<JavbusDataItem>) e.get();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
            return javbusDataItemList;
        }).flatMap(Collection::stream).collect(Collectors.toList());

        return collects;
    }

    /**
     * 获取每一页的作品信息
     *
     * @param pageUrl
     * @return
     */
    public static List<JavbusDataItem> fetchFilmsInfoByEachPageUrl(String pageUrl, boolean hasMagnentOrAll) {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        OkHttpClient okHttpClient = null;
        if (hasMagnentOrAll) {
            okHttpClient = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .retryOnConnectionFailure(true)
                    // 连接超时
                    .connectTimeout(60 * 6, TimeUnit.SECONDS)
                    // 读取超时
                    .readTimeout(60 * 6, TimeUnit.SECONDS)
                    // 写超时
                    .writeTimeout(60 * 6, TimeUnit.SECONDS)
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            // cookieStore.put(httpUrl.host(), list);
                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            int lastIndexOfSlash = httpUrl.url().toString().lastIndexOf("/");
                            if (lastIndexOfSlash <= 27) {
                                List<Cookie> cookies = cookieStore.get(httpUrl.url().toString());
                                return cookies != null ? cookies : new ArrayList<Cookie>();
                            }
                            String substring = httpUrl.url().toString().substring(0, lastIndexOfSlash);

                            List<Cookie> cookies = cookieStore.get(substring);
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })
                    .build();
        } else {
            okHttpClient = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .retryOnConnectionFailure(true)
                    // 连接超时
                    .connectTimeout(60 * 6, TimeUnit.SECONDS)
                    // 读取超时
                    .readTimeout(60 * 6, TimeUnit.SECONDS)
                    // 写超时
                    .writeTimeout(60 * 6, TimeUnit.SECONDS)
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            // ignore
                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            int lastIndexOfSlash = httpUrl.url().toString().lastIndexOf("/");
                            if (lastIndexOfSlash <= 27) {
                                List<Cookie> cookies = cookieStore.get(httpUrl.url().toString());

                                List<Cookie> newCookies = new ArrayList<Cookie>();
                                Cookie cookie1 = cookies.get(cookies.size() - 1);
                                Cookie cookie = new Cookie.Builder()
                                        .name(cookie1.name())
                                        .value("all")
                                        .domain(cookie1.domain())
                                        .path(cookie1.path())
                                        .expiresAt(cookie1.expiresAt())
                                        .httpOnly()
                                        .build();
                                for (int i = 0; i < cookies.size() - 1; i++) {
                                    newCookies.add(i, cookies.get(i));
                                }
                                newCookies.add(cookies.size() - 1, cookie);
                                return newCookies;
                            }
                            String substring = httpUrl.url().toString().substring(0, lastIndexOfSlash);

                            List<Cookie> cookies = cookieStore.get(substring);
                            if (null == cookies) {
                                return new ArrayList<Cookie>();
                            }
                            List<Cookie> newCookies = new ArrayList<Cookie>();
                            Cookie cookie2 = cookies.get(cookies.size() - 1);
                            Cookie cookie = new Cookie.Builder()
                                    .name(cookie2.name())
                                    .value("all")
                                    .domain(cookie2.domain())
                                    .path(cookie2.path())
                                    .expiresAt(cookie2.expiresAt())
                                    .build();
                            for (int i = 0; i < cookies.size() - 1; i++) {
                                newCookies.add(i, cookies.get(i));
                            }
                            newCookies.add(cookies.size() - 1, cookie);
                            return newCookies;
                        }
                    })
                    .build();

        }
        Request request = new Request.Builder()
                .url(pageUrl).get()
                .headers(Headers.of(getStarSearchReqHeader(pageUrl, true)))
                .build();
        String result = "";
        try (Response execute = okHttpClient.newCall(request).execute();) {
            if (execute.code() != 200) {
                logging.info("无法获取作品页面数据......: " + pageUrl);
                return null;
            } else {
                result = Objects.requireNonNull(execute.body()).string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 重试次数
            int retry = 0;
            while (retry < 3) {
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (execute.code() != 200) {
                        logging.info("无法获取作品页面数据......: " + pageUrl);
                        return null;
                    }
                    if (execute.code() == 200) {
                        result = Objects.requireNonNull(execute.body()).string();
                        break;
                    }
                    retry++;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        }
        logging.info("查询数据页成功，正在解析页面......:" + pageUrl);
        Document document = Jsoup.parse(result);
        Elements elements = document.select("#waterfall > div > a");

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        // logging.info(text1);
        String allFilmCountStr = text1.trim().split(" ")[1].trim();

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnentCountStr = text2.trim().split(" ")[1].trim();

        // logging.info(text2);


        ArrayList<String> filmUrls = new ArrayList<>();
        for (Element element : elements) {
            String text = element.attr("href").trim();
            filmUrls.add(text);
            // logging.info(text);
        }

        CompletableFuture[] completableFutures = filmUrls.stream()
                .parallel()
                .map(e -> {
                    if (e.startsWith(baseUrl)) {
                        e = e.replace(baseUrl, "");
                        return e;
                    }
                    if (e.startsWith(foreignerBaseUrl)) {
                        e = e.replace(foreignerBaseUrl, "");
                        return e;
                    }
                    return e;
                })
                .map(e -> {
                    CompletableFuture<JavbusDataItem> dataItemCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        return fetchFilmInFoByCode(e);
                    });
                    return dataItemCompletableFuture;
                }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        List<JavbusDataItem> javbusDataItems = Arrays.stream(completableFutures).map(e -> {
            JavbusDataItem javbusDataItem = null;
            try {
                javbusDataItem = (JavbusDataItem) e.get();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
            return javbusDataItem;
        }).collect(Collectors.toList());

        // StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
        javbusDataItems.stream()
                .forEach(e -> {
                    e.setAllFilmCount(allFilmCountStr);
                    e.setHaveMagnentCount(haveMagnentCountStr);
                });

        return javbusDataItems;
    }

    /**
     * 获取作品数量
     *
     * @param pageUrl
     * @return
     */
    public static String[] fetchFilmsCountsByUrlPage(String pageUrl) {
        OkHttpClient okHttpClient = getCookiedOkHttpClient();

        Request request = new Request.Builder()
                .url(pageUrl).get()
                .headers(Headers.of(getStarSearchReqHeader(pageUrl, true)))
                .build();

        Response execute = null;
        execute = getResponse(pageUrl, okHttpClient, request, execute);
        if (execute == null) {
            return null;
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execute.close();
        }
        logging.info("请求作品页，正在解析页面......");

        Document document = Jsoup.parse(result);

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        String allCounts = text1.trim().split(" ")[1].trim();
        logging.info(text1);

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnents = text2.trim().split(" ")[1].trim();

        logging.info(text2);

        String[] strings = new String[2];
        strings[0] = allCounts;
        strings[1] = haveMagnents;

        return strings;
    }

    @Nullable
    private static Response getResponse(String pageUrl, OkHttpClient okHttpClient, Request request, Response execute) {
        try {
            execute = okHttpClient.newCall(request).execute();
            if (execute.code() != 200) {
                logging.info("无法获取作品页面数据......: " + pageUrl);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 重试次数
            int retry = 0;
            while (retry < 3) {
                try {
                    execute = okHttpClient.newCall(request).execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (execute.code() != 200) {
                    logging.info("无法获取作品页面数据......:" + pageUrl);
                    return null;
                }
                if (execute.code() == 200) {
                    break;
                }
                retry++;
            }
        }
        return execute;
    }

    @NotNull
    private static OkHttpClient getCookiedOkHttpClient() {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        OkHttpClient okHttpClient = null;
        okHttpClient = new OkHttpClient.Builder()
                .proxy(proxy)
                .retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS).cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                        cookieStore.put(httpUrl.url().toString(), list);
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                        List<Cookie> cookies = cookieStore.get(httpUrl.url().toString());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).build();
        return okHttpClient;
    }

    /**
     * 获取演员最新一部作品
     *
     * @param starName
     * @return
     */
    public static JavbusDataItem fetchLatestFilmInfoByName(String starName) {
        String starNameEncode = JavbusHelper.parseStrToUrlEncoder(starName);
        String starUrl = "";
        Response execute = null;
        if (JavbusHelper.startWithAlpha(starName)) {
            starUrl = "https://www.javbus.org/search/" + starNameEncode;
            execute = searchStarByName(starName, starUrl, "occident", false);
        } else {
            starUrl = "https://www.javbus.com/search/" + starNameEncode + "&type=&parent=ce";
            execute = searchStarByName(starName, starUrl, "code", false);
            if (execute == null) {
                starUrl = "https://www.javbus.com/uncensored/search/" + starNameEncode + "&type=0&parent=uc";
                execute = searchStarByName(starName, starUrl, "nocode", false);
            }
        }
        if (null == execute) {
            return null;
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execute.close();
        }

        Document document = Jsoup.parse(result);
        Elements elements = document.select("#waterfall > div > a");

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        // logging.info(text1);
        String allFilmCountStr = text1.trim().split(" ")[1].trim();

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnentCountStr = text2.trim().split(" ")[1].trim();

        ArrayList<String> filmUrls = new ArrayList<>();
        for (Element element : elements) {
            String text = element.attr("href").trim();
            filmUrls.add(text);
            // 只需要第一个
            break;
        }

        CompletableFuture[] completableFutures = filmUrls.stream()
                .map(e -> {
                    if (e.startsWith(baseUrl)) {
                        e = e.replace(baseUrl, "");
                        return e;
                    }
                    if (e.startsWith(foreignerBaseUrl)) {
                        e = e.replace(foreignerBaseUrl, "");
                        return e;
                    }
                    return e;
                }).parallel()
                .map(e -> {
                    CompletableFuture<JavbusDataItem> dataItemCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        return fetchFilmInFoByCode(e);
                    });
                    return dataItemCompletableFuture;
                }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        List<JavbusDataItem> javbusDataItems = Arrays.stream(completableFutures).map(e -> {
            JavbusDataItem javbusDataItem = null;
            try {
                javbusDataItem = (JavbusDataItem) e.get();
            } catch (InterruptedException | ExecutionException interruptedException) {
                interruptedException.printStackTrace();
            }
            return javbusDataItem;
        }).collect(Collectors.toList());

        // StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
        javbusDataItems
                .forEach(e -> {
                    e.setAllFilmCount(allFilmCountStr);
                    e.setHaveMagnentCount(haveMagnentCountStr);
                });
        return javbusDataItems.get(0);
    }


    public static JavbusDataItem fetchLatestMagFilmInfoByName(String starName) {
        String starNameEncode = JavbusHelper.parseStrToUrlEncoder(starName);
        String starUrl = "";
        Response execute = null;
        if (JavbusHelper.startWithAlpha(starName)) {
            starUrl = "https://www.javbus.org/search/" + starNameEncode;
            execute = searchStarByName(starName, starUrl, "occident", true);
        } else {
            starUrl = "https://www.javbus.com/search/" + starNameEncode + "&type=&parent=ce";
            execute = searchStarByName(starName, starUrl, "code", true);
            if (execute == null) {
                starUrl = "https://www.javbus.com/uncensored/search/" + starNameEncode + "&type=0&parent=uc";
                execute = searchStarByName(starName, starUrl, "nocode", true);
            }
        }
        if (null == execute) {
            return null;
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execute.close();
        }

        Document document = Jsoup.parse(result);
        Elements elements = document.select("#waterfall > div > a");

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        // logging.info(text1);
        String allFilmCountStr = text1.trim().split(" ")[1].trim();

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnentCountStr = text2.trim().split(" ")[1].trim();

        ArrayList<String> filmUrls = new ArrayList<>();
        for (Element element : elements) {
            String text = element.attr("href").trim();
            filmUrls.add(text);
            // 只需要第一个
            break;
        }

        CompletableFuture[] completableFutures = filmUrls.stream()
                .map(e -> {
                    if (e.startsWith(baseUrl)) {
                        e = e.replace(baseUrl, "");
                        return e;
                    }
                    if (e.startsWith(foreignerBaseUrl)) {
                        e = e.replace(foreignerBaseUrl, "");
                        return e;
                    }
                    return e;
                }).parallel()
                .map(e -> {
                    CompletableFuture<JavbusDataItem> dataItemCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        return fetchFilmInFoByCode(e);
                    });
                    return dataItemCompletableFuture;
                }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        List<JavbusDataItem> javbusDataItems = Arrays.stream(completableFutures).map(e -> {
            JavbusDataItem javbusDataItem = null;
            try {
                javbusDataItem = (JavbusDataItem) e.get();
            } catch (InterruptedException | ExecutionException interruptedException) {
                interruptedException.printStackTrace();
            }
            return javbusDataItem;
        }).collect(Collectors.toList());

        // StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
        javbusDataItems
                .forEach(e -> {
                    e.setAllFilmCount(allFilmCountStr);
                    e.setHaveMagnentCount(haveMagnentCountStr);
                });
        return javbusDataItems.get(0);
    }

    /**
     * 该方法只能获取到查询页面最多30个作品，远远低于点击个人主页进去所找到的
     * 条目数量
     *
     * @param starName
     * @return
     */
    public static List<JavbusDataItem> fetchFilmsInfoByName(String starName) {
        String starNameEncode = JavbusHelper.parseStrToUrlEncoder(starName);
        String starUrl = "";
        Response execute = null;
        if (JavbusHelper.startWithAlpha(starName)) {
            starUrl = "https://www.javbus.org/search/" + starNameEncode;
            execute = searchStarByName(starName, starUrl, "occident", false);
        } else {
            starUrl = "https://www.javbus.com/search/" + starNameEncode + "&type=&parent=ce";
            execute = searchStarByName(starName, starUrl, "code", false);
            if (execute == null) {
                starUrl = "https://www.javbus.com/uncensored/search/" + starNameEncode + "&type=0&parent=uc";
                execute = searchStarByName(starName, starUrl, "nocode", false);
            }
        }
        if (null == execute) {
            return null;
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execute.close();
        }

        Document document = Jsoup.parse(result);
        Elements elements = document.select("#waterfall > div > a");

        Elements allFilmCount = document.select("#resultshowall");
        Element allFilmNode = allFilmCount.get(0);
        TextNode node = (TextNode) allFilmNode.childNodes().get(2);
        String text1 = node.text();
        // logging.info(text1);
        String allFilmCountStr = text1.trim().split(" ")[1].trim();

        Elements haveMagnentCount = document.select("#resultshowmag");
        Element haveMagnentFilmNode = haveMagnentCount.get(0);
        TextNode node2 = (TextNode) haveMagnentFilmNode.childNodes().get(2);
        String text2 = node2.text();
        String haveMagnentCountStr = text2.trim().split(" ")[1].trim();

        // logging.info(text2);


        ArrayList<String> filmUrls = new ArrayList<>();
        for (Element element : elements) {
            String text = element.attr("href").trim();
            filmUrls.add(text);
            // logging.info(text);
        }

        CompletableFuture[] completableFutures = filmUrls.stream()
                .map(e -> {
                    if (e.startsWith(baseUrl)) {
                        e = e.replace(baseUrl, "");
                        return e;
                    }
                    if (e.startsWith(foreignerBaseUrl)) {
                        e = e.replace(foreignerBaseUrl, "");
                        return e;
                    }
                    return e;
                }).parallel()
                .map(e -> {
                    CompletableFuture<JavbusDataItem> dataItemCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        return fetchFilmInFoByCode(e);
                    });
                    return dataItemCompletableFuture;
                }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        List<JavbusDataItem> javbusDataItems = Arrays.stream(completableFutures).map(e -> {
            JavbusDataItem javbusDataItem = null;
            try {
                javbusDataItem = (JavbusDataItem) e.get();
            } catch (InterruptedException | ExecutionException interruptedException) {
                interruptedException.printStackTrace();
            }
            return javbusDataItem;
        }).collect(Collectors.toList());

        // StarSpiderJob.trigerStarJavbusTask(javbusDataItems);
        javbusDataItems
                .forEach(e -> {
                    e.setAllFilmCount(allFilmCountStr);
                    e.setHaveMagnentCount(haveMagnentCountStr);
                });
        return javbusDataItems;
    }

    @Nullable
    private static Response searchStarByName(String starName, String starUrl, String queryType, boolean magFlag) {
        String msg = "";
        if ("code".equals(queryType)) {
            msg = "有码查询";
        }
        if ("nocode".equals(queryType)) {
            msg = "无码查询";
        }
        if ("occident".equals(queryType)) {
            msg = "欧美查询";
        }
        logging.info("正在进行" + msg + "......");
        Request request = new Request.Builder()
                .url(starUrl).get()
                .headers(Headers.of(getStarSearchReqHeader(starUrl, magFlag)))
                .build();

        Response execute = null;
        try {
            execute = okHttpClient.newCall(request).execute();
            if (execute.code() != 200) {
                logging.info(msg + "--无法查询：" + starName);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 重试次数
            int retry = 0;
            while (retry < 3) {
                try {
                    execute = okHttpClient.newCall(request).execute();
                } catch (IOException ioException) {
                    // ioException.printStackTrace();
                    logging.info(msg + "--请求页面失败，正在重试......");
                }
                if (null != execute && execute.code() != 200) {
                    logging.info(msg + "--无法查询：" + starName);
                    return null;
                }
                if (null != execute && execute.code() == 200) {
                    break;
                }
                retry++;
            }
        }
        if (execute.code() == 200) {
            logging.info("查询搜索页成功，正在解析页面......");
        }
        return execute;
    }

    /**
     * 按照番号获取该番号信息
     * DV-1314
     * TD-026
     *
     * @param filmCode
     * @return
     */
    public static JavbusDataItem fetchFilmInFoByCode(String filmCode) {
        String filmReqUrl = "";
        // 判断filmCode类型
        if (JavbusHelper.isforeignProduct(filmCode)) {
            filmReqUrl = foreignerBaseUrl + filmCode;
        } else {
            // 移除多余的日期
            filmCode = JavbusHelper.removeDateFromFilmCode(filmCode);
            filmReqUrl = baseUrl + filmCode;
        }

        JavbusDataItem javbusDataItem = new JavbusDataItem();

        Request request = new Request.Builder().url(filmReqUrl).get().build();

        String result = "";
        try (Response execute = okHttpClient.newCall(request).execute();) {
            if (execute.code() != 200) {
                logging.info("无法查询：" + filmReqUrl);
                javbusDataItem.setCode(filmCode);
                return javbusDataItem;
            } else {
                result = execute.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 重试次数
            int retry = 0;
            while (retry < 3) {
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (null != execute && execute.code() != 200) {
                        logging.info("无法查询：" + filmReqUrl);
                        return javbusDataItem;
                    }
                    if (null != execute && execute.code() == 200) {
                        result = execute.body().string();
                        break;
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                retry++;
            }
        }
        // logging.info(execute.body().string());

        Document document = Jsoup.parse(result);

        Elements contentContainer = document.select("body > div.container");

        Elements body = document.select("body");
        // 访问链接
        javbusDataItem.setVisitUrl(filmReqUrl);
        // 标题
        parseImageAndTitleContent(javbusDataItem, contentContainer);

        // 简介内容处理
        parseIntroduceContent(javbusDataItem, contentContainer);

        // 抽取样品图地址
        parsetSampleImgsContent(javbusDataItem, contentContainer);

        // 抽取磁力链接
        parseMagnentContent(javbusDataItem, body, filmReqUrl);

        return javbusDataItem;
    }

    private static void parsetSampleImgsContent(JavbusDataItem javbusDataItem, Elements contentContainer) {
        Elements sampleWall = contentContainer.select("#sample-waterfall");
        List<String> sampleUrls = new ArrayList<>();
        try {
            Element sampleImgsEl = sampleWall.get(0);
            List<Node> childNodes = sampleImgsEl.childNodes();

            for (Node childNode : childNodes) {
                if (childNode instanceof Element) {
                    Element node = (Element) childNode;
                    String href = node.attr("href");
                    // 不是来自dmm的图
                    if (href.startsWith("/pics/")) {
                        href = TgBotConfig.SPIDER_BASE_URL + href;
                    }
                    sampleUrls.add(href);
                    // logging.info(href);
                }
            }
        } catch (Exception e) {
            // 没找到图片
        }
        // 处理无码图
        sampleUrls = sampleUrls.stream()
                .filter(e -> !"".equals(e))
                .collect(Collectors.toList());
        javbusDataItem.setSampleImgs(sampleUrls);
    }

    private static void parseImageAndTitleContent(JavbusDataItem javbusDataItem, Elements contentContainer) {
        // title
        Elements titleAndImg = contentContainer.select("div.row.movie > div.col-md-9.screencap > a > img");

        // 图片抽取
        Element element = titleAndImg.get(0);
        // 大图
        String bigImgUrl = element.attr("src");
        javbusDataItem.setBigImgUrl(bigImgUrl);
        // 标题
        String titleStr = element.attr("title");
        javbusDataItem.setTitleStr(titleStr);
    }

    /**
     * 抽取简介内容
     *
     * @param javbusDataItem
     * @param elements
     */
    public static void parseIntroduceContent(JavbusDataItem javbusDataItem, Elements elements) {
        Elements introductionEl = elements.select("div.row.movie > div.col-md-3.info");

        Elements pEls = introductionEl.select("p");


        List<String> contents = new ArrayList<>();

        for (int i = 0; i < pEls.size(); i++) {
            String text = pEls.get(i).text();
            // logging.info(text);

            String[] strings = text.split(":");
            if (strings.length == 1 && i <= pEls.size() - 2) {
                if (text.contains(":") && !pEls.get(i + 1).text().contains(":")) {
                    String combinedStr = text + pEls.get(i + 1).text();
                    contents.add(combinedStr);
                    continue;
                }
            } else if (strings.length == 2) {
                contents.add(text);
            } else {
                continue;
            }
        }
        contents.stream()
                .map(e -> {
                            String[] strings = e.split(":");
                            String key = strings[0].trim();
                            String value = strings[1].trim();
                            switch (key) {
                                case "識別碼":
                                    javbusDataItem.setCode(value);
                                    break;
                                case "發行日期":
                                    javbusDataItem.setPublishDate(value);
                                    break;
                                case "長度":
                                    javbusDataItem.setTotalTime(value);
                                    break;
                                case "製作商":
                                    javbusDataItem.setProduceCompany(value);
                                    break;
                                case "發行商":
                                    javbusDataItem.setPublishCompany(value);
                                    break;
                                case "類別":
                                    javbusDataItem.setTypes(value);
                                    break;
                                case "演員":
                                    javbusDataItem.setStars(value);
                                    break;
                                case "系列":
                                    javbusDataItem.setSeries(value);
                                    break;
                                default:
                            }
                            return e;
                        }
                ).count();

        // 解析主演 作品集合地址
        Elements startUrls = introductionEl.select("p > span > a");

        if (startUrls.size() == 1) {
            Element element = startUrls.get(0);
            String href = element.attr("href");
            String starName = element.text().trim();
            JavbusStarUrlItem starUrlItem = new JavbusStarUrlItem(starName, href);
            javbusDataItem.setMainStarPageUrl(starUrlItem);
            ArrayList<JavbusStarUrlItem> javbusStarUrlItems = new ArrayList<>();
            javbusStarUrlItems.add(starUrlItem);
            javbusDataItem.setStarsPageUrls(javbusStarUrlItems);
            return;
        }
        if (startUrls.size() <= 0) {
            return;
        }
        ArrayList<JavbusStarUrlItem> strings = new ArrayList<>();
        boolean hasSetMainStar = false;
        for (Element startUrl : startUrls) {
            String href = startUrl.attr("href");
            String starName = startUrl.text().trim();
            JavbusStarUrlItem starUrlItem = new JavbusStarUrlItem(starName, href);
            strings.add(starUrlItem);
            // 判断如果链接字符出现在标题里面 那么可以判定是主演
            if (javbusDataItem.getTitleStr().contains(starName) && !hasSetMainStar) {
                javbusDataItem.setMainStarPageUrl(starUrlItem);
                hasSetMainStar = true;
            }
        }
        javbusDataItem.setStarsPageUrls(strings);

    }

    /**
     * 抽取磁力内容
     *
     * @param javbusDataItem
     * @param body
     * @param fileReqUrl
     */
    public static void parseMagnentContent(JavbusDataItem javbusDataItem, Elements body, String fileReqUrl) {
        // 获取磁力连接
        Elements params = body.select("script:nth-child(9)");
        // https://www.javbus.com/ajax/uncledatoolsbyajax.php?gid=46298156144&lang=zh&img=https://pics.javbus.com/cover/87y2_b.jpg&uc=0&floor=734
        // https://www.javbus.org/ajax/uncledatoolsbyajax.php?gid=3622821095&lang=zh&img=https://images.javbus.org/cover/g1q_b.jpg&uc=0&floor=54
        DataNode node = (DataNode) params.get(0).childNodes().get(0);
        String wholeData = node.getWholeData();
        // logging.info(wholeData);
        String magnetReqUrl = getMagnetReqUrl(wholeData, fileReqUrl);

        Request magnentReq = makeMagnentReq(fileReqUrl, magnetReqUrl);
        String magnentStrs = null;
        try (Response execute = okHttpClient.newCall(magnentReq).execute();) {
            if (null != execute) {
                magnentStrs = execute.body().string();
            }
        } catch (IOException e) {
            logging.info("请求磁力失败......" + javbusDataItem.getCode());
            return;
            // e.printStackTrace();
        }
        // logging.info(magnentStrs);
        Document magnentDom = Jsoup.parse(magnentStrs);
        Element node1 = (Element) magnentDom.childNodes().get(0);
        Elements select = node1.select("body > a");

        List<MagnentItem> magnentItems = extractMagnentContent(select);
        // logging.info(magnentItems);

        // logging.info(javbusDataItem);
        javbusDataItem.setMagnents(magnentItems);
    }

    /**
     * 提取磁力内容到列表
     *
     * @param elements
     * @return
     */
    public static List<MagnentItem> extractMagnentContent(Elements elements) {
        if (elements.size() == 0) {
            return new ArrayList<>();
        }
        int listSize = 0;
        for (Element element : elements) {
            String text = element.text();
            // logging.info(text);
            if (JavbusHelper.isValidDate(text)) {
                listSize++;
            }
        }

        ArrayList<ArrayList<Element>> arrayLists = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            ArrayList<Element> strings = new ArrayList<>();

            for (int j = 0; j < elements.size(); j++) {
                Element e = elements.get(j);
                if (JavbusHelper.isValidDate(e.text())) {
                    elements.remove(j);
                    strings.add(e);
                    j--;
                    break;
                }
                elements.remove(j);
                strings.add(e);
                j--;
            }
            arrayLists.add(strings);
        }

        List<MagnentItem> magnentItemList = arrayLists.stream().map(e -> parseFromElementList(e))
                .collect(Collectors.toList());

        return magnentItemList;
    }

    /**
     * 解析磁力相应dom
     *
     * @param elementList
     * @return
     */
    public static MagnentItem parseFromElementList(List<Element> elementList) {
        MagnentItem magnentItem = new MagnentItem();

        if (elementList.size() == 3) {
            Element element = elementList.get(0);
            String title = element.text();
            magnentItem.setTitle(title);
            String magnentStr = element.attr("href").trim();
            magnentItem.setMagnentStr(magnentStr);
            Element element1 = elementList.get(1);
            String fileSize = element1.text();
            magnentItem.setFileSize(fileSize);
            Element element2 = elementList.get(2);
            String shareDate = element2.text();
            magnentItem.setShareDate(shareDate);

            magnentItem.setResolution("");
            magnentItem.setSubTitle("");

            return magnentItem;
        }
        if (elementList.size() == 4) {
            Element element = elementList.get(0);
            String title = element.text();
            magnentItem.setTitle(title);
            String magnentStr = element.attr("href").trim();
            magnentItem.setMagnentStr(magnentStr);
            // 两种情况 有清晰度标签 或者是 字幕标签
            Element temp = elementList.get(1);
            String text = temp.text();
            if (text.contains("清")) {
                magnentItem.setResolution(text.trim());
            }
            if (text.contains("字")) {
                magnentItem.setSubTitle(text.trim());
            }
            Element element1 = elementList.get(2);
            String fileSize = element1.text();
            magnentItem.setFileSize(fileSize);
            Element element2 = elementList.get(3);
            String shareDate = element2.text();
            magnentItem.setShareDate(shareDate);

            magnentItem.setSubTitle("");

            return magnentItem;
        }
        if (elementList.size() == 5) {
            Element element = elementList.get(0);
            String title = element.text();
            magnentItem.setTitle(title);
            String magnentStr = element.attr("href").trim();
            magnentItem.setMagnentStr(magnentStr);

            Element element1 = elementList.get(1);
            String resolution = element1.text();
            magnentItem.setResolution(resolution);

            Element element2 = elementList.get(2);
            String subTitle = element2.text();
            magnentItem.setSubTitle(subTitle);

            Element element3 = elementList.get(3);
            String fileSize = element3.text();
            magnentItem.setFileSize(fileSize);

            Element element4 = elementList.get(4);
            String shareDate = element4.text();
            magnentItem.setShareDate(shareDate);

            return magnentItem;
        }
        return magnentItem;
    }


    /**
     * 组装磁力内容请求
     *
     * @param dataStr
     * @return
     */
    public static String getMagnetReqUrl(String dataStr, String fileReqUrl) {
        String requestBase = "";
        if (fileReqUrl.startsWith("https://www.javbus.org")) {
            requestBase = "https://www.javbus.org/ajax/uncledatoolsbyajax.php?";
        } else {
            requestBase = "https://www.javbus.com/ajax/uncledatoolsbyajax.php?";
        }
        List<String> vars = Arrays.stream(dataStr.trim()
                        .replaceAll("var", "")
                        .replaceAll("'", "")
                        .split(";"))
                .map(e -> e.trim().replaceAll(" ", ""))
                .collect(Collectors.toList());

        return requestBase + vars.get(0) + "&lang=zh&" + vars.get(2) + "&" + vars.get(1) + "&floor=734";
    }

    /**
     * 生成磁力内容请求
     *
     * @param filmreqUrl
     * @param magnetReqUrl
     * @return
     */
    public static Request makeMagnentReq(String filmreqUrl, String magnetReqUrl) {
        Request magnentReq = new Request.Builder().url(magnetReqUrl)
                .headers(Headers.of(getMagentReqHeader(filmreqUrl, magnetReqUrl)))
                .get().build();
        return magnentReq;
    }

    /**
     * 从请求头模版获取请求头
     *
     * @param templatePath
     * @return
     */
    public static HashMap<String, String> getRequestHeader(String templatePath) {
        // 需要主动替换header头中的 referer :path
        ///ajax/uncledatoolsbyajax.php?gid=46298156144&lang=zh&img=https://pics.javbus.com/cover/87y2_b.jpg&uc=0&floor=734
        File file = new File(templatePath);
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<String, String> hashMap = new HashMap<>();
        fileReader.lines().map(e -> {
            String[] split = e.split(": ");
            hashMap.put(split[0], split[1]);
            return e;
        }).collect(Collectors.toList());
        return hashMap;
    }

    /**
     * 获取磁力链接请求头
     *
     * @param filmreqUrl
     * @param magnentReqUrl
     * @return
     */
    public static HashMap<String, String> getMagentReqHeader(String filmreqUrl, String magnentReqUrl) {

        HashMap<String, String> requestHeader = getRequestHeader("src/main/resources/reqHeaders.txt");
        // 需要主动替换header头中的 referer :path
        ///ajax/uncledatoolsbyajax.php?gid=46298156144&lang=zh&img=https://pics.javbus.com/cover/87y2_b.jpg&uc=0&floor=734
        // 替换
        requestHeader.put("referer", filmreqUrl);
        String replace = magnentReqUrl.replace("https://www.javbus.com", "");
        requestHeader.put(":path", replace);
        return requestHeader;
    }

    /**
     * 获取演员搜索请求头
     *
     * @param starReqUrl
     * @return
     */
    public static HashMap<String, String> getStarSearchReqHeader(String starReqUrl, boolean magFlag) {
        HashMap<String, String> requestHeader = getRequestHeader("src/main/resources/searchReqHeader.txt");
        // 替换
        if (!magFlag) {
            requestHeader.put("cookie", "genreinfo=glyphicon glyphicon-plus; starinfo=glyphicon glyphicon-plus; existmag=all");
        }
        String replace = starReqUrl.replace("https://www.javbus.com", "");
        requestHeader.put(":path", replace);
        return requestHeader;
    }

    /**
     * 测试方法
     *
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) {

        // SpiderJob spiderJob = new SpiderJob("FSDSS-211", JobExcutor.concurrentLinkedDeque);
        // JobExcutor.doSpiderJob(spiderJob);

        // fetchFilmsInfoByName("葵つかさ");

        // fetchAllFilmsInfoByName("葵つかさ", false);

        // fetchFilmsCountsByUrlPage("https://www.javbus.com/star/2jv");

        // fetchFilmsInfoByEachPageUrl("https://www.javbus.com/star/2jv");

        // fetchStarInfoByName("永瀬みなも");
        fetchStarInfoByName("天音まひな");


    }

}