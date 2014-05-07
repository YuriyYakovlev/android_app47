package org.m5.ui;

import java.util.ArrayList;
import java.util.List;

import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.ProductsColumns;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.RecipeProducts;
import org.m5.provider.RecipesDatabase.Tables;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;

import org.m5.R;

import android.app.Dialog;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 */
public class SearchActivity extends TabActivity implements AsyncQueryListener {
    public static final String TAG_RECIPES = "recipes";
    private String mQuery;
    private NotifyingAsyncQueryHandler mHandler;
    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView edtSearch;
    private AutoCompleteTextView edtAdd;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_search);
        initOrnament();
        initTitle();
        onNewIntent(getIntent());
        
        edtSearch = ((AutoCompleteTextView) findViewById(R.id.edtSearch)); 
        edtAdd = (AutoCompleteTextView) findViewById(R.id.edtAdd);
        
    	Uri mProductsUri = RecipeProducts.buildProductsUri("-1");
    	// Start background query to load products
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
    	mHandler.startQuery(ProductsQuery._PRODUCTS, mProductsUri, ProductsQuery.PROJECTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
        adapter = null;
        edtAdd = null;
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        mQuery = intent.getStringExtra(SearchManager.QUERY);
        CharSequence title = null;
        if(mQuery != null) {
        	title = getString(R.string.title_search_query, mQuery);
        } else {
        	title = getString(R.string.title_search);
        }

        setTitle(title);
        ((TextView) findViewById(R.id.title_text)).setText(title);

        final TabHost host = getTabHost();
        host.setCurrentTab(0);
        host.clearAllTabs();

        setupRecipesTab();
    }
    
    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if(cursor != null) {
	    	if(token == ProductsQuery._PRODUCTS) {
				try {
					int count = cursor.getCount();
			    	if(count > 0) {
						String[] array = new String[count];
						int counter = 0;
						while(cursor.moveToNext()) {
			            	array[counter++] = cursor.getString(ProductsQuery.NAME);
						}
						adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, array);
				        //edtSearch.setAdapter(adapter);
				        edtAdd.setAdapter(adapter);
			    	}
				} finally {
	        		cursor.close();
	            }
	    	}
		}
    }

    public void onHomeClick(View v) {
    	finish();
    }

    public void onSearchClick(View v) {
    	String query = edtSearch.getText().toString();
    	if(!TextUtils.isEmpty(query)) {
	    	final Intent intent = new Intent(this, SearchActivity.class);
			intent.putExtra(SearchManager.QUERY, query);
	        startActivity(intent);
	        
	    	try {
	    		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
	    		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    		} catch(Exception e) { }
    	}
    }

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 7;
    /** Handle "voice" title-bar action. */
    public void onVoiceClick(View v) {
    	PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if(!RecipesApplication.getInstance().isOnline(true)) {
        	Toast.makeText(SearchActivity.this, getResources().getString(R.string.voice_inet_required), Toast.LENGTH_SHORT).show();
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
					final Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
					intent.putExtra(SearchManager.QUERY, matches.get(position));
			        startActivity(intent);
			        result.cancel();
				}
            });
            result.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final static String P = " и ";
    public void onAddClick(View v) {
    	String product = edtAdd.getText().toString();
    	if(!TextUtils.isEmpty(product)) {
    		String query = edtSearch.getText().toString();
    		if(!TextUtils.isEmpty(query)) {
    			query  = query + P + product;
    		} else {
    			query = product;
    		}
    		edtSearch.setText(query);
    		edtSearch.requestFocus();
    		edtAdd.setText("");
    		
	    	try {
	    		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
	    		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    		} catch(Exception e) { }
    	}
    }
    
    /** Build and add "recipes" tab. */
    private void setupRecipesTab() {
        final TabHost host = getTabHost();
        Uri recipeUri = null;
        if(mQuery != null) {
        	recipeUri = Recipe.buildSearchUri(Uri.encode(mQuery));
        } else {
        	recipeUri = Dish.buildRecipeUri("0");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, recipeUri);
        intent.addCategory(Intent.CATEGORY_TAB);

        // Recipe content comes from reused activity
        host.addTab(host.newTabSpec(TAG_RECIPES).setIndicator(buildIndicator(R.string.search_recipes)).setContent(intent));
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested
     * string resource as its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
        indicator.setText(textRes);
        indicator.setTypeface(RecipesApplication.getInstance().getTypeface());
        return indicator;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.search_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        TextView titleView = ((TextView) findViewById(R.id.title_text));
        titleView.setText(customTitle != null ? customTitle : getTitle());
        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }

    private interface ProductsQuery {
    	int _PRODUCTS = 0x2;
        String[] PROJECTION = {
        		Tables.PRODUCTS + "." + ProductsColumns._ID,
        		Tables.PRODUCTS + "." + ProductsColumns.NAME
        };
        int NAME=1;
    }
    
}
