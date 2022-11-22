package com.winsley.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.winsley.weatherapp.MainActivityContent as MainActivityContent1


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainActivityContent1()
                }
            }
        }
    }
}



var windspeed = ""
var windDirection = ""
var weather = ""



@Composable
fun Background(image: Int, description: String) {
    Image(
        painter = painterResource(image),
        contentDescription = description,
        modifier = Modifier
            .fillMaxHeight()
            .width(130.dp),
        contentScale = ContentScale.Crop,
        alpha = 0.3F
    )
}

@Composable
fun EnterLocation(location: MutableState<String>, changed: (String) -> Unit) {
    TextField(
        value = location.value,
        label = { Text("Please enter your zip code")},
        onValueChange = changed,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun GoButton(clicked: () -> Unit){
    Button(onClick = clicked) {
        Text(text = "Get weather")
    }
}
fun getLocation(context: Context, temp: MutableState<String>, location: MutableState<String>) {
    //This function takes the location string and sends it out to get lat/long
    var latitude = ""
    var longitude = ""


        val locUrl =
            "https://geokeo.com/geocode/v1/search.php?q=${location.value}&api=4f589b118df8b0f07ca23e8e2ebfb34d"
        val queue = Volley.newRequestQueue(context)
        val url: String = locUrl
        val stringReq = StringRequest(Request.Method.GET, url, { response ->

            val obj = JSONObject(response)
            val arr = obj.getJSONArray("results")
            val obj2 = arr.getJSONObject(0)
            val obj3 = obj2.getJSONObject("geometry")
            val obj4 = obj3.getJSONObject("location")

            latitude = obj4.getString("lat")
            longitude = obj4.getString("lng")
            GetWeather(context, latitude, longitude, temp)

        },
            { })
        queue.add(stringReq)

}

fun GetWeather(context: Context, latitude: String, longitude: String, temp: MutableState<String>){
    //This function calls the weather api to get the weather data

    var weatherCode = -1
    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true&temperature_unit=fahrenheit&windspeed_unit=mph&precipitation_unit=inch"
    val queue = Volley.newRequestQueue(context)
    val url: String = weatherUrl
    val stringReq = StringRequest(Request.Method.GET, url, {
        response -> Log.e("lat", response.toString())

        val obj = JSONObject(response)

        val obj2 = obj.getJSONObject("current_weather")
        Log.e("lat obj2", obj2.toString())

        try {
            temp.value = obj2.getString("temperature")
            windspeed = obj2.getString("windspeed")
            windDirection = obj2.getString("winddirection")
            weatherCode = obj2.getInt("weathercode")
            when(weatherCode) {
                0 -> weather = "Sunny"
                1 -> weather = "Mainly Clear"
                2 -> weather = "Partly Cloudy"
                3 -> weather = "Overcast"
                45 -> weather = "Fog"
                48 -> weather = "Depositing Rime Fog"
                51 -> weather = "Light Drizzle"
                53 -> weather = "Moderate Drizzle"
                55 -> weather = "Dense Drizzle"
                56 -> weather = "Light Freezing Drizzle"
                57 -> weather = "Heavy Freezing Drizzle"
                61 -> weather = "Slight Rain"
                63 -> weather = "Moderate Rain"
                65 -> weather = "Heavy Rain"
                66 -> weather = "Light Freezing Rain"
                67 -> weather = "Heavy Freezing Rain"
                71 -> weather = "Light Snow"
                73 -> weather = "Moderate Snow"
                75 -> weather = "Heavy Snow"
                77 -> weather = "Snow Grains"
                80 -> weather = "Light Rain Showers"
                81 -> weather = "Moderate Rain Showers"
                82 -> weather = "Heavy Rain Showers"
                85 -> weather = "Light Snow Showers"
                86 -> weather = "Heavy Snow Showers"
                95 -> weather = "Thunderstorm"
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    },
        { })
    queue.add(stringReq)
}

@Composable
fun WeatherFrame(temp: MutableState<String>) {
    Column(modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 160.dp)
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Top) {
        if (temp.value != "") {
            Text(text = "The temp is ${temp.value} degrees.")
            Text(text = "The windspeed is $windspeed mph.")
            Text(text = "The wind direction is " + windDirection + " degrees.")
            Text(text = weather)
        }
    }
}

@Composable
fun MainActivityContent(){
    val context = LocalContext.current
    val temp = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }

    Background(image = R.drawable.background1, "Landscape")
    WeatherFrame(temp)
    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        , verticalArrangement = Arrangement.Center) {
        EnterLocation(location = location){location.value = it}
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            GoButton {
                if (location.value != "") {
                    getLocation(context, temp, location)
                }

            }
        }
    }
    
}