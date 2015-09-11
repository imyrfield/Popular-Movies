package impactdevs.net.popularmovies;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by Ian on 7/25/2015.
 */
public class DetailActivity extends AppCompatActivity implements Utility.Callback {

    private static String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Prevents landscape orientation on phones. Not because it doesn't save the
        // instance but because it doesn't look good aesthetically.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putAll(getIntent().getExtras());
//            Log.d("DetailActivity", "onCreate (line 40): " + arguments.toString());
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
