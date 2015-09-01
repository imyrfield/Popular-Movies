package impactdevs.net.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
public class DetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
    // TODO: In landscape, you can't scroll down to the TabHost!!

    private String image, id;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mDuration;
    private TextView mRating;
    private ImageView mPoster;
    private CheckBox mFavorite;

    private Context mContext;
    private List<String> mReview;
    private Bundle args = new Bundle();
    private FragmentTabHost mTabHost;
    private Movie m = new Movie();
    Utility utility = new Utility();

    private static final int DETAIL_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            DataContract.MovieEntry._ID,
            DataContract.MovieEntry.COLUMN_MOVIE_ID,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_RELEASE_DATE,
            DataContract.MovieEntry.COLUMN_DURATION,
            DataContract.MovieEntry.COLUMN_RATING,
            DataContract.MovieEntry.COLUMN_SYNOPSIS};

    private static final int COL_ID = 0;
    private static final int COL_MOVIE_ID = 1;
    private static final int COL_TITLE = 2;
    private static final int COL_RELEASE_DATE = 3;
    private static final int COL_DURATION = 4;
    private static final int COL_RATING = 5;
    private static final int COL_SYNOPSIS = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerDetail, Bundle
            savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, containerDetail,
                false);

        //Setting Views
        mTitle = (TextView) rootView.findViewById(R.id.detailview_title);
        mPoster = (ImageView) rootView.findViewById(R.id.detailview_poster);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detailview_release_date);
        mDuration = (TextView) rootView.findViewById(R.id.detailview_duration);
        mRating = (TextView) rootView.findViewById(R.id.detailview_rating);
        mFavorite = (CheckBox) rootView.findViewById(R.id.favorite_checkbox);

        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        if (mTabHost == null) {
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


        } catch (IllegalStateException e) {
            Log.d("DetailActivity", "onCreate (line 45): " + e.toString());
        }

        // Retrieve details from MovieFragment
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getString("id");
            m.setId(id);
            image = bundle.getString("image");
            m.setThumbnailUrl(image);
        }

        // Setting poster
        Glide.with(this)
                .load(utility.getImageUrl(image))
                .error(R.drawable.placeholder)
                .crossFade()
                .into(mPoster);

        // Load from DB, if it's a favorite. Otherwise, fetch data from website.
        if (isFavorite(id)) {
            getLoaderManager().initLoader(DETAIL_LOADER, bundle, this);
        } else {
            makeJsonObjectRequest(id);
        }

        // Logic for favorite button
        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    if (((CheckBox) v).isChecked()) {
                        addMovieToFavorites(m);
                        ((CheckBox) v).setChecked(true);
                    } else {
                        Boolean wasDeleted = removeMovieFromFavorites(id);
                        if (wasDeleted.equals(true)) {
                            ((CheckBox) v).setChecked(false);
                        }
                    }

                }
            }
        });

        return rootView;
    }

    private void makeJsonObjectRequest(final String id) {

        String url = utility.getUrl(getActivity(), id, null);

        JsonObjectRequest movieDetailRequest = new JsonObjectRequest(url, new Response
                .Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    m.setMovieTitle(response.getString("title"));
                    mTitle.setText(m.getMovieTitle());

                    m.setRating(response.getDouble("vote_average"));
                    mRating.setText(getString(R.string.format_rating, m.getRating()));

                    m.setDuration(response.getInt("runtime"));
                    mDuration.setText(getString(R.string.format_duration, m.getDuration()));

                    m.setReleaseDate(Utility.formatDate(response.getString("release_date")));
                    mReleaseDate.setText(m.getReleaseDate());

                    m.setMovieSynopsis(response.getString("overview"));
                    setupSynopsisTab(m.getMovieSynopsis());

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

    private Cursor queryMovie(String id) {
        Cursor cursor = mContext.getContentResolver().query(
                DataContract.MovieEntry.CONTENT_URI,
                new String[]{DataContract.MovieEntry._ID},
                DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{id},
                null);
        return cursor;
    }

    /**
     * Add selected movie to favorites in DB.
     *
     * @param m movie
     * @return Id
     */
    private long addMovieToFavorites(Movie m) {
        long movieId;

        // Check to see if the Movie is already in DB.
        Cursor movieCursor = queryMovie(m.getId());

        if (movieCursor.moveToFirst()) {

            // Movie is already a Favorite
            int movieIdIndex = movieCursor.getColumnIndex(DataContract.MovieEntry._ID);
            movieId = movieCursor.getLong(movieIdIndex);
            Log.d("DetailFragment", "Movie is already in favorites");
        } else {

            // Insert values into DB.
            ContentValues values = new ContentValues();
            values.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, m.getId());
            values.put(DataContract.MovieEntry.COLUMN_TITLE, m.getMovieTitle());
            values.put(DataContract.MovieEntry.COLUMN_RELEASE_DATE, m.getReleaseDate());
            values.put(DataContract.MovieEntry.COLUMN_DURATION, m.getDuration());
            values.put(DataContract.MovieEntry.COLUMN_RATING, m.getRating());
            values.put(DataContract.MovieEntry.COLUMN_SYNOPSIS, m.getMovieSynopsis());
            values.put(DataContract.MovieEntry.COLUMN_IMAGE_URL, m.getThumbnailUrl());

            Uri insertedUri = mContext.getContentResolver().insert(
                    DataContract.MovieEntry.CONTENT_URI,
                    values);

            // The resulting URI contains the ID for the row. Extract the locationId from the Uri.
            movieId = ContentUris.parseId(insertedUri);
            Log.d("DetailFragment", "Movie Added to favorites");
        }

        movieCursor.close();
        return movieId;
    }

    private boolean removeMovieFromFavorites(String id) {
        Log.d("DetailFragment", "Removing movie from favorites");

        boolean deleteStatus = false;

        // Check to see if the Movie is already in DB.
        Cursor movieCursor = queryMovie(id);

        if (movieCursor.moveToFirst()) {

            // Movie is already a Favorite
            Log.d("DetailFragment", "Found Movie in Favorites");

            int deletedUri = mContext.getContentResolver().delete(
                    DataContract.MovieEntry.CONTENT_URI,
                    DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{id});

            if (deletedUri > 0) {
                Log.d("DetailFragment", "Removed from favorites");
                deleteStatus = true;
            } else {
                Log.d("DetailFragment", "Failed to remove from favorites");
                deleteStatus = false;
            }
        }

        movieCursor.close();
        return deleteStatus;
    }

    private boolean isFavorite(String id) {

        Cursor cursor = queryMovie(id);
        boolean favorite = false;

        if (cursor.moveToFirst()) {
            mFavorite.setChecked(true);
            favorite = true;
        }
        return favorite;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long position;
        Uri uri;
        String mId = args.getString("id");
        Cursor c = queryMovie(mId);
        if (c.moveToFirst()) {
            position = c.getPosition();

        } else {
            position = -1;
        }
        uri = DataContract.MovieEntry.buildMovieUri(position);

        return new CursorLoader(
                getActivity(),
                DataContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{mId},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String title = data.getString(COL_TITLE);
        String releaseDate = data.getString(COL_RELEASE_DATE);
        int duration = data.getInt(COL_DURATION);
        double rating = data.getDouble(COL_RATING);
        String synopsis = data.getString(COL_SYNOPSIS);

        mTitle.setText(title);
        mReleaseDate.setText(releaseDate);
        mDuration.setText(getString(R.string.format_duration, duration));
        mRating.setText(getString(R.string.format_rating, rating));

        setupSynopsisTab(synopsis);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }

    /**
    *    Setup and pass info to Synopsis Fragment Tab
    */
    private void setupSynopsisTab(String synopsis){

        // Saving synopsis into bundle so text will load when switching between fragment tabs
        args.putString("synopsis", synopsis);

        SynopsisTab tab = (SynopsisTab) getFragmentManager()
                .findFragmentByTag("synopsis");

        // Checks if tab isVisible, otherewise if YouTube opens to play a
        // trailer it throws a nullpointerexception.
        if (tab != null && tab.isVisible()) {
            tab.setNewText(synopsis);
        }
    }
}
