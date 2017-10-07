package com.zdy.project.wechat_chatroom_helper.servlet;

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

        String msg;

        switch (versionCodeNumber) {
            case 1060:
                jsonObject.addProperty("code", 0);

                if (isPlayVersion.equals("0")) {
                    getConfig1060(data);
                } else {
                    getConfig1060playVersion(data);
                }

                msg = "微信版本 6.5.8(" + versionCodeNumber + ")" + (isPlayVersion.equals("0") ? "" : "[play版] ") + "已经成功适配，如未有效果，请重启微信客户端查看。";

                jsonObject.add("data", data);
                break;
            case 1080:
                jsonObject.addProperty("code", 0);

                if (isPlayVersion.equals("0")) {
                    getConfig1080(data);
                } else {
                    getConfig1080playVersion(data);
                }


                msg = "微信版本 6.5.10(" + versionCodeNumber + ")" + (isPlayVersion.equals("0") ? "" : "[play版] ") + "已经成功适配，如未有效果，请重启微信客户端查看。";

                jsonObject.add("data", data);
                break;

            case 1081:
                jsonObject.addProperty("code", 0);
                jsonObject.addProperty("msg", "success");


                getConfig1081playVersion(data);

                msg = "微信版本 6.5.13(" + versionCodeNumber + ")[play版] 已经成功适配，如未有效果，请重启微信客户端查看。";

                jsonObject.add("data", data);
                break;

            case 1100:
                if (isPlayVersion.equals("0")) {

                    jsonObject.addProperty("code", 0);

                    getConfig1100(data);

                    jsonObject.add("data", data);

                    msg = "微信版本 6.5.13(" + versionCodeNumber + ") 已经成功适配，如未有效果，请重启微信客户端查看。";

                } else {
                    jsonObject.addProperty("code", 1);

                    msg = "老哥，你是不是点错了？？ 你这明明不是play版……";
                }
                break;
            case 1101:
                if (isPlayVersion.equals("0")) {
                    jsonObject.addProperty("code", 0);
                    getConfig1101(data);
                    msg = "微信版本 6.5.14(1100) 已经成功适配，如未有效果，请重启微信客户端查看。";
                } else {
                    msg = "老哥，6.5.14哪里来的play版本？";
                    jsonObject.addProperty("code", 1);
                }

                jsonObject.add("data", data);
                break;

            case 1120:
                if (isPlayVersion.equals("0")) {
                    jsonObject.addProperty("code", 0);
                    getConfig1120(data);
                    msg = "微信版本 6.5.16(1120) 已经成功适配，如未有效果，请重启微信客户端查看。";
                } else {
                    msg = "老哥，6.5.16哪里来的play版本？";
                    jsonObject.addProperty("code", 1);
                }

                jsonObject.add("data", data);
                break;
            default:
                jsonObject.addProperty("code", 1);
                msg = "微信版本" + versionCodeNumber + "暂未适配，请等待开发者解决。";
                break;
        }
        jsonObject.addProperty("msg", msg);

        writer.write(jsonObject.toString());
    }

    private void getConfig1120(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");//主界面 listView 的 adapter
        data.addProperty("cclvapn", "com.tencent.mm.ui.f");//主界面 adapter 的实现类的父类
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");// 主界面 listView 的 onItemListener 的实现类

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "f");

        data.addProperty("mmsb", "j");//adapter 中绑定 itemView 的 model 获取方法
        data.addProperty("mago", "Aa");//adapter 父类中（等同于） getObject 方法

        data.addProperty("vmsim1", "wAm");//adapter 中判断一个 item 是否为免打扰群消息的依据1
        data.addProperty("vmsim2", "vZr");//adapter 中判断一个 item 是否为免打扰群消息的依据2
        data.addProperty("vla", "wzz");//onItemListener 中 adapter 的变量名
        data.addProperty("vl", "wyT");//onItemListener 中 listView 的变量名

        data.addProperty("vlavt", "vSy");//adapter 中 itemView 中 显示 nickname 的 TextView
        data.addProperty("vlava", "iiL");//adapter 中 itemView 中 显示 avatar 的 ImageView
        data.addProperty("vlavc", "vSA");//adapter 中 itemView 中 显示 content 的 TextView

        data.addProperty("magos1", "vkI");//adapter 父类中获取 getObject 的第一步
        data.addProperty("magos2", "vic");//adapter 父类中获取 getObject 的第二步
        data.addProperty("magos3", "Aa");//adapter 父类中获取 getObject 的第三步

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.w");//微信的日志打印类

        data.addProperty("dsa", "rz");//返回 drawable
        data.addProperty("dss", "ak2");//设置 drawable
        data.addProperty("dsca", "ty");//群头像的 drawable

        data.addProperty("vmbc", "wAe");//adapter 中获取 item 的 content 字段
        data.addProperty("vmbn", "nickName");//adapter 中获取 item 的 nickName 字段

        data.addProperty("vmbt", "wAd");//adapter 中获取 item 的 time 字段

        data.addProperty("mmtc", "b");//adapter 中获取 item 的 content 方法
        data.addProperty("vmtcp", "wAk");//adapter 中获取 item 的 content 方法中的参数
        data.addProperty("mmtt", "i");//adapter 中获取 item 的 time 方法

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");//头像的处理生成方法
        data.addProperty("mclga", "a");//adapter 中获取 item 的 avatar 方法

        data.addProperty("vmsio1", "wAl");//判断一个 item 是否为公众号的依据1
        data.addProperty("vmsio2", "wAh");//判断一个 item 是否为公众号的依据2
        data.addProperty("vmsio3", "field_username");//判断一个 item 是否为公众号的依据3

        data.addProperty("mclvap", "com.tencent.mm.ui.f$a");//adapter 构造函数的参数

        data.addProperty("cthu", "com.tencent.mm.ui.d");
        data.addProperty("mhuiv", "ai");//HomeUI 中初始化 View 的方法
        data.addProperty("vhua", "vjK");//HomeUI 中 Activity 的变量名
    }

    private void getConfig1101(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");//主界面 listView 的 adapter
        data.addProperty("cclvapn", "com.tencent.mm.ui.e");//主界面 adapter 的实现类的父类
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");// 主界面 listView 的 onItemListener 的实现类

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "e");

        data.addProperty("mmsb", "j");//adapter 中绑定 itemView 的 model 获取方法
        data.addProperty("mago", "yQ");//adapter 父类中（等同于） getObject 方法

        data.addProperty("vmsim1", "wcZ");//adapter 中判断一个 item 是否为免打扰群消息的依据1
        data.addProperty("vmsim2", "vCN");//adapter 中判断一个 item 是否为免打扰群消息的依据2
        data.addProperty("vla", "wcm");//onItemListener 中 adapter 的变量名
        data.addProperty("vl", "wbJ");//onItemListener 中 listView 的变量名

        data.addProperty("vlavt", "vvZ");//adapter 中 itemView 中 显示 nickname 的 TextView
        data.addProperty("vlava", "ipK");//adapter 中 itemView 中 显示 avatar 的 ImageView
        data.addProperty("vlavc", "vwb");//adapter 中 itemView 中 显示 content 的 TextView

        data.addProperty("magos1", "uOU");//adapter 父类中获取 getObject 的第一步
        data.addProperty("magos2", "uMW");//adapter 父类中获取 getObject 的第二步
        data.addProperty("magos3", "yQ");//adapter 父类中获取 getObject 的第三步

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.w");//微信的日志打印类

        data.addProperty("dsa", "sk");//返回 drawable
        data.addProperty("dss", "ao1");//设置 drawable
        data.addProperty("dsca", "v0");//群头像的 drawable

        data.addProperty("vmbc", "wcR");//adapter 中获取 item 的 content 字段
        data.addProperty("vmbn", "nickName");//adapter 中获取 item 的 nickName 字段

        data.addProperty("vmbt", "wcQ");//adapter 中获取 item 的 time 字段

        data.addProperty("mmtc", "b");//adapter 中获取 item 的 content 方法
        data.addProperty("vmtcp", "wcX");//adapter 中获取 item 的 content 方法中的参数
        data.addProperty("mmtt", "i");//adapter 中获取 item 的 time 方法

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");//头像的处理生成方法
        data.addProperty("mclga", "a");//adapter 中获取 item 的 avatar 方法

        data.addProperty("vmsio1", "wcY");//判断一个 item 是否为公众号的依据1
        data.addProperty("vmsio2", "wcU");//判断一个 item 是否为公众号的依据2
        data.addProperty("vmsio3", "field_username");//判断一个 item 是否为公众号的依据3

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");//adapter 构造函数的参数

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");//HomeUI 中初始化 View 的方法
        data.addProperty("vhua", "uQy");//HomeUI 中 Activity 的变量名
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

        data.addProperty("dsa", "sk");
        data.addProperty("dss", "ao1");
        data.addProperty("dsca", "v0");

        data.addProperty("vmbc", "war");
        data.addProperty("vmbn", "nickName");

        data.addProperty("vmbt", "waq");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "wax");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "a");

        data.addProperty("vmsio1", "way");
        data.addProperty("vmsio2", "wau");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uOv");
    }

    private void getConfig1081playVersion(JsonObject data) {
        data.addProperty("cclvan", "com.tencent.mm.ui.conversation.g");
        data.addProperty("cclvapn", "com.tencent.mm.ui.e");
        data.addProperty("cclaon", "com.tencent.mm.ui.conversation.e");

        data.addProperty("cclvas", "g");
        data.addProperty("cclvaps", "e");

        data.addProperty("mmsb", "j");
        data.addProperty("mago", "yQ");

        data.addProperty("vmsim1", "wcy");
        data.addProperty("vmsim2", "vCm");
        data.addProperty("vla", "wbL");
        data.addProperty("vl", "wbi");

        data.addProperty("vlavt", "vvy");
        data.addProperty("vlava", "ipv");
        data.addProperty("vlavc", "vvA");

        data.addProperty("magos1", "uOG");
        data.addProperty("magos2", "uMI");
        data.addProperty("magos3", "yQ");

        data.addProperty("ctl", "com.tencent.mm.sdk.platformtools.w");
        data.addProperty("dsa", "sk");
        data.addProperty("dss", "ao1");
        data.addProperty("dsca", "v0");

        data.addProperty("vmbc", "wcq");
        data.addProperty("vmbn", "nickName");

        data.addProperty("vmbt", "wcp");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "wcw");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "a");

        data.addProperty("vmsio1", "wcx");
        data.addProperty("vmsio2", "wct");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uQk");
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

        data.addProperty("dsa", "sb");
        data.addProperty("dss", "ao8");
        data.addProperty("dsca", "v2");

        data.addProperty("vmbc", "vJl");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "vJk");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vJr");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "h");

        data.addProperty("vmsio1", "vJs");
        data.addProperty("vmsio2", "vJo");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uyd");
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

        data.addProperty("dsa", "sb");
        data.addProperty("dss", "ao8");
        data.addProperty("dsca", "v2");

        data.addProperty("vmbc", "vID");
        data.addProperty("vmbn", "nickName");

        data.addProperty("vmbt", "vIC");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vIJ");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "h");

        data.addProperty("vmsio1", "vIK");
        data.addProperty("vmsio2", "vIG");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "af");
        data.addProperty("vhua", "uxw");
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

        data.addProperty("dsa", "rj");
        data.addProperty("dss", "ang");
        data.addProperty("dsca", "u_");

        data.addProperty("vmbc", "uZX");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "uZW");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "vad");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "h");

        data.addProperty("vmsio1", "vae");
        data.addProperty("vmsio2", "vaa");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "ad");
        data.addProperty("vhua", "tPE");
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
        data.addProperty("dsa", "rj");
        data.addProperty("dss", "ang");
        data.addProperty("dsca", "u_");

        data.addProperty("vmbc", "uXP");
        data.addProperty("vmbn", "nickName");
        data.addProperty("vmbt", "uXO");

        data.addProperty("mmtc", "b");
        data.addProperty("vmtcp", "uXV");
        data.addProperty("mmtt", "i");

        data.addProperty("csa", "com.tencent.mm.pluginsdk.ui.a$b");
        data.addProperty("mclga", "h");

        data.addProperty("vmsio1", "uXW");
        data.addProperty("vmsio2", "uXS");
        data.addProperty("vmsio3", "field_username");

        data.addProperty("mclvap", "com.tencent.mm.ui.e$a");

        data.addProperty("cthu", "com.tencent.mm.ui.HomeUI");
        data.addProperty("mhuiv", "ad");
        data.addProperty("vhua", "tNB");
    }

}
