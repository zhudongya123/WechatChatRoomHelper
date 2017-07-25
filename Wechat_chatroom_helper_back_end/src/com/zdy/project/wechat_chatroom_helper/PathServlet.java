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

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json;charset=utf-8");
        PrintWriter writer = resp.getWriter();

        String versionCode = req.getParameter("versionCode");

        Integer versionCodeNumber = Integer.valueOf(versionCode);


        JsonObject jsonObject = new JsonObject();
        JsonObject data = new JsonObject();
        switch (versionCodeNumber) {

            case 1060:
                jsonObject.addProperty("code", 0);
                jsonObject.addProperty("msg", "success");


                data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
                data.addProperty("cclvapn", "com.tencent.mm.ui.e");
                data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

                data.addProperty("cclvas", "g");
                data.addProperty("cclvaps", "e");

                data.addProperty("mmsb", "j");
                data.addProperty("mago", "ev");

                data.addProperty("vmsim1", "uyI");
                data.addProperty("vmsim2", "uXX");
                data.addProperty("vla", "uXk");
                data.addProperty("vl", "uWH");

                data.addProperty("vlavt", "usj");

                data.addProperty("magos1", "tMb");
                data.addProperty("magos2", "hdB");
                data.addProperty("magos3", "ev");

                data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
                data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

                data.addProperty("dsa", "rj");
                data.addProperty("dss", "ang");

                data.addProperty("vmbc", "uXP");
                data.addProperty("vmbn", "nickName");
                data.addProperty("vmbt", "uXO");


                jsonObject.add("data", data);

                break;

            case 1080:
                jsonObject.addProperty("code", 0);
                jsonObject.addProperty("msg", "success");

                data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
                data.addProperty("cclvapn", "com.tencent.mm.ui.e");
                data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

                data.addProperty("cclvas", "g");
                data.addProperty("cclvaps", "e");

                data.addProperty("mmsb", "j");
                data.addProperty("mago", "yv");

                data.addProperty("vmsim1", "vIL");
                data.addProperty("vmsim2", "viW");
                data.addProperty("vla", "vHY");
                data.addProperty("vl", "vHv");

                data.addProperty("vlavt", "vcu");

                data.addProperty("magos1", "uvS");
                data.addProperty("magos2", "utT");
                data.addProperty("magos3", "yv");

                data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
                data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

                data.addProperty("dsa", "sb");
                data.addProperty("dss", "ao8");

                data.addProperty("vmbc", "vID");
                data.addProperty("vmbn", "nickName");
                data.addProperty("vmbt", "vIC");



                jsonObject.add("data", data);

                break;

            default:
                jsonObject.addProperty("code", 1);
                jsonObject.addProperty("msg", "no_info");
                break;
        }

        writer.write(jsonObject.toString());

    }
}
