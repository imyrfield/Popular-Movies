package impactdevs.net.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class with Miscellaneous Utility methods.
 * Created by Ian on 7/20/2015.
 */
public class Utility {

    public String url;
    public String imageUrl;
    private String trailer;
    private String trailerUrl;
    private Context mContext;

    public interface Callback {

        void trailerFetchCompleted();
    }

    //Reducing string from 2015-06-12 to just 2015
    public static String formatDate(String date) {
        return date.substring(0, 4);
    }

    // Returns sort preference, either "Most Popular" or "Top Rated"
    public static String getSortPref(Context context) {

        SharedPreferences mSharedPreferences;
        String PREFS_NAME_SORT = context.getString(R.string
                .PREFS_NAME_SORT);
        String PREFS_KEY_SORT = context.getString(R.string.PREFS_KEY_SORT);
        String mSearchParam;

        mSharedPreferences = context.getSharedPreferences(PREFS_NAME_SORT, 0);
        mSearchParam = mSharedPreferences.getString(PREFS_KEY_SORT, context.getString(R
                .string.pref_sort_default));

        return mSearchParam;
    }

    //    Builds and Returns Main Query URL
    public String getUrl(Context context, String searchParam, Integer page) {

        String api = context.getString(R.string.API_KEY);

        if (page == null) page = 1;
        String mPage = page.toString();

        final String MOVIEDB_BASE_URL = context.getString(R.string.BASE_URL);
        final String API_KEY = "api_key";
        final String PAGE = "page";

        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(searchParam)
                .appendQueryParameter(API_KEY, api)
                .appendQueryParameter(PAGE, mPage)
                .build();

        url = builtUri.toString();
//        Log.d("Utility", "getUrl (line 39): " + url);

        return url;
    }

    //  Builds and returns Poster URL
    public String getImageUrl(String thumbnailUrl) {


        final String IMAGEDB_BASE_URL = "http://image.tmdb.org/t/p/w500";
        final String IMAGE_ITEM = thumbnailUrl;

        Uri builtUri = Uri.parse(IMAGEDB_BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_ITEM)
                .build();
        imageUrl = builtUri.toString();

        return imageUrl;
    }

    // Fetches the first trailer, so that it can be shared, even before switching to the
    // Trailers tab
    public void getFirstTrailer(Context context, String id) {

        mContext = context;
        String searchParam = id + "/videos";
        String url = getUrl(context, searchParam, null);

        //Creating Volley request obj
        final JsonObjectRequest req = new JsonObjectRequest(url, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Returns full Json request string

// Log.d("Utility", "onResponse (line 98): " + response.toString());

                        // Parsing JSON
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < 1; i++) {
                                JSONObject jsonObject = results.getJSONObject(i);
                                try {

                                    trailer = jsonObject.getString("key");
//                                    Log.d("Utility", "getFirstTrailer (line 126): " + trailer);
                                    ((Callback) mContext).trailerFetchCompleted();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Utility", "onErrorResponse (line 119): " + error.getMessage());
            }
        });

        VolleyClass.getInstance().addToRequestQueue(req);
    }

    public String formatUrl() {

        final String baseUrl = mContext.getString(R.string.youtube_url);
        trailerUrl = baseUrl + trailer;
//        Log.d("Utility", "formatUrl (line 138): " + trailerUrl);
        return trailerUrl;
    }
}