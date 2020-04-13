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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.android.gms.tasks.OnSuccessListener;



import org.json.JSONArray;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {


    ViewPager viewPager;
    Adapter adapter;
    List<CardModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private FusedLocationProviderClient client;
    private double lat;
    private double lng;
    private String city;
    private TextView start_text;
    private TextView tv_username;
    private String username;
    private JSONArray opening_hours;
    private boolean must_be_open_today;
    private int today_day_number = 0;
    private ListView listView;
    ArrayList<String> arrayList;

    final static String LOG_TAG_TASK = "Done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        start_text = findViewById(R.id.start_text);
        tv_username = findViewById(R.id.tv_username);
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
        start_text.setText("Loading...");
        requestPermission();

    }


        client.getLastLocation().addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i(LOG_TAG_TASK, "I tried to get your location.");
                if (location != null) {
                    Log.i(LOG_TAG_TASK, "I got your location.");

                    Log.d("Location", location.toString());
                    //    Toast.makeText((HomeActivity.this), location.toString(), Toast.LENGTH_SHORT).show();
                    city = hereLocation(location.getLatitude(), location.getLongitude());
                    Toast.makeText((HomeActivity.this), city, Toast.LENGTH_SHORT).show();
//                    Toast.makeText((HomeActivity.this), API_url, Toast.LENGTH_SHORT).show();
                    lat = location.getLatitude();
                    lng = location.getLongitude();


                    //Only if a location was found, you can get the location_id for the location.
//                    new HomeActivity.AsyncHttpTaskLocation().execute(API_location_url);
                    int PLACE_PICKER_REQUEST = 1;
                        getLocationList();


                } else {
                    Log.d("Location??", "Unknown");
                    start_text.setText("Oops! I don't know where you are.");
                    Toast.makeText((HomeActivity.this), "I don't know your location.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // END OF GET LOCATION

        models = new ArrayList<>();
        adapter = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);

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

            listView = (ListView) findViewById(R.id.listview);

            arrayList = new ArrayList<>();

            arrayList.add("hey");

            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);

            listView.setAdapter(arrayAdapter);
    }

    private boolean doNotifyDataSetChangedOnce = false;

        protected void getCount() {
            if (doNotifyDataSetChangedOnce) {
                doNotifyDataSetChangedOnce = false;
                adapter.notifyDataSetChanged();
                viewPager.setAdapter(adapter);
            }
        }

    public void getLocationList() {

        Log.d(LOG_TAG_TASK, "Get list");

        RequestQueue queue = Volley.newRequestQueue(this);

        String uri = "https://api.eet.nu/locations";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
                // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                            // TODO: Handle response
                            Log.d(LOG_TAG_TASK, "I found all the locations in the eet.nu api.");
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
    public void getRestaurantList(String location_id) {

        Log.d(LOG_TAG_TASK, "Get restaurant list");

        RequestQueue queue = Volley.newRequestQueue(this);

        String uri = "https://api.eet.nu/venues?location_id=" + location_id;

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
                    String id = location.getString("id");
                    getRestaurantList(id);
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

    public void updateRestaurants(JSONObject data) {
        try {
            JSONArray restaurants = (JSONArray) data.get("results");
            JSONObject firstItem = (JSONObject) restaurants.get(0);
            String name = (String) firstItem.get("name");




            if(restaurants != null) {
                Log.i("Must be open today", ""+ must_be_open_today);
                if(must_be_open_today == false){
                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject restaurant = restaurants.optJSONObject(i);
                        Log.d("Restaurant", restaurant.optString("name" + "oooooooooooo"));
                        models.add(new CardModel(R.drawable.dog, restaurant.optString("name"), restaurant.optString("category"), restaurant.optInt("id")));
                        arrayList.add(restaurant.optString("name"));
                    }
                    doNotifyDataSetChangedOnce = true;
                    getCount();
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

                    for (int k = 0; k < restaurants.length(); k++) {
                        Log.i("count","hoi" + k);
                        final JSONObject restaurant = restaurants.optJSONObject(k);
                        Log.i("Restaurant", restaurant.optString("name" + "oooooooooooo"));
                        // get detailed data
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

    public void settings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
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
                "https://api.eet.nu/venues/" + id,
                null,
                new Response.Listener<JSONObject>() {
                    // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                        // TODO: Handle response
//                        Log.i(LOG_TAG_TASK, "I found the data of " + response.optString("name"));
                        opening_hours = response.optJSONArray("opening_hours");
//                        Log.i(LOG_TAG_TASK, "Number of days is " + opening_hours.length());

                        for(int j = 0; j < opening_hours.length(); j++) {
                            JSONObject dayObject = opening_hours.optJSONObject(j);
                            int day_number = dayObject.optInt("day");
                            boolean closed = dayObject.optBoolean("closed");
                            if(day_number == today_day_number){
                                if(closed == false) {
                                    Log.i("hey", "hey " + closed);
                                    models.add(new CardModel(R.drawable.dog, restaurant.optString("name"), restaurant.optString("category"), restaurant.optInt("id")));
                                    arrayList.add(restaurant.optString("name"));
                                } else {
                                    // do not display, but log I guess
//
                                    Log.i("hey", "hey " + closed);
                                }
                            } else {
                                Log.i("Day number match", "no");
                            }

                        }
                        doNotifyDataSetChangedOnce = true;
                        getCount();

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
