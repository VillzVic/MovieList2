package com.mariam.MovieList;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;





public class MovieDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        MovieDetailsFragment detailsFragment = new MovieDetailsFragment();

        Bundle args = new Bundle();

        args.putParcelable("movie" , getIntent().getParcelableExtra("movie"));

        detailsFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().add(R.id.detailsFragmentContainer , detailsFragment).commit();


    }


}




