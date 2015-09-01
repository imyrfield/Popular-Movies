package impactdevs.net.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private String lastSort;
    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lastSort = Utility.getSortPref(this);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(),
                                DETAILFRAGMENT_TAG)
                        .commit();
            }
        }else{
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
    protected void onResume() {
        super.onResume();
        String sort = Utility.getSortPref(this);
        if (sort != null && !sort.equals(lastSort)) {
            MovieFragment mf = (MovieFragment) getSupportFragmentManager().findFragmentById
                    (R.id.fragment_main);
            if (mf != null) {
                mf.fetchData(sort, null);
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if(null != df){
                //TODO: onSortChanged (restart loader)
            }
            lastSort = sort;
        }
    }

    //Todo:
//    public void onItemSelected(Uri contentUri){
//        if(mTwoPane){
//            Bundle args = new Bundle();
//            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
//
//            DetailFragment fragment = new DetailFragment();
//            fragment.setArguments(args);
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.movie_detail_container, fragment,  DETAILFRAGMENT_TAG)
//                    .commit();
//        }else{
//            Intent intent = new Intent(this, DetailActivity.class)
//                    .setData();
//            startActivity(intent);
//        }
//    }
}