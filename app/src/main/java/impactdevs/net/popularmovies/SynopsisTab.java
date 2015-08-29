package impactdevs.net.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
// Todo: If synopsis is too long, it gets cut off. Implement scrollview?

/**
 * Created by Ian on 8/17/2015.
 */
public class SynopsisTab extends Fragment {
    Bundle args = new Bundle();
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
        Log.d("SynopsisTab", " " + args.getString("synopsis"));
        Log.d("SynopsisTab", "onCreateView (line 32): ");

        return v;
    }

    public void setNewText(String text){
        mTextView.setText(text);
        Log.d("SynopsisTab", "setNewText (line 42): " + text);
    }
}
