package impactdevs.net.popularmovies;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by Ian on 7/25/2015.
 */
public class DetailActivity extends AppCompatActivity{

    private DetailFragment mDetailFragment;
    private static String FRAGMENT_DETAIL_TAG = "DetailFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DetailFragment())
                    .commit();
        }
    }
}
