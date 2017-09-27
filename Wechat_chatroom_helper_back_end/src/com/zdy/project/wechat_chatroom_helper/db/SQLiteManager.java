package com.zdy.project.wechat_chatroom_helper.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteManager {

    private static final SQLiteManager INSTANCE = new SQLiteManager();

    private Connection c = null;

    private SQLiteManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:user_info.db");
            System.out.println("Opened database successfully");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static SQLiteManager getInstance() {
        return INSTANCE;
    }

    public void insertUserStatisticsData(String uuid, String model, String action) {
        String sql;
        try {
            Statement stmt = c.createStatement();

            sql = "INSERT INTO user_statistics " + "(uuid,model,action,time) " + "VALUES ('"
                    + uuid + "', '" + model + "', '" + action + "', " + System.currentTimeMillis() + " );";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
