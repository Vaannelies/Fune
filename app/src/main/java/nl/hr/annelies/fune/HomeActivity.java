package nl.hr.annelies.fune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // API STUFF


        API_url =  "https://api.eet.nu/locations?type=Rotterdam";

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
                Log.d("Restaurant names", name);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // END OF API STUFF
}
