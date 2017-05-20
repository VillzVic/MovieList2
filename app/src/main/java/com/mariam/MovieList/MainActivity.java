package com.mariam.MovieList;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shamdroid.popularmovies.R;

public class MainActivity extends AppCompatActivity implements MovieListFragment.ChangeDetails{



    MovieListFragment movieListFragment;

    boolean towPane ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        movieListFragment = (MovieListFragment) getSupportFragmentManager().findFragmentById(R.id.mainMoviesListFragment);

        if (findViewById(R.id.mainDetailsContainer) != null ){
            towPane = true ;

        }else {
            towPane = false ;
        }




    }


    @Override
    protected void onRestart() {
        super.onRestart();

        if(movieListFragment != null){
            movieListFragment.refreshFavoriteMovies();
        }

    }

    @Override
    public void showDetails( Movie movie , boolean byClick) {


        if (towPane){

            final DetailsFragment detailsFragment = new DetailsFragment();

            Bundle args = new Bundle();

            args.putParcelable("movie" , movie);

            detailsFragment.setArguments(args);

            new Handler().post(new Runnable() {
                @Override
                public void run() {


                    getSupportFragmentManager().beginTransaction().replace(R.id.mainDetailsContainer , detailsFragment).commit();
                }
            });

        }else {
            if (byClick) {
                Intent intent = new Intent(this, MovieDetails.class);
                intent.putExtra("movie", movie); // Since the class Movie implements the Parcelable class , we can pass it with the intent
                startActivity(intent);
            }
        }
    }
}
