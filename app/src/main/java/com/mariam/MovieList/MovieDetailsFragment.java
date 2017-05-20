package com.mariam.MovieList;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.shamdroid.popularmovies.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Utilities.Util;
import Utilities.VolleySingleton;
import butterknife.BindView;
import butterknife.ButterKnife;
import data.MovieContract;


public class MovieDetailsFragment extends Fragment {

    @BindView(R.id.txtDetailsOverview)
    TextView textOverview;
    @BindView(R.id.imgDetailsPoster)
    ImageView imagePoster;
    @BindView(R.id.txtDetailsTitle)
    TextView textTitle;
    @BindView(R.id.txtDetailsReleaseDate)
    TextView textReleaseDate;
    @BindView(R.id.txtDetailsRate)
    TextView textRating;


    @BindView(R.id.tgleFavorite)
    ToggleButton toggleFavorite;

    @BindView(R.id.lvDetailsTrailers)
    ListView listTrailers;

    @BindView(R.id.lvDetailsReviews)
    ListView listReviews;
    @BindView(R.id.txtNoConnectionReviews)
    TextView textNoConnectionReviews;
    @BindView(R.id.txtNoConnectionTrailers)
    TextView textNoConnectionTrailers;
    @BindView(R.id.txtDetailsNoReviews)
    TextView textNoReviews;

    Movie movie;

    ProgressDialog progressDialog;
    boolean trailersLoaded = false, reviewsLoaded = false;

    RequestQueue requestQueue;


    public DetailsFragment() {
        // Required public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        ButterKnife.bind(this, view);


        movie = getArguments().getParcelable("movie"); // Get the Movie object passed with the intent


        requestQueue = VolleySingleton.getInstance().getRequestQueue(getActivity());


        // Set the texts
        textTitle.setText(movie.getTitle());
        textReleaseDate.setText(movie.getReleaseDate());
        textOverview.setText(movie.getOverview());

        String rate = movie.getVoteAverage() + getString(R.string.outOf10);
        textRating.setText(rate);

        // Load the posters
        loadImage();

        final ContentResolver contentResolver = getActivity().getContentResolver();


        // Set the favorite toggle button checked if the movies is favorite
        toggleFavorite.setChecked(Util.isFavorite(getActivity(), movie.getId()));


        // Add the movie to favorites or delete it
        toggleFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieContract.MovieEntry.MOVIE_ID, movie.getId());
                    contentValues.put(MovieContract.MovieEntry.TITLE, movie.getTitle());
                    contentValues.put(MovieContract.MovieEntry.OVERVIEW, movie.getOverview());
                    contentValues.put(MovieContract.MovieEntry.RELEASE_DATE, movie.getReleaseDate());
                    contentValues.put(MovieContract.MovieEntry.VOTE_RATE, movie.getVoteAverage());

                    String posterUrl = movie.getPosterURL();

                    String imageFileName = posterUrl.substring(posterUrl.lastIndexOf("/") + 1);

                    Util.saveImageToImagesDir(getActivity(), ((BitmapDrawable) imagePoster.getDrawable()).getBitmap(), imageFileName);


                    contentValues.put(MovieContract.MovieEntry.POSTER_URL, imageFileName);


                    contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
                    Toast.makeText(getActivity(), getString(R.string.addedToFavorites), Toast.LENGTH_SHORT).show();
                } else {
                    contentResolver.delete(MovieContract.MovieEntry.CONTENT_URI, MovieContract.MovieEntry.MOVIE_ID + "=?", new String[]{String.valueOf(movie.getId())});

                    Toast.makeText(getActivity(), getString(R.string.removeFromFavorites), Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (Util.isConnected(getActivity())) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.plesaseWait));
            progressDialog.show();

            loadTrailers();

            loadReviews();

        } else {
            textNoConnectionReviews.setVisibility(View.VISIBLE);
            textNoConnectionTrailers.setVisibility(View.VISIBLE);

        }

        return view;

    }


    private void loadImage() {

        String posterUrl = movie.getPosterURL();

        if (posterUrl.startsWith("http")) { // for online mode
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .placeholder(Util.getDrawable(getActivity(), R.drawable.ic_image_black_48dp))
                    .error(Util.getDrawable(getActivity(), R.drawable.ic_error_black_48dp))
                    .into(imagePoster);
        } else { //for offline mode ( Only Favorite will be shown )


            Bitmap bitmap = Util.loadImageFromImagesDir(getActivity(), posterUrl);
            if (bitmap != null)
                imagePoster.setImageBitmap(bitmap);
            else
                imagePoster.setImageDrawable(Util.getDrawable(getActivity(), R.drawable.ic_error_black_48dp));
        }

    }

    private void loadTrailers() {

        final ArrayList<String> trailersUrl = new ArrayList<>();
        final ArrayList<String> trailers = new ArrayList<>();

        String urlRequest = VolleySingleton.TRAILERS_URL;
        urlRequest = urlRequest.replace(VolleySingleton.ID, String.valueOf(movie.getId()));

        urlRequest += "?" + VolleySingleton.API_KEY_GET_REQUEST_NAME + "=" + VolleySingleton.API_KEY;


        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRequest, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        trailersUrl.add(data.getString("key"));
                        trailers.add(getString(R.string.trailer) + (i + 1));
                    }

                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.trailer_item, R.id.txtTrailerItem, trailers);
                    listTrailers.setAdapter(arrayAdapter);

                    Util.setListViewHeightBasedOnChildren(listTrailers, getActivity()); // Modifies the height of ListView
                    trailersLoaded = true;

                    if (reviewsLoaded) {
                        progressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleySingleton.buildConnectionErrorDialog(getActivity()).show();
            }
        });

        requestQueue.add(stringRequest);


        listTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(Intent.ACTION_VIEW);

                String videoUrl = VolleySingleton.TRAILERS_PREFIX + trailersUrl.get(i);

                intent.setData(Uri.parse(videoUrl));

                startActivity(Intent.createChooser(intent, getString(R.string.chooseApplication)));

            }
        });

    }


    private void loadReviews() {



        String reviewsUrl = VolleySingleton.REVIEWS_URL;
        reviewsUrl = reviewsUrl.replace(VolleySingleton.ID, String.valueOf(movie.getId()));
        reviewsUrl += "?" + VolleySingleton.API_KEY_GET_REQUEST_NAME + "=" + VolleySingleton.API_KEY;

        final ArrayList<String> reviews = new ArrayList<>();


        StringRequest stringRequest = new StringRequest(Request.Method.GET, reviewsUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject review = jsonArray.getJSONObject(i);

                        String author = review.getString("author");
                        String content = review.getString("content");

                        String formatedReview = author + " :\n\n" + content;

                        reviews.add(formatedReview);

                    }


                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.reviews_item, R.id.txtReviewItem, reviews);
                    listReviews.setAdapter(arrayAdapter);

                    Util.setListViewHeightBasedOnChildren(listReviews, getActivity());

                    reviewsLoaded = true;

                    if (trailersLoaded)
                        progressDialog.dismiss();

                    if (jsonArray.length() == 0) {
                        textNoReviews.setVisibility(View.VISIBLE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleySingleton.buildConnectionErrorDialog(getActivity());
            }
        });


        requestQueue.add(stringRequest);

    }


}
