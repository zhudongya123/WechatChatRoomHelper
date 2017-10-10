package com.zdy.project.wechat_chatroom_helper.db;

import java.sql.*;

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

    public void insertUserStatisticsData(String uuid, String model, String action, String version) {
        String sql;
        try {
            Statement stmt = c.createStatement();

            sql = "INSERT INTO user_statistics " + "(uuid,model,action,time,version) " + "VALUES ('"
                    + uuid + "', '" + model + "', '" + action + "', " + System.currentTimeMillis() + ", '" + version + "' );";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public int queryDataByTime(long start, long end) {

        String sql;
        try {
            Statement stmt = c.createStatement();


            sql = "SELECT count(DISTINCT uuid) FROM user_statistics where time BETWEEN " + start + " AND " + end;
            ResultSet resultSet = stmt.executeQuery(sql);

            stmt.close();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

}
