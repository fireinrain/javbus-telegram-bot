package com.sunrise.spider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 4:47 PM
 */
public class JavbusDataItem {
    //访问url
    private String visitUrl;

    //封面大图
    private String bigImgUrl;

    //标题
    private String titleStr;

    //识别码
    private String code;

    //发行日期
    private String publishDate;

    //片长
    private String totalTime;

    //导演
    private String director;

    //制作商
    private String produceCompany;

    //发行商
    private String publishCompany;

    //系列
    private String series;

    //类别
    private String types;

    //演员
    private String stars;

    //样品图地址
    private List<String> sampleImgs;

    //磁力连接地址
    private List<MagnentItem> magnents;

    public JavbusDataItem() {
        this.bigImgUrl = "";
        this.titleStr = "";
        this.code = "";
        this.publishDate = "";
        this.totalTime = "";
        this.director = "";
        this.produceCompany = "";
        this.publishCompany = "";
        this.series = "";
        this.types = "";
        this.stars = "";
    }

    //拆分类型
    public List<String> typesToTypeList() {
        Objects.requireNonNull(this.types);
        if (types.contains(" ")) {
            return Arrays.stream(types.split(" ")).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    //拆分演员
    public List<String> starsToStarList() {
        Objects.requireNonNull(this.stars);
        if (stars.contains(" ")) {
            return Arrays.stream(stars.split(" ")).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    public String getBigImgUrl() {
        return bigImgUrl;
    }

    public void setBigImgUrl(String bigImgUrl) {
        this.bigImgUrl = bigImgUrl;
    }

    public String getTitleStr() {
        return titleStr;
    }

    public void setTitleStr(String titleStr) {
        this.titleStr = titleStr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getProduceCompany() {
        return produceCompany;
    }

    public void setProduceCompany(String produceCompany) {
        this.produceCompany = produceCompany;
    }

    public String getPublishCompany() {
        return publishCompany;
    }

    public void setPublishCompany(String publishCompany) {
        this.publishCompany = publishCompany;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public List<String> getSampleImgs() {
        return sampleImgs;
    }

    public void setSampleImgs(List<String> sampleImgs) {
        this.sampleImgs = sampleImgs;
    }

    public List<MagnentItem> getMagnents() {
        return magnents;
    }

    public void setMagnents(List<MagnentItem> magnents) {
        this.magnents = magnents;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getVisitUrl() {
        return visitUrl;
    }

    public void setVisitUrl(String visitUrl) {
        this.visitUrl = visitUrl;
    }

    @Override
    public String toString() {
        return "JavbusDataItem{" +
                "visitUrl='" + visitUrl + '\'' +
                ", bigImgUrl='" + bigImgUrl + '\'' +
                ", titleStr='" + titleStr + '\'' +
                ", code='" + code + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", totalTime='" + totalTime + '\'' +
                ", director='" + director + '\'' +
                ", produceCompany='" + produceCompany + '\'' +
                ", publishCompany='" + publishCompany + '\'' +
                ", series='" + series + '\'' +
                ", types='" + types + '\'' +
                ", stars='" + stars + '\'' +
                ", sampleImgs=" + sampleImgs +
                ", magnents=" + magnents +
                '}';
    }

    public String toPrettyStr() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("封面：").append(bigImgUrl).append("\n")
                .append("访问链接：").append(visitUrl).append("\n")
                .append("标题：").append(titleStr).append("\n")
                .append("识别码：").append(code).append("\n")
                .append("发布时间：").append(publishDate).append("\n")
                .append("导演：").append(director).append("\n")
                .append("制作商：").append(produceCompany).append("\n")
                .append("发行商：").append(publishCompany).append("\n")
                .append("类型：").append(types).append("\n")
                .append("类别：").append(series).append("\n")
                .append("演员：").append(stars).append("\n")
                .append("#").append(code.replace("-", ""));

        return stringBuilder.toString();

    }

    public String toPrettySampleImgs() {
        StringBuilder stringBuilder = new StringBuilder();
        if (sampleImgs != null && sampleImgs.size() > 0) {
            stringBuilder.append("样品图地址：\n");
            for (String sampleImg : sampleImgs) {
                stringBuilder.append(sampleImg + "\n");
            }
        }
        return stringBuilder.toString();
    }

    public String toPrettyMagnetStrs() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("磁力链接地址：\n");
        if (magnents != null && magnents.size() > 0) {
            for (int i = 0; i < magnents.size(); i++) {
                if (i < 5) {
                    stringBuilder.append("-----------------------------------------------------\n");
                    stringBuilder.append(magnents.get(i).toPrettyStr() + "\n");
                }
            }
        } else {
            stringBuilder.append("暂无\n");
        }
        return stringBuilder.append("#").append(code.replace("-","")).toString();
    }

    public List<List<String>> sliceSampleImgUrlForupload() {
        ArrayList<List<String>> lists = new ArrayList<>();
        if (null != sampleImgs && !sampleImgs.isEmpty()) {
            //(先计算出余数)
            //int remainder = sampleImgs.size() % 10;
            //然后是商
            int number = (int) Math.ceil((float) sampleImgs.size() / 10);

            if (number == 0) {
                lists.add(sampleImgs);
                return lists;
            } else {
                for (int i = 1; i <= number; i++) {

                    if (i == number) {
                        List<String> strings = sampleImgs.subList((number - 1) * 10, sampleImgs.size());
                        lists.add(strings);
                    } else {
                        List<String> strings = sampleImgs.subList((i - 1) * 10, i * 10);
                        lists.add(strings);
                    }

                }
            }

        }

        return lists;
    }
}
