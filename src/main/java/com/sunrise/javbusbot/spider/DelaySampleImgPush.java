package com.sunrise.javbusbot.spider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @description: 延迟样品图推送 防止过多的请求导致tg服务报错
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/26 10:55 PM
 */
public class DelaySampleImgPush implements Delayed {
    private String code;

    private JavbusDataItem javbusDataItem;

    //延迟时长
    private long excuteTime;

    public DelaySampleImgPush(String code, JavbusDataItem javbusDataItem, long delayTime) {
        this.code = code;
        this.javbusDataItem = javbusDataItem;
        this.excuteTime = TimeUnit.NANOSECONDS.convert(delayTime,TimeUnit.MILLISECONDS)+System.nanoTime();
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(this.excuteTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        DelaySampleImgPush msg = (DelaySampleImgPush) o;
        long diff = this.excuteTime - msg.excuteTime;
        // 改成>=会造成问题
        if (diff <= 0) {
            return -1;
        }else {
            return 1;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public JavbusDataItem getJavbusDataItem() {
        return javbusDataItem;
    }

    public void setJavbusDataItem(JavbusDataItem javbusDataItem) {
        this.javbusDataItem = javbusDataItem;
    }

    public long getExcuteTime() {
        return excuteTime;
    }

    public void setExcuteTime(long excuteTime) {
        this.excuteTime = excuteTime;
    }

    @Override
    public String toString() {
        return "DelaySampleImgPush{" +
                "code='" + code + '\'' +
                ", javbusDataItem=" + javbusDataItem +
                ", excuteTime=" + excuteTime +
                '}';
    }
}
