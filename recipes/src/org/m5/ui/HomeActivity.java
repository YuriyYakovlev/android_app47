package org.m5.ui;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.m5.provider.RecipesContract.E;
import org.m5.provider.RecipesContract.Kitchen;
import org.m5.provider.RecipesContract.Profile;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.RecipeProducts;
import org.m5.provider.RecipesContract.Reference;
import org.m5.provider.RecipesContract.Units;
import org.m5.social.CommentsService;
import org.m5.ui.qa.ActionItem;
import org.m5.ui.qa.QuickAction;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import org.m5.R;

//import com.loopme.LoopMe;
//import com.loopme.widget.LoopMeExitPopup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * Front-door {@link Activity} that displays high-level features the recipes
 * application offers to users.
 */
public class HomeActivity extends Activity implements AsyncQueryListener {
	private String mROTDId;
    private static WeakReference<ProgressBar> mProgress;
    private static WeakReference<TextView> mStatus;
    public static String SERVER_URL;
	private static Handler mHandler;
    private NotifyingAsyncQueryHandler mQueryHandler;
    private ListView mGuessList; 
    private ActionItem eAction;
    private ActionItem unitsAction;
    private ActionItem commentsAction;
    private ActionItem referenceAction;
    private ActionItem profileAction;
    private QuickAction qa;
    private static String progressTitle;
    private Random random;
	private Button btnDish;
	private Button btnKitchen;
	private Button btnStarred;
	private Button btnBasket;
	protected CharSequence[] _options = {"English", "Русский"};
	protected boolean[] _selections =  {true, false};
	
	
	      
