package com.zdy.project.wechat_chatroom_helper.test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by zhudo on 2017/11/24.
 */
public class DBdemo {
    private static Connection c = null;

    public static final String url = "jdbc:mysql://116.62.247.71:3306/wechat_chatroom_helper";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "admin";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        long currentTime = System.currentTimeMillis();

        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);

        long time = instance.getTimeInMillis();

        queryWechatVersionPercent(time, currentTime);
    }

    public static HashMap<String, Integer> queryWechatVersionPercent(long start, long end) {

        HashMap<String, Integer> data = new HashMap<>();

        String sql;
        try {
            Statement stmt = c.createStatement();
            sql = "SELECT DISTINCT wechat_version FROM user_statistics where time BETWEEN " + start + " AND " + end;
            ResultSet resultSet = stmt.executeQuery(sql);


            ArrayList<Integer> versionData = new ArrayList();

            while (resultSet.next()) {
                int version = Integer.valueOf(resultSet.getString(1));
                int versionCount = getUserCount(start, end, "wechat_version", String.valueOf(version));

                System.out.println("versionCount = " + versionCount + ", version = " + version);

                data.put(String.valueOf(version), versionCount);


                if (versionData.size() == 0) {
                    versionData.add(version);
                    continue;
                }
                for (int i = 0; i < versionData.size(); i++) {
                    if (versionData.get(i) > version) {
                        versionData.add(i, version);
                        break;
                    }
                    if (i == versionData.size() - 1)
                        versionData.add(version);
                }

            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    public static int getUserCount(long start, long end, String field, String value) {
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
