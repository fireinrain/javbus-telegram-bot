package com.sunrise.storege;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * @description:
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/24 8:38 PM
 */
public class MongodbStorege {

    private static String mongoDbUrl = "mongodb://admin:admin@127.0.0.1:27018";

    public static MongoClient mongoClient = null;

    static {
        ConnectionString connString = new ConnectionString(
                mongoDbUrl
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        mongoClient = MongoClients.create(settings);
    }

    /**
     * 创建db
     *
     * @param dbName
     * @return
     */
    private static MongoDatabase createMongoDbIfNotExist(String dbName) {
        MongoDatabase database = mongoClient.getDatabase(dbName);
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
        if (null == documentMongoCollection) {
            database.createCollection(collectionName);
        }
        return documentMongoCollection;
    }


    public static void main(String[] args) {
        MongoDatabase javbus = createMongoDbIfNotExist("javbus");
        MongoCollection<Document> mongoCollection = createCollectionIfNotExist("javbus", "javFilm");

        Document document = new Document();
        document.append("name", "xiaoqian")
                .append("age", 12)
                .append("addr", "beijin");
        mongoCollection.insertOne(document);
    }


}
