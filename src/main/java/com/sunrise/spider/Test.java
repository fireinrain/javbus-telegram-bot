package com.sunrise.spider;

import okhttp3.OkHttpClient;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @description: 测试代码
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 5:42 PM
 */
public class Test {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        //String a = "\n" +
        //        "\tvar gid = 46298157174;\n" +
        //        "\tvar uc = 0;\n" +
        //        "\tvar img = 'https://pics.javbus.com/cover/87y2_b.jpg';";
        //
        //List<String> vars = Arrays.stream(a.trim()
        //        .replaceAll("var", "")
        //        .replaceAll("'","")
        //        .split(";"))
        //        .map(e->e.trim().replaceAll(" ",""))
        //        .collect(Collectors.toList());
        //
        // logging.info(vars);
        //
        // logging.info(JavbusSpider.getMagnetReqUrl(a));

        //File file = new File("src/main/resources/reqHeaders.txt");
        //BufferedReader fileReader = new BufferedReader(new FileReader(file));
        //HashMap<String,String> hashMap = new HashMap<>();
        //fileReader.lines().map(e->{
        //    String[] split = e.split(": ");
        //    hashMap.put(split[0],split[1]);
        //    return e;
        //}).collect(Collectors.toList());
        // logging.info(hashMap.size());
        //
        // logging.info(file.exists());


        //String message = "hello";
        //String channelName = "@sunrisechannel_8888";
        //String chatId = "-1001371132897";
        //String url = "https://api.telegram.org/bot"+BotConfig.BOT_TOKEN+
        //        "/sendMessage?chat_id="+chatId+"&text="+message;
        ////发送消息给特定的频道
        //final Request request = new Request.Builder()
        //        .url(url)
        //        .addHeader("Accept","*/*")
        //        .addHeader("Accept-Encoding","gzip, deflate")
        //        .addHeader("Accept-Language","zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6")
        //        .addHeader("Cache-Control","no-cache")
        //        .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
        //        .build();
        //
        // okHttpClient.newCall(request).enqueue(new Callback() {
        //    @Override
        //    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        //
        //    }
        //
        //    @Override
        //    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        //        logging.info(response.toString());
        //        logging.info(response.body().string());
        //    }
        //});

        // logging.info(JavbusSpider.isValidDate("2020-12-01"));
        // logging.info(JavbusSpider.isValidDate("2020-12\nxsxasasx"));
        // logging.info("2020-12\nxsxasasx");

        // int number = (int) Math.ceil((float) 13 / 10);
        // logging.info(number);
        //
        // ArrayList<String> strings = new ArrayList<>();
        // String a = "https://pics.dmm.co.jp/digital/video/ssni00876/ssni00876jp-1.jpg";
        // String b ="https://pics.dmm.co.jp/digital/video/ssni00876/ssni00876jp-2.jpg";
        // String c ="https://pics.dmm.co.jp/digital/video/ssni00876/ssni00876jp-3.jpg";
        // String d ="https://pics.dmm.co.jp/digital/video/ssni00876/ssni00876jp-4.jpg";
        // String e ="https://pics.dmm.co.jp/digital/video/ssni00876/ssni00876jp-5.jpg";
        //
        //strings.add(a);
        //strings.add(b);
        //strings.add(c);
        //strings.add(d);
        //strings.add(e);
        //
        //CompletableFuture[] objects = strings.stream()
        //        .map(el -> {
        //            CompletableFuture<InputStream> inputStreamCompletableFuture = CompletableFuture.supplyAsync(() -> {
        //                //下载图片
        //                OkHttpClient client = new OkHttpClient();
        //                //获取请求对象
        //                Request request = new Request.Builder().url(el.trim()).build();
        //                //获取响应体
        //                ResponseBody body = null;
        //                try {
        //                    body = client.newCall(request).execute().body();
        //                } catch (IOException exception) {
        //                    body.close();
        //                    exception.printStackTrace();
        //                }
        //                return body.byteStream();
        //            });
        //
        //            return inputStreamCompletableFuture;
        //        }).toArray(CompletableFuture[]::new);
        //
        //
        //CompletableFuture.allOf(objects).join();
        //
        //
        //
        // logging.info("");

        //OkHttpClient okHttpClient = new OkHttpClient();
        //
        //Request request = new Request.Builder().url("https://www.javbus.com/xiaoqian").get().build();
        //
        //Response execute = null;
        //try {
        //    execute = okHttpClient.newCall(request).execute();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        //String result = null;
        //try {
        //    if (execute.code() != 200){
        //        logging.info("无法查询");
        //        return;
        //    }
        //    result = execute.body().string();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        //
        //Document document = Jsoup.parse(result);
        //
        //Elements contentContainer = document.select("body > div.wrap.mt30 > ul > li");
        //
        // for (Element element : contentContainer) {
        //    logging.info(element.text());
        //}


        //body > div.wrap.mt30 > ul > li:nth-child(29) > a > p

        // Thread.sleep(5000);
        // logging.info("xxx");


        String proxyHost = "127.0.0.1";

        int proxyPort = 7891;
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .proxy(proxy)
                .retryOnConnectionFailure(true)
                //连接超时
                .connectTimeout(60, TimeUnit.SECONDS)
                //读取超时
                .readTimeout(60, TimeUnit.SECONDS)
                //写超时
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        //okHttpClient.newCall()
    }
}
