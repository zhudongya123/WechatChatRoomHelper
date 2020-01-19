package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

import com.zdy.project.wechat_chatroom_helper.Constants

object WXObject {


    object Message {

        object C {
            const val SQLiteDatabase = "com.tencent.wcdb.database.SQLiteDatabase"
            const val SQLiteDatabaseCursorFactory = "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"
            const val SQLiteErrorHandler = "com.tencent.wcdb.DatabaseErrorHandler"
            const val SQLiteCancellationSignal = "com.tencent.wcdb.support.CancellationSignal"
        }

        object M {
            const val QUERY = "rawQueryWithFactory"
            const val INSERT = "insertWithOnConflict"
            const val UPDATE = "updateWithOnConflict"
            const val EXECSQL = "execSQL"

        }
    }


    object MainUI {

        object M {
            const val DispatchKeyEventOfLauncherUI = "dispatchKeyEvent"
            const val OnCreate = "onCreate"
            const val OnResume = "onResume"

            const val StartChattingOfLauncherUI = "startChatting"
            const val CloseChattingOfLauncherUI = "closeChatting"
        }

        object C {
            const val LauncherUI = "com.tencent.mm.ui.LauncherUI"
            const val FitSystemWindowLayoutView = "com.tencent.mm.ui.HomeUI\$FitSystemWindowLayoutView"
            const val MMFragmentActivity = "com.tencent.mm.ui.MMFragmentActivity"
        }

    }


    object Adapter {

        object M {

            const val SetActivity = "setActivity"
            const val SetAdapter = "setAdapter"
            const val GetCount = "getCount"
            const val OnItemClick = "onItemClick"
            const val OnItemLongClick = "onItemLongClick"
            const val OnCreateContextMenu = "onCreateContextMenu"
            const val OnMMMenuItemSelected = "onMMMenuItemSelected"


            const val GetView = "getView"
        }

        object C {
            var ConversationWithCacheAdapter = ""
            var ConversationWithAppBrandListView = "${Constants.WECHAT_PACKAGE_NAME}.ui.conversation.ConversationWithAppBrandListView"
            var ConversationListView = "${Constants.WECHAT_PACKAGE_NAME}.ui.conversation.ConversationListView"
            var ConversationAvatar = ""
            var ConversationClickListener = ""
            var ConversationLongClickListener = ""
            var ConversationMenuItemSelectedListener = ""
            var ConversationStickyHeaderHandler = ""

            const val NoMeasuredTextView = "com.tencent.mm.ui.base.NoMeasuredTextView"
        }

        object F {
            var ConversationItemHighLightSelectorBackGroundInt = 0
            var ConversationItemSelectorBackGroundInt = 0
        }
    }


    object Tool {

        object C {
            var Logcat = ""
        }

    }


}