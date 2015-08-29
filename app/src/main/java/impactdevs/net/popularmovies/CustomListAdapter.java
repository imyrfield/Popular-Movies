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
 * Created by Ian on 8/21/2015.
 */
public class CustomListAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private Activity mActivity;
    private List<String> mList;

    public CustomListAdapter(Activity activity, List<String> reviewItems){
            this.mList = reviewItems;
            this.mActivity = activity;
        }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if(inflater == null) {
            inflater = (LayoutInflater) mActivity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
        }

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_trailer, null);
        }

        TextView textView = (TextView) view.findViewById(R.id.list_item_trailer_title);
        textView.setText(mList.get(position));

        return view;
    }
}
