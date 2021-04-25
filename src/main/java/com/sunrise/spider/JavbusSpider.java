package com.sunrise.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 2:42 PM
 */
public class JavbusSpider {
    private static String proxyHost = "127.0.0.1";

    private static int proxyPort = 7891;

    private static String baseUrl = "https://www.javbus.com/";

    private static OkHttpClient okHttpClient;

    static {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
        okHttpClient = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
    }

    public static List<JavbusDataItem> fetchFilmsInfoByName(String starName) {

        return null;
    }

    /**
     * 按照番号获取该番号信息
     * @param fileCode
     * @return
     */
    public static JavbusDataItem fetchFilmInFoByCode(String fileCode) {
        String filmReqUrl = baseUrl + fileCode;

        JavbusDataItem javbusDataItem = new JavbusDataItem();

        Request request = new Request.Builder().url(filmReqUrl).get().build();

        Response execute = null;
        try {
            execute = okHttpClient.newCall(request).execute();
            if (execute.code() != 200){
                System.out.println("无法查询："+filmReqUrl);
                return javbusDataItem;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(execute.body().string());

        Document document = Jsoup.parse(result);

        Elements contentContainer = document.select("body > div.container");

        Elements body = document.select("body");
        //访问链接
        javbusDataItem.setVisitUrl(filmReqUrl);
        //标题
        parseImageAndTitleContent(javbusDataItem, contentContainer);

        //简介内容处理
        parseIntroduceContent(javbusDataItem, contentContainer);

        //抽取样品图地址
        parsetSampleImgsContent(javbusDataItem, contentContainer);

        //抽取磁力链接
        parseMagnentContent(javbusDataItem, body, filmReqUrl);

        return javbusDataItem;
    }

    private static void parsetSampleImgsContent(JavbusDataItem javbusDataItem, Elements contentContainer) {
        Elements sampleWall = contentContainer.select("#sample-waterfall");

        Element sampleImgsEl = sampleWall.get(0);
        List<Node> childNodes = sampleImgsEl.childNodes();
        ArrayList<String> sampleUrls = new ArrayList<>();
        for (Node childNode : childNodes) {
            if (childNode instanceof Element) {
                Element node = (Element) childNode;
                String href = node.attr("href");
                sampleUrls.add(href);
                //System.out.println(href);
            }
        }
        javbusDataItem.setSampleImgs(sampleUrls);
    }

    private static void parseImageAndTitleContent(JavbusDataItem javbusDataItem, Elements contentContainer) {
        //title
        Elements titleAndImg = contentContainer.select("div.row.movie > div.col-md-9.screencap > a > img");

        //图片抽取
        Element element = titleAndImg.get(0);
        //大图
        String bigImgUrl = element.attr("src");
        javbusDataItem.setBigImgUrl(bigImgUrl);
        //标题
        String titleStr = element.attr("title");
        javbusDataItem.setTitleStr(titleStr);
    }

    /**
     * 抽取简介内容
     * @param javbusDataItem
     * @param elements
     */
    public static void parseIntroduceContent(JavbusDataItem javbusDataItem, Elements elements) {
        Elements introductionEl = elements.select("div.row.movie > div.col-md-3.info");

        Elements pEls = introductionEl.select("p");

        List<String> contents = new ArrayList<>();

        for (int i = 0; i < pEls.size(); i++) {
            String text = pEls.get(i).text();
            //System.out.println(text);

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

    }

    /**
     * 抽取磁力内容
     * @param javbusDataItem
     * @param body
     * @param fileReqUrl
     */
    public static void parseMagnentContent(JavbusDataItem javbusDataItem, Elements body, String fileReqUrl) {
        //获取磁力连接
        Elements params = body.select("script:nth-child(9)");
        // https://www.javbus.com/ajax/uncledatoolsbyajax.php?gid=46298156144&lang=zh&img=https://pics.javbus.com/cover/87y2_b.jpg&uc=0&floor=734
        DataNode node = (DataNode) params.get(0).childNodes().get(0);
        String wholeData = node.getWholeData();
        //System.out.println(wholeData);
        String magnetReqUrl = getMagnetReqUrl(wholeData);

        Request magnentReq = makeMagnentReq(fileReqUrl, magnetReqUrl);
        String magnentStrs = null;
        try {
            magnentStrs = okHttpClient.newCall(magnentReq).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(magnentStrs);

        Document magnentDom = Jsoup.parse(magnentStrs);
        Element node1 = (Element) magnentDom.childNodes().get(0);
        Elements select = node1.select("body > a");

        List<MagnentItem> magnentItems = extractMagnentContent(select);
        //System.out.println(magnentItems);

        //System.out.println(javbusDataItem);
        javbusDataItem.setMagnents(magnentItems);
    }

    /**
     * 提取磁力内容到列表
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
            //System.out.println(text);
            if (isValidDate(text)) {
                listSize++;
            }
        }

        ArrayList<ArrayList<Element>> arrayLists = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            ArrayList<Element> strings = new ArrayList<>();

            for (int j = 0; j < elements.size(); j++) {
                Element e = elements.get(j);
                if (isValidDate(e.text())) {
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
            //两种情况 有清晰度标签 或者是 字幕标签
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
     * 判断是否是日期
     * @param str
     * @return
     */
    public static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            // e.printStackTrace();
            convertSuccess = false;
        }
        return convertSuccess;

    }

    /**
     * 组装磁力内容请求
     * @param dataStr
     * @return
     */
    public static String getMagnetReqUrl(String dataStr) {
        String requestBase = "https://www.javbus.com/ajax/uncledatoolsbyajax.php?";
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
     * 获取磁力链接请求头
     * @param filmreqUrl
     * @param magnentReqUrl
     * @return
     */
    public static HashMap<String, String> getMagentReqHeader(String filmreqUrl, String magnentReqUrl) {
        //需要主动替换header头中的 referer :path
        ///ajax/uncledatoolsbyajax.php?gid=46298156144&lang=zh&img=https://pics.javbus.com/cover/87y2_b.jpg&uc=0&floor=734
        File file = new File("src/main/resources/reqHeaders.txt");
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

        //替换
        hashMap.put("referer", filmreqUrl);
        String replace = magnentReqUrl.replace("https://www.javbus.com", "");
        hashMap.put(":path", replace);
        return hashMap;
    }

    /**
     * 测试方法
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args){

        SpiderJob spiderJob = new SpiderJob("FSDSS-211",JobExcutor.concurrentLinkedDeque);
        JobExcutor.doSpiderJob(spiderJob);


    }

}
