package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

import static nl.hr.annelies.fune.HomeActivity.LOG_TAG_TASK;








public class ListActivity extends AppCompatActivity {

    private final static String URI_RESTAURANT = "https://api.eet.nu/venues?location_id=";
    private int restaurant_id;
    private String name;
    private String category;
    private ListView listView;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        fetchData();

        listView = findViewById(R.id.listview);
        arrayList = new ArrayList<>();




    }

    private void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(LOG_TAG_TASK, "hey");
        JsonObjectRequest getRestaurantData = new JsonObjectRequest(
                Request.Method.GET,
                URI_RESTAURANT + HomeActivity.location_id,
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



    private void showData(JSONObject data) {
        try {

            JSONArray restaurants = data.optJSONArray("results");

            for(int i = 0; i < restaurants.length(); i++) {
                JSONObject restaurant = restaurants.optJSONObject(i);
                restaurant_id = restaurant.optInt("id");
                name = restaurant.optString("name");
//                Log.i("THE NAME. IS.", name);

                arrayList.add(""+restaurant_id);
                // Set an item click listener for ListView



            }




            // Display the information
//            tv_name.setText(name);
//            tv_category.setText(category);
//            tv_street.setText(street);


//            private int rating;
        }  catch (Exception e) {
            e.printStackTrace();
            Log.i("Error", "Error in JSON.");
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Go to the DetailActivity

                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("id", selectedItem);
                startActivity(intent);
            }
        });
    }







}
