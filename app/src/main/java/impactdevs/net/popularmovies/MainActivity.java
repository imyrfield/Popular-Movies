package impactdevs.net.popularmovies;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private MovieFragment mMovieFragment;
    private static String FRAGMENT_TAG = "MovieFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        mMovieFragment = (MovieFragment) fragmentManager.findFragmentByTag
                (FRAGMENT_TAG);
        if (mMovieFragment == null) {
            Log.e("MainActivity", "onCreate (line 24): mMovieFragment is NULL");
            mMovieFragment = new MovieFragment();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, mMovieFragment, FRAGMENT_TAG)
                .commit();
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if()
//        MovieFragment mf = (MovieFragment) getSupportFragmentManager().findFragmentById
//                (R.id.fragment_main);
//    }
}