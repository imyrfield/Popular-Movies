package impactdevs.net.popularmovies;

    import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

/**
 * Main Activity Fragment
 * Created by Ian on 7/23/2015.
 */
public class MovieFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private GridAdapter mGridAdapter;
    private String mSearchParam;
    private SharedPreferences mSharedPreferences;
    private String lastSort;
    public static final String PREFS_NAME = "SortPref";
    private static final String KEY_SORT = "searchParam";
    private ProgressBar mProgressBar;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.moviefragmentmenu, menu);
        String title = "";
        if (getSortPref().equals(getString(R.string.param_sort_most_popular))) {
            title = getString(R.string.sort_most_popular_title);
        } else {
            title = getString(R.string.sort_top_rated_title);
        }
        menu.findItem(R.id.most_popular).setTitle(title);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mSharedPreferences = this.getActivity().getSharedPreferences
                (PREFS_NAME, 0);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        mMovieList.clear();

        if (item.getTitle() == getString(R.string.sort_most_popular_title)) {
            editor.putString(KEY_SORT, getString(R.string.param_sort_top_rated));
            editor.apply();
            item.setTitle(R.string.sort_top_rated_title);
            Toast.makeText(getActivity(),
                    "Sorting change to Top Rated",
                    Toast.LENGTH_LONG).show();
        } else {
            editor.putString(KEY_SORT, getString(R.string.param_sort_most_popular));
            editor.apply();
            item.setTitle(R.string.sort_most_popular_title);
            Toast.makeText(getActivity(),
                    "Sorting change to Most Popular",
                    Toast.LENGTH_LONG).show();
        }

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

        if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
            Log.d("MovieFragment", "onCreateView (line 114): no saved instance found");
            mMovieList = new ArrayList<Movie>();
            fetchData(getSortPref(), null);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            Log.d("MovieFragment", "onCreateView (line 118): restoring saved instance " +
                    "state");
            mMovieList = savedInstanceState.getParcelableArrayList("movie");
        }
        // TODO: Gridview looses position after rotation. gridview.setSelection(int)
        // or gridview.smoothScrollToPosition(int)
        mGridView = (GridView) rootView.findViewById(R.id.gridView);

//        mGridView.setOnScrollListener(new ScrollListener() {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount) {
//                    fetchData(getSortPref(), page);
//            }
//        });

        mGridAdapter = new GridAdapter(getActivity(), mMovieList);
        mGridView.setAdapter(mGridAdapter);

        //Setting up onClick
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long l) {

                //Packaging variables
                Movie movie = (Movie) parent.getItemAtPosition(position);
                Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra("id", movie.getId());
                i.putExtra("title", movie.getMovieTitle());
                i.putExtra("image", movie.getThumbnailUrl());

                //Starting Detail Activity
                startActivity(i);
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
    // sorting
    private String getSortPref() {
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences
        //       (getActivity());
        mSharedPreferences = this.getActivity().getSharedPreferences(PREFS_NAME, 0);
        mSearchParam = mSharedPreferences.getString(KEY_SORT, getString(R
                .string.pref_sort_default));

        return mSearchParam;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        fetchData(getSortPref(), null);

    }
}