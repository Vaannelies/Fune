package nl.hr.annelies.fune;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import static nl.hr.annelies.fune.HomeActivity.LOG_TAG_TASK;

public class DetailActivity extends AppCompatActivity {

    private int id;
    private String name;
    private String category;
    private String telephone;
    private String website_url;
    private String street;
    private String zipcode;
    private String city;
    private String region;
    private String country;
    private int rating;
    private final static String URI_RESTAURANT = "https://api.eet.nu/venues/";
    private TextView tv_name;
    private TextView tv_category;
    private TextView tv_street;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tv_name = findViewById(R.id.name);
        tv_category = findViewById(R.id.category);
        tv_street = findViewById(R.id.street);


        this.id = getIntent().getIntExtra("id", 0);     //get the restaurant id
        fetchData();

    }

    private void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(LOG_TAG_TASK, "hey");
        JsonObjectRequest getRestaurantData = new JsonObjectRequest(
                Request.Method.GET,
                URI_RESTAURANT + this.id,
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
            name = data.optString("name");
            Log.i("THE NAME. IShee.", name);
            category = data.optString("category");
            telephone = data.optString("telephone");
            website_url = data.optString("website_url");
            JSONObject address = (JSONObject) data.get("address");
                street = address.optString("street");
                zipcode = address.optString("zipcode");
                city = address.optString("city");
                region = address.optString("region");
                country =  address.optString("country");


            // Display the information
            tv_name.setText(name);
            tv_category.setText(category);
            tv_street.setText(street);

//            private int rating;
        }  catch (Exception e) {
            e.printStackTrace();
            Log.i("Error", "Error in JSON.");
        }

    }



}




