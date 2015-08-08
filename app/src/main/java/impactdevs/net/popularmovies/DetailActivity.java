package impactdevs.net.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ian on 7/25/2015.
 */
public class DetailActivity extends AppCompatActivity{

    private String image, id;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mDuration;
    private TextView mRating;
    private TextView mSynopsis;
    private ImageView mPoster;

    Utility utility = new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        //Retrieve details from MovieFragment
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        image = bundle.getString("image");

        //Setting Views
        mTitle = (TextView) findViewById(R.id.detailview_title);
        mPoster = (ImageView) findViewById(R.id.detailview_poster);
        mReleaseDate = (TextView) findViewById(R.id.detailview_release_date);
        mDuration = (TextView) findViewById(R.id.detailview_duration);
        mRating = (TextView) findViewById(R.id.detailview_rating);
        mSynopsis = (TextView) findViewById(R.id.detailview_synopsis);

        Glide.with(this)
                .load(utility.getImageUrl(image))
                .error(R.drawable.placeholder)
                .crossFade()
                .into(mPoster);

        //Request Movie Details from website. Because I want to display the duration
        //I'm having to query a different endpoint, and do another network call.
        makeJsonObjectRequest(id);

    }

    private void makeJsonObjectRequest(String id){

        String url = utility.getUrl(getApplicationContext(), id);

        JsonObjectRequest movieDetailRequest = new JsonObjectRequest(url, new Response
                .Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    Movie m = new Movie();
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
                    mSynopsis.setText(m.getMovieSynopsis());

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
}
