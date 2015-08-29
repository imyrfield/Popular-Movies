package impactdevs.net.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Handles loading images into GridView
 * Created by Ian on 7/21/2015.
 */
public class GridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Activity mActivity;
    private List<Movie> movieItems;

    public GridAdapter(Activity activity, List<Movie> movieItems) {
        this.movieItems = movieItems;
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return movieItems.size();
    }

    @Override
    public Object getItem(int position) {
        return movieItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_item, null);
        }

        ImageView imageView =
                (ImageView) convertView.findViewById(R.id.gridview_item_poster);

        // getting movie data for the row
        Movie m = movieItems.get(position);

        //Image
        Utility utility = new Utility();
        DrawableRequestBuilder builder = Glide.with(mActivity)
                .load(utility.getImageUrl(m.getThumbnailUrl()))
                .error(R.drawable.placeholder)
                .crossFade();
        builder.into(imageView);

        return convertView;
    }
}
