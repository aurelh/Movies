package com.example.gabriel.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailActivityFragment extends Fragment {

        private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

        public String mForecastStr;

        public DetailActivityFragment() {
        }

        public void buildDetails(View rootView) throws JSONException{

            JSONObject forecastJson = new JSONObject(mForecastStr);

            String detail = "Title: " + forecastJson.getString("title") + "\n"
                    + "Release date: " + forecastJson.getString("release_date") + "\n"
                    + "Vote Average: " + forecastJson.getString("vote_average") + "\n";

            String overview = "Plot synopsis: " + forecastJson.getString("overview") + "\n";

            String url = "http://image.tmdb.org/t/p/w185/"+forecastJson.getString("poster_path");


            ((TextView) rootView.findViewById(R.id.detail_text))
                    .setText(detail);

            Picasso
                    .with(rootView.getContext())
                    .load(url)
                    .fit() // will explain later
                    .into((ImageView) rootView.findViewById(R.id.poster));

            ((TextView) rootView.findViewById(R.id.detail_overview))
                    .setText(overview);


        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                try{
                    buildDetails(rootView);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
            return rootView;
        }
    }

}
