<%@ page import="com.zdy.project.wechat_chatroom_helper.db.DataBaseManager" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %><%--
  Created by IntelliJ IDEA.
  User: zhudo
  Date: 2017/7/24
  Time: 17:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>$Title$</title>
</head>
<body>


<%
    long currentTime = System.currentTimeMillis();

    Calendar instance = Calendar.getInstance();
    instance.set(Calendar.HOUR_OF_DAY, 0);
    instance.set(Calendar.MINUTE, 0);
    instance.set(Calendar.SECOND, 0);

    long time = instance.getTimeInMillis();

    int todayCount = DataBaseManager.getInstance().queryDataByTime(time, currentTime);

    out.println(todayCount);
%>

<p><a href="https://github.com/zhudongya123/WechatChatroomHelper/issues">反馈地址</a></p>
鸣谢:<br>
<p><a href="https://www.coolapk.com/apk/com.toshiba_dealin.developerhelper">开发者助手开发者（东芝）</a></p>
<p><a href="https://github.com/veryyoung">微信红包开发者（veryyoung）</a></p>
<br>


<p>微信6.5.14即将支持，请耐心等候~</p>

<p>欢迎加入反馈群，测试版本在此提供，同时可以更好的反馈BUG及建议~</p>



<img src="http://mr-zdy-shanghai.oss-cn-shanghai.aliyuncs.com/wechat_chatroom_helper/feedback_chatroom.png" width="300"
     height="412">


</body>
</html>
