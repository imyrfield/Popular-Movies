package impactdevs.net.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
public class MovieFragment extends Fragment {

    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private GridAdapter mGridAdapter;
    private String mSearchParam;
    private SharedPreferences mSharedPreferences;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movie", mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.moviefragmentmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        // Todo: Remove refresh button for final version
        if (id == R.id.refresh) {
            //Assigns mSearchParam with preference value for sorting
            mSearchParam = mSharedPreferences.getString(getString(R.string.pref_sort_key), getString
                    (R.string.pref_sort_default));

            // Initiates network call
            mMovieList.clear();
            fetchData(mSearchParam);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Initializes Preferences and assigns mSearchParam with preference value for
        // sorting
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (getActivity());
        mSearchParam = mSharedPreferences.getString(getString(R.string.pref_sort_key), getString
                (R.string.pref_sort_default));

        //Starts Network Call and clears previous array
        mMovieList.clear();
        fetchData(mSearchParam);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState == null || !savedInstanceState.containsKey("movie")){
            mMovieList = new ArrayList<Movie>();
            Log.d("MovieFragment", "onCreateView (line 114): no saved instance found");
        }
        else {
            mMovieList = savedInstanceState.getParcelableArrayList("movie");
            Log.d("MovieFragment", "onCreateView (line 118): restoring saved instance " +
                    "state");
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridView);
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

    public void fetchData(String searchParam) {

        Utility util = new Utility();
        String url = util.getUrl(getActivity(), searchParam);

        //Creating Volley request obj
        JsonObjectRequest movieReq = new JsonObjectRequest( url, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Returns full Json request string
                        Log.d("MovieFragment", "onResponse (line 118): " + response
                                .toString());

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
}