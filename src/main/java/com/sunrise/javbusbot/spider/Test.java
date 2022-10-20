package com.sunrise.javbusbot.spider;

import com.google.common.base.Strings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: 测试代码
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 5:42 PM
 */
public class Test {
    private static final Logger logging = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        // String a = "\n" +
        //        "\tvar gid = 46298157174;\n" +
        //        "\tvar uc = 0;\n" +
        //        "\tvar img = 'https://pics.javbus.com/cover/87y2_b.jpg';";
        //
        // List<String> vars = Arrays.stream(a.trim()
        //        .replaceAll("var", "")
        //        .replaceAll("'","")
        //        .split(";"))
        //        .map(e->e.trim().replaceAll(" ",""))
        //        .collect(Collectors.toList());
        //
        // logging.info(vars);
        //
        // logging.info(JavbusSpider.getMagnetReqUrl(a));

        // File file = new File("src/main/resources/reqHeaders.txt");
        // BufferedReader fileReader = new BufferedReader(new FileReader(file));
        // HashMap<String,String> hashMap = new HashMap<>();
        // fileReader.lines().map(e->{
        //    String[] split = e.split(": ");
        //    hashMap.put(split[0],split[1]);
        //    return e;
        //}).collect(Collectors.toList());
        // logging.info(hashMap.size());
        //
        // logging.info(file.exists());


        // String message = "hello";
        // String channelName = "@sunrisechannel_8888";
        // String chatId = "-1001371132897";
        // String url = "https://api.telegram.org/bot"+BotConfig.BOT_TOKEN+
        //        "/sendMessage?chat_id="+chatId+"&text="+message;
        ////发送消息给特定的频道
        // final Request request = new Request.Builder()
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
        // strings.add(a);
        // strings.add(b);
        // strings.add(c);
        // strings.add(d);
        // strings.add(e);
        //
        // CompletableFuture[] objects = strings.stream()
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
        // CompletableFuture.allOf(objects).join();
        //
        //
        //
        // logging.info("");

        // OkHttpClient okHttpClient = new OkHttpClient();
        //
        // Request request = new Request.Builder().url("https://www.javbus.com/xiaoqian").get().build();
        //
        // Response execute = null;
        // try {
        //    execute = okHttpClient.newCall(request).execute();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        // String result = null;
        // try {
        //    if (execute.code() != 200){
        //        logging.info("无法查询");
        //        return;
        //    }
        //    result = execute.body().string();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        //
        // Document document = Jsoup.parse(result);
        //
        // Elements contentContainer = document.select("body > div.wrap.mt30 > ul > li");
        //
        // for (Element element : contentContainer) {
        //    logging.info(element.text());
        //}


        // body > div.wrap.mt30 > ul > li:nth-child(29) > a > p

        // Thread.sleep(5000);
        // logging.info("xxx");

        // String code = "PRED-438";
        // String title = "M男クンのお家（ウチ）までデリバリー楪カレン。";
        // String videoPreview = getVideoPreview(code, title, "");
        // System.out.println(videoPreview);

