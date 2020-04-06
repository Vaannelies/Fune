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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView textView = findViewById(R.id.textView);
        textView.setText(getIntent().getStringExtra("name"));

        this.id = getIntent().getIntExtra("id",0);     //get the restaurant id
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
                        Log.d(LOG_TAG_TASK, "I found the data of " + getIntent().getStringExtra("name"));
                        showData(response);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("Error", "Failed to fetch the restaurant JSON data.");
                    }
                });

                queue.add(getRestaurantData);

            }


    private void showData(JSONObject data) {
        try {
            name = (String) data.get("name");
            Log.i("THE NAME. IS.", name);
            category = (String) data.get("category");
            telephone = (String) data.get("telephone");
            website_url = (String) data.get("website_url");
            JSONObject address = (JSONObject) data.get("address");
            street = (String) address.get("street");
            zipcode = (String) address.get("zipcode");
            city = (String) address.get("city");
            region = (String) address.get("region");
            country = (String) address.get("country");


//            private int rating;
        }  catch (Exception e) {
            e.printStackTrace();
            Log.i("Error", "Error in JSON.");
        }

        }


}




