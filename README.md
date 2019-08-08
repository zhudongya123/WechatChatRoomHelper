# （微信群消息助手）WechatChatroomHelper

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3d5206b20875422f969a0068b36746bf)](https://app.codacy.com/app/zhudongya123/WechatChatRoomHelper?utm_source=github.com&utm_medium=referral&utm_content=zhudongya123/WechatChatRoomHelper&utm_campaign=Badge_Grade_Dashboard)
[![](https://img.shields.io/github/tag/zhudongya123/WechatChatroomHelper.svg?style=flat-square)](https://github.com/zhudongya123/WechatChatroomHelper/tags)
[![](https://img.shields.io/github/release/zhudongya123/WechatChatroomHelper.svg?style=flat-square)](https://github.com/zhudongya123/WechatChatroomHelper/releases)
![](https://img.shields.io/github/forks/zhudongya123/WechatChatroomHelper.svg?style=flat-square)
![](https://img.shields.io/github/stars/zhudongya123/WechatChatroomHelper.svg?style=flat-square)

[![](https://img.shields.io/github/issues/zhudongya123/WechatChatroomHelper.svg?style=flat-square)](https://github.com/zhudongya123/WechatChatroomHelper/issues)

 [![](https://img.shields.io/gitter/room/WechatChatroomHelper/nw.js.svg?style=flat-square)](https://gitter.im/WechatChatroomHelper)

## 简介

本软件在微信（WeChat）中实现了类似于 QQ 中群消息助手的功能，可以将所有**群聊**和**服务号**从主界面收入至单独的二级界面进行统一管理，使得主界面更加清爽高效，同时也使得群消息管理起来更加方便快捷。


下载地址：
- [酷安](https://www.coolapk.com/apk/com.zdy.project.wechat_chatroom_helper)
- [手机乐园 (历史版本)](https://soft.shouji.com.cn/down/168556.html)
- [GitHub Realase](https://github.com/zhudongya123/WechatChatRoomHelper/releases)

项目主页及更多信息：
- [项目反馈地址](http://122.152.202.233:8080/wechat/wechat_download.jsp)
- [每日用户统计](http://122.152.202.233:8080/wechat/user_stat.jsp)

## 博客
开发历程和Xposed部分教程正在逐步编写中，访问：[WechatChatRoomHelper_Tutorial](https://github.com/zhudongya123/WechatChatRoomHelper_Tutorial)


## 功能

- 群聊可以收入至二级界面进行管理。
- 公众号（服务号）可以收入至二级界面进行管理。


- 群聊和公众号可以设置白名单，使其不在二级界面出现。
- 二级界面的 UI 组件颜色可以自定义。
- 两个入口提供一键清除未读数和置顶功能。
- 可以使订阅号返回老版本微信的会话列表页而不是新版的时间线页。


## 效果演示

![](github_page_resource/1.gif)

## 提示

 本软件需要在 Root 后的手机中安装 Xposed 框架后方能使用。

 关于 Xposed ，可以在这个[链接](http://repo.xposed.info/module/de.robv.android.xposed.installer)了解并下载安装。

 本软件在初次使用和微信版本发生变更时需要重新获取配置，请在程序中查看。

## 支持的微信版本

自 1.3.17 后使用了动态技术适配了最近几个月的微信版本，理论上来说大于 微信6.6.7 (1320) 的版本都可生效。

具体情况请在程序中获取配置时查看。

## 目前存在的问题

- （可能）存在一些导致微信闪退的问题。
