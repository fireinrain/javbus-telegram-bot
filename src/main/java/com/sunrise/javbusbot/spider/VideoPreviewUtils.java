package com.sunrise.javbusbot.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.sunrise.javbusbot.tgbot.TgBotConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : VideoPreviewUtils
 * @software: IntelliJ IDEA
 * @time : 2022/10/20 5:46 AM
 */

public class VideoPreviewUtils {
    private static final Logger logger = LoggerFactory.getLogger(VideoPreviewUtils.class);

    private static String proxyHost = TgBotConfig.PROXY_HOST;

    private static int proxyPort = TgBotConfig.PROXY_PORT;

    private static boolean enableProxy = TgBotConfig.ENABLE_PROXY;

    /**
     * 获取预览视频地址
     *
     * @param codeStr
     * @param titleStr
     * @return
     */
    public static String getVideoPreviewUrl(String codeStr, String titleStr) {
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                // 连接超时
                .connectTimeout(60 * 6, TimeUnit.SECONDS)
                // 读取超时
                .readTimeout(60 * 6, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(60 * 6, TimeUnit.SECONDS);

        if (enableProxy) {
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
            okHttpClient = clientBuilder.proxy(proxy).build();
        } else {
            okHttpClient = clientBuilder.build();
        }
        // okHttpClient.newCall()
        HashMap<String, String> urlMap = new HashMap<>();
        urlMap.put("r18", "https://www.r18.com/common/search/order=match/searchword=${code}/");
        urlMap.put("dmm", "https://www.dmm.co.jp/digital/videoa/-/list/search/=/?searchstr=${title}");
        urlMap.put("mgs", "https://www.mgstage.com/search/cSearch.php?search_word=${title}&x=26&y=8&search_shop_id=&type=top");
        urlMap.put("avp", "https://avpreview.com/zh/search?keywords=${code}");


        String code = codeStr;
        String title = titleStr;
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
                builder.addHeader("Cookie", "coc=1; uuid=8e414da4ef13b3933ec37649f97003c4; bWdzdGFnZS5jb20=-_lr_hb_-r2icil/mgs={\"heartbeat\":1666201658904}; adc=1");
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
        OkHttpClient finalOkHttpClient = okHttpClient;
        ConcurrentMap<String, String> concurrentMap = requstsMap.entrySet().parallelStream().map(e -> {
            Request request = e.getValue();
            String result = "";
            try (Response execute = finalOkHttpClient.newCall(request).execute();) {
                if (execute.code() != 200) {
                    logger.info("无法获取响应：" + request.url());
                } else {
                    result = execute.body().string();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                // 重试次数
                int retry = 0;
                while (retry < 3) {
                    try (Response execute = finalOkHttpClient.newCall(request).execute();) {
                        if (null != execute && execute.code() != 200) {
                            logger.info("无法获取响应：" + request.url());
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
        String r18 = concurrentMap.get("r18");
        if (!Strings.isNullOrEmpty(r18)) {
            Document document = Jsoup.parse(r18);
            Elements elements = document.selectXpath("//*[@id=\"contents\"]/div[2]/section/ul[2]/li/div/p/a");
            if (!elements.isEmpty()) {
                Element element = elements.get(0);
                videoUrl = element.attr("data-video-high");
                // 替换访问质量更好的cdn
                videoUrl = videoUrl.replace("awscc3001.r18.com", "cc3001.dmm.co.jp");
                return videoUrl;
            }
        }

        // 处理mgs
        String mgs = concurrentMap.get("mgs");
        if (!Strings.isNullOrEmpty(mgs)) {
            Document document = Jsoup.parse(mgs);
            Elements elements = document.selectXpath("//*[@id=\"center_column\"]/div[2]/div/ul/li/p[1]/a");
            if (!elements.isEmpty()) {
                Element element = elements.get(0);
                String href = element.attr("href");
                href = "https://www.mgstage.com" + href;
                String replace = href.replace("player.html/", "Respons.php?pid=");
                Request.Builder builder = new Request.Builder().url(replace).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
                builder.addHeader("Cookie", "coc=1; PHPSESSID=2kjqnoogu9ff6q76l3mdipljl5; uuid=8e414da4ef13b3933ec37649f97003c4; bWdzdGFnZS5jb20=-_lr_hb_-r2icil/mgs={\"heartbeat\":1666201658904}; adc=1");
                Request request = builder.build();
                String result = "";
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (execute.code() != 200) {
                        logger.info("无法获取响应：" + request.url());
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
                                logger.info("无法获取响应：" + request.url());
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
                // 字符串转json
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    result = result.replace("\\", "");
                    MsgVideoUrl videoTmpUrl = objectMapper.readValue(result, MsgVideoUrl.class);
                    String url = videoTmpUrl.getUrl();
                    String mp4 = url.split("\\?")[0].replace("ism/request", "mp4");
                    videoUrl = mp4;
                    return videoUrl;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    // throw new RuntimeException(e);
                }
            }
        }
        // 处理dmm
        String dmm = concurrentMap.get("dmm");
        if (!Strings.isNullOrEmpty(dmm)) {
            Document document = Jsoup.parse(dmm);
            Elements elements = document.selectXpath("//*[@id=\"list\"]/li/div/p[4]/a");
            if (!elements.isEmpty()) {
                Element element = elements.get(0);
                String href = element.attr("href");
                // 获取cid
                String[] split = href.split("=");
                String s = split[split.length - 1];
                href = "https://www.dmm.co.jp/service/digitalapi/-/html5_player/=/cid=" + s + "/mtype=AhRVShI_/service=digital/floor=videoa/mode=list/";
                // 请求视频页面
                Request.Builder builder = new Request.Builder().url(href).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
                builder.addHeader("Cookie", "_gali=naviapi-search-submit; ckcy=1; mbox=check#true#1666207260|session#1666207199640-935175#1666209060; __utma=125690133.1119214179.1666207200.1666207200.1666207200.1; __utmb=125690133.0.10.1666207200; __utmc=125690133; __utmz=125690133.1666207200.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); app_uid=ygb2CmNQTeBsnSRFDGGZAg==; _gcl_au=1.1.89843547.1666207201; _gaSessionTime=2022-10-20 04:20:00; _gaReferrer=https://www.dmm.co.jp/; i3_ab=0540ea94-67ce-40a6-87f4-cf65e3d5c669; AMP_TOKEN=$RETRIEVING; _ga_SFMSWE0TVN=GS1.1.1666207201.1.0.1666207201.0.0.0; _ga=GA1.1.1856807291.1666207201; _ga_G34HHM5C8N=GS1.1.1666206782.2.1.1666207201.0.0.0; age_check_done=1");

                Request request = builder.build();
                String result = "";
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (execute.code() != 200) {
                        logger.info("无法获取响应：" + request.url());
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
                                logger.info("无法获取响应：" + request.url());
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

                Document parse = Jsoup.parse(result);
                // 内容在js里面
                Elements selectXpath = parse.select("script");
                if (!selectXpath.isEmpty()) {
                    Element videoNode = selectXpath.get(5);
                    Node node = videoNode.childNodes().get(0);
                    String source = node.toString();
                    // 截取地址
                    String[] tempStr = source.split("\\{\"bitrate\":3000,\"src\":");
                    String videoPart = tempStr[1];
                    String[] tempStr2 = videoPart.split("}],\"affiliateId\":");
                    String videoStrRaw = tempStr2[0];
                    String videoSrc = videoStrRaw.replace("\\", "").replace("\"", "").replace("//", "https://");
                    videoUrl = videoSrc;
                    return videoUrl;
                }
            }
        }
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

                href = "https://avpreview.com/API/v1.0/index.php?system=videos&action=detail&contentid=" + queryCode + "&sitecode=avpreview&ip=&token=";

                Request.Builder builder = new Request.Builder().url(href).get().addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").addHeader("Accept-Language", "zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6").addHeader("Cache-Control", "no-cache").addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");

                Request request = builder.build();
                String result = "";
                try (Response execute = okHttpClient.newCall(request).execute();) {
                    if (execute.code() != 200) {
                        logger.info("无法获取响应：" + request.url());
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
                                logger.info("无法获取响应：" + request.url());
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
                String replace = videoTmpStr.replace("\"", "").replace("\\", "").replace("/hlsvideo/", "/litevideo/").replace("/playlist.m3u8", "");
                // 默认获取720
                String mp4Url = replace + "/" + queryCode + "_mhb_w.mp4";
                videoUrl = mp4Url;
            }
        }
        return videoUrl;
    }

    /**
     * 获取作品预览视频地址
     *
     * @param javbusDataItem
     * @return
     */
    public static String getFilmPreviewUrl(JavbusDataItem javbusDataItem) {
        // code 番号, studio 制作商, title 标题
        String title = javbusDataItem.getTitleStr().trim();
        String code = javbusDataItem.getCode().toLowerCase();
        String studio = javbusDataItem.getProduceCompany();

        // 处理特殊地址番号
        if (!Strings.isNullOrEmpty(studio)) {
            HashMap<String, String> companysMap = new HashMap<>();
            companysMap.put("東京熱", "https://my.cdn.tokyo-hot.com/media/samples/%s.mp4");
            companysMap.put("カリビアンコム", "https://smovie.caribbeancom.com/sample/movies/%s/1080p.mp4");
            companysMap.put("一本道", "http://smovie.1pondo.tv/sample/movies/%s/1080p.mp4");
            companysMap.put("HEYZO", "https://www.heyzo.com/contents/3000/%s/heyzo_hd_%s_sample.mp4");
            if (companysMap.containsKey(studio)) {
                String videoTmpUrl = companysMap.get(studio);
                if ("HEYZO".equals(studio)) {
                    code = code.replace("HEYZO-", "");
                }
                return videoTmpUrl.replace("%s", code);
            }
        }
        return getVideoPreviewUrl(code, title);
    }

    /**
     * 获取视频元数据
     * 高 宽 时长
     *
     * @param inputStream
     * @return
     */
    public static ArrayList<Integer> getVideoMetaData(InputStream inputStream) {
        ArrayList<Integer> result = new ArrayList<>();
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);

        // IsoFile isoFile = new IsoFile("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4");
        IsoFile isoFile = null;
        try {
            isoFile = new IsoFile(readableByteChannel);
            MovieBox movieBox = org.mp4parser.tools.Path.getPath(isoFile, "moov");
            // 可以打印这个 movieBox  toString 看看里面有啥
            List<Box> boxes = movieBox.getBoxes();
            // 宽高时长获取
            long durationLong = (long) isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            int duration = (int) durationLong;
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
            logger.info("预览视频信息: ");
            logger.info("视频宽度: " + width);
            logger.info("视频高度: " + height);
            logger.info("视频长度: " + duration + "s");
            result.add(height);
            result.add(width);
            result.add(duration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                isoFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    // 获取视频高度 宽度
    public static DataPair<Integer, Integer> getVideoHeightAndWidth(InputStream inputStream) {
        DataPair<Integer, Integer> result = null;
        MetaDataHelp metaDataHelp = new MetaDataHelp();
        try {
            metaDataHelp.find(inputStream);
            result = new DataPair<Integer, Integer>(metaDataHelp.metaHeight, metaDataHelp.metaWidth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // 获取视频时长 单位秒
    public static int getVideoDuration(InputStream inputStream) {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        IsoFile isoFile = null;
        try {
            isoFile = new IsoFile(readableByteChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long durationLong = (long) isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        int duration = (int) durationLong;
        logger.info(String.valueOf(duration));
        return duration;
    }

    private static class MetaDataHelp {
        List<String> containers = Arrays.asList(
                "moov",
                "mdia",
                "trak"
        );
        byte[] lastTkhd;

        int metaHeight = 0;

        int metaWidth = 0;

        private void find(InputStream fis) throws IOException {

            while (fis.available() > 0) {
                byte[] header = new byte[8];
                fis.read(header);

                long size = readUint32(header, 0);
                String type = new String(header, 4, 4, "ISO-8859-1");
                if (containers.contains(type)) {
                    find(fis);
                } else {
                    if (type.equals("tkhd")) {
                        lastTkhd = new byte[(int) (size - 8)];
                        fis.read(lastTkhd);
                    } else {
                        if (type.equals("hdlr")) {
                            byte[] hdlr = new byte[(int) (size - 8)];
                            fis.read(hdlr);
                            if (hdlr[8] == 0x76 && hdlr[9] == 0x69 && hdlr[10] == 0x64 && hdlr[11] == 0x65) {
                                double width = readFixedPoint1616(lastTkhd, lastTkhd.length - 8);
                                double height = readFixedPoint1616(lastTkhd, lastTkhd.length - 4);
                                logger.info("Video Track Header identified");
                                logger.info("width: " + width);
                                logger.info("height: " + height);
                                metaWidth = (int) width;
                                metaHeight = (int) height;
                                return;
                            }
                        } else {
                            fis.skip(size - 8);
                        }
                    }
                }
            }
        }

        public long readUint32(byte[] b, int s) {
            long result = 0;
            result |= ((b[s + 0] << 24) & 0xFF000000);
            result |= ((b[s + 1] << 16) & 0xFF0000);
            result |= ((b[s + 2] << 8) & 0xFF00);
            result |= ((b[s + 3]) & 0xFF);
            return result;
        }

        public double readFixedPoint1616(byte[] b, int s) {
            return ((double) readUint32(b, s)) / 65536;
        }

    }

    public static void main(String[] args) throws IOException {
        MetaDataHelp metaDataHelp = new MetaDataHelp();
        FileInputStream fis = new FileInputStream(new File("/Users/sunrise/Downloads/1fsdss408_dmb_w.mp4"));
        metaDataHelp.find(fis);
        System.out.println(metaDataHelp.metaHeight);
        System.out.println(metaDataHelp.metaWidth);

        getVideoDuration(new FileInputStream("/Users/sunrise/Downloads/Telegram Desktop/ipx00768_dmb_w.mp4"));

    }

}
