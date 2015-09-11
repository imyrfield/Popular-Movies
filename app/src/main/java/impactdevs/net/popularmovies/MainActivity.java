package impactdevs.net.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MovieFragment.Callback,
        Utility.Callback {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
//            Log.d("MainActivity", "onCreate (line 26): Tablet mode");
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(),
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
//            Log.d("MainActivity", "onCreate (line 34): Cellphone mode");
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String id) {
        Bundle args = new Bundle();
        args.putString("id", id);
        Log.d("MainActivity", "onItemSelected, id: " + id);
        if (mTwoPane) {

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtras(args);
            startActivity(intent);
        }
    }

    @Override
    public void onMovieLoaded(String id) {

        Log.d("MainActivity", "onMovieLoaded (line 92): " + id);

        if (mTwoPane) {
//            Log.d("MainActivity", "onMovieLoaded (line 94): Tablet Mode");
            DetailFragment df = (DetailFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                Log.d("MainActivity", "onMovieLoaded (line 97): detail fragment NOT null");
                df.isFavorite(id);
            }
        } else {
//            Log.d("MainActivity", "onMovieLoaded (line 112): Cellphone mode");
        }
    }

    @Override
    public void trailerFetchCompleted() {
        if (mTwoPane) {
            DetailFragment df = (DetailFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                df.initializeShareIntent();
            }
        }
    }
}