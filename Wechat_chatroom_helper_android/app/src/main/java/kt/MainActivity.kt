package kt

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.ui.MainActivity

/**
 * Created by Mr.Zdy on 2017/10/17.
 */
class MainActivity : AppCompatActivity() {


    var setttingFragment: SettingFragment? = null

    var sharedPreferences: SharedPreferences? = null

    private var textView: TextView? = null
    private var button: Button? = null
    private var detail: TextView? = null

    internal var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView) as TextView
        button = findViewById(R.id.button) as Button
        detail = findViewById(R.id.detail) as TextView

        var fragmentContent: FrameLayout = findViewById(R.id.fragment_content) as FrameLayout

        sharedPreferences = getSharedPreferences(this.packageName + "_preferences", Context.MODE_WORLD_READABLE)


        setttingFragment = SettingFragment()
    }

    class SettingFragment :PreferenceFragment(){

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }

    }
}