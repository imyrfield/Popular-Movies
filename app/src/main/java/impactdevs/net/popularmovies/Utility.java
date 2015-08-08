package impactdevs.net.popularmovies;

import android.content.Context;
import android.net.Uri;

/**
 * Class with Miscellaneous Utility methods.
 * Created by Ian on 7/20/2015.
 */
public class Utility {

    public String url;
    public String imageUrl;

//    Builds and Returns Main Query URL
    public String getUrl(Context context, String searchParam) {

        String query = searchParam;
        String api = context.getString(R.string.API_KEY);

        final String MOVIEDB_BASE_URL = context.getString(R.string.BASE_URL);
        final String API_KEY = "api_key";

        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendEncodedPath(query)
                .appendQueryParameter(API_KEY, api)
                .build();
        url = builtUri.toString();

        return url;
    }

//  Builds and returns Poster URL
    public String getImageUrl(String thumbnailUrl) {


        final String IMAGEDB_BASE_URL = "http://image.tmdb.org/t/p/w185";
        final String IMAGE_ITEM = thumbnailUrl;

        Uri builtUri = Uri.parse(IMAGEDB_BASE_URL).buildUpon()
                .appendEncodedPath(IMAGE_ITEM)
                .build();
        imageUrl = builtUri.toString();

        return imageUrl;
    }
}