        // File source = new File("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        // Encoder encoder = new Encoder();
        // String length = "";
        // try {
        //     MultimediaInfo m = encoder.getInfo(source);
        //
        //     int height = m.getVideo().getSize().getHeight();
        //     int width = m.getVideo().getSize().getWidth();
        //     System.out.println("width:"+width);
        //     System.out.println("height:" + height);
        //     FileInputStream fis = new FileInputStream(source);
        //     FileChannel fc = fis.getChannel();
        //     BigDecimal fileSize = new BigDecimal(fc.size());
        //     String size = fileSize.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP) + "MB";
        //     System.out.println("size:" + size);
        //     long duration = m.getDuration()/1000;
        //     System.out.println("duration:" + duration + "s");
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // String xml = Test.read("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        FileInputStream fileInputStream = new FileInputStream("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        File file = new File("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        FileInputStream fileInputStream1 = new FileInputStream(file);
        ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream1);

        // IsoFile isoFile = new IsoFile("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        IsoFile isoFile = new IsoFile(readableByteChannel);
        long l = isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        System.err.println(l);

        // XmlBox xmlBox = Path.getPath(isoFile, "moov/meta/xml ");
        // assert xmlBox != null;
        // String xml = xmlBox.getXml();
        // System.err.println(xml);
        System.out.println("---------------------");

        MovieBox movieBox = org.mp4parser.tools.Path.getPath(isoFile, "moov");
        // 可以打印这个 movieBox  toString 看看里面有啥
        List<Box> boxes = movieBox.getBoxes();
        // 宽高时长获取
        long durationLong = movieBox.getMovieHeaderBox().getDuration();
        int duration = (int) (durationLong / 10000);
        int width = 0;
        int height = 0;
        for (Box box : boxes) {
            if (box instanceof TrackBox) {
                TrackBox tBbx = (TrackBox) box;
                width = (int) tBbx.getTrackHeaderBox().getWidth();
                height = (int) tBbx.getTrackHeaderBox().getHeight();
                break;
            }
        }
        System.out.println(width);
        System.out.println(height);
        System.out.println(duration);
        isoFile.close();
    }


    public static String read(String videoFilePath) throws IOException {

        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFilePath + " not exists");
        }

        if (!videoFile.canRead()) {
            throw new IllegalStateException("No read permissions to file " + videoFilePath);
        }
        IsoFile isoFile = new IsoFile(new FileInputStream(videoFilePath).getChannel());


        // AppleNameBox nam = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/ilst/©nam");
        // String xml = nam.getValue();
        // isoFile.close();
        // return xml;
        return "";
    }

    public static long readUint32(byte[] b, int s) {
        long result = 0;
        result |= ((b[s + 0] << 24) & 0xFF000000);
        result |= ((b[s + 1] << 16) & 0xFF0000);
        result |= ((b[s + 2] << 8) & 0xFF00);
        result |= ((b[s + 3]) & 0xFF);
        return result;
    }

    public static double readFixedPoint1616(byte[] b, int s) {
        return ((double) readUint32(b, s)) / 65536;
    }

    public static String getVideoPreview(String codeStr, String titleStr, String studioStr) {
        String proxyHost = "127.0.0.1";
        int proxyPort = 7891;
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().proxy(proxy).retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS).build();

        // okHttpClient.newCall()
        HashMap<String, String> urlMap = new HashMap<>();
        urlMap.put("r18", "https://www.r18.com/common/search/order=match/searchword=${code}/");
        urlMap.put("dmm", "https://www.dmm.co.jp/digital/videoa/-/list/search/=/?searchstr=${title}");
        urlMap.put("mgs", "https://www.mgstage.com/search/cSearch.php?search_word=${title}&x=26&y=8&search_shop_id=&type=top");
        urlMap.put("avp", "https://avpreview.com/zh/search?keywords=${code}");


        String code = codeStr;
        String title = titleStr;
        String studio = studioStr;
        Map<String, Request> requstsMap = urlMap.entrySet().stream().peek(e -> {
            if (e.getValue().contains("${code}")) {
                e.setValue(e.getValue().replace("${code}", code));
            }
            if (e.getValue().contains("${title")) {
                e.setValue(e.getValue().replace("${title}", title));
            }
        }).map(e -> {
            Request request = null;
            Request.Builder builder = new Request.Builder().url(e.getValue()).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
            // 设置一些cookie
            if (e.getKey().equals("mgs")) {
                builder.addHeader("Cookie", "coc=1; PHPSESSID=2kjqnoogu9ff6q76l3mdipljl5; uuid=8e414da4ef13b3933ec37649f97003c4; bWdzdGFnZS5jb20=-_lr_hb_-r2icil/mgs={\"heartbeat\":1666201658904}; adc=1");
                request = builder.build();
            } else if (e.getKey().equals("dmm")) {
                builder.addHeader("Cookie", "_gali=naviapi-search-submit; ckcy=1; mbox=check#true#1666207260|session#1666207199640-935175#1666209060; __utma=125690133.1119214179.1666207200.1666207200.1666207200.1; __utmb=125690133.0.10.1666207200; __utmc=125690133; __utmz=125690133.1666207200.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); app_uid=ygb2CmNQTeBsnSRFDGGZAg==; _gcl_au=1.1.89843547.1666207201; _gaSessionTime=2022-10-20 04:20:00; _gaReferrer=https://www.dmm.co.jp/; i3_ab=0540ea94-67ce-40a6-87f4-cf65e3d5c669; AMP_TOKEN=$RETRIEVING; _ga_SFMSWE0TVN=GS1.1.1666207201.1.0.1666207201.0.0.0; _ga=GA1.1.1856807291.1666207201; _ga_G34HHM5C8N=GS1.1.1666206782.2.1.1666207201.0.0.0; age_check_done=1");
                request = builder.build();
            } else if (e.getKey().equals("avp")) {
                request = builder.build();
            } else if (e.getKey().equals("r18")) {
                request = builder.build();
            }

            return new DataPair<String, Request>(e.getKey(), request);
        }).collect(Collectors.toMap(DataPair::getKey, DataPair::getValue));

        // request map
        ConcurrentMap<String, String> concurrentMap = requstsMap.entrySet().parallelStream().map(e -> {
            Request request = e.getValue();
            String result = "";
            try (Response execute = okHttpClient.newCall(request).execute();) {
                if (execute.code() != 200) {
                    logging.info("无法获取响应：" + request.url());
                } else {
                    result = execute.body().string();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                // 重试次数
                int retry = 0;
                while (retry < 3) {
                    try (Response execute = okHttpClient.newCall(request).execute();) {
                        if (null != execute && execute.code() != 200) {
                            logging.info("无法获取响应：" + request.url());
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
            return new DataPair<String, String>(e.getKey(), result);
        }).collect(Collectors.toConcurrentMap(DataPair::getKey, DataPair::getValue));

        String videoUrl = "";
        // 处理r18页面
        // String r18 = concurrentMap.get("r18");
        // if (!Strings.isNullOrEmpty(r18)) {
        //     Document document = Jsoup.parse(r18);
        //     Elements elements = document.selectXpath("//*[@id=\"contents\"]/div[2]/section/ul[2]/li/div/p/a");
        //     if (!elements.isEmpty()) {
        //         Element element = elements.get(0);
        //         videoUrl = element.attr("data-video-high");
        //         // 替换访问质量更好的cdn
        //         videoUrl = videoUrl.replace("awscc3001.r18.com", "cc3001.dmm.co.jp");
        //         return videoUrl;
        //     }
        // }

        // 处理mgs
        // String mgs = concurrentMap.get("mgs");
        // if (!Strings.isNullOrEmpty(mgs)) {
        //     Document document = Jsoup.parse(mgs);
        //     Elements elements = document.selectXpath("//*[@id=\"center_column\"]/div[2]/div/ul/li/p[1]/a");
        //     if (!elements.isEmpty()) {
        //         Element element = elements.get(0);
        //         String href = element.attr("href");
        //         href = "https://www.mgstage.com" + href;
        //         String replace = href.replace("player.html/", "Respons.php?pid=");
        //         Request.Builder builder = new Request.Builder().url(replace).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        //         builder.addHeader("Cookie", "coc=1; PHPSESSID=2kjqnoogu9ff6q76l3mdipljl5; uuid=8e414da4ef13b3933ec37649f97003c4; bWdzdGFnZS5jb20=-_lr_hb_-r2icil/mgs={\"heartbeat\":1666201658904}; adc=1");
        //         Request request = builder.build();
        //         String result = "";
        //         try (Response execute = okHttpClient.newCall(request).execute();) {
        //             if (execute.code() != 200) {
        //                 logging.info("无法获取响应：" + request.url());
        //             } else {
        //                 result = execute.body().string();
        //             }
        //         } catch (IOException e1) {
        //             e1.printStackTrace();
        //             // 重试次数
        //             int retry = 0;
        //             while (retry < 3) {
        //                 try (Response execute = okHttpClient.newCall(request).execute();) {
        //                     if (null != execute && execute.code() != 200) {
        //                         logging.info("无法获取响应：" + request.url());
        //                     }
        //                     if (null != execute && execute.code() == 200) {
        //                         result = execute.body().string();
        //                         break;
        //                     }
        //                 } catch (IOException ioException) {
        //                     ioException.printStackTrace();
        //                 }
        //                 retry++;
        //             }
        //         }
        //
        //         // 字符串转json
        //         ObjectMapper objectMapper = new ObjectMapper();
        //         try {
        //             result = result.replace("\\", "");
        //             MsgVideoUrl videoTmpUrl = objectMapper.readValue(result, MsgVideoUrl.class);
        //             String url = videoTmpUrl.getUrl();
        //             String mp4 = url.split("\\?")[0].replace("ism/request", "mp4");
        //             videoUrl = mp4;
        //             return videoUrl;
        //         } catch (JsonProcessingException e) {
        //             e.printStackTrace();
        //             // throw new RuntimeException(e);
        //         }
        //     }
        // }

        // 处理dmm
        // String dmm = concurrentMap.get("dmm");
        // if (!Strings.isNullOrEmpty(dmm)) {
        //     Document document = Jsoup.parse(dmm);
        //     Elements elements = document.selectXpath("//*[@id=\"list\"]/li/div/p[4]/a");
        //     if (!elements.isEmpty()) {
        //         Element element = elements.get(0);
        //         String href = element.attr("href");
        //         // 获取cid
        //         String[] split = href.split("=");
        //         String s = split[split.length - 1];
        //         href = "https://www.dmm.co.jp/service/digitalapi/-/html5_player/=/cid=" + s + "/mtype=AhRVShI_/service=digital/floor=videoa/mode=list/";
        //         // 请求视频页面
        //         Request.Builder builder = new Request.Builder().url(href).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        //         builder.addHeader("Cookie", "_gali=naviapi-search-submit; ckcy=1; mbox=check#true#1666207260|session#1666207199640-935175#1666209060; __utma=125690133.1119214179.1666207200.1666207200.1666207200.1; __utmb=125690133.0.10.1666207200; __utmc=125690133; __utmz=125690133.1666207200.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); app_uid=ygb2CmNQTeBsnSRFDGGZAg==; _gcl_au=1.1.89843547.1666207201; _gaSessionTime=2022-10-20 04:20:00; _gaReferrer=https://www.dmm.co.jp/; i3_ab=0540ea94-67ce-40a6-87f4-cf65e3d5c669; AMP_TOKEN=$RETRIEVING; _ga_SFMSWE0TVN=GS1.1.1666207201.1.0.1666207201.0.0.0; _ga=GA1.1.1856807291.1666207201; _ga_G34HHM5C8N=GS1.1.1666206782.2.1.1666207201.0.0.0; age_check_done=1");
        //
        //         Request request = builder.build();
        //         String result = "";
        //         try (Response execute = okHttpClient.newCall(request).execute();) {
        //             if (execute.code() != 200) {
        //                 logging.info("无法获取响应：" + request.url());
        //             } else {
        //                 result = execute.body().string();
        //             }
        //         } catch (IOException e1) {
        //             e1.printStackTrace();
        //             // 重试次数
        //             int retry = 0;
        //             while (retry < 3) {
        //                 try (Response execute = okHttpClient.newCall(request).execute();) {
        //                     if (null != execute && execute.code() != 200) {
        //                         logging.info("无法获取响应：" + request.url());
        //                     }
        //                     if (null != execute && execute.code() == 200) {
        //                         result = execute.body().string();
        //                         break;
        //                     }
        //                 } catch (IOException ioException) {
        //                     ioException.printStackTrace();
        //                 }
        //                 retry++;
        //             }
        //         }
        //
        //         Document parse = Jsoup.parse(result);
        //         //内容在js里面
        //         Elements selectXpath = parse.select("script");
        //         if (!selectXpath.isEmpty()) {
        //             Element videoNode = selectXpath.get(5);
        //             Node node = videoNode.childNodes().get(0);
        //             String source = node.toString();
        //             //截取地址
        //             String[] tempStr = source.split("\\{\"bitrate\":3000,\"src\":");
        //             String videoPart = tempStr[1];
        //             String[] tempStr2 = videoPart.split("}],\"affiliateId\":");
        //             String videoStrRaw = tempStr2[0];
        //             String videoSrc = videoStrRaw.replace("\\", "")
        //                     .replace("\"","")
        //                     .replace("//", "https://");
        //             videoUrl = videoSrc;
        //             return videoUrl;
        //         }
        //     }
        // }

        // 处理avp
        String avp = concurrentMap.get("avp");
        if (!Strings.isNullOrEmpty(avp)) {
            Document document = Jsoup.parse(avp);
            Elements elements = document.selectXpath("//*[@id=\"maincontainer\"]/div[2]/div/div/div/a");
            if (!elements.isEmpty()) {
                Element element = elements.get(0);
                String href = element.attr("href");
                String[] strs = href.split("/");
                String queryCode = strs[strs.length - 1];

                href = "https://avpreview.com/API/v1.0/index.php?system=videos&action=detail&contentid=" + queryCode +
                        "&sitecode=avpreview&ip=&token=";

                Request.Builder builder = new Request.Builder().url(href).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");

                Request request = builder.build();
                String result = "";
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (execute.code() != 200) {
                        logging.info("无法获取响应：" + request.url());
                    } else {
                        result = execute.body().string();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                    // 重试次数
                    int retry = 0;
                    while (retry < 3) {
                        try (Response execute = okHttpClient.newCall(request).execute();) {
                            if (null != execute && execute.code() != 200) {
                                logging.info("无法获取响应：" + request.url());
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

                // 抽取m3u8地址 最后转换为mp4地址
                String[] split = result.split("\"videos\":");
                String needPart = split[1];
                String[] videoPart = needPart.split(",\"country\":");
                String s = videoPart[0];
                String[] tempStr = s.split("\"trailer\":");
                String videoUrlPart = tempStr[tempStr.length - 1];
                String[] tempStr2 = videoUrlPart.split(",\"saved\":0");
                String videoTmpStr = tempStr2[0];
                String replace = videoTmpStr.replace("\"", "")
                        .replace("\\", "")
                        .replace("/hlsvideo/", "/litevideo/")
                        .replace("/playlist.m3u8", "");
                // 默认获取720
                String mp4Url = replace + "/" + queryCode + "_mhb_w.mp4";
                videoUrl = mp4Url;
            }
        }

        return videoUrl;
    }
}
