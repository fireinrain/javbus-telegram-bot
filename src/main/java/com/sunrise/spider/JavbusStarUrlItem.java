package com.sunrise.spider;

/**
 * @description: 演员主页链接对象
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/30 1:03 AM
 */
public class JavbusStarUrlItem {

    private String starName;

    private String startPageUrl;

    public JavbusStarUrlItem(String starName, String startPageUrl) {
        this.starName = starName;
        this.startPageUrl = startPageUrl;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getStartPageUrl() {
        return startPageUrl;
    }

    public void setStartPageUrl(String startPageUrl) {
        this.startPageUrl = startPageUrl;
    }

    @Override
    public String toString() {
        return "JavbusStarUrlItem{" +
                "starName='" + starName + '\'' +
                ", startPageUrl='" + startPageUrl + '\'' +
                '}';
    }
}
