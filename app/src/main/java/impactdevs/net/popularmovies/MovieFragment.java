package impactdevs.net.popularmovies;

import android.content.Context;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import impactdevs.net.popularmovies.data.DataContract;

/**
 * Main Activity Fragment
 * Created by Ian on 7/23/2015.
 */
public class MovieFragment extends Fragment implements SharedPreferences
        .OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor> {

    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private GridAdapter mGridAdapter;
    private FavoriteAdapter mFavoriteAdapter;
    private String mSearchParam;
    private SharedPreferences mSharedPreferences;
    private String lastSort;
    private String PREFS_NAME_SORT;
    private String PREFS_KEY_SORT;
    private ProgressBar mProgressBar;
    private Context mContext;
    private String sort;

    private static final int FAVORITE_LOADER = 1;

    private static final String[] MOVIE_COLUMNS = {
            DataContract.MovieEntry._ID,
            DataContract.MovieEntry.COLUMN_MOVIE_ID,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_RELEASE_DATE,
            DataContract.MovieEntry.COLUMN_DURATION,
            DataContract.MovieEntry.COLUMN_RATING,
            DataContract.MovieEntry.COLUMN_SYNOPSIS,
            DataContract.MovieEntry.COLUMN_IMAGE_URL};

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_RELEASE_DATE = 3;
    static final int COL_DURATION = 4;
    static final int COL_RATING = 5;
    static final int COL_SYNOPSIS = 6;
    static final int COL_IMAGE_URL = 7;

    public interface Callback {


        void onItemSelected(String id);

        void onMovieLoaded(String id);

    }

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mContext = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_main_menu, menu);
        String title = "";
        String sort = Utility.getSortPref(getActivity());

        if (sort.equals(getString(R.string.param_sort_most_popular))) {
            title = getString(R.string.sort_most_popular_title);
        } else if (sort.equals(getString(R.string.param_sort_top_rated))) {
            title = getString(R.string.sort_top_rated_title);
        } else {
            title = getString(R.string.sort_favorite_title);
        }
        menu.findItem(R.id.menu_sort_method).setTitle(title);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        PREFS_NAME_SORT = getString(R.string.PREFS_NAME_SORT);
        PREFS_KEY_SORT = getString(R.string.PREFS_KEY_SORT);

        mSharedPreferences = this.getActivity().getSharedPreferences
                (PREFS_NAME_SORT, 0);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        mMovieList.clear();

        if (item.getTitle() == getString(R.string.sort_most_popular_title)) {
            editor.putString(PREFS_KEY_SORT, getString(R.string.param_sort_top_rated));
            editor.apply();
            item.setTitle(R.string.sort_top_rated_title);
            Toast.makeText(getActivity(),
                    "Sorting change to Top Rated",
                    Toast.LENGTH_LONG).show();
        } else if (item.getTitle() == getString(R.string.sort_top_rated_title)) {
            editor.putString(PREFS_KEY_SORT, getString(R.string.param_sort_favorites));
            editor.apply();
            item.setTitle(R.string.sort_favorite_title);
            Toast.makeText(getActivity(),
                    "Sorting change to Personal Favorites",
                    Toast.LENGTH_LONG).show();
        } else {
            editor.putString(PREFS_KEY_SORT, getString(R.string.param_sort_most_popular));
            editor.apply();
            item.setTitle(R.string.sort_most_popular_title);
            Toast.makeText(getActivity(),
                    "Sorting change to Most Popular",
                    Toast.LENGTH_LONG).show();
        }
        sort = Utility.getSortPref(getActivity());
        // Todo: Remove refresh button for final version
