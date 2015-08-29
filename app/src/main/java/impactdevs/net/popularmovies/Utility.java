package impactdevs.net.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * Class with Miscellaneous Utility methods.
 * Created by Ian on 7/20/2015.
 */
public class Utility {

    public String url;
    public String imageUrl;

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

//        if (!searchParam.equals(context.getString(R.string.param_sort_most_popular))
//                && !searchParam.equals(context.getString(R.string.param_sort_top_rated))) {
//            url += "&append_to_response=trailers,reviews";
//        }
        Log.d("Utility", "getUrl (line 39): " + url);

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
}
