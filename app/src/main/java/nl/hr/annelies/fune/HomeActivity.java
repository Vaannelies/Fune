package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;



import org.json.JSONArray;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.view.Menu;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {


    // VIEW PAGER
    ViewPager viewPager;
    AdapterViewPager adapter;
    List<CardModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    // LOCATION
    private FusedLocationProviderClient client;
    private double lat;
    private double lng;
    private String city;

    // URI's
    final static String URI_LOCATION_SPECIFIC = "https://api.eet.nu/venues?location_id=";
    final static String URI_LOCATIONS = "https://api.eet.nu/locations";
    private final static String URI_RESTAURANT = "https://api.eet.nu/venues/";
    protected static String location_id;
    private TextView start_text;
    private TextView tv_username;
    private String username;

    // CHECKING IF OPEN
    private JSONArray opening_hours;
    private boolean must_be_open_today;
    private int today_day_number = 0;

    private Button btn_all_restaurants;

    private GoogleMap mMap;


    final static String LOG_TAG_TASK = "Done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ACTION BAR
        getSupportActionBar().setTitle(R.string.app_name);


        // DEFINE ITEMS
        btn_all_restaurants = findViewById(R.id.btn_list);
        start_text = findViewById(R.id.start_text); // LIKE "LOADING" OR "UNKNOWN LOCATION"
        tv_username = findViewById(R.id.tv_username); // MORE LIKE A RANDOM PERSONAL MESSAGE OPTION

        // SHARED PREFERENCES --> SETTINGS
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        username = sharedPreferences.getString("signature", "");
        tv_username.setText(username);

        must_be_open_today = sharedPreferences.getBoolean("must_be_open_today", false);


        requestPermission();
//
        // GET LOCATION:
        client = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(HomeActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            start_text.setText(R.string.loading);
            requestPermission();
        }

        // GET THE LOCATION
        client.getLastLocation().addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i(LOG_TAG_TASK, "I tried to get your location.");
                if (location != null) {
                    Log.i(LOG_TAG_TASK, "I got your location.");

                    Log.d("Location", location.toString());

                    // EXECUTE 'HERELOCATION' TO FIND THE NAME OF THE CITY YOU'RE IN (BASED ON COORDINATES)
                    city = hereLocation(location.getLatitude(), location.getLongitude());

                    // DISPLAY CITY ON DEVICE
                    Toast.makeText((HomeActivity.this), city, Toast.LENGTH_SHORT).show();


                    // COORDINATES
                    lat = location.getLatitude();
                    lng = location.getLongitude();


                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(HomeActivity.this);

                    //Only if a location was found, you can get the location_id for the location (from the eet.nu API)
                    int PLACE_PICKER_REQUEST = 1;
                        getLocationList();


                } else {
                    Log.d("Location??", "Unknown");
                    start_text.setText(R.string.unknown_location); // TELL THE USER THAT THE LOCATION IS UNKNOWN
                    Toast.makeText((HomeActivity.this), R.string.unknown_location, Toast.LENGTH_SHORT).show();
                }
            }
        });


        // END OF GET LOCATION


        // VIEW PAGER STUFF

        models = new ArrayList<>();
        adapter = new AdapterViewPager(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);

        // CHANGING BACKGROUND COLOR

        Integer[] colors_temp =  {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4),
                getResources().getColor(R.color.color5),
                getResources().getColor(R.color.color6),
                getResources().getColor(R.color.color7),
                getResources().getColor(R.color.color8),
                getResources().getColor(R.color.color9),
                getResources().getColor(R.color.color10),
                getResources().getColor(R.color.color11),
                getResources().getColor(R.color.color12),
                getResources().getColor(R.color.color13),
                getResources().getColor(R.color.color14),
                getResources().getColor(R.color.color15),
                getResources().getColor(R.color.color16)
        };

        colors = colors_temp;

        // CHANGE COLOR IF SCROLLED

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }

                else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
            });

    }


    // MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    // GOOGLE MAPS

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Log.i("Latitude", "" + lat);
        Log.i("Longitude", "" + lng);

        LatLng sydney = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    // END OF GOOGLE MAPS

    // IF ADAPTER CONTENT CHANGES, IT WILL CRASH UNLESS YOU EXECUTE
    //                        doNotifyDataSetChangedOnce = true;
    //                        getCount();

    private boolean doNotifyDataSetChangedOnce = false;

        protected void getCount() {
            if (doNotifyDataSetChangedOnce) {
                doNotifyDataSetChangedOnce = false;
                adapter.notifyDataSetChanged();
                viewPager.setAdapter(adapter);
            }
        }


    // FIND ALL THE AVAILABLE LOCATIONS IN THE EET.NU API

    public void getLocationList() {

        Log.d(LOG_TAG_TASK, "Get list");

        RequestQueue queue = Volley.newRequestQueue(this);

        String uri = URI_LOCATIONS;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
                // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                            // TODO: Handle response
                            Log.d(LOG_TAG_TASK, "I found all the locations in the eet.nu api.");
                            // THEN FIND OUT WHICH OF THESE LOCATIONS IS MY CURRENT LOCATION
                            updateLocations(response);

                        }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                             // TODO: Handle error
                            Log.d("Error", "Failed to fetch the location JSON data.");
                        }
                    });

        queue.add(jsonObjectRequest);

    }

    // FETCH ALL THE RESTAURANTS THAT ARE AT MY LOCATION (IN THE EET.NU API)

    public void getRestaurantList(String location_id) {

        Log.d(LOG_TAG_TASK, "Get restaurant list");

        RequestQueue queue = Volley.newRequestQueue(this);

        String uri = URI_LOCATION_SPECIFIC + location_id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
                // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                            // TODO: Handle response
                            Log.d(LOG_TAG_TASK, "I found all the locations in the eet.nu api.");
                            updateRestaurants(response);
                        }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                             // TODO: Handle error
                            Log.d("Error", "Failed to fetch the restaurant JSON data.");
                        }
                    });

        queue.add(jsonObjectRequest);

    }


    // FOR EACH LOCATION IN THE EET.NU API, CHECK IF IT'S THE SAME AS **MY** LOCATION
    public void updateLocations(JSONObject data) {

        try {
            JSONArray locations = (JSONArray) data.get("results");
            JSONObject firstItem = (JSONObject) locations.get(0);
            String name = (String) firstItem.get("name");

            String city = hereLocation(lat,lng);

            for(int i = 0; i < locations.length(); i++) {
                JSONObject location = locations.optJSONObject(i);
                if(location.optString("name").equals(city)) {
                    // If there is a match between your location and a location the list,
                    // fetch the restaurants.
                    location_id = location.getString("id");
                    getRestaurantList(location_id);
                } else {
                    Log.d("Error", "No location match was found in the eet.nu.api. " + location.optString("name") + " != " + lat + ", " + lng);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "Error in JSON.");
        }

        // Added try and catch clauses to handle (unexpected) data, that's not called 'results'.
    }

    // FETCH RESTAUARANTS AT CURRENT LOCATION (IN EET.NU API)
    public void updateRestaurants(JSONObject data) {
        try {
            JSONArray restaurants = (JSONArray) data.get("results");

            // CHECK IF IT HAS TO DISPLAY **ALL** RESTAURANTS OR JUST THE ONES THAT ARE **OPEN TODAY**

            if(restaurants != null) {
                Log.i("Must be open today", ""+ must_be_open_today);

                // ALL RESTAURANTS
                if(must_be_open_today == false){
                    // ONLY SHOW 6 RESTAURANTS IN HOMEACTIVITY
                    for (int i = 0; i <6; i++) {
                        JSONObject restaurant = restaurants.optJSONObject(i);
                        // DISPLAY RESTAURANT IN VIEW PAGER
                        models.add(new CardModel(R.drawable.dog, restaurant.optString("name"), restaurant.optString("category"), restaurant.optInt("id")));
                    }
                    doNotifyDataSetChangedOnce = true;
                    getCount();

                    //WHEN DONE, DISPLAY THE 'ALL RESTAURANTS' BUTTON
                    btn_all_restaurants.setVisibility(View.VISIBLE);

                // ONLY THE RESTAURANTS THAT ARE OPEN TODAY
                } else { //Check which restaurants are open right now
                    Log.i(LOG_TAG_TASK, "Restaurants must be open.");

                    // First, get todays date (day format)
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    Log.i("Day of the week is", "" + day);

                    switch (day) {
                        case Calendar.SUNDAY:
                            today_day_number = 6;
                            break;
                        case Calendar.MONDAY:
                            today_day_number = 0;
                            break;
                        case Calendar.TUESDAY:
                            today_day_number = 1;
                            break;
                        case Calendar.WEDNESDAY:
                            today_day_number = 2;
                            break;
                        case Calendar.THURSDAY:
                            today_day_number = 3;
                            break;
                        case Calendar.FRIDAY:
                            today_day_number = 4;
                            break;
                        case Calendar.SATURDAY:
                            today_day_number = 5;
                            break;
                    }
                    Log.i("today_day_number is", "" + today_day_number);
                    Log.i("number of restaurants", ""+ restaurants.length());


                    // ONLY SHOW 6 RESTAURANTS IN HOME ACTIVITY
                    for (int k = 0; k < 6; k++) {
                        final JSONObject restaurant = restaurants.optJSONObject(k);
                        // get detailed data, find out if it's open today, and then add to the view pager
                        int id = restaurant.optInt("id");
                        fetchDetailData(id, restaurant);
                    }
                    doNotifyDataSetChangedOnce = true;
                    getCount();
                }

            } else {
                Log.d("Error", "No restaurants found.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "Error in JSON.");
        }
        // Added try and catch clauses to handle (unexpected) data, that's not called 'results'.
    }


    // GETTING THE NAME OF THE CITY BASED ON YOUR COORDINATES
    private String hereLocation(double lat, double lng) {
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 10);
            if(addresses.size() > 0) {
                for(Address adr: addresses) {
                    if(adr.getLocality() != null && adr.getLocality().length() > 0) {
                        cityName = adr.getLocality();
                        Log.i(LOG_TAG_TASK, "I found the name of your city.");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;


    }

    public void restaurantList(View view) {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("location_id", location_id);
        startActivity(i);
    }
//
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    private void fetchDetailData(int id, final JSONObject restaurant) {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(LOG_TAG_TASK, "hey");
        JsonObjectRequest getRestaurantData = new JsonObjectRequest(
                Request.Method.GET,
                URI_RESTAURANT + id,
                null,
                new Response.Listener<JSONObject>() {
                    // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                        // TODO: Handle response

                        // FOR EACH RESTAURANT, CHECK IF THERE IS INFO ABOUT THE OPENING HOURS FOR EACH DAY OF THE WEEK
                        opening_hours = response.optJSONArray("opening_hours");

                        // FOR EACH DAY OF THE WEEK, CHECK IF THE RESTAURANT IS OPEN OR NOT
                        for(int j = 0; j < opening_hours.length(); j++) {
                            JSONObject dayObject = opening_hours.optJSONObject(j);
                            int day_number = dayObject.optInt("day");
                            boolean closed = dayObject.optBoolean("closed");
                            if(day_number == today_day_number){
                                // IF RESTAURANT IS OPEN TODAY, ADD IT TO THE VIEW PAGER
                                if(closed == false) {
                                    models.add(new CardModel(R.drawable.dog, restaurant.optString("name"), restaurant.optString("category"), restaurant.optInt("id")));
                                }
                            }
                        }
                        doNotifyDataSetChangedOnce = true;
                        getCount();

                        //WHEN DONE, DISPLAY THE 'ALL RESTAURANTS' BUTTON
                        btn_all_restaurants.setVisibility(View.VISIBLE);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.i("Error", "Failed to fetch the restaurant JSON data.");
            }
        });

        queue.add(getRestaurantData);

    }
}
