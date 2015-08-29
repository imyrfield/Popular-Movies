package impactdevs.net.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ian on 8/22/2015.
 */
public class ReviewListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Activity mActivity;
    private List<Review> mReviews;

    public ReviewListAdapter(Activity activity, List<Review> reviews){
        this.mReviews = reviews;
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return mReviews.size();
    }

    @Override
    public Object getItem(int position) {
        return mReviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (mInflater == null) {
            mInflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_review, null);
        }

        TextView author = (TextView) convertView.findViewById(R.id.list_item_review_author);
        TextView comment = (TextView) convertView.findViewById(R.id.list_item_review_comment);

        Review c = mReviews.get(position);

        author.setText(c.getAuthor());
        comment.setText(c.getComment());

        return convertView;
    }
}
