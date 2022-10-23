package com.sunrise.javbusbot.storege;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : fireinrain
 * @description: 统计数据
 * @site : https://github.com/fireinrain
 * @file : QueryStaticEntity
 * @software: IntelliJ IDEA
 * @time : 2022/10/23 8:35 PM
 */

public class QueryStaticEntity {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 当日查询
     */
    private Integer todayQueryCounts;

    /**
     * 全部查询数量
     */
    private Integer totalQueryCounts;

    public QueryStaticEntity(Integer todayQueryCounts, Integer totalQueryCounts) {
        this.todayQueryCounts = todayQueryCounts;
        this.totalQueryCounts = totalQueryCounts;
    }

    public QueryStaticEntity() {
    }

    public Integer getTodayQueryCounts() {
        return todayQueryCounts;
    }

    public void setTodayQueryCounts(Integer todayQueryCounts) {
        this.todayQueryCounts = todayQueryCounts;
    }

    public Integer getTotalQueryCounts() {
        return totalQueryCounts;
    }

    public void setTotalQueryCounts(Integer totalQueryCounts) {
        this.totalQueryCounts = totalQueryCounts;
    }

    public String getMaxDateTimeBounds() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = now.format(dateTimeFormatter);
        String[] s = dateTimeStr.split(" ");
        return s[0] + " 23:59:59";
    }

    public String getMinDateTimeBounds() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = now.format(dateTimeFormatter);
        String[] s = dateTimeStr.split(" ");
        return s[0] + " 00:00:00";
    }

    @Override
    public String toString() {
        return "QueryStaticEntity{" +
                "todayQueryCounts=" + todayQueryCounts +
                ", totalQueryCounts=" + totalQueryCounts +
                '}';
    }

    public String getPrettyTgMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("当前Javbus-Telegram-Bot机器人服务统计: \n");
        sb.append("------------------------------------------\n");
        sb.append("今日已接收查询次数: " + this.todayQueryCounts + "\n");
        sb.append("历史接收查询次数: " + this.totalQueryCounts + "\n");
        sb.append("------------------------------------------\n");
        sb.append("生活不易, 记得喝水\uD83E\uDD17\uD83E\uDD17\uD83E\uDD17");
        return sb.toString();
    }
}
