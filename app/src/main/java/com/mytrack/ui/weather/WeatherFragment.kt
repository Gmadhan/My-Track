package com.mytrack.ui.weather

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import com.mytrack.R
import com.mytrack.api.ApiCall
import com.mytrack.api.RetrofitClientInstance
import com.mytrack.databinding.FragmentWeatherBinding
import com.mytrack.model.WeatherResponse
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify.Appid
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils.capitalizeWords
import com.mytrack.utils.Utils.convertDateTime
import com.mytrack.utils.Utils.isNetworkAvailable
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.nigthtime
import com.mytrack.utils.Utils.showSnackBar
import com.mytrack.utils.Utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WeatherFragment: Fragment() {

    private lateinit var fragmentWeatherBinding: FragmentWeatherBinding
    private var currentweather: String? = ""
    private var lat:String? = "0.0"
    private var lng:String? = "0.0"
    private var TAG = "WeatherFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentWeatherBinding = FragmentWeatherBinding.inflate(layoutInflater, container, false)
        init()
        return fragmentWeatherBinding.root
    }

    private fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        fragmentWeatherBinding.header.txtHeaderName.text = requireActivity().getString(R.string.weather)
        fragmentWeatherBinding.header.btnBack.visibility = View.GONE
        lat = SessionSave.getSession(Constants.GPSLAT, requireActivity())
        lng = SessionSave.getSession(Constants.GPSLNG, requireActivity())

        val sdf = SimpleDateFormat("HH")
        val currentTime = sdf.format(Date()).toInt()
        nigthtime = currentTime >= 19 || currentTime <= 6

        weather()
        fragmentWeatherBinding.pullToRefresh.setOnRefreshListener { weather() }

