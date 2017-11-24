package com.zdy.project.wechat_chatroom_helper.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DataBaseManager {

    private static final DataBaseManager INSTANCE = new DataBaseManager();

    private Connection c = null;


    public static final String url = "jdbc:mysql://116.62.247.71:3306/wechat_chatroom_helper";
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

    public int queryUserCountByTime(long start, long end) {

        String sql;
        try {
            Statement stmt = c.createStatement();

            sql = "SELECT count(DISTINCT uuid) FROM user_statistics where time BETWEEN " + start + " AND " + end;

            ResultSet resultSet = stmt.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
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


    public HashMap<Integer, Integer> queryWechatVersionPercent(long start, long end) {

        HashMap<Integer, Integer> data = new HashMap<>();

        ArrayList<Integer> versionData = new ArrayList();

        String sql;
        try {
            Statement stmt = c.createStatement();
            sql = "SELECT DISTINCT wechat_version FROM user_statistics where time BETWEEN " + start + " AND " + end;
            ResultSet resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                int version = Integer.valueOf(resultSet.getString(1));

                int versionCount = getUserCount(start, end, "wechat_version", String.valueOf(version));

                System.out.println("versionCount = " + versionCount + ", version = " + version);

                data.put(version, versionCount);


                if (versionData.size() == 0)
                    versionData.add(version);
                for (int i = 0; i < versionData.size(); i++) {
                    if (versionData.get(i) > version) {
                        versionData.add(i, version);
                        break;
                    }
                    if (i == versionData.size())
                        versionData.add(version);
                }

            }
            System.out.println();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }

    public HashMap<String, Integer> queryHelperVersionPercent(long start, long end) {

        HashMap<String, Integer> data = new HashMap<>();

        String sql;
        try {
            Statement stmt = c.createStatement();
            sql = "SELECT DISTINCT version FROM user_statistics where time BETWEEN " + start + " AND " + end;
            ResultSet resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                String version = resultSet.getString(1);

                int versionCount = getUserCount(start, end, "version", version);

                System.out.println("versionCount = " + versionCount + ", version = " + version);

                data.put(version, versionCount);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }


    public int getUserCount(long start, long end, String field, String value) {
        String sql;
        try {
            Statement stmt = c.createStatement();
            sql = "SELECT count(DISTINCT uuid) FROM user_statistics WHERE time BETWEEN " + start + " AND " + end + " AND " + field + " = " + value;
            ResultSet resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;

    }
}
