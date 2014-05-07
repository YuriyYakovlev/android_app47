package org.m5.ui;

import java.util.ArrayList;
import java.util.List;

import org.m5.R;
import org.m5.ui.DishActivity;
import org.m5.ui.RecipesApplication;
import org.m5.ui.SearchActivity;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import org.m5.provider.RecipesContract.Dish;
import org.m5.util.UIUtils;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link Dish}, as requested
 * through {@link Intent#getData()}.
 */
public class DishActivity extends ListActivity implements AsyncQueryListener {
    private static String KEY = "a14ddbcb298ca1d";
    private AdView adView;
    private DishAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    private int mIdp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_dish);
        initOrnament();
        initTitle();

        mAdapter = new DishAdapter(this);
        setListAdapter(mAdapter);

        final Intent intent = getIntent();
        final Uri dishUri = intent.getData();
        
        mIdp = Integer.valueOf(Dish.getDishId(dishUri));
        // Start background query to load dish
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(dishUri, DishQuery.PROJECTION, null, null, Dish.DEFAULT_SORT);
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(cursor != null && mAdapter != null) {
        	startManagingCursor(cursor);
        	mAdapter.changeCursor(cursor);
            System.gc();
        }
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
        	Toast.makeText(DishActivity.this, getResources().getString(R.string.voice_inet_required), Toast.LENGTH_SHORT).show();
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
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            final Dialog result = new Dialog(this);
            result.setCancelable(true);
            result.setTitle(getString(R.string.voice_result));
			result.setContentView(R.layout.voice_recognition_dialog);
			ListView mList = (ListView) result.findViewById(R.id.results);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            mList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> list, View v, int position, long id) {
					final Intent intent = new Intent(DishActivity.this, SearchActivity.class);
					intent.putExtra(SearchManager.QUERY, matches.get(position));
			        startActivity(intent);
			        result.cancel();
				}
            });
            result.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Launch viewer for specific track
        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        Intent intent = null;
        String dishId = cursor.getString(DishQuery._ID);
        if(mIdp == 0) {
        	mIdp = Integer.valueOf(dishId);
        	mHandler.startQuery(Dish.buildDishUri(dishId), DishQuery.PROJECTION, null, null, Dish.DEFAULT_SORT);
        	return;
        } else {
        	intent = new Intent(Intent.ACTION_VIEW, Dish.buildRecipeUri(dishId));
        }
    	intent.putExtra(Intent.EXTRA_TITLE, cursor.getString(DishQuery.NAME));
    	startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Null out the group cursor. This will cause the group cursor and all of the child cursors to be closed.
        //mAdapter.changeCursor(null);
        mAdapter = null;
        mHandler = null;
    }
    
    /**
     * {@link CursorAdapter} that renders a {@link DishQuery}.
     */
    private class DishAdapter extends CursorAdapter {
        public DishAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_dish, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView name = (TextView) view.findViewById(android.R.id.text1);
            name.setText(cursor.getString(DishQuery.NAME));
            name.setTypeface(RecipesApplication.getInstance().getTypeface());

            int count = mIdp > 0 ? cursor.getInt(DishQuery.COUNT) : 0;
            if(count > 0) {
	            final TextView countView = (TextView) view.findViewById(android.R.id.text2);
	            countView.setText(""+count);
	            countView.setTypeface(RecipesApplication.getInstance().getTypeface());
            }
        }
    }

    /** {@link Dish} query parameters. */
    private interface DishQuery {
        String[] PROJECTION = {
                Dish._ID,
                Dish.NAME,
                Dish.COUNT
        };

        int _ID = 0;
        int NAME = 1;
        int COUNT = 2;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
		// Create the adView
        final LinearLayout layout = (LinearLayout)findViewById(R.id.AdView1);
	    /*AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
        alpha.setDuration(0); // Make animation instant
        alpha.setFillAfter(true); // Tell it to persist after the animation ends
        layout.startAnimation(alpha);*/
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean isPremium = preferences.getBoolean(AdActivity.REMOVE_ADS_KEY, false);
    	
        if(layout != null) {
        	if(isPremium || /*RecipesApplication.adClicked ||*/ getIntent().hasCategory(Intent.CATEGORY_TAB)) {
        		layout.setVisibility(View.GONE);
        	} else {
        		adView = new AdView(this, AdSize.BANNER, KEY);
        		layout.addView(adView);
            	adView.loadAd(new AdRequest());
            	adView.setAdListener(new AdListener() {
					@Override
					public void onDismissScreen(Ad arg0) {
						RecipesApplication.adClicked = true;
						layout.setVisibility(View.GONE);
					}

					@Override
					public void onLeaveApplication(Ad arg0) {
						RecipesApplication.adClicked = true;
						layout.setVisibility(View.GONE);
					}

					@Override
					public void onPresentScreen(Ad arg0) {
					}

					@Override
					public void onReceiveAd(Ad arg0) {
					}

					@Override
					public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
						// TODO Auto-generated method stub
						
					}
            	});
            	layout.setVisibility(View.VISIBLE);
        	}
        }
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.dish_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        TextView titleView = ((TextView) findViewById(R.id.title_text));
        titleView.setText(customTitle != null ? customTitle : getTitle());
        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }
        
}
