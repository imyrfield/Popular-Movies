package impactdevs.net.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Ian on 8/22/2015.
 */
public class TrailerListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Activity mActivity;
    private List<Trailer> mTrailers;
    private String YouTube_Key;
    private final ThumbnailListener thumbnailListener;
    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
    private Trailer t;


    public TrailerListAdapter(Activity activity, List<Trailer> trailers) {
        this.mTrailers = trailers;
        this.mActivity = activity;

        thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
        thumbnailListener = new ThumbnailListener();

        YouTube_Key = mActivity.getString(R.string.YouTube_Key);
    }

    @Override
    public int getCount() {
        return mTrailers.size();
    }

    @Override
    public Object getItem(int position) {
        return mTrailers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        t = mTrailers.get(position);
        YouTubeThumbnailView trailer;
        if (mInflater == null) {
            mInflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_trailer, null);
            trailer = (YouTubeThumbnailView) convertView.findViewById(R.id
                    .list_item_trailer_videoView);
            trailer.setTag(t.getVideoId());
            trailer.initialize(YouTube_Key, thumbnailListener);

        } else {
            trailer = (YouTubeThumbnailView) convertView.findViewById(R.id
                    .list_item_trailer_videoView);
            YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(trailer);

            if (loader == null) {
                trailer.setTag(t.getVideoId());
            } else {
                loader.setVideo(t.getVideoId());
            }
        }

//        trailer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent;
//                if(YouTubeIntents.canResolvePlayVideoIntentWithOptions(mActivity)){
//                    intent = YouTubeIntents.createPlayVideoIntentWithOptions(mActivity,
//                            t.getVideoId(), true, true);
//                }else{
//                    intent = YouTubeIntents.createPlayVideoIntent(mActivity,
//                            t.getVideoId());
//                }
//                mActivity.startActivity(intent);
//            }
//        });

        TextView title = (TextView) convertView.findViewById(R.id.list_item_trailer_title);
        title.setText(t.getTitle());

        return convertView;
    }

    public void releaseLoaders() {
        for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
            loader.release();
        }
    }

    private final class ThumbnailListener implements
            YouTubeThumbnailView.OnInitializedListener,
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(
                YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
            loader.setOnThumbnailLoadedListener(this);
            thumbnailViewToLoaderMap.put(view, loader);
            String videoId = (String) view.getTag();
            loader.setVideo(videoId);
        }

        @Override
        public void onInitializationFailure(
                YouTubeThumbnailView view, YouTubeInitializationResult loader) {
            view.setImageResource(R.drawable.placeholder);
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
            view.setImageResource(R.drawable.placeholder);
            Log.d("ThumbnailListener", "onThumbnailError (line 132): " + errorReason.toString());
        }
    }

}


