package com.sunrise.javbusbot.spider;

import com.sunrise.javbusbot.tgbot.TgBotConfig;

/**
 * @description: 演员
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/5/1 4:02 PM
 */
public class JavbusStarInfoItem {
    /**
     * 所有作品数量
     */
    private String allFilmNum;
    /**
     * 有磁力作品数量
     */
    private String hasMagNum;
    /**
     * 头像
     */
    private String headPhoto;
    /**
     * 名字
     */
    private String starName;
    /**
     * 生日
     */
    private String birthday;
    /**
     * 年龄
     */
    private String age;

    /**
     * 身高
     */
    private String height;

    /**
     * 罩杯
     */
    private String cup;
    /**
     * 胸围
     */
    private String chestCircumference;
    /**
     * 腰围
     */
    private String waistline;

    /**
     * 臀围
     */
    private String hips;
    /**
     * 出生地
     */
    private String birthPlace;
    /**
     * 爱好
     */
    private String hobby;

    public String getAllFilmNum() {
        return allFilmNum;
    }

    public void setAllFilmNum(String allFilmNum) {
        this.allFilmNum = allFilmNum;
    }

    public String getHasMagNum() {
        return hasMagNum;
    }

    public void setHasMagNum(String hasMagNum) {
        this.hasMagNum = hasMagNum;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getCup() {
        return cup;
    }

    public void setCup(String cup) {
        this.cup = cup;
    }

    public String getChestCircumference() {
        return chestCircumference;
    }

    public void setChestCircumference(String chestCircumference) {
        this.chestCircumference = chestCircumference;
    }

    public String getWaistline() {
        return waistline;
    }

    public void setWaistline(String waistline) {
        this.waistline = waistline;
    }

    public String getHips() {
        return hips;
    }

    public void setHips(String hips) {
        this.hips = hips;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getHeadPhoto() {
        return headPhoto;
    }

    public void setHeadPhoto(String headPhoto) {
        this.headPhoto = headPhoto;
    }

    public JavbusStarInfoItem() {
        this.headPhoto = "";
        this.starName = "";
        this.birthday = "";
        this.age = "";
        this.height = "";
        this.cup = "";
        this.chestCircumference = "";
        this.waistline = "";
        this.hips = "";
        this.birthPlace = "";
        this.hobby = "";
    }

    public String toPrettyStr() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!headPhoto.contains("http://") && !headPhoto.contains("https://")){
            headPhoto = TgBotConfig.SPIDER_BASE_URL + headPhoto;
        }

        stringBuilder
                .append("大头贴：").append(headPhoto).append("\n")
                .append("演员：").append(starName).append("\n")
                .append("所有作品数量：").append(allFilmNum).append("\n")
                .append("已有磁力作品数量：").append(hasMagNum).append("\n")
                .append("生日：").append(birthday).append("\n")
                .append("年龄：").append(age).append("\n")
                .append("身高：").append(height).append("\n")
                .append("罩杯：").append(cup).append("\n")
                .append("胸围：").append(chestCircumference).append("\n")
                .append("腰围：").append(waistline).append("\n")
                .append("臀围：").append(hips).append("\n")
                .append("出生地：").append(birthPlace).append("\n")
                .append("爱好：").append(hobby).append("\n")
                .append("#").append(starName.replaceAll(" ", "_"));

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "JavbusStarInfoItem{" +
                "allFilmNum='" + allFilmNum + '\'' +
                ", hasMagNum='" + hasMagNum + '\'' +
                ", headPhoto='" + headPhoto + '\'' +
                ", starName='" + starName + '\'' +
                ", birthday='" + birthday + '\'' +
                ", age='" + age + '\'' +
                ", height='" + height + '\'' +
                ", cup='" + cup + '\'' +
                ", chestCircumference='" + chestCircumference + '\'' +
                ", waistline='" + waistline + '\'' +
                ", hips='" + hips + '\'' +
                ", birthPlace='" + birthPlace + '\'' +
                ", hobby='" + hobby + '\'' +
                '}';
    }
}

