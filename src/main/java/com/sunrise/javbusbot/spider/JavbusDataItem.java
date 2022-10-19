package com.sunrise.javbusbot.spider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sunrise.javbusbot.tgbot.TgBotConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 详情页数据传输对象
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 4:47 PM
 */
public class JavbusDataItem {
    //所有作品数量
    private String allFilmCount;
    //含有磁力作品数量
    private String haveMagnentCount;
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

    //演员首页
    private List<JavbusStarUrlItem> starsPageUrls;

    //主演首页
    private JavbusStarUrlItem mainStarPageUrl;

    //样品图地址
    private List<String> sampleImgs;

    //磁力连接地址
    private List<MagnentItem> magnents;

    //重试次数
    @JsonIgnore
    private volatile int fetchRetry;

    public JavbusDataItem() {
        this.setStarsPageUrls(null);
        this.setMainStarPageUrl(null);
        this.setBigImgUrl("");
        this.setTitleStr("");
        this.setCode("");
        this.setPublishDate("");
        this.setTotalTime("");
        this.setDirector("");
        this.setProduceCompany("");
        this.setPublishCompany("");
        this.setTypes("");
        this.setStars("");
        this.setSampleImgs(null);
        this.setMagnents(null);
        this.setSeries("");
        this.setVisitUrl("");
        this.setFetchRetry(0);
        this.setAllFilmCount("");
        this.setHaveMagnentCount("");
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

    public List<JavbusStarUrlItem> getStarsPageUrls() {
        return starsPageUrls;
    }

    public void setStarsPageUrls(List<JavbusStarUrlItem> starsPageUrls) {
        this.starsPageUrls = starsPageUrls;
    }

    public JavbusStarUrlItem getMainStarPageUrl() {
        return mainStarPageUrl;
    }

    public void setMainStarPageUrl(JavbusStarUrlItem mainStarPageUrl) {
        this.mainStarPageUrl = mainStarPageUrl;
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

    public int getFetchRetry() {
        return fetchRetry;
    }

    public void setFetchRetry(int fetchRetry) {
        this.fetchRetry = fetchRetry;
    }

    public String getAllFilmCount() {
        return allFilmCount;
    }

    public void setAllFilmCount(String allFilmCount) {
        this.allFilmCount = allFilmCount;
    }

    public String getHaveMagnentCount() {
        return haveMagnentCount;
    }

    public void setHaveMagnentCount(String haveMagnentCount) {
        this.haveMagnentCount = haveMagnentCount;
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
                ", starsPageUrls=" + starsPageUrls +
                ", mainStarPageUrl='" + mainStarPageUrl + '\'' +
                ", sampleImgs=" + sampleImgs +
                ", magnents=" + magnents +
                ", fetchRetry=" + fetchRetry +
                '}';
    }

    public String toPrettyStr() {
        StringBuilder stringBuilder = new StringBuilder();
        String startPageUrl = null;
        if (null == mainStarPageUrl) {
            startPageUrl = "多演员作品，暂无主演作品主页";
        } else {
            startPageUrl = mainStarPageUrl.getStartPageUrl();
        }
        if (!bigImgUrl.contains("http://") && !bigImgUrl.contains("https://")) {
            bigImgUrl = TgBotConfig.SPIDER_BASE_URL + bigImgUrl;
        }

        stringBuilder
                .append("封面：").append(bigImgUrl).append("\n")
                .append("访问链接：").append(visitUrl).append("\n")
                .append("演员作品主页：").append(startPageUrl).append("\n")
                .append("标题：").append(titleStr).append("\n")
                .append("识别码：").append(code).append("\n")
                .append("发布时间：").append(publishDate).append("\n")
                .append("导演：").append(director).append("\n")
                .append("制作商：").append(produceCompany).append("\n")
                .append("发行商：").append(publishCompany).append("\n")
                .append("类型：").append(types).append("\n")
                .append("类别：").append(series).append("\n")
                .append("演员：").append(stars).append("\n")
                .append("#");
        //无码
        if (JavbusHelper.startWithNumber(code)) {
            code = "A" + code;
        }
        //欧美
        if (JavbusHelper.isforeignProduct(code)) {
            code = code.replaceAll("\\.", "_");
        }
        stringBuilder.append(code.replaceAll("-", "_"));
        if (null != mainStarPageUrl && null != mainStarPageUrl.getStartPageUrl()) {
            stringBuilder.append(" ").append("#").append(stars);
        }

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
        //无码
        if (JavbusHelper.startWithNumber(code)) {
            code = "A" + code;
        }
        //欧美
        if (JavbusHelper.isforeignProduct(code)) {
            code = code.replaceAll("\\.", "_");
        }
        stringBuilder.append("#").append(code.replaceAll("-", "_"));
        if (null != mainStarPageUrl && null != mainStarPageUrl.getStartPageUrl()) {
            stringBuilder.append(" ").append("#").append(stars);
        }
        return stringBuilder.toString();
    }

    public List<List<String>> sliceSampleImgUrlForupload() {
        ArrayList<List<String>> lists = new ArrayList<>();
        if (null != sampleImgs && !sampleImgs.isEmpty()) {
            //(先计算出余数)
            int remainder = sampleImgs.size() % 10;
            if (remainder == 1) {
                String s = sampleImgs.get(sampleImgs.size() - 1);
                sampleImgs.add(s);
            }
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
        ////针对推送图片必须是2-10 所以如果长度为1 或者是
        // 该段代码回造成并发修改错误
        //List<String> left = lists.get(lists.size() - 1);
        //if (left.size()==1){
        //    String s = left.get(0);
        //    left.add(s);
        //}
        //lists.set(lists.size()-1,left);

        return lists;
    }
}
