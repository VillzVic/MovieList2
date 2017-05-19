package Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.shamdroid.popularmovies.R;

/*
    This class is for applying the concept of Sigleton for Volley Library
    And for making the access to the API easier .
*/

public class VolleySingleton {




    public static final String API_KEY = ""; // Put your API Key Here 
    public static final String API_KEY_GET_REQUEST_NAME = "api_key"; // INSERT YOUR API KRY HERE
    public static final String API_URL = "http://api.themoviedb.org/3"; // The URL for getting the movies
    public static final String IMAGES_URL="http://image.tmdb.org/t/p/w342"; // The URL for getting the images ( Posters )

    public static final String POPULAR_URL = API_URL + "/movie/popular"; // The URL for getting the movies ordered by popularity
    public static final String TOP_RATER_URL=API_URL + "/movie/top_rated"; // The URL for getting the movies ordered by rating


    public static final String TRAILERS_PREFIX =  "https://www.youtube.com/watch?v=" ;
    public static final String ID = "?id" ;
    public static final String TRAILERS_URL = API_URL + "/movie/"+ ID +"/videos";


    public static final String REVIEWS_URL = API_URL + "/movie/" + ID + "/reviews" ;


    private static VolleySingleton instance ;

    private RequestQueue requestQueue ;


    private VolleySingleton (){
    }

    public static VolleySingleton getInstance (){
        if (instance == null)
            instance = new VolleySingleton();
        return instance;
    }
    public static AlertDialog buildConnectionErrorDialog (Context context){ // For Building an alert progressDialog to show that there is a problem with the Internet connection
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(context.getString(R.string.errorNoConnection));
        alertDialog.setNeutralButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return alertDialog.create();
    }


    public RequestQueue getRequestQueue(Context context){
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        return requestQueue;
    }




}
