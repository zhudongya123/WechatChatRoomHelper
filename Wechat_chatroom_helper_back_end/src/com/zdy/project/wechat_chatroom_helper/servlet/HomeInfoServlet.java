package com.zdy.project.wechat_chatroom_helper.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Mr.Zdy on 2017/9/13.
 */
public class HomeInfoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json;charset=utf-8");
        PrintWriter writer = resp.getWriter();


//        String result = "<p><a href=\"https://github.com/zhudongya123/WechatChatroomHelper/issues\">反馈地址</a></p>"
//                + "鸣谢:<br>"
//                + "<p><a href=\"https://www.coolapk.com/apk/com.toshiba_dealin.developerhelper\">开发者助手开发者（东芝）</a></p>"
//                + "<p><a href=\"https://github.com/veryyoung\">微信红包开发者（veryyoung）</a></p>"
//                + "<br><br>";
//
//        String versionCode = req.getParameter("versionCode");
//
//        if (Integer.valueOf(versionCode) <= 16) {
//            result += "有新版本可以升级有新版本可以升级有新版本可以升级:<br>";
//        }
//
//        result += "不支持6.5.8以下的微信版本以后也不会支持谢谢各位老爹爹快点升级吧！<br>";
//
//        result += "不支持6.5.14为什么呢因为酷安上都没有酷安上都不资瓷我怎么能资瓷呢！<br>";

        String result = "<html>\n" +
                "<head>\n" +
                "    <title>$Title$</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<p><a href=\"https://github.com/zhudongya123/WechatChatroomHelper/issues\">反馈地址</a></p>\n" +
                "鸣谢:<br>\n" +
                "<p><a href=\"https://www.coolapk.com/apk/com.toshiba_dealin.developerhelper\">开发者助手开发者（东芝）</a></p>\n" +
                "<p><a href=\"https://github.com/veryyoung\">微信红包开发者（veryyoung）</a></p>\n" +
                "<br>\n" +
                "\n" +
                "\n" +
                "\n" +
                "<p>微信6.5.14已经支持~</p>\n" +
                "\n" +
                "<p>欢迎加入反馈群，测试版本在此提供，同时可以更好的反馈BUG及建议~</p>\n" +
                "\n" +
                "<img src=\"http://mr-zdy-shanghai.oss-cn-shanghai.aliyuncs.com/wechat_chatroom_helper/feedback_chatroom.png\" width=\"300\" height=\"412\">\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "</html>";


        writer.write(result);
    }
}
