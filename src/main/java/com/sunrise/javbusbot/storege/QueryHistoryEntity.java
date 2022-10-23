package com.sunrise.javbusbot.storege;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : QueryHistoryEntity
 * @software: IntelliJ IDEA
 * @time : 2022/10/23 5:25 PM
 */

public class QueryHistoryEntity {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // id
    private Integer id;
    // 查询命令
    private String queryCommand;
    // 查询字符串
    private String queryStr;
    // 查询消息
    private String queryText;
    // 创建时间
    private String updateTime;

    public QueryHistoryEntity() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = now.format(dateTimeFormatter);
        this.updateTime = dateTimeStr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQueryCommand() {
        return queryCommand;
    }

    public void setQueryCommand(String queryCommand) {
        this.queryCommand = queryCommand;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "QueryHistoryEntity{" +
                "id=" + id +
                ", queryCommand='" + queryCommand + '\'' +
                ", queryStr='" + queryStr + '\'' +
                ", queryText='" + queryText + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(new QueryHistoryEntity().toString());
    }
}
