package algonquin.cst2335.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import algonquin.cst2335.finalproject.databinding.ActivityNasamarsBinding;
import algonquin.cst2335.finalproject.databinding.MarsResultBinding;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



/**
 * This class is responsible for displaying the NASA Mars Rover photographs. It allows to search for photos
 * taken on a specific date, view additional information about each photo, and download the photos to their device.
 */
public class nasaMarsActivity extends AppCompatActivity {

    private ActivityNasamarsBinding binding;

    private nasaMarsViewModel marsViewModel;

    private ArrayList<nasaMarsResult> result;

    private ArrayList<nasaMarsResult> favourites;

    private RecyclerView.Adapter adapter;

    private String date;

    private String url;

    private int position;

    boolean isFavList = false;

    nasaMarsDatabase db;

    /**
     * This method is called when the options menu is created, and it inflates the menu to display the help button.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){

            case R.id.Item1:
                AlertDialog.Builder builder = new AlertDialog.Builder(nasaMarsActivity.this);
                builder.setMessage("Enter a date and press the Search Photos button to search for NASA Mars rover photographs taken on that date. ")
                        .setPositiveButton("Photo", (dialog, cl) -> {
                            builder.setMessage("Click an image in the list for additional information and to download it.")
                                    .setPositiveButton(null, null)
                                    .show();
                        })
                        .setTitle("Help")
                        .create().show();
        }
        return true;
    }

    /**
     * This method is called when an options menu item is selected, and it displays the help dialog when the help button is clicked.
     * @param menu
     * @return True if the help dialog was displayed successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     *This method is called when the activity is created, and it initializes the necessary variables and UI components.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Connect to the database
        db = Room.databaseBuilder(getApplicationContext(), nasaMarsDatabase.class, "mars-favs").build();
        nasaMarsResultDAO mrDAO = db.marsResultDAO();

        binding = ActivityNasamarsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


        marsViewModel = new ViewModelProvider(this).get(nasaMarsViewModel.class);
        isFavList = marsViewModel.favouriteList;
        if(isFavList) { favourites = marsViewModel.favourites.getValue(); }
        else { result = marsViewModel.result.getValue(); }

        if (result == null){
            marsViewModel.result.setValue(result = new ArrayList<>());
        }

        EditText editTextDateNumber = binding.editTextDate;
        Button searchButton = binding.searchButton;
        Button favButton = binding.favouriteButton;

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        SharedPreferences prefs = getSharedPreferences("MarsData", Context.MODE_PRIVATE);
        int marsInput = prefs.getInt("marsDate", 0);
        editTextDateNumber.setText(String.valueOf(marsInput));

        RequestQueue queue = Volley.newRequestQueue(this);


        searchButton.setOnClickListener( clk -> {
            isFavList = false;
            result = new ArrayList<>();
            adapter.notifyDataSetChanged();
            date = binding.editTextDate.getText().toString();
            url = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=" + date
                    + "&api_key=L3Uob8dUvNWDm4zR7AD5LVzJK5HWqibZb6cEGX2M";


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray photoArray = response.getJSONArray("photos");


                            for (int i=0; i < photoArray.length()-1; i++){

                                JSONObject currentResult = photoArray.getJSONObject(i);
                                JSONObject currentCamera = currentResult.getJSONObject("camera");
                                JSONObject currentRover = currentResult.getJSONObject("rover");

                                String imgID = currentResult.getString("id");
                                String imgSrc = currentResult.getString("img_src").replace("http://mars.jpl", "https://mars");
                                String camName = currentCamera.getString("name");
                                String roverName = currentRover.getString("name");
                                nasaMarsResult result = new nasaMarsResult(imgID, imgSrc, camName, roverName);


                                ImageRequest imgReq = new ImageRequest(imgSrc, new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        result.setBitmap(bitmap);
                                        nasaMarsActivity.this.result.add(result);
                                        adapter.notifyItemInserted(nasaMarsActivity.this.result.size()-1);
                                    }
                                }, 1024, 1024, ImageView.ScaleType.CENTER, null,
                                        error  -> {
                                        });
                                queue.add(imgReq);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error ->{

                    });
            queue.add(request);

            marsViewModel.result.postValue(result);
            marsViewModel.favouriteList = isFavList;

            Toast.makeText(getApplicationContext(), "Looking for photographs of a date " + date, Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("marsDate", Integer.parseInt(binding.editTextDate.getText().toString()));
            editor.apply();
        });


        favButton.setOnClickListener( clk -> {
            favourites = new ArrayList<>();
            adapter.notifyDataSetChanged();
            isFavList = true;
            Executor thread= Executors.newSingleThreadExecutor();
            thread.execute( () -> {
                favourites.addAll(mrDAO.getAllFavs());
                runOnUiThread( () -> binding.recyclerView.setAdapter(adapter));
            });
            marsViewModel.favourites.postValue(favourites);
            marsViewModel.favouriteList = isFavList;
        });

        binding.recyclerView.setAdapter(adapter = new RecyclerView.Adapter<ResultHolder>() {


            @NonNull
            @Override
            public ResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                MarsResultBinding binding = MarsResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ResultHolder(binding.getRoot());
            }


            @Override
            public void onBindViewHolder(@NonNull ResultHolder holder, int position) {
                if (isFavList) {
                    nasaMarsResult obj = favourites.get(position);

                    String dir = getApplicationContext().getFilesDir().getPath();
                    File imgFile = new File(dir, obj.getImgPath());
                    Bitmap img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    obj.setBitmap(img);
                    holder.roverText.setText(obj.getRoverName());
                    holder.thumbnail.setImageBitmap(img);
                }
                else {
                    nasaMarsResult obj = result.get(position);
                    holder.roverText.setText(obj.getRoverName());
                    holder.thumbnail.setImageBitmap(obj.getBitmap());
                }
            }


            @Override
            public int getItemCount() {
                int itemCount;
                if (isFavList){
                    itemCount = favourites.size();
                }
                else {
                    itemCount = result.size();
                }
                return itemCount;
            }


            @Override
            public int getItemViewType(int position){
                return 0;
            }
        });
    }


    class ResultHolder extends RecyclerView.ViewHolder {

        TextView roverText;

        ImageView thumbnail;
        public ResultHolder(View itemView){
            super(itemView);
            thumbnail = itemView.findViewById(R.id.roverImage);


            itemView.setOnClickListener(clk -> {

                nasaMarsDetailsFragment marsFragment;
                if (isFavList){
                    marsFragment = new nasaMarsDetailsFragment(favourites.get(position), db);
                }
                else {
                    marsFragment = new nasaMarsDetailsFragment(result.get(position),db);
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("")
                        .replace(R.id.marsFragment, marsFragment)
                        .commit();
            });

            roverText = itemView.findViewById(R.id.roverText);
        }
    }
}