package org.m5.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesDatabase.Tables;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import org.m5.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link org.m5.provider.RecipesContract.Recipe}, as requested
 * through {@link Intent#getData()}.
 */
public class RecipeActivity extends ListActivity implements AsyncQueryListener, DialogInterface.OnClickListener {
    public static CursorAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    private Uri mRecipeUri;
    private Builder alert;
    //private static String KEY = "a14ddbcb298ca1d";
    //private AdView adView;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		
        if (!getIntent().hasCategory(Intent.CATEGORY_TAB)) {
            setContentView(R.layout.activity_recipe);
        } else {
            setContentView(R.layout.activity_recipe_content);
        }
        
        initOrnament();
        initTitle();
        
        final Intent intent = getIntent();
        mRecipeUri = intent.getData();

        if(Recipe.isStarredUri(mRecipeUri)) {
        	findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
        }

        mAdapter = new RecipeAdapter(this);
        setListAdapter(mAdapter);
        
        String[] projection;
        projection = RecipeQuery.PROJECTION;
        
        // Start background query to load sessions
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(mRecipeUri, projection, Recipe.DEFAULT_SORT);
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(mAdapter != null && cursor != null) {
	    	startManagingCursor(cursor);
	        mAdapter.changeCursor(cursor);
	
	        TextView empty = (TextView) findViewById(android.R.id.empty);
	        if(empty != null) {
	        	if(cursor.getCount()<=0) {
	        		if(Recipe.isSearchUri(mRecipeUri)) {
	        			empty.setText(getResources().getString(R.string.search_not_found));
	        			return;
	        		} else if(Recipe.isStarredUri(mRecipeUri)) {
	        			empty.setText(getResources().getString(R.string.starred_not_found));
	        			return;
	        		}
	        	}
	        	empty.setText(null);
	        }
	        System.gc();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if(adView != null) {
        	adView.destroy();
        }*/
        // Null out the group cursor. This will cause the group cursor and all of the child cursors to be closed.
        //if(mAdapter != null) {
        //	try { mAdapter.changeCursor(null); } catch(Exception e) {}
        	mAdapter = null;
        //}
        mHandler = null;
    }

    @Override
    public void onBackPressed() {
    }

    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(mAdapter != null) {
	    	final Cursor cursor = (Cursor)mAdapter.getItem(position);
	        final String recipeId = cursor.getString(cursor.getColumnIndex(Recipe._ID));
	        final Uri recipeUri = Recipe.buildRecipeUri(recipeId);
	        final Intent intent = new Intent(Intent.ACTION_VIEW, recipeUri);
	        intent.putExtra("position", position);
	        startActivity(intent);
        }
    }

    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
    	finish();
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
        	Toast.makeText(RecipeActivity.this, getResources().getString(R.string.voice_inet_required), Toast.LENGTH_SHORT).show();
        } else if(activities.size() != 0) {
        	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
	        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_description));
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru");
	        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onClick(DialogInterface arg0, int arg1) {
        if(mAdapter != null && mAdapter.getCursor() != null) {
        	final ContentValues values = new ContentValues();
            values.put(Recipe.STARRED, 0);

        	if(!mAdapter.getCursor().isClosed() && mAdapter.getCursor().moveToFirst()) {
		        do {
		        	final int id = mAdapter.getCursor().getInt(RecipeQuery._ID);
		        	mHandler.startUpdate(Recipe.buildRecipeUri(""+id), values); 	
		        } while(mAdapter.getCursor().moveToNext());
	        }
		    try {
		    	mAdapter.getCursor().requery();
		    } catch(Exception e) {
		    	Log.w(this.getClass().getSimpleName(), e.toString());
		    }
        }
   }
    
    /** Handle "delete" title-bar action. */
    public void onDeleteClick(View v) {
    	alert = new AlertDialog.Builder(this)
	        .setTitle(R.string.title_starred)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setMessage(R.string.delete_confirm)
	        .setNegativeButton(android.R.string.cancel, null)
	        .setPositiveButton(android.R.string.ok, this)
	        .setCancelable(false);
    	alert.show(); 
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
					final Intent intent = new Intent(RecipeActivity.this, SearchActivity.class);
					intent.putExtra(SearchManager.QUERY, matches.get(position));
			        startActivity(intent);
			        result.cancel();
				}
            });
            result.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    
    /**
     * {@link CursorAdapter}
     */
    private class RecipeAdapter extends CursorAdapter /*implements SectionIndexer*/ {
    	//AlphabetIndexer alphaIndexer;
    	
    	public RecipeAdapter(Context context) {
            super(context, null);
            //alphaIndexer = new AlphabetIndexer(null, RecipeQuery.NAME, " АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ");
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //alphaIndexer.setCursor(cursor);
        	return getLayoutInflater().inflate(R.layout.list_item_recipe, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView titleView = (TextView) view.findViewById(R.id.recipe_title);
            final TextView subtitleView = (TextView) view.findViewById(R.id.recipe_subtitle);
            final CheckBox starButton = (CheckBox) view.findViewById(R.id.star_button);
            final View icon = (View) view.findViewById(R.id.icon1);

            titleView.setText(cursor.getString(RecipeQuery.NAME));
            titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
            
            if(cursor.getInt(RecipeQuery.STARRED) != 0) {
            	starButton.setChecked(true);
            } else {
            	starButton.setVisibility(View.GONE);
            }
            
            final int id = cursor.getInt(RecipeQuery._ID);
            if(id > 0){
            	UIUtils.fetchDrawableOnThread(id, icon);
            	icon.setVisibility(View.VISIBLE);
            } else {
            	icon.setVisibility(View.GONE);
            }
            
        	String make = ""; //cursor.getString(RecipeQuery.MAKE);
            byte[] input = cursor.getBlob(RecipeQuery.STEPS);
            Inflater decompresser = new Inflater();
            try {
            	decompresser.setInput(input, 0, input.length);
    	        byte[] result = new byte[input.length*2];
    	        int resultLength = decompresser.inflate(result);
    	        make = new String(result, 0, resultLength, "UTF-8");
    		
                if (!TextUtils.isEmpty(make)) {
                	int l = make.length();
                	if(l > 50) l = 50;
                	int i = make.indexOf(".", l);
                	if(i > 0 && i+1 < l) {
                		subtitleView.setText(make.substring(0, i+1).replaceAll("\\$",""));		
                	}  else { 
                		subtitleView.setText(make.substring(0, l).replaceAll("\\$",""));
                	}
                }
            } catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    
		/*@Override
		public int getPositionForSection(int section) {
			return alphaIndexer.getPositionForSection(section); //use the indexer
		}

		@Override
		public int getSectionForPosition(int position) {
            return 0;
            return alphaIndexer.getSectionForPosition(position); //use the indexer
		}

		@Override
		public Object[] getSections() {
			return alphaIndexer.getSections(); //use the indexer
		}*/
    }
    
    /** {@link RecipeQuery} query parameters. */
    private interface RecipeQuery {
        String[] PROJECTION = {
                Tables.RECIPE + "." + BaseColumns._ID,
                Tables.RECIPE + "." + Recipe.NAME,
                Tables.RECIPE + "." + Recipe.STEPS,
                Tables.RECIPE + "." + Recipe.STARRED
        };

        int _ID=0;
        int NAME=1;
        int STEPS=2;
        int STARRED=3;
    }
    
    //private static String KEY = "a14ddbcb298ca1d";
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
    	if(ornament != null) {
    		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    	}
		// Create the adView
        /*final LinearLayout layout = (LinearLayout)findViewById(R.id.AdView1);
	    AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
        alpha.setDuration(0); // Make animation instant
        alpha.setFillAfter(true); // Tell it to persist after the animation ends
        layout.startAnimation(alpha);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean isPremium = preferences.getBoolean(AdActivity.REMOVE_ADS_KEY, false);
    	
        if(layout != null) {
        	if(isPremium || RecipesApplication.adClicked || getIntent().hasCategory(Intent.CATEGORY_TAB)) {
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
        }*/
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.recipe_title);
		if(title != null) {
			title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());
	
			final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
	        TextView titleView = ((TextView) findViewById(R.id.title_text));
	        titleView.setText(customTitle != null ? customTitle : getTitle());
	        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
		}
    }

}
