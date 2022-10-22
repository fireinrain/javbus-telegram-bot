package com.sunrise.javbusbot.spider;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description: 辅助类 主要包含判断是否为欧美 判断番号类型
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/25 4:27 AM
 */
public class JavbusHelper {
    public static final Logger logger = LoggerFactory.getLogger(JavbusHelper.class);

    public static int defaultPageSize = 30;

    public static String normalCode(String code) {
        // 欧美番号
        if (code.length() >= 8) {
            code = code.replaceAll("\\.", "-");
            return code.toUpperCase(Locale.ROOT);
        }
        // 无码
        if (startWithNumber(code)) {
            code = code.replaceAll("_", "-");
            return code.toUpperCase(Locale.ROOT);
        }

        // 日本
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

        String result = "";
        try (Response execute = okHttpClient.newCall(request).execute()) {
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document document = Jsoup.parse(result);

        Elements contentContainer = document.select("body > div.wrap.mt30 > ul > li");

        List<String> collect = contentContainer.stream().map(e -> e.text()).collect(Collectors.toList());

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
        if (code.length() <= 9 && code.length() > 0) {
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(code.charAt(0) + "");
            if (!isNum.matches()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 判断字符为字母开头
     *
     * @param starName
     * @return
     */
    public static boolean startWithAlpha(String starName) {
        Pattern pattern = Pattern.compile("[a-zA-Z]*");
        Matcher isNum = pattern.matcher(starName.charAt(0) + "");
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否是英语作品
     *
     * @param code
     * @return
     */
    public static boolean isforeignProduct(String code) {
        String s = code.replaceAll("-", "");
        // 移除可能存在的日期
        // https://www.javbus.com/FSDSS-408_2022-05-12
        s = s.split("_")[0];
        if (s.length() <= 9) {
            return false;
        }
        return true;
    }

    /**
     * 移除日本番号可能存在的日期
     *
     * @param filmCode
     * @return
     */
    public static String removeDateFromFilmCode(String filmCode) {
        String[] s = filmCode.split("_");
        String dateStr = s[s.length - 1];
        try {
            LocalDate date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return filmCode;
        }
        return s[0];
    }


    public static void main(String[] args) {
        logger.info(normalCode("abp334"));
        caculatePageInfo(10, 30);

        logger.info(parseStrToUrlEncoder("つかさ"));

        logger.info("{}", startWithNumber("123434-1"));
        logger.info("{}", startWithAlpha("我是sads"));
        logger.info("{}", isforeignProduct("DayWithAPornstar.20.04.21"));
        logger.info(normalCode("DDFBusty.16.10.25"));

    }


}
