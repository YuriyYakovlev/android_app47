package org.m5.ui;

import java.util.ArrayList;
import java.util.List;

import org.m5.provider.RecipesContract.Kitchen;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;

import org.m5.R;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link Kitchen}, as requested
 * through {@link Intent#getData()}.
 */
public class KitchenActivity extends ListActivity implements AsyncQueryListener, OnItemClickListener {
    private KitchenAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    private ArrayList<String> matches;
    private Dialog result;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_kitchen);
        initOrnament();
        initTitle();

        mAdapter = new KitchenAdapter(this);
        setListAdapter(mAdapter);
        
        final Intent intent = getIntent();
        final Uri kitchenUri = intent.getData();
        
        // Filter our tracks query to only include those with valid results
        String[] projection = KitchenQuery.PROJECTION;
        String selection = Kitchen.KITCHEN + " > 0";

        // Start background query to load dish
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(kitchenUri, projection, selection, null, Kitchen.DEFAULT_SORT);
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(cursor != null && mAdapter != null) {
	    	startManagingCursor(cursor);
	        mAdapter.changeCursor(cursor);
	        System.gc();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Null out the group cursor. This will cause the group cursor and all of the child cursors to be closed.
        //mAdapter.changeCursor(null);
        mAdapter = null;
        mHandler = null;
        matches = null;
        result = null;
    }

    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
    	finish();
    }

    /** Handle "refresh" title-bar action. */
    public void onRefreshClick(View v) {
    }

    /** Handle "search" title-bar action. */
    public void onSearchClick(View v) {
        UIUtils.goSearch(this);
    }

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 7;
    /** Handle "voice" title-bar action. */
    public void onVoiceClick(View v) {
    	PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if(!RecipesApplication.getInstance().isOnline(true)) {
        	Toast.makeText(KitchenActivity.this, getResources().getString(R.string.voice_inet_required), Toast.LENGTH_SHORT).show();
        } else if(activities.size() != 0) {
        	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_description));
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru");
	        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }
    
    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            result = new Dialog(this);
            result.setCancelable(true);
            result.setTitle(getString(R.string.voice_result));
			result.setContentView(R.layout.voice_recognition_dialog);
			ListView mList = (ListView) result.findViewById(R.id.results);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            mList.setOnItemClickListener(this);
            result.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
	@Override
	public void onItemClick(AdapterView<?> list, View v, int position, long id) {
		final Intent intent = new Intent(KitchenActivity.this, SearchActivity.class);
		intent.putExtra(SearchManager.QUERY, matches.get(position));
        startActivity(intent);
        result.cancel();
	}
    
    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final Cursor cursor = (Cursor)mAdapter.getItem(position);
        Intent intent = null;
        String kitchenId = cursor.getString(KitchenQuery._ID);
      	intent = new Intent(Intent.ACTION_VIEW, Kitchen.buildRecipeUri(kitchenId));
        intent.putExtra(Intent.EXTRA_TITLE, cursor.getString(KitchenQuery.KITCHEN));
        startActivity(intent);
    }

    /**
     * {@link CursorAdapter} that renders a {@link DishQuery}.
     */
    private class KitchenAdapter extends CursorAdapter {
        public KitchenAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_kitchen, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView name = (TextView) view.findViewById(android.R.id.text1);
            name.setText(cursor.getString(KitchenQuery.KITCHEN));
            name.setTypeface(RecipesApplication.getInstance().getTypeface());

            int count = cursor.getInt(KitchenQuery.COUNT);
            if(count > 0) {
	            final TextView countView = (TextView) view.findViewById(android.R.id.text2);
	            countView.setText(""+count);
	            countView.setTypeface(RecipesApplication.getInstance().getTypeface());
            }
        }
    }

    /** {@link Kitchen} query parameters. */
    private interface KitchenQuery {
        String[] PROJECTION = {
                Kitchen._ID,
                Kitchen.KITCHEN,
                Kitchen.COUNT
        };
        int _ID = 0;
        int KITCHEN = 1;
        int COUNT = 2;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.kitchen_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        TextView titleView = ((TextView) findViewById(R.id.title_text));
        titleView.setText(customTitle != null ? customTitle : getTitle());
        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }
    
}
