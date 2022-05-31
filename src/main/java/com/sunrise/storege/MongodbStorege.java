package com.sunrise.storege;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sunrise.tgbot.TgBotConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @description: 存储数据
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 8:38 PM
 */
public class MongodbStorege {
    public static final Logger logging = LoggerFactory.getLogger(MongodbStorege.class);


    private static String mongoDbUrl = TgBotConfig.MONGO_DB_URL;

    public static volatile AtomicBoolean isMongoDatabaseAvailable = new AtomicBoolean(false);

    public static MongoClient mongoClient = null;


    public static void isMongoDatabaseAvailable() {
        String[] split = mongoDbUrl.split("@");
        String ipAndPort = split[1];
        String[] ipPort = ipAndPort.split(":");
        String ip = ipPort[0];
        int port = Integer.parseInt(ipPort[1]);
        ;
        boolean isAvailable = portIsUsing(ip, port);
        if (isAvailable) {
            isMongoDatabaseAvailable.set(true);
            ConnectionString connString = new ConnectionString(mongoDbUrl);
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connString).retryWrites(true).build();
            mongoClient = MongoClients.create(settings);
        }

    }


    private static boolean portIsUsing(String host, int port) {
        if (host != null && host.startsWith("https://")) {
            host = host.substring(8);
        }
        if (host != null && host.startsWith("http://")) {
            host = host.substring(7);
        }
        try {
            InetAddress theAddress = InetAddress.getByName(host);
            new Socket(theAddress, port);
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }


    /**
     * 创建db
     *
     * @param dbName
     * @return
     */
    private static MongoDatabase createMongoDbIfNotExist(String dbName) {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        Document ping = database.runCommand(new BasicDBObject("ping", 1));
        return database;
    }

    /**
     * 创建文档集合
     *
     * @param dbName
     * @param collectionName
     */
    public static MongoCollection<Document> createCollectionIfNotExist(String dbName, String collectionName) {
        MongoDatabase database = createMongoDbIfNotExist(dbName);
        MongoCollection<Document> documentMongoCollection = database.getCollection(collectionName);
        return documentMongoCollection;

    }

    /**
     * 存储信息
     *
     * @param info 信息
     */
    public static void storeInfo(String info, String dbName, String collectionName) {
        // do insert info
        MongoCollection<Document> mongoCollection = MongodbStorege.createCollectionIfNotExist(dbName, collectionName);

        org.bson.Document document = org.bson.Document.parse(info);

        mongoCollection.insertOne(document);

        logging.info("插入成功：" + info);

    }

    /**
     * 存储列表信息
     *
     * @param listInfos
     * @param dbName
     * @param collectionName
     * @param <T>
     */
    public static <T> void storeInfos(List<T> listInfos, String dbName, String collectionName) {
        List<Document> documents = listInfos.stream().map(e -> {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = null;
            try {
                jsonStr = objectMapper.writeValueAsString(e);
                Document document = Document.parse(jsonStr);
                return document;
            } catch (JsonProcessingException exception) {
                exception.printStackTrace();
            }
            return new Document();
        }).collect(Collectors.toList());


        MongoCollection<Document> mongoCollection = MongodbStorege.createCollectionIfNotExist(dbName, collectionName);

        mongoCollection.insertMany(documents);

        logging.info("插入批量数据成功：" + listInfos.size());

    }


    public static void main(String[] args) {
        // MongoDatabase javbus = createMongoDbIfNotExist("javbus");
        // MongoCollection<Document> mongoCollection = createCollectionIfNotExist("javbus", "javFilm");
        //
        // Document document = new Document();
        // document.append("name", "xiaoqian").append("age", 12).append("addr", "beijin");
        // mongoCollection.insertOne(document);
        boolean portIsUsing = portIsUsing("127.0.0.1", 27017);
        logging.info("portIsUsing: {}", portIsUsing);
    }


}
