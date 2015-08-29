package impactdevs.net.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import impactdevs.net.popularmovies.data.DataContract;

/**
 * For Future implementation of Detail View.
 * Created by Ian on 7/24/2015.
 */
public class DetailFragment extends Fragment {
    // TODO: In landscape, you can't scroll down to the TabHost!!

    private String image, id;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mDuration;
    private TextView mRating;
    private TextView mSynopsis;
    private ImageView mPoster;
    private Context mContext;
    private List<String> mReview;
    private Bundle args = new Bundle();
    private FragmentTabHost mTabHost;
    private Movie m = new Movie();


    Utility utility = new Utility();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerDetail, Bundle
            savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, containerDetail,
                false);

        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        if (mTabHost == null){
            mTabHost = new FragmentTabHost(getActivity());
        }
        try {
            mTabHost.setup(getActivity(), getFragmentManager(), android.R.id.tabcontent);
            mTabHost.addTab(mTabHost.newTabSpec("synopsis").setIndicator("Synopsis", null),
                    SynopsisTab.class, args);
            mTabHost.addTab(mTabHost.newTabSpec("reviews").setIndicator("Reviews", null),
                    ReviewsTab.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("trailers").setIndicator("Trailers", null),
                    TrailersTab.class, null);


        }catch (IllegalStateException e){
            Log.d("DetailActivity", "onCreate (line 45): " + e.toString());
        }

        //Retrieve details from MovieFragment
        Bundle bundle = getActivity().getIntent().getExtras();
        id = bundle.getString("id");
        image = bundle.getString("image");

        //Setting Views
        mTitle = (TextView) rootView.findViewById(R.id.detailview_title);
        mPoster = (ImageView) rootView.findViewById(R.id.detailview_poster);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detailview_release_date);
        mDuration = (TextView) rootView.findViewById(R.id.detailview_duration);
        mRating = (TextView) rootView.findViewById(R.id.detailview_rating);
//        mSynopsis = (TextView) synopsisView.findViewById(R.id.synopsis);

        Glide.with(this)
                .load(utility.getImageUrl(image))
                .error(R.drawable.placeholder)
                .crossFade()
                .into(mPoster);

        //Request Movie Details from website. Because I want to display the duration
        //I'm having to query a different endpoint, and do another network call.
        makeJsonObjectRequest(id);
        return rootView;
    }

    private void makeJsonObjectRequest(final String id){

        String url = utility.getUrl(getActivity(), id, null);

        JsonObjectRequest movieDetailRequest = new JsonObjectRequest(url, new Response
                .Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    m.setMovieTitle(response.getString("title"));
                    mTitle.setText(m.getMovieTitle());

                    m.setRating(response.getDouble("vote_average"));
                    //Formating String to appear as #/10
                    String userRating = m.getRating() + "/10";
                    mRating.setText(userRating);

                    m.setDuration(response.getInt("runtime"));
                    String duration = String.valueOf(m.getDuration()) + "min";
                    mDuration.setText(duration);

                    m.setReleaseDate(response.getString("release_date"));
                    //Reducing string from 2015-06-12 to just 2015
                    String releaseDate = (m.getReleaseDate()).substring(0, 4);
                    mReleaseDate.setText(releaseDate);

                    m.setMovieSynopsis(response.getString("overview"));
                    args.putString("synopsis", m.getMovieSynopsis());

                    // Passing synopsis to Synopsis Fragment Tab.
                    SynopsisTab tab = (SynopsisTab) getFragmentManager()
                            .findFragmentByTag("synopsis");

                    // Checks if tab isVisible, otherewise if YouTube opens to play a
                    // trailer it throws a nullpointerexception.
                    if(tab != null && tab.isVisible()) {
                        tab.setNewText(m.getMovieSynopsis());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DetailActivity", "onErrorResponse (line 68): " + error.getMessage());
            }
        });

        //Adding to request Queue
        VolleyClass.getInstance().addToRequestQueue(movieDetailRequest);
    }

    private long addMovieToDB(){
        long movieId = 0;
        return movieId;
    }

    /**
     * Add selected movie to favorites in DB.
     * @param movie movie
     * @return Id
     */
    private long addMovieToFavorites(Movie movie){
        long movieId;

        // Check to see if the Movie is already in DB.
        Cursor movieCursor = mContext.getContentResolver().query(
                DataContract.MovieEntry.CONTENT_URI,
                new String[]{DataContract.MovieEntry._ID},
                DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie.getId()},
                null);

        if (movieCursor.moveToFirst()){

            // Movie is already a Favorite
            int movieIdIndex = movieCursor.getColumnIndex(DataContract.MovieEntry._ID);
            movieId = movieCursor.getLong(movieIdIndex);

        } else {

            // Insert values into DB.
            ContentValues values = new ContentValues();
            values.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
            values.put(DataContract.MovieEntry.COLUMN_TITLE, movie.getMovieTitle());
            values.put(DataContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            values.put(DataContract.MovieEntry.COLUMN_DURATION, movie.getDuration());
            values.put(DataContract.MovieEntry.COLUMN_RATING, movie.getRating());
            values.put(DataContract.MovieEntry.COLUMN_SYNOPSIS, movie.getMovieSynopsis());
            values.put(DataContract.MovieEntry.COLUMN_IMAGE_URL, movie.getThumbnailUrl());

            Uri insertedUri = mContext.getContentResolver().insert(
                    DataContract.MovieEntry.CONTENT_URI,
                    values);

            // The resulting URI contains the ID for the row. Extract the locationId from the Uri.
            movieId = ContentUris.parseId(insertedUri);
        }

        movieCursor.close();
        return movieId;
    }

    private long removeMovieFromFavorites(){
        long movieId = 0;
        return movieId;
    }

}
