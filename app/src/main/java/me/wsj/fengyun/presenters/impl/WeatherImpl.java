package me.wsj.fengyun.presenters.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import me.wsj.fengyun.presenters.WeatherInterface;
import me.wsj.fengyun.presenters.WeatherPresenters;
import me.wsj.fengyun.utils.ContentUtil;
import me.wsj.fengyun.utils.SpUtils;
import per.wsj.commonlib.utils.LogUtil;

import com.qweather.sdk.bean.WarningBean;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Range;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherHourlyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;


/**
 * Created by niuchong on 2018/5/17.
 */

public class WeatherImpl implements WeatherPresenters {

    private Context context;
    private WeatherInterface weatherInterface;
    private String TAG = "sky";
    private Lang lang;
    private Unit unit;


    public WeatherImpl(Context context, WeatherInterface weatherInterface) {
        this.context = context;
        this.weatherInterface = weatherInterface;
        lang = Lang.ZH_HANS;

        unit = Unit.METRIC;
    }

    @Override
    public void getWeatherNow(String location) {
//        QWeather.getWeatherNow(context, location, lang, unit, new QWeather.OnResultWeatherNowListener() {
//            @Override
//            public void onError(Throwable throwable) {
//                WeatherNowBean weatherNow = SpUtils.getBean(context, "weatherNow", WeatherNowBean.class);
//                weatherInterface.getWeatherNow(weatherNow);
//            }
//
//            @Override
//            public void onSuccess(WeatherNowBean weatherNowBean) {
//                if (Code.OK.getCode().equalsIgnoreCase(weatherNowBean.getCode().getCode())) {
//                    weatherInterface.getWeatherNow(weatherNowBean);
//                    SpUtils.saveBean(context, "weatherNow", weatherNowBean);
//                }
//            }
//
//        });

    }


    @Override
    public void getWeatherForecast(final String location) {

        QWeather.getWeather3D(context, location, lang, unit, new QWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("sky", "getWeatherForecast onError: ");
                WeatherDailyBean weatherForecast = SpUtils.getBean(context, "weatherForecast", WeatherDailyBean.class);
                weatherInterface.getWeatherForecast(weatherForecast);
                getAirForecast(location);
            }

            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                if (Code.OK.getCode().equalsIgnoreCase(weatherDailyBean.getCode().getCode())) {
                    weatherInterface.getWeatherForecast(weatherDailyBean);
                    getAirForecast(location);
                    SpUtils.saveBean(context, "weatherForecast", weatherDailyBean);
                }
            }

        });
    }

    @Override
    public void getWarning(String location) {
//        QWeather.getWarning(context, location, lang, new QWeather.OnResultWarningListener() {
//            @Override
//            public void onError(Throwable throwable) {
//                weatherInterface.getWarning(null);
//                Log.i("sky", "getWarning onError: " + throwable);
//            }
//
//            @Override
//            public void onSuccess(WarningBean warningBean) {
//                if (Code.OK.getCode().equalsIgnoreCase(warningBean.getCode().getCode())) {
//                    if (warningBean.getWarningList() != null && warningBean.getWarningList().size() > 0) {
//                        weatherInterface.getWarning(warningBean.getWarningList().get(0));
//                        SpUtils.saveBean(context, "alarm", warningBean);
//                    }
//                }
//            }
//
//        });
    }

    @Override
    public void getAirNow(final String location) {
        QWeather.getAirNow(context, location, lang, new QWeather.OnResultAirNowListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("sky", "getAirNow onError: ");
                getParentAir(location);
            }

            @Override
            public void onSuccess(AirNowBean airNowBean) {
                if (Code.OK.getCode().equalsIgnoreCase(airNowBean.getCode().getCode())) {
                    weatherInterface.getAirNow(airNowBean);
                    SpUtils.saveBean(context, "airNow", airNowBean);
                }
            }
        });
    }

    private void getParentAir(String location) {
        QWeather.getGeoCityLookup(context, location, Range.WORLD, 3, lang, new QWeather.OnResultGeoListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(GeoBean geoBean) {
                String parentCity = geoBean.getLocationBean().get(0).getAdm2();
                if (TextUtils.isEmpty(parentCity)) {
                    parentCity = geoBean.getLocationBean().get(0).getAdm1();
                }
                QWeather.getAirNow(context, parentCity, lang, new QWeather.OnResultAirNowListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        weatherInterface.getAirNow(null);
                    }

                    @Override
                    public void onSuccess(AirNowBean airNow) {
                        if (Code.OK.getCode().equalsIgnoreCase(airNow.getCode().getCode())) {
                            weatherInterface.getAirNow(airNow);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void getAirForecast(String location) {
//        HeWeather.getAirForecast(context, location, lang, unit, new HeWeather.OnResultAirForecastBeansListener() {
//            @Override
//            public void onError(Throwable throwable) {
//                Log.i(TAG, "getAirForecast onError: ");
//                AirForecast airForecast = SpUtils.getBean(context, "airForecast", AirForecast.class);
//                weatherInterface.getAirForecast(airForecast);
//            }
//
//            @Override
//            public void onSuccess(List<AirForecast> list) {
//                weatherInterface.getAirForecast(list.get(0));
//                SpUtils.saveBean(context, "airForecast", list.get(0));
//
//            }
//        });
    }


    @Override
    public void getWeatherHourly(String location) {
//        LogUtil.LOGE("getWeatherHourly location: " + location);
        QWeather.getWeather24Hourly(context, location, lang, unit, new QWeather.OnResultWeatherHourlyListener() {
            @Override
            public void onError(Throwable throwable) {
//                LogUtil.LOGE("getWeatherHourly onError: " + throwable.toString());
            }

            @Override
            public void onSuccess(WeatherHourlyBean weatherHourlyBean) {
//                LogUtil.LOGE("getWeatherHourly onSuccess: " + Code.OK.getCode());
                if (Code.OK.getCode().equalsIgnoreCase(weatherHourlyBean.getCode().getCode())) {
                    weatherInterface.getWeatherHourly(weatherHourlyBean);
                    SpUtils.saveBean(context, "weatherHourly", weatherHourlyBean);
                }
            }
        });
    }
}
