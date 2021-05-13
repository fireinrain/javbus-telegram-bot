package com.sunrise.spider;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 4:27 AM
 */
public class JavbusHelper {
    public static int defaultPageSize = 30;

    public static String normalCode(String code) {
        if (!code.contains("-")) {
            assert code.length() >= 3;
            String number = code.substring(code.length() - 3);
            String alpha = code.substring(0, code.length() - 3);
            String s = alpha.trim() + "-" + number.trim();
            return s.toUpperCase(Locale.ROOT);
        }
        return code.toUpperCase(Locale.ROOT);

    }

    /**
     * 获取演员首页所有番号
     *
     * @param url
     * @return
     */
    public static List<String> getStarAllCodeNrFanHao(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).get().build();

        Response execute = null;
        try {
            execute = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = null;
        try {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = Jsoup.parse(result);

        Elements contentContainer = document.select("body > div.wrap.mt30 > ul > li");

        List<String> collect = contentContainer.stream()
                .map(e -> e.text())
                .collect(Collectors.toList());

        return collect;

    }

    /**
     * 将字符串转换成url中的编码格式字符串
     *
     * @param str
     * @return
     */
    public static String parseStrToUrlEncoder(String str) {
        String encode = null;
        try {
            encode = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encode;
    }

    /**
     * 判断是否是日期
     *
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
     * 计算分页信息，用作后面遍历作品页
     *
     * @param total
     * @param pageSize
     * @return
     */
    public static int[] caculatePageInfo(int total, int pageSize) {
        int totalPageNum = (total + pageSize - 1) / pageSize;
        int left = total - (totalPageNum - 1) * pageSize;
        int[] ints = new int[2];
        ints[0] = totalPageNum;
        ints[1] = left;
        return ints;
    }

    /**
     * 计算默认分页
     *
     * @param total
     * @return
     */
    public static int[] caculatePageInfo(int total) {
        return caculatePageInfo(total, defaultPageSize);
    }

    /**
     * 判断code 时候是欧美类型
     * 1. 开头以多余5个英文字符开口
     * @param args
     */

    /**
     * 判断是否是数字开头
     *
     * @param code
     * @return
     */
    public static boolean startWithNumber(String code) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(code.charAt(0) + "");
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    public static void main(String[] args) {
        System.out.println(normalCode("abp334"));
        caculatePageInfo(10, 30);

        //System.out.println(parseStrToUrlEncoder("つかさ"));

        System.out.println(startWithNumber("123434-1"));

    }
}
