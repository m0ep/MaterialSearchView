package de.florianm.android.materialsearchview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import de.florianm.android.materialsearchview.MaterialSearchView;

public class MainActivity extends AppCompatActivity implements MaterialSearchView.OnSearchViewListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        searchView = (MaterialSearchView) findViewById(R.id.searchView);
        searchView.setSearchViewListener(this);
        searchView.setSuggestionEntries(new String[]{"Käse","Vegan","bla", "Blub"});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.manu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_search:
                searchView.showSearchView();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MaterialSearchView.REQUEST_VOICE_INPUT && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, true);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchViewVisible()) {
            searchView.hideSearchView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onQueryTextChanged(CharSequence text) {
        Log.d(TAG, "Query changed to: " + text);
    }

    @Override
    public boolean onSubmitQuery(CharSequence text) {
        Toast.makeText(this, "Query: " + text, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onShowSearchView() {
        Log.d(TAG, "SearchView shown");
    }

    @Override
    public void onHideSearchView() {
        Log.d(TAG, "SearchView shown");
    }
}
