package algonquin.cst2335.finalproject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * This Activity shows the brief weather description of cities stored by user in database
 */

public class StoredCities extends AppCompatActivity implements RecyclerClickInterface {

    RecyclerView recyclerView;
    ImageView search;
    EditText editCityName;
    Adapter adapter = null;
    DBHandler dbHandler;
    ArrayList<DBCitiesRecord> DBCities;
    RelativeLayout layout;
    String selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_cities);
        recyclerView = findViewById(R.id.CitiesRecylerView);
        editCityName = findViewById(R.id.editTextTextCityName);
        search = findViewById(R.id.searchIcon);
        layout = findViewById(R.id.storedCity);
        dbHandler = DBHandler.getInstance(StoredCities.this);
        DBCities = dbHandler.readCity();
        if (DBCities.size() <= 0) {
            //snack bar
            Snackbar.make(layout, getString(R.string.no_cities_added), Snackbar.LENGTH_LONG).show();
        } else {
            //alert notification
            alertNotification();
            fetchCityData(DBCities);
        }


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to make the keyboard disappear after button click
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(layout.getWindowToken(), 0);

                String cityName = editCityName.getText().toString().trim().toUpperCase();
                if (cityName.equals("")) {
                    fetchCityData(DBCities);
                    return;
                }
                ArrayList<DBCitiesRecord> result = new ArrayList<>();
                for (DBCitiesRecord city : DBCities) {
                    if (city.getCities().contains(cityName)) {
                        result.add(city);
                    }
                }
                if (result.size() > 0) {
                    fetchCityData(result);
                } else {
                    Toast.makeText(StoredCities.this, getString(R.string.no_cities_available), Toast.LENGTH_SHORT).show();
                }
            }
        });

        editCityName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search.performClick();
                }
                return false;
            }
        });
    }

    /**
     * This method is called when a list of cities data has been received and is ready to be displayed.
     * It sets up the RecyclerView and Adapter to display the data, and also updates the database with
     * the new list of cities.
     *
     * @param citiesDataModals The list of cities data to display.
     */
    private void requestCompleted(ArrayList<CitiesDataModal> citiesDataModals) {
        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(StoredCities.this));

        // Set up the Adapter with the cities data
        adapter = new Adapter(StoredCities.this, citiesDataModals, this);
        recyclerView.setAdapter(adapter);

        // Update the database cities list with the new list of cities
        DBCities.removeAll(DBCities);
        for (CitiesDataModal temp : citiesDataModals)
            DBCities.add(new DBCitiesRecord(temp.getCityName()));
    }


    /**
     * This method fetches the weather data for the list of cities provided in the database, and
     * creates an ArrayList of CitiesDataModal objects to hold the retrieved data.
     *
     * @param DBCities The list of cities for which to fetch weather data.
     */
    private void fetchCityData(ArrayList<DBCitiesRecord> DBCities) {
        // Create an ArrayList to hold the retrieved responded data
        ArrayList<CitiesDataModal> citiesDataModals = new ArrayList<>();

        // Create a new RequestQueue for making API requests
        RequestQueue requestQueue = null;

        // Loop through the list of cities and fetch weather data for each one
        for (int i = 0; i < DBCities.size(); i++) {
            String cityName = DBCities.get(i).getCities();
            String accessKey = "bfac4d2fffab8231729760adadab6da0";
            String url = "http://api.weatherstack.com/current?access_key=" + accessKey + "&query=" + cityName;

            // Create a new RequestQueue if one doesn't already exist
            if (requestQueue == null) requestQueue = Volley.newRequestQueue(StoredCities.this);

            int finalI = i;
            // Make an API request to fetch the weather data for this city
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    // Parse the response JSON and extract the relevant data
                    String localTime = MainActivity.formatDateTime(response.getJSONObject("location").getString("localtime"));
                    String temperature = response.getJSONObject("current").getString("temperature") + " °C";
                    JSONArray temp1 = response.getJSONObject("current").getJSONArray("weather_icons");
                    String weather_icon = temp1.getString(0);

                    // Create a new CitiesDataModal object and add it to the list
                    citiesDataModals.add(new CitiesDataModal(cityName, temperature, localTime, weather_icon));

                    // If we've fetched data for all cities, call the requestCompleted method
                    if (finalI + 1 >= DBCities.size()) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            requestCompleted(citiesDataModals);
                        }, 1000);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }, error -> Toast.makeText(StoredCities.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show());

            // Add the request to the RequestQueue
            requestQueue.add(jsonObjectRequest);
        }
    }

    /**
     * Shows an alert dialog with a message and title, providing information to the user about how to interact with the city list.
     */
    public void alertNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StoredCities.this);

        // Set the message for the alert dialog
        builder.setMessage(getString(R.string.stored_city_message));

        // Set the title for the alert dialog
        builder.setTitle(getString(R.string.alert));

        // Set the dialog to not be cancelable by pressing outside the dialog
        builder.setCancelable(false);

        // Set the positive button to close the dialog
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            dialog.cancel();
        });

        // Create and show the alert dialog
        builder.create().show();
    }

    /**
     * Called when a city item is clicked. Sets the selected city, fetches its weather data, and finishes the activity.
     *
     * @param position the position of the clicked city in the Recyclerview
     */
    @Override
    public void itemClick(int position) {
        // Set the Clicked city
        selectedCity = DBCities.get(position).getCities();
        // Fetch the weather data for the Clicked city
        MainActivity.getInstance().fetchWeather(selectedCity);
        // Finish the activity
        this.finish();
    }

    /**
     * Called when a city item is long-clicked. Removes the clicked city from the database and the list.
     *
     * @param position the position of the long-clicked city in the list
     */

    @Override
    public void itemLongClick(int position) {
        // Remove the city from the database
        dbHandler.removeCity(DBCities.get(position).getCities());
        // Remove the city from the list
        DBCities.remove(position);
    }
}