    public static void updateProgress(final String status) {
    	if(mProgress != null && mStatus != null) {	
    		mHandler.post(new Runnable() {	
    			public void run() {	
    				ProgressBar progress = mProgress.get();
    				if(progress != null) progress.incrementProgressBy(1);
    				
    				TextView view = mStatus.get();
    				if(view != null) view.setText(status);
    			} 
    		}); 
    	}
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		mHandler = new Handler();
        setContentView(R.layout.activity_home);
        
        
    	
        initOrnament();
        initTitle();
        initButtons();
        initRecipeOfTheDay();
        onRefreshClick(null);
        
        boolean firstRun = L10N.getPreferences().getBoolean(Config.PARAM_FIRST_RUN, true);
        if(firstRun) {
        	//showDialog(0);
            SharedPreferences.Editor editor = L10N.getPreferences().edit();
        	editor.putBoolean(Config.PARAM_FIRST_RUN, false);
        	editor.commit();
        }
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
        	.setTitle(getResources().getString(R.string.select_language))
        	.setSingleChoiceItems(_options, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    _selections[position] = true;
                }})
        	.setPositiveButton(getResources().getString(R.string.ok), new DialogButtonClickHandler())
        	.create();
	}

	public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int clicked) {
			switch(clicked) {
				case DialogInterface.BUTTON_POSITIVE:
					String locale = "en";
					if(_selections[1]) {
						locale = "ru";
					}
					if(!locale.equals(L10N.getLocale())) {
						L10N.changeLocale(locale);
					    finish();
					    startActivity(getIntent());
					} 
					break;
			}
		}
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
        mQueryHandler = null;
        qa = null;
        random = null;
    	btnDish = null;
    	btnKitchen = null;
    	btnStarred = null;
    	btnBasket = null;

        if(mGuessList != null) {
        	mGuessList.setAdapter(null);
        	mGuessList.setOnItemClickListener(null);
        	mGuessList  = null;
        }
        
        if(unitsAction != null) {
        	unitsAction.setOnClickListener(null);
        	unitsAction = null;
        }

        if(eAction != null) {
        	eAction.setOnClickListener(null);
        	eAction = null;
        }

        if(commentsAction != null) {
        	commentsAction.setOnClickListener(null);
        	commentsAction = null;
        }        
    }
    
    private NotifyingAsyncQueryHandler getQueryHandler() {
    	if(mQueryHandler == null) {
    		 mQueryHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
    	}
    	return mQueryHandler;
    }

    /** Handle "refresh" title-bar action. */
    public void onRefreshClick(View v) {
    	getQueryHandler().startQuery(Recipe.CONTENT_URI, RecipeQuery.PROJECTION);
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
        	Toast.makeText(HomeActivity.this, getResources().getString(R.string.voice_inet_required), Toast.LENGTH_SHORT).show();
        } else if(activities.size() != 0) {
        	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
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
			mGuessList = (ListView) result.findViewById(R.id.results);
            mGuessList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            mGuessList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> list, View v, int position, long id) {
					final Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
					intent.putExtra(SearchManager.QUERY, matches.get(position));
			        startActivity(intent);
			        result.cancel();
				}
            });
            result.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /** Handle "basket" action. */
    public void onBasketClick(View v) {
    	final Intent intent = new Intent(Intent.ACTION_VIEW, RecipeProducts.CONTENT_PRODUCTS_URI);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_basket));
        startActivity(intent);
    }

    /** Handle "dish" action. */
    public void onDishClick(View v) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Dish.buildDishUri("0"), HomeActivity.this,	DishActivity.class);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_dish));
        startActivity(intent);
    }

    /** Handle "starred" action. */
    public void onStarredClick(View v) {
    	Intent intent = new Intent(Intent.ACTION_VIEW, Recipe.CONTENT_STARRED_URI);
    	intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_starred));
        startActivity(intent);
    }

    /** Handle "kitchen" action. */
    public void onKitchenClick(View v) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Kitchen.CONTENT_URI);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_kitchen));
        startActivity(intent);
    }

    /** Handle "recipe of the day" action. */
    public void onRecipeOfTheDayClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Recipe.buildRecipeUri(mROTDId)));
    }

    private ActionItem getUnitsAction() {
		if(unitsAction == null) {
			unitsAction = new ActionItem();
			
			SoftReference<Drawable> icUnitsAction = RecipesApplication.getInstance().getDrawableHashMap().get("icUnitsAction");
			if(icUnitsAction == null) {
				icUnitsAction = new SoftReference<Drawable>(getResources().getDrawable(R.drawable.scales));   
				RecipesApplication.getInstance().getDrawableHashMap().put("icUnitsAction", icUnitsAction);   
			}
			unitsAction.setIcon(icUnitsAction.get());

			unitsAction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Units.buildUnitsUri("0"));
			        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_units));
			        startActivity(intent);
			        qa.dismiss();
				}
			});
		}
		return unitsAction;
    }

    private ActionItem getEAction() {
		if(eAction == null) {
			eAction = new ActionItem();
			
			SoftReference<Drawable> icEAction = RecipesApplication.getInstance().getDrawableHashMap().get("icEAction");
			if(icEAction == null) {
				icEAction = new SoftReference<Drawable>(getResources().getDrawable(R.drawable.e));   
				RecipesApplication.getInstance().getDrawableHashMap().put("icEAction", icEAction);   
			}
			eAction.setIcon(icEAction.get());

			eAction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, E.buildEUri("1"));
			        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_e));
			        startActivity(intent);
			        qa.dismiss();
				}
			});
		}
		return eAction;
    }
    
    private ActionItem getReferenceAction() {
		if(referenceAction == null) {
			referenceAction = new ActionItem();
			
			SoftReference<Drawable> icEAction = RecipesApplication.getInstance().getDrawableHashMap().get("icReferenceAction");
			if(icEAction == null) {
				icEAction = new SoftReference<Drawable>(getResources().getDrawable(R.drawable.reference));   
				RecipesApplication.getInstance().getDrawableHashMap().put("icReferenceAction", icEAction);   
			}
			referenceAction.setIcon(icEAction.get());

			referenceAction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Reference.CONTENT_URI);
			        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_reference));
			        startActivity(intent);
			        qa.dismiss();
				}
			});
		}
		return referenceAction;
    }
    
    private ActionItem getProfileAction() {
		if(profileAction == null) {
			profileAction = new ActionItem();
			
			SoftReference<Drawable> icEAction = RecipesApplication.getInstance().getDrawableHashMap().get("icProfileAction");
			if(icEAction == null) {
				icEAction = new SoftReference<Drawable>(getResources().getDrawable(R.drawable.profile));   
				RecipesApplication.getInstance().getDrawableHashMap().put("icProfileAction", icEAction);   
			}
			profileAction.setIcon(icEAction.get());

			profileAction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Profile.CONTENT_URI);
			        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_profile));
			        startActivity(intent);
			        qa.dismiss();
				}
			});
		}
		return profileAction;
    }
    
    private ActionItem getCommentsAction() {
		if(commentsAction == null) {
			commentsAction = new ActionItem();
			
			SoftReference<Drawable> icCommentsAction = RecipesApplication.getInstance().getDrawableHashMap().get("icCommentsAction");
			if(icCommentsAction == null) {
				icCommentsAction = new SoftReference<Drawable>(getResources().getDrawable(R.drawable.chat));   
				RecipesApplication.getInstance().getDrawableHashMap().put("icCommentsAction", icCommentsAction);   
			}
			commentsAction.setIcon(icCommentsAction.get());
			commentsAction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(HomeActivity.this, CommentsActivity.class);
			    	CommentsService.recipeId = -1;
			        startActivity(intent);
			        qa.dismiss();
				}
			});
		}
		return commentsAction;
    }

    /** Handle "more" action. */
    public void onMoreClick(View v) {
		qa = new QuickAction(v);
		
		qa.addActionItem(getCommentsAction());
		qa.addActionItem(getUnitsAction());
		qa.addActionItem(getEAction());
		qa.addActionItem(getReferenceAction());
		qa.addActionItem(getProfileAction());
		qa.setAnimStyle(QuickAction.ANIM_AUTO);
		
		qa.show();
    }
    
    private void initRecipeOfTheDay() {
		View recipeOfTheDay = findViewById(R.id.recipe_of_the_day);
    	if(recipeOfTheDay != null) {
			((TextView) recipeOfTheDay.findViewById(R.id.recipe_of_the_day_title)).setTypeface(RecipesApplication.getInstance().getTypeface());
	    	if(progressTitle == null) {
                progressTitle = this.getResources().getString(R.string.update);
            }
	        ((TextView) recipeOfTheDay.findViewById(R.id.recipe_of_the_day_title)).setText(progressTitle);
		    progressTitle = this.getResources().getString(R.string.recipe_of_the_day_title);
		    
		    ProgressBar progress = (ProgressBar) recipeOfTheDay.findViewById(R.id.progressbar);
			mProgress = new WeakReference<ProgressBar>(progress);
		    
		    TextView status = (TextView) recipeOfTheDay.findViewById(R.id.recipe_of_the_day_subtitle);
			mStatus = new WeakReference<TextView>(status);
        }
    }
    
    private Random getRandom() {
    	if(random == null) {
    		random = new Random();
    	}
    	return random;
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
    	if(cursor != null) {
	    	try {
	            int count = cursor.getCount();
	            if(count > 0) {
		            int j = Math.abs(getRandom().nextInt()) % count;
		        	if(j == 0) {
                        j = 1;
                    }
		            if(!cursor.move(j)) {
		                return;
		            }
		            mROTDId = cursor.getString(RecipeQuery._ID);
		            
		            // landscape orientation
		            if(findViewById(R.id.recipe_of_the_day) == null) {
                        return;
                    }
		            L10N.init();
		            ((TextView) findViewById(R.id.recipe_of_the_day_title)).setText(this.getResources().getString(R.string.recipe_of_the_day_title));
			        ((TextView) findViewById(R.id.recipe_of_the_day_subtitle)).setText(cursor.getString(RecipeQuery.NAME));
		            ((ProgressBar) findViewById(R.id.progressbar)).setVisibility(View.GONE);
	            }
	        } finally {
	        	cursor.close();
	        }
    	}
    }

    private interface RecipeQuery {
        String[] PROJECTION = {
                Recipe._ID,
                Recipe.NAME
        };
        int _ID = 0;
        int NAME = 1;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.home_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());
    }
 
    private void initButtons() {
    	btnDish = (Button) findViewById(R.id.home_btn_dish);
		btnKitchen = (Button) findViewById(R.id.home_btn_kitchen);
		btnStarred = (Button) findViewById(R.id.home_btn_starred);
		btnBasket = (Button) findViewById(R.id.home_btn_basket);
		
		btnDish.setTypeface(RecipesApplication.getInstance().getTypeface());
		btnKitchen.setTypeface(RecipesApplication.getInstance().getTypeface());
		btnStarred.setTypeface(RecipesApplication.getInstance().getTypeface());
		btnBasket.setTypeface(RecipesApplication.getInstance().getTypeface());
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
        return true;
	}
	
    private String APP_MARKET_URL = "market://details?id=org.m5.plus"; 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.russian) {
			L10N.changeLocale("ru");
			finish();
			startActivity(getIntent());
			return true;
		} else if (itemId == R.id.english) {
			L10N.changeLocale("en");
			finish();
			startActivity(getIntent());
			return true;
		} else if (itemId == R.id.remove_ads) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(APP_MARKET_URL));
			startActivity(intent);
		}
		return true;
	}
	
    /*
    @Override
    public void onBackPressed() {
        LoopMeExitPopup exitPopup = new LoopMeExitPopup(HomeActivity.this);
        exitPopup.mayBeShowExitPopup(HomeActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoopMe loopMe = LoopMe.getInstance(this);
        loopMe.preloadInbox();
    }
    */
    
}