//        fragmentWeatherBinding.btnSearchVoice.setOnClickListener { displaySpeechRecognizer() }
    }

    private fun weather() {
        try {
            if (isNetworkAvailable(requireActivity())) {
                val service: ApiCall =
                    RetrofitClientInstance.getRetrofitInstance()!!.create(ApiCall::class.java)
                val call: Call<WeatherResponse> = service.getweather(lat, lng, Appid)

                call.enqueue(object : Callback<WeatherResponse?> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<WeatherResponse?>,
                        response: Response<WeatherResponse?>
                    ) {
                        fragmentWeatherBinding.pullToRefresh.isRefreshing = false
                        if (response.isSuccessful) {
                            val data: WeatherResponse? = response.body()
                            val temp: Double = data!!.main.temp.toDouble() - 273.15
                            val tempMin: Double =
                                data!!.main.temp_min.toDouble() - 277.15 /*-4 for avg min temp*/
                            val tempMax: Double = data!!.main.temp_max.toDouble() - 273.15
                            val windspeed: Double = data!!.wind.speed.toDouble() * 3.6
                            val rise: Long = data!!.sys.sunrise.toLong()
                            val set: Long = data!!.sys.sunset.toLong()
                            val risedate = Date(rise * 1000)
                            val setdate = Date(set * 1000)

                            val sunrise = convertDateTime(risedate, "hh:mm a")
                            val sunset = convertDateTime(setdate, "hh:mm a")
                            logger(TAG, "date -- $sunrise $sunset")

                            fragmentWeatherBinding.txtSunrise.text = sunrise
                            fragmentWeatherBinding.txtSunset.text = sunset
                            fragmentWeatherBinding.txtTemp.text = temp.toInt()
                                .toString() + getString(R.string.space) + getString(R.string.celsius)
                            fragmentWeatherBinding.txtTempMin.text = tempMin.toInt()
                                .toString() + getString(R.string.space) + getString(R.string.celsius)
                            fragmentWeatherBinding.txtTempMax.text = tempMax.toInt()
                                .toString() + getString(R.string.space) + getString(R.string.celsius)
                            fragmentWeatherBinding.txtPressure.text =
                                data.main.pressure.toString() + getString(R.string.space) + getString(
                                    R.string.mb
                                )
                            fragmentWeatherBinding.txtWindSpeed.text = windspeed.toInt()
                                .toString() + getString(R.string.space) + getString(R.string.km)
                            fragmentWeatherBinding.txtHumidity.text =
                                data.main.humidity.toString() + getString(R.string.space) + getString(
                                    R.string.percentage
                                )
                            fragmentWeatherBinding.txtCity.text = data.name
                            fragmentWeatherBinding.txtCountry.text = data.sys.country
                            fragmentWeatherBinding.txtWeather.text =
                                capitalizeWords(data.weather[0].description)
                            fragmentWeatherBinding.txtTemperature.text = temp.toInt()
                                .toString() + getString(R.string.space) + getString(R.string.celsius)
                            for (i in 0 until data.weather.size) {
                                currentweather = data.weather[i].main
                            }
                            SessionSave.saveSession(
                                Constants.WEATHER,
                                currentweather,
                                requireActivity()
                            )
                            if (currentweather != null) {
                                if (currentweather.equals(
                                        "Clouds",
                                        ignoreCase = true
                                    )
                                ) /* Clouds-id:"801",icon:"02n" */ fragmentWeatherBinding.LAWeatherImage.setAnimation(
                                    if (nigthtime) "weather-cloudynight.json" else "weather-partly-cloudy.json"
                                ) else if (currentweather.equals(
                                        "Mist",
                                        ignoreCase = true
                                    ) || currentweather.equals("Haze", ignoreCase = true)
                                ) /* Mist-id:"701" , icon:"50n" haze-id:"721", icon:"50d" */ fragmentWeatherBinding.LAWeatherImage.setAnimation(
                                    "weather-mist.json"
                                ) else if (currentweather.equals(
                                        "Rain",
                                        ignoreCase = true
                                    ) || currentweather.equals("Drizzle", ignoreCase = true)
                                ) /* Rain-"id":500" , "icon":"10d" drizzle-"id":300,"icon":"09n" */ fragmentWeatherBinding.LAWeatherImage.setAnimation(
                                    if (nigthtime) "weather-rainynight.json" else "weather-partly-shower.json"
                                ) else if (currentweather.equals(
                                        "Thunderstorm",
                                        ignoreCase = true
                                    ) || currentweather.equals("Lightning", ignoreCase = true)
                                ) /* Rain-"id":500" , "icon":"10d" drizzle-"id":300,"icon":"09n" */ fragmentWeatherBinding.LAWeatherImage.setAnimation(
                                    if (nigthtime) "weather-storm.json" else "weather-stormshowersday.json"
                                ) else if (currentweather.equals(
                                        "Snow",
                                        ignoreCase = true
                                    )
                                ) fragmentWeatherBinding.LAWeatherImage.setAnimation(if (nigthtime) "weather-snownight.json" else "weather-snow-sunny.json") else fragmentWeatherBinding.LAWeatherImage.setAnimation(
                                    if (nigthtime) "weather-night.json" else "weather-sunny.json"
                                )
                            }
                            fragmentWeatherBinding.tempLay.visibility = View.VISIBLE
                            fragmentWeatherBinding.txtWeather.visibility = View.VISIBLE
                            fragmentWeatherBinding.LAWeatherImage.playAnimation()
                            fragmentWeatherBinding.LAWeatherImage.repeatCount =
                                LottieDrawable.INFINITE
                            fragmentWeatherBinding.pullToRefresh.isRefreshing = false
                            logger(TAG, "weatherActivity : success $response")
                        } else {
                            showSnackBar(
                                requireView(),
                                getString(R.string.something_went_wrong),
                                requireActivity(),
                                false
                            )

                            showToast(requireActivity(), getString(R.string.something_went_wrong))
                            logger(TAG, "weatherActivity : isfailure $response")
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse?>, t: Throwable) {
                        showToast(requireActivity(), getString(R.string.something_went_wrong))
                        showSnackBar(requireView(), t.message.toString(), requireActivity(), false)
                        logger(TAG, "weatherActivity : failure $t")
                        fragmentWeatherBinding.pullToRefresh.isRefreshing = false
                    }
                })
            } else {
                showSnackBar(
                    view = requireActivity().findViewById(R.id.fab),
                    getString(R.string.no_network_connection),
                    requireActivity(),
                    false
                )
                fragmentWeatherBinding.pullToRefresh.isRefreshing = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}