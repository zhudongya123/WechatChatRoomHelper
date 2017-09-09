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

    /**
     * vmbt 字段已在2017年8月8日 21:02:01废弃
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/json;charset=utf-8");
        PrintWriter writer = resp.getWriter();

        String versionCode = req.getParameter("versionCode");
        String isPlayVersion = req.getParameter("isPlayVersion") == null ? "0" : req.getParameter("isPlayVersion");

        Integer versionCodeNumber = Integer.valueOf(versionCode);

        JsonObject jsonObject = new JsonObject();
        JsonObject data = new JsonObject();
        switch (versionCodeNumber) {
            case 1060:
                jsonObject.addProperty("code", 0);
                jsonObject.addProperty("msg", "success");

                if (isPlayVersion.endsWith("0")) {
                    getConfig1060(data);
                } else {
                    getConfig1060playVersion(data);
                }
                jsonObject.add("data", data);
                break;
            case 1080:
                jsonObject.addProperty("code", 0);
                jsonObject.addProperty("msg", "success");

                if (isPlayVersion.endsWith("0")) {
                    getConfig1080(data);
                } else {
                    getConfig1080playVersion(data);
                }
                jsonObject.add("data", data);
                break;
            case 1100:
                if (isPlayVersion.endsWith("0")) {

                    jsonObject.addProperty("code", 0);
                    jsonObject.addProperty("msg", "success");

                    getConfig1100(data);

                    jsonObject.add("data", data);
                } else {
                    jsonObject.addProperty("code", 1);
                    jsonObject.addProperty("msg", "no_info");
                }
                break;
            default:
                jsonObject.addProperty("code", 1);
                jsonObject.addProperty("msg", "no_info");
                break;
        }

        writer.write(jsonObject.toString());
    }

    private void getConfig1100(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
        data.addProperty("cclvapn", "com.tencent.mm.ui.e");
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "e");

        data.addProperty("mmsb", "j");
        data.addProperty("mago", "yL");

        data.addProperty("vmsim1", "waz");
        data.addProperty("vmsim2", "vAw");
        data.addProperty("vla", "vZM");
        data.addProperty("vl", "vZj");

        data.addProperty("vlavt", "vtJ");
        data.addProperty("vlava", "ipb");
        data.addProperty("vlavc", "vtL");

        data.addProperty("magos1", "uMR");
        data.addProperty("magos2", "uKT");
        data.addProperty("magos3", "yL");


        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

        data.addProperty("dsa", "sk");
        data.addProperty("dss", "ao1");
        data.addProperty("dsca", "v0");

        data.addProperty("vmbc", "war");
        data.addProperty("vmbn", "nickName");

        data.addProperty("vmbt", "waq");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "wax");
        data.addProperty("mmtt", "i");


        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uOv");
        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");
    }

    private void getConfig1080playVersion(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
        data.addProperty("cclvapn", "com.tencent.mm.ui.e");
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "e");

        data.addProperty("mmsb", "j");
        data.addProperty("mago", "yA");

        data.addProperty("vmsim1", "vJt");
        data.addProperty("vmsim2", "vjC");
        data.addProperty("vla", "vIG");
        data.addProperty("vl", "vId");

        data.addProperty("vlavt", "vda");
        data.addProperty("vlava", "iAJ");
        data.addProperty("vlavc", "vdc");

        data.addProperty("magos1", "uwz");
        data.addProperty("magos2", "uuA");
        data.addProperty("magos3", "yA");

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

        data.addProperty("dsa", "sb");
        data.addProperty("dss", "ao8");
        data.addProperty("dsca", "v2");

        data.addProperty("vmbc", "vJl");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "vJk");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vJr");
        data.addProperty("mmtt", "i");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uyd");
        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");
    }

    private void getConfig1080(JsonObject data) {
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
        data.addProperty("vlava", "iAt");
        data.addProperty("vlavc", "vcw");

        data.addProperty("magos1", "uvS");
        data.addProperty("magos2", "utT");
        data.addProperty("magos3", "yv");

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

        data.addProperty("dsa", "sb");
        data.addProperty("dss", "ao8");
        data.addProperty("dsca", "v2");

        data.addProperty("vmbc", "vID");
        data.addProperty("vmbn", "nickName");

        data.addProperty("vmbt", "vIC");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vIJ");
        data.addProperty("mmtt", "i");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uxw");
        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");
    }

    private void getConfig1060playVersion(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
        data.addProperty("cclvapn", "com.tencent.mm.ui.e");
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "e");

        data.addProperty("mmsb", "j");
        data.addProperty("mago", "ew");

        data.addProperty("vmsim1", "uAO");
        data.addProperty("vmsim2", "vaf");
        data.addProperty("vla", "uZs");
        data.addProperty("vl", "uYP");

        data.addProperty("vlavt", "uup");
        data.addProperty("vlava", "iyA");
        data.addProperty("vlavc", "uur");

        data.addProperty("magos1", "tOb");
        data.addProperty("magos2", "hdN");
        data.addProperty("magos3", "ew");

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

        data.addProperty("dsa", "rj");
        data.addProperty("dss", "ang");
        data.addProperty("dsca", "u_");

        data.addProperty("vmbc", "uZX");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "uZW");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vad");
        data.addProperty("mmtt", "i");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "ad");
        data.addProperty("vhua", "tPE");
        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");
    }

    private void getConfig1060(JsonObject data) {
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
        data.addProperty("vlava", "iym");
        data.addProperty("vlavc", "usl");

        data.addProperty("magos1", "tMb");
        data.addProperty("magos2", "hdB");
        data.addProperty("magos3", "ev");

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.v");
        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");

        data.addProperty("dsa", "rj");
        data.addProperty("dss", "ang");
        data.addProperty("dsca", "u_");

        data.addProperty("vmbc", "uXP");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "uXO");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "uXV");
        data.addProperty("mmtt", "i");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "ad");
        data.addProperty("vhua", "tNB");
        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");
    }


}
