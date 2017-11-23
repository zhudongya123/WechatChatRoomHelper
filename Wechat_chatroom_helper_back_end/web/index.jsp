<%@ page import="com.zdy.project.wechat_chatroom_helper.db.DataBaseManager" %>
<%@ page import="java.util.Calendar" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>😐</title>

    <style type="text/css">
        th, td {
            border: 1px solid black;
            padding: 5px;
            text-align: center;
        }</style>
</head>
<body>


<p>用户数量统计</p>
<table>
    <tr>
        <th>日期</th>
        <th>用户数量</th>
    </tr>
    <tr>
        <td>今天</td>
        <td><%
            long currentTime = System.currentTimeMillis();

            Calendar instance = Calendar.getInstance();
            instance.set(Calendar.HOUR_OF_DAY, 0);
            instance.set(Calendar.MINUTE, 0);
            instance.set(Calendar.SECOND, 0);

            long time = instance.getTimeInMillis();

            int todayCount = DataBaseManager.getInstance().queryDataByTime(time, currentTime);

            out.println(todayCount);
        %></td>
    </tr>
    <tr>
        <td>昨天</td>
        <td><%out.println(DataBaseManager.getInstance().queryDataByTime(time - 86400000, time)); %></td>
    </tr>

    <tr>
        <td>过去七天</td>
        <td><%
            instance = Calendar.getInstance();
            instance.set(Calendar.HOUR_OF_DAY, 0);
            instance.set(Calendar.MINUTE, 0);
            instance.set(Calendar.SECOND, 0);

            time = instance.getTimeInMillis();

            out.println(DataBaseManager.getInstance().queryDataByTime(time - 86400000 * 7, time));
        %></td>
    </tr>
</table>

<p><a href="https://github.com/zhudongya123/WechatChatroomHelper/issues">反馈地址</a></p>
鸣谢:<br>
<p><a href="https://www.coolapk.com/apk/com.toshiba_dealin.developerhelper">开发者助手开发者（东芝）</a></p>
<p><a href="https://github.com/veryyoung">微信红包开发者（veryyoung）</a></p>
<br>


<p>微信6.5.19与6.5.22已经支持</p>

<a href="wechat_download.jsp">点我下载支持的微信版本~</a>

<p>欢迎加入反馈群，测试版本在此提供，同时可以更好的反馈BUG及建议~</p>


<img src="http://mr-zdy-shanghai.oss-cn-shanghai.aliyuncs.com/wechat_chatroom_helper/feedback_chatroom.png" width="400"
     height="250">


</body>
</html>
