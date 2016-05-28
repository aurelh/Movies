package com.example.gabriel.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainActivityFragment extends Fragment {

        private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

        private ImageAdapter mImagesAdapter;

        public GetPosters getPosters;

        public String mExtraText;

        public MainActivityFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onStart(){
            super.onStart();
            getPosters = new GetPosters();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String order = sharedPreferences.getString(getString(R.string.order),
                    getString(R.string.popular));
            getPosters.execute(order);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.menu_main, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mImagesAdapter = new ImageAdapter(rootView.findViewById(R.id.gridview).getContext());
            GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
            gridview.setAdapter(mImagesAdapter);

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    try{
                        getDetails(position);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getContext(), DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, mExtraText);
                    startActivity(intent);
                }
            });

            return rootView;
        }

        public void getDetails(int position) throws JSONException{
            JSONObject forecastJson = new JSONObject(getPosters.mForecastJsonStr).getJSONArray("results")
                    .getJSONObject(position);
            mExtraText = "Title: "+forecastJson.getString("title")+"\n"
                        +"Original Title: "+forecastJson.getString("original_title")+"\n"
                        +"Original Language: "+forecastJson.getString("original_language")+"\n"
                        +"Released: "+forecastJson.getString("release_date")+"\n"
                        +"Vote Average: "+forecastJson.getString("vote_average")+"\n"
                        +"Overview: "+forecastJson.getString("overview")+"\n";

        }

        public class ImageAdapter extends ArrayAdapter {
            private Context context;
            private LayoutInflater inflater;

            public ImageAdapter(Context context) {
                super(context, R.layout.gridview_item_image);

                this.context = context;

                inflater = LayoutInflater.from(context);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    convertView = inflater.inflate(R.layout.gridview_item_image, parent, false);
                }

                Picasso
                        .with(context)
                        .load(getItem(position).toString())
                        .fit() // will explain later
                        .into((ImageView) convertView);

                return convertView;
            }
        }

        public class GetPosters extends AsyncTask<String, Void, String> {

            private final String LOG_TAG = GetPosters.class.getSimpleName();
            public String mForecastJsonStr;

            private void getImages()
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String OWM_RESULTS = "results";
                final String OWM_PATH = "poster_path";
                final String BASE_URL = "http://image.tmdb.org/t/p/w185/";

                JSONObject forecastJson = new JSONObject(mForecastJsonStr);
                JSONArray moviesArray = forecastJson.getJSONArray(OWM_RESULTS);

                for(int i = 0; i < moviesArray.length(); i++) {
                    Uri uri = Uri.parse(BASE_URL).buildUpon()
                            .appendEncodedPath(moviesArray.getJSONObject(i).getString(OWM_PATH))
                            .build();

                    // Get the JSON object representing the day
                    mImagesAdapter.add(uri.toString());
                }

            }

            @Override
            protected String doInBackground(String... param) {

                if (param.length == 0){
                    return null;
                }
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;


                try {
                    // Construct the URL for the OpenWeatherMap query

                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie";
                    final String API_KEY_REQUEST = "api_key";

                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendEncodedPath(param[0])
                            .appendQueryParameter(API_KEY_REQUEST, BuildConfig.API_KEY)
                            .build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    Log.v(LOG_TAG, inputStream.toString());
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    mForecastJsonStr = buffer.toString();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                return mForecastJsonStr;
            }


            protected void onPostExecute(String mForecastJsonStr){
                try {
                    getImages();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    }
}
