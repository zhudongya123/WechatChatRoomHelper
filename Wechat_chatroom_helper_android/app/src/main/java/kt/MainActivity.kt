package kt

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.ui.MainActivity

/**
 * Created by Mr.Zdy on 2017/10/17.
 */
class MainActivity : AppCompatActivity() {


    var setttingFragment: MainActivity.SettingFragment? = null

    var sharedPreferences: SharedPreferences? = null

    private var textView: TextView? = null
    private var button: Button? = null
    private var detail: TextView? = null

    internal var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}