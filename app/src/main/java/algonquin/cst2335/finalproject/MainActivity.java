package algonquin.cst2335.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather.R;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button addCity, showCities, changeLanguage;
    TextView windSpeed, precip, pressure, tempDisplay, dateTimeDisplay, cityName, weatherDesc;
    private EditText editCityName;
    ImageView search, weatherIco;
    private String searchedCity, language;
    private DBHandler dbHandler;
    private RelativeLayout layout;
    private static MainActivity instance = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addCity = findViewById(R.id.addCity);
        showCities = findViewById(R.id.showCities);
        changeLanguage = findViewById(R.id.changeLanguage);
        editCityName = findViewById(R.id.editTextTextCityName);
        windSpeed = findViewById(R.id.windSpeed);
        precip = findViewById(R.id.precip);
        pressure = findViewById(R.id.pressure);
        tempDisplay = findViewById(R.id.tempDisplay);
        dateTimeDisplay = findViewById(R.id.dateTimeDisplay);
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.searchIcon);
        weatherDesc = findViewById(R.id.weatherDescription);
        weatherIco = findViewById(R.id.weatherIcon);
        layout = findViewById(R.id.wrapper);
        dbHandler = DBHandler.getInstance(MainActivity.this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        instance = this;
//        language = "en";
//        language = getPref();

        search.setOnClickListener(view -> {
            //to make the keyboard disappear after button click
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(layout.getWindowToken(), 0);

            searchedCity = editCityName.getText().toString().trim().toUpperCase();
            if (!searchedCity.isEmpty()) {
                if (checkCityName(searchedCity)) {
                    fetchWeather(searchedCity);
                    editCityName.setText("");
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.wrong_city_name), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.enter_city_first), Toast.LENGTH_SHORT).show();
            }
        });

        editCityName.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                search.performClick();
            }
            return false;
        });

        addCity.setOnClickListener(view -> {
            String city = String.valueOf(cityName.getText());
            // Create a snack bar
            Snackbar.make(layout, getString(R.string.confirmation) + city + " ? ", Snackbar.LENGTH_LONG).setAction(getString(R.string.add),
                    view1 -> {
                        if (checkCityName(city)) {
                            if (dbHandler.addCity(city)) {
                                Toast.makeText(MainActivity.this, getString(R.string.city_added), Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(MainActivity.this, getString(R.string.city_already_added), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.please_enter_city), Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });

        showCities.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), StoredCities.class);
            startActivity(intent);
        });

        changeLanguage.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("PreviousWeatherData", MODE_PRIVATE);

            String language = sharedPreferences.getString("My_language", "en");
            language = language.equalsIgnoreCase("en") ? "hi" : "en";

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("My_language", language);
            editor.apply();
            setChangeLanguage(language);
        });
        getPref();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private void setChangeLanguage(String language) {
        //to change the language
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = MainActivity.instance.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        //updating views
        finish();
        startActivity(getIntent());
    }

    /**
     * This method retrieves the city name and language saved in the shared preferences and updates the UI by calling the fetchWeather() method.
     */
    private void getPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("PreviousWeatherData", MODE_PRIVATE);
        String cityName = sharedPreferences.getString("newCity", "N/A");
        fetchWeather(cityName);
    }

    /**
     * This method saves the searched city name and language in the shared preferences.
     *
     * @param searchedCity The city name that is searched.
     */
    private void setPref(String searchedCity) {
        SharedPreferences sharedPreferences = getSharedPreferences("PreviousWeatherData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("newCity", searchedCity);
        editor.apply();
    }

    /**
     * This method formats the date-time string from the API response and returns it in the desired format.
     *
     * @param localtime The date-time string from the API response.
     * @return The formatted date-time string.
     */
    public static String formatDateTime(String localtime) {
        final String OLD_FORMAT = "yyyy-MM-dd hh:mm";
        final String NEW_FORMAT = "hh:mm MMM, d";

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OLD_FORMAT);
            Date date = simpleDateFormat.parse(localtime);
            simpleDateFormat.applyPattern(NEW_FORMAT);
            return simpleDateFormat.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the input city name is valid by using Geocoder class to get a list of matching addresses.
     *
     * @param city the city name to be checked
     * @return true if the city name is valid and has at least one matching address, false otherwise
     */
    private Boolean checkCityName(String city) {
        if (city.equalsIgnoreCase("City name")) return false;
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(city, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * This method fetches weather data from the WeatherStack API for a specified city.
     *
     * @param city the name of the city for which to fetch weather data.
     */
    public void fetchWeather(String city) {
        String accessKey = "6662d2d156d8912ac99877fe033a4418\n";
        String url = "http://api.weatherstack.com/current?access_key=" + accessKey + "&query=" + city;
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Make an API request to fetch the weather data for this city
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Parse the response JSON and extract the relevant data
                    String localTime = formatDateTime(response.getJSONObject("location").getString("localtime"));
                    String temperature = response.getJSONObject("current").getString("temperature");
                    String wind = response.getJSONObject("current").getString("wind_speed");
                    String press = response.getJSONObject("current").getString("pressure");
                    String preci = response.getJSONObject("current").getString("precip");

                    //Putting icon on weatherIcon
                    JSONArray temp1 = response.getJSONObject("current").getJSONArray("weather_icons");
                    JSONArray temp2 = response.getJSONObject("current").getJSONArray("weather_descriptions");
                    String weather_icon = temp1.getString(0);
                    String weather_descrip = temp2.getString(0);

                    cityName.setText(city);
                    tempDisplay.setText(temperature + " \u2103");
                    dateTimeDisplay.setText(localTime);
                    windSpeed.setText(getString(R.string.wind_speed) + wind + " kmph");
                    pressure.setText(getString(R.string.pressure) + press + " mb");
                    precip.setText(getString(R.string.precip) + preci + " mm");
                    Picasso.get().load(weather_icon).into(weatherIco);
                    weatherDesc.setText(weather_descrip);

                    setPref(city);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, error -> Toast.makeText(MainActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show());
        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Overrides the default behavior of the back button press, showing an alert dialog to confirm if the user wants to exit the application.
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.exit));
        builder.setTitle(getString(R.string.alert));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            setPref(cityName.getText().toString());
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.cancel();
        });
        builder.create().show();
    }
}