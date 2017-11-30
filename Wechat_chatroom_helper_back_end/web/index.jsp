<%@ page import="com.zdy.project.wechat_chatroom_helper.db.DataBaseManager" %>
<%@ page import="java.util.*" %>
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
    <%
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);

        long currentTime = System.currentTimeMillis();
        long time = instance.getTimeInMillis();
    %>
    <tr>
        <th>日期</th>
        <td>今天</td>
        <td>昨天</td>
        <td>过去七天</td>
    </tr>
    <tr>
        <th>用户数量</th>
        <td><%
            int todayCount = DataBaseManager.getInstance().queryUserCountByTime(time, currentTime);
            out.println(DataBaseManager.getInstance().queryUserCountByTime(time, currentTime)); %></td>
        <td><%out.println(DataBaseManager.getInstance().queryUserCountByTime(time - 86400000, time)); %></td>
        <td><%out.println(DataBaseManager.getInstance().queryUserCountByTime(time - 86400000 * 7, time));%></td>
    </tr>
</table>


<p>今日微信版本统计</p>

<%
    HashMap<Integer, Integer> wechatMap = DataBaseManager.getInstance().queryWechatVersionPercent(time, currentTime);

%>
<table>
    <%
        ArrayList<String> wechatNameArray = new ArrayList<>();
        ArrayList<Integer> wechatCountArray = new ArrayList<>();
        ArrayList<Float> wechatFloatArray = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : wechatMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            switch (key) {
                case -1:
                    wechatNameArray.add("未记录版本");
                    break;
                case 0:
                    wechatNameArray.add("未适配版本");
                    break;
                case 1060:
                    wechatNameArray.add("6.5.8（1060）及其play版本");
                    break;
                case 1080:
                    wechatNameArray.add("6.5.10（1080）及其play版本");
                    break;
                case 1081:
                    wechatNameArray.add("6.5.13（1081）play版本");
                    break;
                case 1100:
                    wechatNameArray.add("6.5.13（1100）或6.5.14（1100）");
                    break;
                case 1101:
                    wechatNameArray.add("6.5.16（1101）play版本");
                    break;
                case 1120:
                    wechatNameArray.add("6.5.16（1120）");
                    break;
                case 1140:
                    wechatNameArray.add("6.5.19（1140）");
                    break;
                case 1160:
                    wechatNameArray.add("6.5.22（1160）");
                    break;
            }
            wechatCountArray.add(value);
            wechatFloatArray.add(Float.valueOf(value) / todayCount);
        }
    %>

    <tr>
        <th>版本号</th>

        <%
            for (String item : wechatNameArray) {
        %>
        <td><% out.print(item);%></td>

        <% } %>

    </tr>


    <tr>
        <th>用户数量</th>

        <%
            for (Integer item : wechatCountArray) {
        %>
        <td><% out.print(item);%></td>

        <% } %>

    </tr>

    <tr>
        <th>百分比</th>

        <%
            for (Float item : wechatFloatArray) {
        %>
        <td><% out.print(String.format(Locale.CHINESE, "%.2f", item * 100));%>%</td>

        <% } %>

    </tr>

</table>


<p>今日群助手版本统计</p>

<%
    HashMap<Integer, Integer> helperMap = DataBaseManager.getInstance().queryHelperVersionPercent(time, currentTime);

    Integer temp = helperMap.get(24);
    helperMap.remove(24);
    helperMap.put(26, helperMap.get(26) + temp);
%>

<table>
    <%
        ArrayList<String> helperNameArray = new ArrayList<>();
        ArrayList<Integer> helperCountArray = new ArrayList<>();
        ArrayList<Float> helperFloatArray = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : helperMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            switch (key) {
                case 26:
                    helperNameArray.add("1.3.11beta(26)");
                    break;
                case 25:
                    helperNameArray.add("1.3.10beta-branch2(25)");
                    break;
                case 24:
                  //  helperNameArray.add("1.3.11beta(26)");
                    break;
                case 23:
                    helperNameArray.add("1.3.10beta(23)");
                    break;
                case 22:
                    helperNameArray.add("1.3.9(22)");
                    break;
                case 21:
                    helperNameArray.add("1.3.8beta(21)");
                    break;
                case 20:
                    helperNameArray.add("1.3.7(20)");
                    break;
                case 19:
                    helperNameArray.add("1.3.6(19)");
                    break;
                case 18:
                    helperNameArray.add("1.3.5beta(18)");
                    break;
                case 17:
                    helperNameArray.add("1.3.4(17)");
                    break;
                case 16:
                    helperNameArray.add("1.3.3beta(16)");
                    break;
                default:
                    helperNameArray.add("其他版本");
                    break;
            }
            helperCountArray.add(value);
            helperFloatArray.add(Float.valueOf(value) / todayCount);
        }
    %>

    <tr>
        <th>版本号</th>

        <%
            for (String item : helperNameArray) {
        %>
        <td><% out.print(item);%></td>

        <% } %>

    </tr>


    <tr>
        <th>用户数量</th>

        <%
            for (Integer item : helperCountArray) {
        %>
        <td><% out.print(item);%></td>

        <% } %>

    </tr>

    <tr>
        <th>百分比</th>

        <%
            for (Float item : helperFloatArray) {
        %>
        <td><% out.print(String.format(Locale.CHINESE, "%.2f", item * 100));%>%</td>

        <% } %>

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
