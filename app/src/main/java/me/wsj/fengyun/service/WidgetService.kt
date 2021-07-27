package me.wsj.fengyun.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.wsj.fengyun.BuildConfig
import me.wsj.fengyun.R
import me.wsj.fengyun.bean.Now
import me.wsj.fengyun.bean.WeatherNow
import me.wsj.fengyun.db.AppRepo
import me.wsj.fengyun.ui.activity.HomeActivity
import me.wsj.fengyun.ui.fragment.CACHE_WEATHER_NOW
import me.wsj.fengyun.utils.Lunar
import me.wsj.fengyun.utils.NotificationUtil
import me.wsj.fengyun.utils.RomUtil
import me.wsj.fengyun.widget.WeatherWidget
import me.wsj.lib.net.HttpUtils
import me.wsj.lib.utils.IconUtils
import per.wsj.commonlib.utils.LogUtil
import java.util.*

const val Notify_Id = 999

class WidgetService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.e("onCreate: ---------------------")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForeground(Notify_Id, NotificationUtil.createNotification(this, Notify_Id))
//        }

        updateRemote()
    }

    private suspend fun getWeather(cityId: String) {
        flow {
            val url = "https://devapi.qweather.com/v7/weather/now"
            val param = HashMap<String, Any>()
            param["location"] = cityId
            param["key"] = BuildConfig.HeFengKey

            HttpUtils.get<WeatherNow>(url, param, null)?.now?.let { emit(it) }
        }.flowOn(Dispatchers.Main).collect {

        }
    }

    private fun updateRemote() {

        lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
            updateRemote()
        }) {
            withContext(Dispatchers.IO) {
                while (true) {
                    delay(5_000)
                    val cities = AppRepo.getInstance().getCities()
                    if (cities.isNotEmpty()) {
                        var cityId = cities[0].cityId
                        var cityName = cities[0].cityName
                        cities.forEach {
                            if (it.isLocal) {
                                cityId = it.cityId
                                cityName = it.cityName
                            }
                            return@forEach
                        }

                        val url = "https://devapi.qweather.com/v7/weather/now"
                        val param = HashMap<String, Any>()
                        param["location"] = cityId
                        param["key"] = BuildConfig.HeFengKey

                        var now: Now? = null
                        HttpUtils.get<WeatherNow>(url, param) { _, result ->
                            now = result.now
                        }

                        NotificationUtil.updateNotification(
                            this@WidgetService,
                            Notify_Id,
                            cityName,
                            now
                        )

                        updateWidget(cityId, cityName, now)
                    }
                    delay(1800_000)
                }
            }
        }
    }

    private suspend fun updateWidget(cityId: String, cityName: String, now: Now?) {
        LogUtil.e("update....")

        val views = RemoteViews(packageName, R.layout.weather_widget)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.tvLocation, location)

        now?.let {
            AppRepo.getInstance()
                .saveCache(CACHE_WEATHER_NOW + cityId, it)

            views.setTextViewText(R.id.tvWeather, it.text)
            views.setTextViewText(R.id.tvTemp, it.temp + "°C")
            views.setImageViewResource(R.id.ivWeather, IconUtils.getDayIconDark(this, it.icon))
        }
        views.setTextViewText(R.id.tvLunarDate, Lunar(Calendar.getInstance()).toString())

        initEvent(views)

        val componentName = ComponentName(this, WeatherWidget::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, views);
    }

    /**
     * 点击事件相关
     */
    private fun initEvent(views: RemoteViews) {
        // 日历
        val calendarIntent = Intent()
        val pkg =
            if (Build.VERSION.SDK_INT >= 8) "com.android.calendar" else "com.google.android.calendar"
        calendarIntent.component = ComponentName(pkg, "com.android.calendar.LaunchActivity")

        val calendarPI = PendingIntent.getActivity(this, 0, calendarIntent, 0)
        views.setOnClickPendingIntent(R.id.llCalendar, calendarPI)
        views.setOnClickPendingIntent(R.id.tvLunarDate, calendarPI)

        // 时钟
        val clockIntent = Intent()
//        LogUtil.e("brand: " + Build.BOARD)
        val cls = if (RomUtil.isEmui()) "com.android.deskclock.AlarmsMainActivity"
        else "com.android.deskclock.DeskClock"

        clockIntent.component =
            ComponentName("com.android.deskclock", cls)
        val timePI = PendingIntent.getActivity(this, 0, clockIntent, 0)
        views.setOnClickPendingIntent(R.id.clockTime, timePI)

        // 风云
        val weatherIntent = Intent(this, HomeActivity::class.java)
        val weatherPI = PendingIntent.getActivity(this, 0, weatherIntent, 0)
        views.setOnClickPendingIntent(R.id.llWeather, weatherPI)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.e("onDestroy: ---------------------")
    }
}