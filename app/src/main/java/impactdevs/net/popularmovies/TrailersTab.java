package impactdevs.net.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.youtube.player.YouTubeIntents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ian on 8/17/2015.
 */
public class TrailersTab extends Fragment {

    // TODO: SaveInstanceState(?), currently, it has to re-poll after rotation.
    private String id;
    private ListView mListView;
    private List<Trailer> mList;
    private TrailerListAdapter mTrailerListAdapter;
    private String lastId;
    private View emptyView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getActivity().getIntent().getExtras();
        id = bundle.getString("id");
        id += "/videos";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_trailers, container, false);
        mListView = (ListView) v.findViewById(R.id.list_trailers);

        if (mList == null) {
            mList = new ArrayList<Trailer>();
        }

        mTrailerListAdapter = new TrailerListAdapter(getActivity(), mList);
        mListView.setAdapter(mTrailerListAdapter);
        emptyView = v.findViewById(android.R.id.empty);

        if (lastId == null || !lastId.equals(id)) {
            fetchData(id);
        } else {
            isEmpty();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer trailer = (Trailer) parent.getItemAtPosition(position);
                Intent i;
                if(YouTubeIntents.canResolvePlayVideoIntentWithOptions(getActivity())){
                    i = YouTubeIntents.createPlayVideoIntentWithOptions(getActivity(),
                            trailer.getVideoId(), true, true);
                }else{
                    i = YouTubeIntents.createPlayVideoIntent(getActivity
                            (), trailer.getVideoId());
                }

                startActivity(i);
            }
        });

        return v;
    }

    public void fetchData(String searchParam) {
        Log.d("TrailersTab", "fetchData (line 63): ");
        Utility util = new Utility();
        String url = util.getUrl(getActivity(), searchParam, null);

        //Creating Volley request obj
        final JsonObjectRequest req = new JsonObjectRequest(url, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Returns full Json request string
                        Log.d("TrailersTab", "onResponse (line 74): " + response.toString());

                        // Parsing JSON
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject jsonObject = results.getJSONObject(i);
                                try {

                                    Trailer t = new Trailer();

                                    t.setTitle(jsonObject.getString("name"));
                                    t.setVideoId(jsonObject.getString("key"));

                                    mList.add(t);

                                    isEmpty();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Notifying list adapter about data changes
                        //So that it renders the list view with updated data
                        //mAdapter.notifyDataSetChanged();
                        mTrailerListAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TrailersTab", "instance initializer (line 91): " + error
                        .getMessage());
            }
        });

        //Adding request to request queue;
        VolleyClass.getInstance().addToRequestQueue(req);
    }

    private void isEmpty() {

        if (mList.isEmpty()) {
            Log.d("ReviewsTab", "isEmpty (line 131): reviews is empty, show emptyView");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            Log.d("ReviewsTab", "isEmpty (line 134): hiding emptyView");
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        Log.d("ReviewsTab", "onPause (line 134): ");
        lastId = id;
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTrailerListAdapter.releaseLoaders();
    }
}
