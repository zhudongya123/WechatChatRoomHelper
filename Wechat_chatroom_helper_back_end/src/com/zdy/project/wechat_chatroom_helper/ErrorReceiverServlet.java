package com.zdy.project.wechat_chatroom_helper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by zhudo on 2017/8/10.
 */
public class ErrorReceiverServlet extends HttpServlet {


    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static final String PATH = "/usr/java/tomcat/apache-tomcat-8.5.15/webapps/Wechat_chatroom_helper_back_end/crash/";


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Part part = req.getPart("file");

        String versionCode = "";
        String sdkVersion = "";
        String deviceName = "";
        try {
            versionCode = convertStreamToString(req.getPart("versionCode").getInputStream()).trim();
            sdkVersion = convertStreamToString(req.getPart("sdkVersion").getInputStream()).trim();
            deviceName = convertStreamToString(req.getPart("deviceName").getInputStream()).trim();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        InputStream inputStream = part.getInputStream();

        String format = formatter.format(Calendar.getInstance().getTime());

        File rootFolder = new File(PATH + format);

        if (!rootFolder.exists())
            rootFolder.mkdirs();

        File parentFolder = new File(PATH + format + "/" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        if (!parentFolder.exists())
            parentFolder.mkdirs();

        String fileName = format + "/" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "/" + System.currentTimeMillis() / 1000 +
                "_versionCode_" + versionCode + "_sdkVersion_" + sdkVersion + "_deviceName_" + deviceName + ".txt";
        File file = new File(PATH + fileName);

        if (!file.exists())
            file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);
        }
        fileOutputStream.close();
        inputStream.close();

    }

    private static String convertStreamToString(InputStream is) {
        /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
