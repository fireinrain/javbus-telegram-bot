package com.sunrise.tgbot;

import okhttp3.*;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/17 4:05 PM
 */
public class FetchPlateStockData {
    public static void main(String[] args) throws IOException {

        /**
         * http://56.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112405951858150061431_1618644332226&pn=1&pz=20&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=m:90+t:2+f:!50&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f22,f33,f11,f62,f128,f136,f115,f152,f124,f107,f104,f105,f140,f141,f207,f208,f209,f222&_=1618644332227
         *
         *
         * http://push2.eastmoney.com/api/qt/clist/get\?pn\=1\&pz\=500\&po\=1\&np\=1\&fields\=f12,f13,f14,f62\&fid\=f62\&fs\=m:90+t:2\&_\=1618644989522
         *
         *
         * f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f22,f33,f11,f62,f128,f136,f115,f152,f124,f107,f104,f105,f140,f141,f207,f208,f209,f222
         *
         *
         * http://push2.eastmoney.com/api/qt/clist/get\?pn\=1\&pz\=500\&po\=1\&np\=1\&fields\=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f22,f33,f11,f62,f128,f136,f115,f152,f124,f107,f104,f105,f140,f141,f207,f208,f209,f222
         * \&fid\=f62\&fs\=m:90+t:2\&_\=1618644989522
         */

        String valuePairStr = "pn\\=1\\&pz\\=500\\&po\\=1\\&np\\=1\\&fields\\=f12,f13,f14,f62\\&fid\\=f62\\&fs\\=m:90+t:2\\&_\\=1618644989522";

        String url = "http://push2.eastmoney.com/api/qt/clist/get\\?pn\\=1\\&pz\\=500\\&po\\=1\\&np\\=1\\&fields\\=f12,f13,f14,f62\\&fid\\=f62\\&fs\\=m:90+t:2\\&_\\=1618644989522";

        //List<String> collect = Arrays.stream(valuePairStr.replaceAll("\\\\", "").split("&"))
        //        .collect(Collectors.toList());
        //
        //List<BasicNameValuePair> nameValuePairList = collect.stream()
        //        .map(e -> {
        //            String[] split = e.split("=");
        //            return new BasicNameValuePair(split[0], split[1]);
        //        }).collect(Collectors.toList());


        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept","*/*")
                .addHeader("Accept-Encoding","gzip, deflate")
                .addHeader("Accept-Language","zh,en;q=0.9,zh-TW;q=0.8,zh-CN;q=0.7,ja;q=0.6")
                .addHeader("Cache-Control","no-cache")
                .addHeader("Referer","http://quote.eastmoney.com/")
                .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println(response.body().string());
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });

    }
}
