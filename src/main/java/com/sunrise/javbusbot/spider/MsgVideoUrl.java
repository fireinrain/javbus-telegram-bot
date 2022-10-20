package com.sunrise.javbusbot.spider;

/**
 * @author : fireinrain
 * @description:
 * @site : https://github.com/fireinrain
 * @file : MsgVideoUrl
 * @software: IntelliJ IDEA
 * @time : 2022/10/20 2:49 AM
 */

public class MsgVideoUrl {
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "MsgVideoUrl{" +
                "url='" + url + '\'' +
                '}';
    }
}
