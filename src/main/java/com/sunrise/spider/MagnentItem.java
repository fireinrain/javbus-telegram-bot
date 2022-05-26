package com.sunrise.spider;

/**
 * @description: 番号对象
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 6:52 PM
 */
public class MagnentItem {
    //标题
    private String title;

    //清晰度
    private String resolution;
    //是否有字幕
    private String subTitle;
    //文件大小
    private String fileSize;
    //分享日期
    private String shareDate;

    //磁力字符串
    private String magnentStr;

    public String getMagnentStr() {
        return magnentStr;
    }

    public void setMagnentStr(String magnentStr) {
        this.magnentStr = magnentStr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getShareDate() {
        return shareDate;
    }

    public void setShareDate(String shareDate) {
        this.shareDate = shareDate;
    }

    @Override
    public String toString() {
        return "MagnentItem{" +
                "title='" + title + '\'' +
                ", resolution='" + resolution + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", shareDate='" + shareDate + '\'' +
                ", magnentStr='" + magnentStr + '\'' +
                '}';
    }

    public String toPrettyStr() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("标题：").append(title).append("\n")
                .append("清晰度：").append(resolution).append("\n")
                .append("字幕：").append(subTitle).append("\n")
                .append("文件大小：").append(fileSize).append("\n")
                .append("分享时间：").append(shareDate).append("\n")
                .append("磁力地址：").append(magnentStr);

        return stringBuilder.toString();
    }
}
