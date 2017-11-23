package com.zdy.project.wechat_chatroom_helper.db;

import java.sql.*;

public class DataBaseManager {

    private static final DataBaseManager INSTANCE = new DataBaseManager();

    private Connection c = null;


    public static final String url = "jdbc:mysql://localhost/wechat_chatroom_helper";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "admin";

    private DataBaseManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static DataBaseManager getInstance() {
        return INSTANCE;
    }

    public void insertUserStatisticsData(String uuid, String model, String action, String version, String wechat_version) {
        String sql;
        try {
            Statement stmt = c.createStatement();

            sql = "INSERT INTO user_statistics " + "( uuid, model, action, time, version ,wechat_version) " + "VALUES (" + uuid +
                    ", '" + model + "', '" + action + "', " + System.currentTimeMillis() + ", '" + version + "', '" + wechat_version + "' );";
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

            //     sql = "SELECT * FROM user_statistics ";
            ResultSet resultSet = stmt.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
//                String ufname = resultSet.getString(2);
//                String ulname = resultSet.getString(3);
//                String udate = resultSet.getString(4);
                count = resultSet.getInt(1);
            }

            stmt.close();

            return count;

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

}
