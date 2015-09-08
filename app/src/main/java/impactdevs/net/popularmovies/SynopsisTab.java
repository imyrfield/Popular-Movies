package impactdevs.net.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Ian on 8/17/2015.
 */
public class SynopsisTab extends Fragment {

    Bundle args;
    TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        args = getArguments();
        View v = inflater.inflate(R.layout.fragment_synopsis, container, false);
        mTextView = (TextView) v.findViewById(R.id.synopsis);

        mTextView.setText(args.getString("synopsis"));

        return v;
    }

    public void setNewText(String text){
        mTextView.setText(text);
    }
}
