package impactdevs.net.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ian on 8/17/2015.
 */
public class ReviewsTab extends Fragment {

    private ListView mListView;
    private String id;
    private String lastId;
    private View emptyView;
    private List<Review> mList;
    private ReviewListAdapter mReviewListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            id = bundle.getString("id");
            id += "/reviews";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reviews, container, false);
        mListView = (ListView) v.findViewById(R.id.list_reviews);

        if (mList == null) {
            mList = new ArrayList<Review>();
        }

        mReviewListAdapter = new ReviewListAdapter(getActivity(),  mList);
        mListView.setAdapter(mReviewListAdapter);
        emptyView = v.findViewById(android.R.id.empty);

        if (lastId == null || !lastId.equals(id)) {
            fetchData(id);

        } else {
            isEmpty();
        }
        return v;
    }

    public void fetchData(String searchParam) {

        Utility util = new Utility();
        String url = util.getUrl(getActivity(), searchParam, null);

        //Creating Volley request obj
        final JsonObjectRequest req = new JsonObjectRequest(url, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Returns full Json request string
                        // Log.d("MovieFragment", "onResponse (line 118): " + response.toString());

                        //Parsing JSON
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {

                                JSONObject jsonObject = results.getJSONObject(i);
                                try {

                                    Review c = new Review();
                                    c.setAuthor(jsonObject.getString("author"));
                                    c.setComment(jsonObject.getString("content"));
                                    mList.add(c);

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
                        mReviewListAdapter.notifyDataSetChanged();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ReviewsTab", "onErrorResponse (line 147): " + error
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
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        lastId = id;
        super.onPause();
    }

}