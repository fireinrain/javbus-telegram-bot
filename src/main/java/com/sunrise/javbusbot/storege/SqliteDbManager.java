package com.sunrise.javbusbot.storege;

import com.sunrise.javbusbot.tgbot.TgBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

/**
 * @Description:
 * @Author : fireinrain
 * @Site : https://github.com/fireinrain
 * @File : DbManager
 * @Software: IntelliJ IDEA
 * @Time : 2022/9/14 7:38 PM
 */

public class SqliteDbManager {
    private static final Logger logger = LoggerFactory.getLogger(SqliteDbManager.class);

    private static final String dbFileNamePath = TgBotConfig.SQLITE_DB_PATH;

    private static final String dbUrl = "jdbc:sqlite:" + dbFileNamePath;

    private static Connection connection;


    static {
        checkIfHasDbFile();
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkIfHasDbFile() {
        Path path = Paths.get(dbFileNamePath);
        boolean exists = Files.exists(path);
        if (!exists) {
            logger.warn("javbus-tg-bot.db数据库文件: " + dbFileNamePath + "不存在!!!");
            logger.info("自动创建......");
            createDatabase(dbFileNamePath);
        } else {
            logger.info("sqlite数据库文件已存在......");
        }

    }

    public static Connection getConnection() {
        return connection;
    }


    public static void createDatabase(String fileName) {
        String url = "jdbc:sqlite:" + fileName;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("the driver name is " + meta.getDriverName());
                logger.info("a new database has been created.");
                // 创建表
                String createSql = "create table query_history(" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "query_command TEXT," + "query_str TEXT," + "query_text TEXT," + "update_time TEXT)";

                PreparedStatement preparedStatement = conn.prepareStatement(createSql);
                int update = preparedStatement.executeUpdate();
                if (update == 1) {
                    logger.warn("创建表query_history成功.");
                }
                String createIndex = "create index query_history_update_time_index" + "  on query_history (update_time)";
                PreparedStatement statement = conn.prepareStatement(createIndex);
                statement.executeUpdate();
                logger.warn("创建表query_history 索引成功.");
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                Objects.requireNonNull(conn).close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void insertQueryHistory(QueryHistoryEntity queryHistory) {
        String insertSql = "insert into query_history(query_command,query_str,query_text,update_time) values (?,?,?,?)";
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(insertSql);
            statement.setString(1, queryHistory.getQueryCommand());
            statement.setString(2, queryHistory.getQueryStr());
            statement.setString(3, queryHistory.getQueryText());
            statement.setString(4, queryHistory.getUpdateTime());

            int update = statement.executeUpdate();
            if (update != 1) {
                logger.warn("插入查询历史失败");
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("插入历史查询失败");
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }


    }

    public static QueryStaticEntity getQueryStatic() {
        QueryStaticEntity queryStaticEntity = new QueryStaticEntity();
        String queryStr = "select atable.a,btable.b from (select count(*) as a from query_history) atable,(select count(*) as b from (select id from query_history where update_time >= ? and update_time <= ? )) btable";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queryStr);
            preparedStatement.setString(1, queryStaticEntity.getMinDateTimeBounds());
            preparedStatement.setString(2, queryStaticEntity.getMaxDateTimeBounds());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int allCounts = resultSet.getInt(1);
                int todayQueryCounts = resultSet.getInt(2);
                // System.out.println(allCounts);
                // System.out.println(todayQueryCounts);
                queryStaticEntity.setTodayQueryCounts(todayQueryCounts);
                queryStaticEntity.setTotalQueryCounts(allCounts);
            }
        } catch (SQLException e) {
            // throw new RuntimeException(e);
            logger.warn("查询统计失败: " + e.getMessage());
            e.printStackTrace();
        }
        return queryStaticEntity;
    }


    public static void main(String[] args) {
        // createDatabase(TgBotConfig.SQLITE_DB_PATH);
        QueryHistoryEntity entity = new QueryHistoryEntity();
        entity.setQueryCommand("testCommand");
        entity.setQueryStr("testStr");
        entity.setQueryText("testText");
        insertQueryHistory(entity);

        getQueryStatic();

    }

}
