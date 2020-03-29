package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {

    // API STUFF

    String API_url;
    String API_location_url;
    String API_key;


    // END OF API STUFF

    ViewPager viewPager;
    Adapter adapter;
    List<CardModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private JSONArray restaurants;
    private JSONArray locations;
    private FusedLocationProviderClient client;
    private double lat;
    private double lng;
    private String city;
    private String location_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission();

        // GET LOCATION:
        client = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(HomeActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i("Done", "I tried to get your location.");
                if (location != null) {
                    Log.i("Done", "I got your location.");
//                    TextView textView = findViewById(R.id.textView2);
////                    textView.setText(location.toString());
                    Log.d("Location", location.toString());
                    //    Toast.makeText((HomeActivity.this), location.toString(), Toast.LENGTH_SHORT).show();
                    city = hereLocation(location.getLatitude(), location.getLongitude());
                    Toast.makeText((HomeActivity.this), city, Toast.LENGTH_SHORT).show();
//                    Toast.makeText((HomeActivity.this), API_url, Toast.LENGTH_SHORT).show();
                    lat = location.getLatitude();
                    lng = location.getLongitude();


                    //Only if a location was found, you can get the location_id for the location.
                    new HomeActivity.AsyncHttpTaskLocation().execute(API_location_url);


                } else {
                    Log.d("Location??", "Unknown");

                    Toast.makeText((HomeActivity.this), "I don't know your location.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // END OF GET LOCATION

        // API STUFF

        API_location_url =  "https://api.eet.nu/locations";






        // END OF API STUFF

        models = new ArrayList<>();
        models.add(new CardModel(R.drawable.dog, "Dog", "this is a cool dog"));
        models.add(new CardModel(R.drawable.dog, "Dog 2", "this is also a cool dog"));

        adapter = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);

        Integer[] colors_temp =  {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4)
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
                        Log.i("Done", "I found the name of your city.");
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


    // API STUFF

    public class AsyncHttpTask extends AsyncTask<String, Void, String> {

        private boolean doNotifyDataSetChangedOnce = false;

        protected void getCount() {
            if (doNotifyDataSetChangedOnce) {
                doNotifyDataSetChangedOnce = false;
                adapter.notifyDataSetChanged();
                viewPager.setAdapter(adapter);
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                String response = streamToString(urlConnection.getInputStream());
                parseResult(response);
                for (int i=0; i<restaurants.length(); i++)
                {
                    JSONObject restaurant = restaurants.optJSONObject(i);

                    String name = restaurant.optString("name");
                    String desc = restaurant.optString("category");
                    Toast.makeText(HomeActivity.this, name, Toast.LENGTH_SHORT).show();
                    models.add(new CardModel(R.drawable.cooldog, name, desc));
                    doNotifyDataSetChangedOnce = true;
                    getCount();
                    Log.d("poep", name);
                }

                Log.i("Done", "I found all restaurants in the city and turned them into CardModels.");
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    public class AsyncHttpTaskLocation extends AsyncTask<String, Void, String> {

        private boolean doNotifyDataSetChangedOnce = false;

        protected void getCount() {
            if (doNotifyDataSetChangedOnce) {
                doNotifyDataSetChangedOnce = false;
                adapter.notifyDataSetChanged();
                viewPager.setAdapter(adapter);
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                String response = streamToStringLocation(urlConnection.getInputStream());
                parseResultLocation(response);
                for (int i=0; i<locations.length(); i++)
                {
                    JSONObject location = locations.optJSONObject(i);

                    String name = location.optString("name");
                    String id = location.optString("id");
//                    (Toast.makeText(this, name, Toast.LENGTH_SHORT)).show();
//                    Log.i("Location namessssss", city);
//
//                    if (name.equals(city)) {
//                        location_id = id;
//                        Toast.makeText(HomeActivity.this, "dit is het location_id: " + location_id, Toast.LENGTH_SHORT).show();
//                    } else {
//                        location_id = "Unknown";
//                    }



                    Log.d("id", id);
                }
                Log.i("Done", "I found all locations in the eet.nu api.");
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String data;
        String result = "";

        while ((data = bufferedReader.readLine()) != null) {
            result += data;
        }
        if (null != stream) {
            stream.close();
        }

      Log.i("Done", "I gathered all the restaurants once again so you can use them in parseResult as the data you want to turn into a string..");
        return result;
    }

    private void parseResult(String result) {
        JSONObject response = null;
        try {
            response = new JSONObject(result);
            JSONArray restaurants = response.optJSONArray("results");

            for (int i = 0; i < restaurants.length(); i++) {
                JSONObject restaurant = restaurants.optJSONObject(i);
                String name = restaurant.optString("name");
                String desc = restaurant.optString("category");
                String image = restaurant.optString("images","original");
                Log.i("Restaurant names", name);
//                (Toast.makeText(this, name, Toast.LENGTH_SHORT)).show();

//                models.add(new CardModel(R.drawable.dog, "Dog 3", "this is alsooo a cool dog"));
                models.add(new CardModel(R.drawable.dog, name, desc));
            }





        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("Done", "I turned the list of restaurants into separate restaurants and they are strings.");

        restaurants = response.optJSONArray("results");
    }


    String streamToStringLocation(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String data;
        String result = "";

        while ((data = bufferedReader.readLine()) != null) {
            result += data;
        }
        if (null != stream) {
            stream.close();
        }
        Log.i("Done", "I gathered all the locations once again so you can use them in parseResultLocation as the data you want to turn into a string..");
        return result;
    }

    private String parseResultLocation(String result) {
        JSONObject response = null;
        try {
            response = new JSONObject(result);
            JSONArray locations = response.optJSONArray("results");

            for (int i = 0; i < locations.length(); i++) {
                JSONObject location = locations.optJSONObject(i);
                String name = location.optString("name");
                String id = location.optString("id");
//                Log.i("Location nammmmmes", name);
//                Log.i("Location names", this.city + "hey");

                if (name.equals(this.city)) {
                    Log.i("Location names", this.city +" ========= "+name);
                    location_id = id;
                    API_url =  "https://api.eet.nu/venues?location_id=" + location_id;
                    new HomeActivity.AsyncHttpTask().execute(API_url);
                } else {
                    Toast.makeText(this, "No available data for your location.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        locations = response.optJSONArray("results");

        Log.i("Done", "I turned the list of locations into separate locations and they are strings.");

        return (location_id);
    }


    // END OF API STUFF

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }
}
