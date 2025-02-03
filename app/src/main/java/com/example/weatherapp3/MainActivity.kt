package com.example.weatherapp3

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val API_KEY = "e3ddbe05691cbfd23374e78d0bb16f75"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCity = findViewById<EditText>(R.id.etCity)
        val btnGetWeather = findViewById<Button>(R.id.btnGetWeather)
        val tvWeather = findViewById<TextView>(R.id.tvWeather)
        val btnFavorite = findViewById<Button>(R.id.btnFavorite) // Кнопка звездочки
        val btnChangeTheme = findViewById<Button>(R.id.btnChangeTheme)

        // Загрузка города при запуске приложения
        loadCity(etCity)

        btnGetWeather.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeather(city, tvWeather)
                btnFavorite.visibility = Button.VISIBLE // Показываем кнопку звездочки
                saveCity(city) // Сохраняем город
            } else {
                Toast.makeText(this, "Введите город", Toast.LENGTH_SHORT).show()
            }
        }

        btnFavorite.setOnClickListener {
            val city = etCity.text.toString()
            if (city.isNotEmpty()) {
                // Сохранить город в избранное, если нужно
                Toast.makeText(this, "$city добавлен в избранное", Toast.LENGTH_SHORT).show()
            }
        }

        btnChangeTheme.setOnClickListener {
            toggleTheme()
        }
    }

    private fun getWeatherEmoji(description: String, main: String): String {
        return when (main.lowercase()) {
            "clear" -> "☀️"
            "clouds" -> when (description.lowercase()) {
                "небольшая облачность" -> "🌤"
                "переменная облачность" -> "⛅"
                "облачно с прояснениями" -> "⛅"
                else -> "☁️"
            }
            "rain" -> when (description.lowercase()) {
                "небольшой дождь" -> "🌦"
                "дождь" -> "🌧"
                "сильный дождь" -> "🌧"
                else -> "🌧"
            }
            "drizzle" -> "🌦"
            "thunderstorm" -> "⛈"
            "snow" -> when (description.lowercase()) {
                "небольшой снег" -> "🌨"
                "снег" -> "🌨"
                "сильный снег" -> "❄️"
                else -> "🌨"
            }
            "mist", "fog" -> "🌫"
            "haze" -> "🌫"
            else -> "🌡" // значок по умолчанию, если ничего не подошло
        }
    }

    private fun getWeather(city: String, tvWeather: TextView) {
        RetrofitClient.instance.getWeather(city, API_KEY)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { weather ->
                            val weatherData = weather.weather.firstOrNull()
                            val description = weatherData?.description ?: "нет данных"
                            val main = weatherData?.main ?: ""
                            val weatherEmoji = getWeatherEmoji(description, main)

                            val weatherText = buildString {
                                append("Температура: 🌡 ${weather.main.temp}°C\n")
                                append("Погодные условия: $weatherEmoji ${description.capitalize()}\n")
                                append("Скорость ветра: 💨 ${weather.wind.speed} м/с\n")
                                append("Облачность: ☁️ ${weather.clouds.all}%\n")
                                append("Влажность: 💧 ${weather.main.humidity}%\n")
                                weather.main.feels_like?.let {
                                    append("Ощущается как: 🌡 ${it}°C")
                                }
                            }
                            tvWeather.text = weatherText
                        } ?: run {
                            tvWeather.text = "Ошибка получения данных ❌"
                        }
                    } else {
                        tvWeather.text = "Ошибка! Город не найден ❌"
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    tvWeather.text = "Ошибка сети ❌: ${t.message}"
                }
            })
    }

    private fun saveCity(city: String) {
        val sharedPreferences = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("city", city)
        editor.apply()
    }

    private fun loadCity(etCity: EditText) {
        val sharedPreferences = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)
        val savedCity = sharedPreferences.getString("city", "")
        if (!savedCity.isNullOrEmpty()) {
            etCity.setText(savedCity)
        }
    }

    private fun toggleTheme() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
