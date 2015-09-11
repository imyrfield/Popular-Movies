package impactdevs.net.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
        .OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private GridAdapter mGridAdapter;
    private SharedPreferences mSharedPreferences;
    private String PREFS_NAME_SORT;
    private String PREFS_KEY_SORT;
    private ProgressBar mProgressBar;
    private Context mContext;
    private String sort;

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int COL_MOVIE_ID = 1;
    private static final int COL_TITLE = 2;
    private static final int COL_IMAGE_URL = 3;
    static final int DETAIL_LOADER = 0;
    static final int MOVIE_LOADER = 1;

    public interface Callback {

        void onItemSelected(String id);

        void onMovieLoaded(String id);

    }

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MovieFragment", "onCreate");
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
        sort = Utility.getSortPref(mContext);

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
        sort = Utility.getSortPref(mContext);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        sort = Utility.getSortPref(mContext);

        if (savedInstanceState != null) {

            Log.d("MovieFragment", "onCreateView: restoring saved instance state");
            mMovieList = savedInstanceState.getParcelableArrayList("movie");
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                mPosition = savedInstanceState.getInt(SELECTED_KEY);

                if (mPosition != GridView.INVALID_POSITION) {
                    mGridView.smoothScrollToPosition(mPosition);
                }
            }
        } else {
            mMovieList = new ArrayList<Movie>();

        }

        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        mGridAdapter = new GridAdapter(getActivity(), mMovieList);
        mGridView.setAdapter(mGridAdapter);

//        Used for scrollListener
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

                Movie movie = (Movie) parent.getItemAtPosition(position);
                ((Callback) getActivity())
                        .onItemSelected(movie.getId());
                Log.d("MovieFragment", "onItemClick (line 208): " + movie.getId());

                mPosition = position;
            }

        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarts loader, or else the loader starts acting funny.
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movie", mMovieList);
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void fetchData(String searchParam, Integer page) {

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
        sort = Utility.getSortPref(mContext);
        if (sort.equals(getString(R.string.param_sort_favorites))) {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        } else {
            fetchData(sort, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                DataContract.MovieEntry.CONTENT_URI,
                null, null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mProgressBar.setVisibility(View.GONE);

        if (!data.moveToFirst()) {
            return;
        }

        if (sort.equals(getString(R.string.param_sort_favorites))) {

            mMovieList.clear();
            while (data.moveToNext()) {
                Movie m = new Movie();
                m.setId(data.getString(COL_MOVIE_ID));
                m.setThumbnailUrl(data.getString(COL_IMAGE_URL));
                m.setMovieTitle(data.getString(COL_TITLE));
                mMovieList.add(m);
                Log.d("MovieFragment", "onLoadFinished (line 382): " + m.getId());
            }

            mGridAdapter.notifyDataSetChanged();

            if (mMovieList.size() > 0) {
                ((Callback) getActivity()).onMovieLoaded
                        (mMovieList.get(0).getId());
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        Log.d("MovieFragment", "onActivityCreated");

        if (savedInstanceState == null) {
            if (sort.equals(getString(R.string.param_sort_favorites))) {
                Log.d("MovieFragment", "onActivityCreated - load favorites");
                getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            } else {
                Log.d("MovieFragment", "onActivityCreated - load from web");
                fetchData(sort, null);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }
}