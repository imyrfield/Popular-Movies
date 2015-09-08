package impactdevs.net.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;

/**
 * Created by Ian on 9/7/2015.
 */
public class FavoriteAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    private ImageView imageView;

    public FavoriteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
        }

        View view = LayoutInflater.from(context).inflate(R.layout.gridview_item,
                parent,  false);

        imageView = (ImageView) view.findViewById(R.id.gridview_item_poster);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String image = cursor.getString(MovieFragment.COL_IMAGE_URL);

        Utility utility = new Utility();
        DrawableRequestBuilder builder = Glide.with(context)
                .load(utility.getImageUrl(image))
                .error(R.drawable.placeholder)
                .crossFade();
        builder.into(imageView);
    }
}
