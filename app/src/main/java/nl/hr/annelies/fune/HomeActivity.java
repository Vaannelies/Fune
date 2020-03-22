package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {

    // API STUFF

    String API_url;
    String API_key;

    // END OF API STUFF

    ViewPager viewPager;
    Adapter adapter;
    List<CardModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private JSONArray restaurants;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission();

        // GET LOCATION:
        client = LocationServices.getFusedLocationProviderClient(this);
        Button button = findViewById(R.id.btnChoose);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
//                    TextView textView = findViewById(R.id.textView2);
////                    textView.setText(location.toString());
                            Log.d("Location", location.toString());
                            Toast.makeText((HomeActivity.this), location.toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Location", "Unknown");
                            Toast.makeText((HomeActivity.this), "I don't know your location.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        // END OF GET LOCATION

        // API STUFF


        API_url =  "https://api.eet.nu/venues?location_id=193&tags=has-photos";

        new HomeActivity.AsyncHttpTask().execute(API_url);

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
//                    (Toast.makeText(this, name, Toast.LENGTH_SHORT)).show();

                    models.add(new CardModel(R.drawable.dog, name, "this is alsooo a cool dog"));

                    Log.d("poep", name);
                }
                doNotifyDataSetChangedOnce = true;
                getCount();
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
//                models.add(new CardModel(, name, desc));
            }





        } catch (Exception e) {
            e.printStackTrace();
        }

        restaurants = response.optJSONArray("results");
    }


    // END OF API STUFF

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }
}
