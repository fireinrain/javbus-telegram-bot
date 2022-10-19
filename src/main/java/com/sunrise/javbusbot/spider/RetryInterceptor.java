package com.sunrise.javbusbot.spider;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Description: okhttp重试策略
 * @Author : fireinrain
 * @Site : https://github.com/fireinrain
 * @File : RetryInterceptor
 * @Software: IntelliJ IDEA
 * @Time : 2022/9/21 3:25 PM
 */


public class RetryInterceptor implements Interceptor {
    public static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    // 最大重试次数
    private int maxRetry;
    // 重试次数计数
    private int retryNum = 0;

    public RetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        // logger.info("当前请求重试次数: "+retryNum);
        Response response = chain.proceed(request);
        while (!response.isSuccessful() && retryNum < maxRetry) {
            retryNum++;
            response.close();
            logger.info("当前请求正在进行重试,retryNum: " + retryNum);
            response = chain.proceed(request);
        }
        return response;
    }
}