//        if (id == R.id.refresh) {
//                //Assigns mSearchParam with preference value for sorting
//                mSearchParam = mSharedPreferences.getString(getString(R.string.pref_sort_key), getString
//                        (R.string.pref_sort_default));
//
//                // Initiates network call
//                mMovieList.clear();
//                fetchData(mSearchParam);
//
//                return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        sort = Utility.getSortPref(getActivity());
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mMovieList = new ArrayList<Movie>();
        mFavoriteAdapter = new FavoriteAdapter(getActivity(), null, 0);
        mGridAdapter = new GridAdapter(getActivity(), mMovieList);

        if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
            Log.d("MovieFragment", "onCreateView (line 114): no saved instance found");
            if (sort.equals(getString(R.string.param_sort_favorites))) {
                mGridView.setAdapter(mFavoriteAdapter);
                getLoaderManager().initLoader(FAVORITE_LOADER, null, this);
            } else {
                mGridView.setAdapter(mGridAdapter);
                fetchData(sort, null);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {

            Log.d("MovieFragment", "onCreateView (line 118): restoring saved instance " +
                    "state");
            mMovieList = savedInstanceState.getParcelableArrayList("movie");
        }
        // TODO: Gridview looses position after rotation. gridview.setSelection(int)
        // or gridview.smoothScrollToPosition(int)

//        mGridView.setOnScrollListener(new ScrollListener() {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount) {
//                    fetchData(getSortPref(), page);
//            }
//        });


        //Setting up onClick
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long l) {

//                //Packaging variables
//                Movie movie = (Movie) parent.getItemAtPosition(position);
//                Intent i = new Intent(getActivity(), DetailActivity.class);
//                i.putExtra("id", movie.getId());
//                i.putExtra("title", movie.getMovieTitle());
//                i.putExtra("image", movie.getThumbnailUrl());
//
//                //Starting Detail Activity
//                startActivity(i);

                // TODO: ??
                if (sort.equals(getString(R.string.param_sort_favorites))) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        ((Callback) getActivity()).onItemSelected(
                                cursor.getString(COL_MOVIE_ID));
                    }
                } else {
                    Movie movie = (Movie) parent.getItemAtPosition(position);
                    ((Callback) getActivity())
                            .onItemSelected(movie.getId());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MovieFragment", "onResume");
//        String currentSortPref = getSortPref();
//        if(!currentSortPref.equals(lastSort) || mMovieList == null) {
//
//            lastSort = currentSortPref;
//            // Clears previous array
//            if(mMovieList != null) mMovieList.clear();
//            // Starts Network Call
//            fetchData(lastSort);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movie", mMovieList);
        super.onSaveInstanceState(outState);
    }


    public void fetchData(String searchParam, Integer page) {
        Log.d("MovieFragment", "fetchData");

        Utility util = new Utility();
        String url = util.getUrl(getActivity(), searchParam, page);

        //Creating Volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest(url, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Returns full Json request string
                        // Log.d("MovieFragment", "onResponse (line 118): " + response
                        //        .toString());

                        //Parsing JSON
                        try {
                            JSONArray movieResults = response.getJSONArray("results");

                            for (int i = 0; i < movieResults.length(); i++) {

                                JSONObject jsonObject = movieResults.getJSONObject(i);
                                try {
                                    Movie movie = new Movie();
                                    movie.setId(jsonObject.getString("id"));
                                    movie.setMovieTitle(jsonObject.getString("title"));
                                    movie.setThumbnailUrl(jsonObject.getString("poster_path"));

                                    //Add movie to movies array
                                    mMovieList.add(movie);
                                    if (i == 19) {
                                        ((Callback) getActivity()).onMovieLoaded
                                                (mMovieList.get(0).getId());
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Notifying list adapter about data changes
                        //So that it renders the list view with updated data
                        mGridAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MovieFragment", "onErrorResponse (line 160): " + error
                        .getMessage());
            }
        });

        //Adding request to request queue;
        VolleyClass.getInstance().addToRequestQueue(movieReq);
    }

    //Initializes Preferences and assigns mSearchParam with preference value for

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        String sort = Utility.getSortPref(getActivity());
        if (sort.equals(getString(R.string.param_sort_favorites))) {
            mGridView.setAdapter(mFavoriteAdapter);

            getLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        } else {
            mGridView.setAdapter(mGridAdapter);
            fetchData(sort, null);
        }
    }

    private Cursor queryMovie() {

        return mContext.getContentResolver().query(
                DataContract.MovieEntry.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Cursor cursor = queryMovie();
        if (cursor.moveToFirst()) {

            return new CursorLoader(getActivity(),
                    DataContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null, null, null);

        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mFavoriteAdapter.swapCursor(data);

        if (data.moveToFirst()) {
            data.moveToFirst();
            ((Callback) getActivity()).onMovieLoaded(
                    data.getString(COL_MOVIE_ID));
        }
Log.d("MovieFragment", "onLoadFinished (line 369): ");
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteAdapter.swapCursor(null);
        Log.d("MovieFragment", "onLoaderReset (line 376): ");
    }
}