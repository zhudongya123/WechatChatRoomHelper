package com.zdy.project.wechat_chatroom_helper;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by zhudo on 2017/7/24.
 */
public class PathServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String versionCode = req.getParameter("versionCode");

        Integer versionCodeNumber = Integer.valueOf(versionCode);


        JsonObject jsonObject = new JsonObject();
        switch (versionCodeNumber) {

            case 1060:

                break;
        }


    }
}
