package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static nl.hr.annelies.fune.HomeActivity.LOG_TAG_TASK;








public class ListActivity extends AppCompatActivity {

    private final static String URI_RESTAURANT = "https://api.eet.nu/venues?location_id=";
    private int restaurant_id;
    private String name;
    private String category;
    private ListView listView;
    private JSONArray opening_hours;
    private boolean must_be_open_today;
    private int today_day_number = 0;

    ArrayList<ListItemModel> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Log.i("location id", getIntent().getStringExtra("location_id"));

        getSupportActionBar().setTitle("All restaurants");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        must_be_open_today = sharedPreferences.getBoolean("must_be_open_today", false);

        listView = findViewById(R.id.listview);

        arrayList = new ArrayList<>();

        Log.i(LOG_TAG_TASK, "Created an array");



        fetchData();


    }

    private void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(LOG_TAG_TASK, "hey");
        JsonObjectRequest getRestaurantData = new JsonObjectRequest(
                Request.Method.GET,
                URI_RESTAURANT + getIntent().getStringExtra("location_id"),
                null,
                new Response.Listener<JSONObject>() {
                    // Additions to the Response listener:

                    @Override
                    public void onResponse(JSONObject response) {
                        // TODO: Handle response
                        Log.i(LOG_TAG_TASK, "I found the data of " + getIntent().getStringExtra("name"));
                        showData(response);

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
//
//    private void showData(JSONArray restaurants) {
//        try {
//
//
//
//            for(int i = 0; i < restaurants.length(); i++) {
//                JSONObject restaurant = restaurants.optJSONObject(i);
//                restaurant_id = restaurant.optInt("id");
//                name = restaurant.optString("name");
//                category = restaurant.optString("category");
////                Log.i("THE NAME. IS.", name);
//
//
//
//                arrayList.add(new ListItemModel(restaurant_id, name, category));
//
//                // Set an item click listener for ListView
//
//            }
//
//
//
//
//            // Display the information
////            tv_name.setText(name);
////            tv_category.setText(category);
////            tv_street.setText(street);
//
//
////            private int rating;
//        }  catch (Exception e) {
//            e.printStackTrace();
//            Log.i("Error", "Error in JSON.");
//        }
//
//
////
//        AdapterListView adapter = new AdapterListView(this, arrayList);
////
//        listView.setAdapter(adapter);
//
//    }



    private void showData(JSONObject data) {
        try {

            JSONArray restaurants = data.optJSONArray("results");


            // Set an item click listener for ListView

            if (restaurants != null) {
                Log.i("Must be open today", "" + must_be_open_today);
                if (must_be_open_today == false) {
                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject restaurant = restaurants.optJSONObject(i);
                        Log.d("Restaurant", restaurant.optString("name" + "oooooooooooo"));
                        restaurant_id = restaurant.optInt("id");
                        name = restaurant.optString("name");
                        category = restaurant.optString("category");

                        arrayList.add(new ListItemModel(restaurant_id, name, category));

                        AdapterListView adapter = new AdapterListView(this, arrayList);
////
                        listView.setAdapter(adapter);
                    }


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
                    Log.i("number of restaurants", "" + restaurants.length());

                    for (int k = 0; k < restaurants.length(); k++) {
                        Log.i("count", "hoi" + k);
                        final JSONObject restaurant = restaurants.optJSONObject(k);
                        Log.i("Restaurant", restaurant.optString("name" + "oooooooooooo"));
                        // get detailed data
                        int id = restaurant.optInt("id");
                        fetchDetailData(id, restaurant);

                    }
//                    arrayList.add(new ListItemModel(restaurant_id, name, category));
//
                }

            } else {
                Log.d("Error", "No restaurants found.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "Error in JSON.");
        }


//        AdapterListView adapter = new AdapterListView(this, arrayList);
////
//        listView.setAdapter(adapter);
    }


    // Display the information
//            tv_name.setText(name);
//            tv_category.setText(category);
//            tv_street.setText(street);


    //




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
                                    restaurant_id = restaurant.optInt("id");
                                    name = restaurant.optString("name");
                                    category = restaurant.optString("category");
                                    Log.i("hey", "hey " + closed);
                                    arrayList.add(new ListItemModel(restaurant_id, name, category));

                                    AdapterListView adapter = new AdapterListView(ListActivity.this, arrayList);
//
                                    listView.setAdapter(adapter);
                                } else {
                                    // do not display, but log I guess
//
                                    Log.i("hey", "hey " + closed);
                                }
                            } else {
                                Log.i("Day number match", "no");
                            }

                        }


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
