package com.uas.cinetix.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.uas.cinetix.Config;
import com.uas.cinetix.DetailActivity;
import com.uas.cinetix.R;
import com.uas.cinetix.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Movie {
    int id;
    String title;
    String posterImg;
    String genres;
}

public class MoviesFragment extends Fragment {

    ArrayList<Movie> movieArrayList;

    Context context;
    ImageLoader imageLoader;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);

        final ListView lv = root.findViewById(R.id.movieList);
        final ProgressBar progressBar = root.findViewById(R.id.progressBar);

        context = container.getContext();

        movieArrayList = new ArrayList<>();

        final RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        String url = Config.HOST + '/';

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray moviesJson = response.getJSONArray("movies");

                            for (int i = 0; i < moviesJson.length(); i++) {
                                JSONObject movieJson = moviesJson.getJSONObject(i);
                                Movie movie = new Movie();
                                movie.id = movieJson.getInt("id");
                                movie.title = movieJson.getString("title");
                                movie.posterImg = movieJson.getString("poster_img");
                                movie.genres = movieJson.getJSONObject("omdb_data").getString("Genre");
                                movieArrayList.add(movie);
                            }

                            progressBar.setVisibility(View.GONE);

                            lv.setAdapter(new MyAdapter());
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movieArrayList.get(position);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("id", movie.id);
                startActivity(i);
            }
        });

        return root;
    }

    private class MyAdapter extends ArrayAdapter<Movie> {
        MyAdapter() {
            super(context, R.layout.movie_row, movieArrayList);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = inflater.inflate(R.layout.movie_row, null);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Movie movie = movieArrayList.get(position);

            holder.movieName.setText(movie.title);
            holder.movieGenres.setText(movie.genres);
            holder.poster.setImageUrl(movie.posterImg, imageLoader);

            row.setBackgroundResource(R.drawable.list_design);

            return row;
        }
    }

    private class ViewHolder {
        NetworkImageView poster;
        TextView movieName;
        TextView movieGenres;

        ViewHolder(View v) {
            poster = v.findViewById(R.id.poster);
            movieName = v.findViewById(R.id.movieTitle);
            movieGenres = v.findViewById(R.id.movieGenres);
        }
    }
}
