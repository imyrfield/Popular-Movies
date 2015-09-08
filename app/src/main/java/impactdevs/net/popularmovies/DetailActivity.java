package impactdevs.net.popularmovies;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * Created by Ian on 7/25/2015.
 */
public class DetailActivity extends AppCompatActivity implements Utility.Callback {

    private DetailFragment mDetailFragment;
    private static String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Prevents landscape orientation on phones.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        mDetailFragment = (DetailFragment) fragmentManager.findFragmentByTag
//                (FRAGMENT_DETAIL_TAG);
//        if (mDetailFragment == null) {
//            Log.e("DetailActivity", "onCreate (line 53): mDetailFragment is NULL");
//            mDetailFragment = new DetailFragment();
//        }
//        fragmentManager.beginTransaction()
//                .replace(R.id.container_detail, mDetailFragment, FRAGMENT_DETAIL_TAG)
//                .commit();

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putAll(getIntent().getExtras());
            Log.d("DetailActivity", "onCreate (line 40): " + arguments.toString());
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void trailerFetchCompleted() {
        DetailFragment df = (DetailFragment) getSupportFragmentManager()
                .findFragmentByTag(DETAILFRAGMENT_TAG);

            df.initializeShareIntent();

    }
}
