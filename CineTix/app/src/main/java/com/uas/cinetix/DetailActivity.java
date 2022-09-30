package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.uas.cinetix.Config;
import com.uas.cinetix.R;
import com.uas.cinetix.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity {

    private JSONArray locations = null;
    private JSONArray schedules = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Button bookButton = findViewById(R.id.bookButton);
        final ScrollView scrollView = findViewById(R.id.scrollView);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final NetworkImageView imageView = findViewById(R.id.image);
        final TextView title = findViewById(R.id.title);
        final TextView plot = findViewById(R.id.plot);
        final TextView dateText = findViewById(R.id.dateText);
        final TextView timeText = findViewById(R.id.timeText);
        final TextView dirText = findViewById(R.id.dirText);
        final TextView genreText = findViewById(R.id.genreText);
        final RatingBar ratingBar = findViewById(R.id.ratingbar);

        final RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        final ImageLoader imageLoader = VolleySingleton.getInstance(this).getImageLoader();

        scrollView.setVisibility(View.INVISIBLE);

        int id = getIntent().getIntExtra("id", 1);
        String url = Config.HOST + "/movie.php?id=" + id;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject movieJson = response.getJSONObject("movie");

                            imageView.setImageUrl(movieJson.getString("poster_img"), imageLoader);
                            title.setText(movieJson.getString("title"));

                            JSONObject omdbJson = movieJson.getJSONObject("omdb_data");
                            plot.setText(omdbJson.getString("Plot"));
                            dateText.setText(omdbJson.getString("Released"));
                            timeText.setText(omdbJson.getString("Runtime"));
                            dirText.setText(omdbJson.getString("Director"));
                            genreText.setText(omdbJson.getString("Genre"));

                            locations = response.getJSONArray("locations");
                            schedules = response.getJSONArray("schedules");

                            try {
                                float rating = Float.parseFloat(omdbJson.getString("imdbRating"));
                                ratingBar.setRating(rating / 2);
                            } catch (NumberFormatException ignored) {}

                            scrollView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        queue.add(request);

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locations == null) {
                    return;
                }

                Intent i = new Intent(DetailActivity.this, MovieTimingsActivity.class);
                i.putExtra("locations", locations.toString());
                i.putExtra("schedules", schedules.toString());
                startActivity(i);
            }
        });
    }
}
