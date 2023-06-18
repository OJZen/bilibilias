package com.imcys.bilibilias.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.imcys.bilibilias.base.utils.asLogD
import com.imcys.bilibilias.common.R
import com.imcys.bilibilias.common.base.app.BaseApplication
import com.imcys.bilibilias.common.base.model.user.AsUser
import com.imcys.bilibilias.common.broadcast.ThemeChangedBroadcast
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.tencent.mmkv.MMKV
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX
import java.util.Locale


open class AbsActivity : AppCompatActivity() {

    private val mThemeChangedBroadcast by lazy {
        ThemeChangedBroadcast()

    }

    open val asSharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val asUser: AsUser
        get() = run {
            val kv = BaseApplication.dataKv
            AsUser.apply {
                cookie = kv.decodeString("cookies", "")!!
                sessdata = kv.decodeString("SESSDATA", "")!!
                biliJct = kv.decodeString("bili_jct", "")!!
                mid = kv.decodeLong("mid", 0)
                asCookie = kv.decodeString("as_cookie", "")!!
            }
        }


    // 存储所有活动的列表
    private val activities = mutableListOf<Activity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 打印活动名称
        asLogD(this, javaClass.simpleName)
        // 启动APP统计
        startAppCenter()
        // 添加当前活动
        addActivity(this)
        // 判断主题
        setTheme()

    }

    private fun initAsUser() {

    }


    override fun onDestroy() {
        super.onDestroy()
        // 取消注册（防止泄露）
        unregisterReceiver(mThemeChangedBroadcast)
        // 移除当前活动
        removeActivity(this)
    }

    /**
     * 启动统计
     */
    private fun startAppCenter() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("microsoft_app_center_type", false)) {
            if (!AppCenter.isConfigured()) {
                // 统计接入
                AppCenter.start(
                    application,
                    BaseApplication.appSecret,
                    Analytics::class.java,
                    Crashes::class.java
                )
            }

        }

    }


    override fun onResume() {
        super.onResume()
        // 注册广播
        registerReceiver(
            // 这块是主题广播
            mThemeChangedBroadcast,
            IntentFilter("com.imcys.bilibilias.app.THEME_CHANGED")
        )

    }


    // 添加活动
    private fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    // 移除活动
    private fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    // 结束所有活动
    fun finishAll() {
        activities.forEach {
            if (!it.isFinishing) it.finish()
        }
    }

    // 沉浸式状态栏
    fun statusBarOnly(fragmentActivity: FragmentActivity) {
        UltimateBarX.statusBarOnly(fragmentActivity)
            .fitWindow(false)
            .light(true)
            .apply()
    }


    open fun updateTheme() {
        // 重启activity（）
        recreate()
    }


    // 检查主题
    private fun setTheme() {
        val theme = PreferenceManager.getDefaultSharedPreferences(this).run {
            getString("app_theme", "System")
        }
        when (theme) {
            "System" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            }

            "Light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            "Dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            "Pink" -> {
                this.setTheme(R.style.Theme_BILIBILIAS)
            }

            "Blue" -> {
                this.setTheme(R.style.BILIBILIAS_BLUE)
            }
        }
    }

    private fun setLanguage() {
        val language = PreferenceManager.getDefaultSharedPreferences(this).run {
            getString("app_language", "System")
        }

        val config = resources.configuration
        val locale = when (language) {
            "System" -> {
                // Let the system handle the language
                Locale.setDefault(Locale(""))
                config.setLocale(Locale(""))
                config
            }
            "zh_CN" -> {
                Locale.setDefault(Locale("zh", "CN"))
                config.setLocale(Locale("zh", "CN"))
                config
            }
            "en_US" -> {
                Locale.setDefault(Locale("en", "US"))
                config.setLocale(Locale("en", "US"))
                config
            }
            "zh_TW" -> {
                Locale.setDefault(Locale("zh", "TW"))
                config.setLocale(Locale("zh", "TW"))
                config
            }
            else -> config
        }

        // Update the configuration
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save the language preference
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putString("app_language", language).apply()

        // Restart the activity
        recreate()
    }

}