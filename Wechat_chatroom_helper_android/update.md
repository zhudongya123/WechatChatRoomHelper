人老了 要写更新 不然做了什么事情自己不知道，我知道这是md，累了不想用MD写了。


versionCode 56
versionName "1.4.21"

以微信8027版本为反编译的源包适配。https://dldir1.qq.com/weixin/android/weixin8027android2220_arm64_1.apk

同时在2022年六月更新了添加助手View页面至 微信MainActivity的方法
参见提交 #8961852664f15812f724bbb01dcaa8ee1a9bcdcb
现在大概不会一直循环打印日志了

主要整理了微信聊天列表 conversationItem获取时间 内容 群聊名称/服务号名称 的方法。

获取ConversationItem content的方法
private java.lang.CharSequence a(com.tencent.mm.storage.be beVar, com.tencent.mm.ui.conversation.k.d dVar, int i2, com.tencent.mm.ui.conversation.k.e eVar, boolean z, com.tencent.mm.protocal.protobuf.has has) {

获取ConversationItem 回话名称的方法
现在直接取bean字段的成员变量 可能某些情况有Bug，比如解散的群聊不显示名字 就显示群聊。

更新公告可以这么写：

1. 以8027为基准适配。
2. 优化初始化View时的性能。
3. 新增Bug 部分群聊/服务号名称 显示不正常。
