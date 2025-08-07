package org.m5.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import org.m5.R;

import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.ItemsColumns;
import org.m5.provider.RecipesContract.ProductsColumns;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.RecipeProductColumns;
import org.m5.provider.RecipesContract.RecipeProducts;
import org.m5.provider.RecipesDatabase.Tables;
import org.m5.social.CommentsService;
import org.m5.util.FractionalTouchDelegate;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link Activity} that displays details about a specific
 * {@link Recipe#_ID}, as requested through {@link Intent#getData()}.
 */
public class RecipeDetailActivity extends Activity implements AsyncQueryListener, OnCheckedChangeListener, OnClickListener, OnLongClickListener {
    private Uri mRecipeUri;
    private String mTitleString;
    private NotifyingAsyncQueryHandler mHandler;
    private SharedPreferences prefs;
    private TextView mTimer;
    private static float fontSize = 16f;
    private boolean mHasSummaryContent;
    static class RecipeItem {
    	String name, steps;
    	private List<String> products;
    	
    	List<String> getProducts() {
    		if(products == null) {
    			products = new ArrayList<String>();
    		}
    		return products;
    	}
    }
    private Count cdt;
    private MediaPlayer player;
    private RecipeItem mRecipe;
    private static String M = "make";
    private static String P = "products";
    private static String T = " - ";
    private static String E = " ";
    private static String BR = "<br>";
    private static String BRS = "<br/>";
    private static String BRD = "<br/><br/>";
    private static String S1 = "\\$";
    private static String N = "\n";
    private static String NT = "\n\n";
    private static String UTF8 = "UTF-8";
    private static String Z = "0";
    private static String FKEY = "org.m5.font.size";
    private int recipeId;
    private int idp;
    private int position;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private String g = "г";
    private String kg = "кг";
    private String st = "шт";
    private String l = "л";
    private String ml = "мл";
    private String kkal = " ккал)";

    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_recipe_detail);

        initOrnament();
        initTitle();
        
        prefs = PreferenceManager.getDefaultSharedPreferences(RecipeDetailActivity.this);
        fontSize = prefs.getFloat(FKEY, fontSize);
        
        mRecipe = new RecipeItem();

        // Larger target triggers star toggle
        final View starParent = findViewById(R.id.recipe_header);
        CompoundButton mStarred = (CompoundButton) findViewById(R.id.star_button);
        FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(0.6f, 0f, 1f, 0.8f));

        final Intent intent = getIntent();
        mRecipeUri = intent.getData();
        if(intent.hasExtra("position")) {
        	position = intent.getExtras().getInt("position");
        }

        setupTabs();

        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(RecipeQuery._TOKEN, mRecipeUri, RecipeQuery.PROJECTION);
        
        // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
    }
    
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
        mRecipe = null;
        prefs = null;
        mTimer = null;
        if(player != null) {
        	player.release();
        	player = null;
        }
    }
    
    /** Build and add "summary" tab. */
    private void setupTabs() {
        //final TabHost host = getTabHost();
        // Summary content comes from existing layout
        //host.addTab(host.newTabSpec(M).setIndicator(buildIndicator(R.string.recipe_make)).setContent(R.id.tab_recipe_make));
        //host.addTab(host.newTabSpec(P).setIndicator(buildIndicator(R.string.recipe_products)).setContent(R.id.tab_recipe_products));
    }

    /** Build and add "products" tab. */
    private void setupProductsTab() {
        // Insert Products when available
    	Uri mProductsUri = RecipeProducts.buildProductsUri("" + recipeId);
    	mHandler.startQuery(ProductsQuery._PRODUCTS, mProductsUri, ProductsQuery.PROJECTION);
    	
        // Set link, but handle clicks manually
        final TextView textView = (TextView) findViewById(R.id.basket_link);
        textView.setText(getResources().getString(R.string.add_to_basket));
        textView.setMovementMethod(null);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }
    
    /** Handle {@link ProductsQuery} {@link Cursor}. */
    private void onProductsQueryComplete(Cursor cursor) {
    	if(cursor!=null) {
    		LinearLayout products = (LinearLayout) findViewById(R.id.products);  
    		int calories = 0;
    		while(cursor.moveToNext()) {
    		    if(cursor.getString(ProductsQuery.NAME)!=null) {
	    			LinearLayout root = new LinearLayout(this);
	    		    root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	    		    root.setOrientation(LinearLayout.HORIZONTAL);
	    		    root.setPadding(5,0,0,0);
	    		    
	    		    TextView edtName = new TextView(this);
	    		    edtName.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0.3f));
	    		    edtName.setTextColor(getResources().getColor(R.color.foreground2));
	    		    edtName.setTextSize(fontSize);
	    		    root.addView(edtName);
	    		    
	    		    TextView edtAmount = new TextView(this);
	    		    edtAmount.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0.7f));
	    		    edtAmount.setTextColor(getResources().getColor(R.color.foreground2));
	    		    edtAmount.setTextSize(fontSize);
	                root.addView(edtAmount);
	                
	                StringBuilder sbName = new StringBuilder(32);
	                StringBuilder sbAmount = new StringBuilder(32);
	                
	                sbName.append(cursor.getString(ProductsQuery.NAME));
	                
	                if(cursor.getString(ProductsQuery.NAME) != null) {
	        	       	String amount = cursor.getString(ProductsQuery.AMOUNT);
	        	        String item = cursor.getString(ProductsQuery.ITEM);
	                	if(amount != null && item != null) {
	                		int itemCalories = cursor.getInt(ProductsQuery.CALORIES);
	    	            	if(itemCalories > 0) {
	    	            		sbName.append(" (").append(itemCalories).append(kkal);
	    	            	}
	    	            	sbAmount.append(T).append(amount).append(E).append(item).append(E);
	    	            	if(item != null && (g.equals(item) || kg.equals(item) || st.equals(item) || l.equals(item) || ml.equals(item))) {
	    		            	int amountNum = 0;
	    		            	try { amountNum = Integer.parseInt(amount); } catch(Exception e) { }
	    		            	if(kg.equals(item) || l.equals(item)) { 
	    		            		calories += itemCalories * amountNum * 1000 / 100;
	    		            	} else if(st.equals(item)) { 
	    		            		calories += itemCalories;
	    		            	} else {
	    		            		calories += itemCalories * amountNum / 100;
	    		            	}
	    	            	}
	                	}
	            	}
	            	mRecipe.getProducts().add(sbName.toString() + sbAmount.toString());
	                
	                edtName.setText(sbName.toString());
	                edtAmount.setText(sbAmount.toString());
	                
	                products.addView(root);
	                View v = new View(this);
	                v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 1));
	                v.setBackgroundColor(getResources().getColor(R.color.title_separator));
	    		    products.addView(v);
	    		    
	            }
    		}
    		if(calories > 0) {
    			TextView edtCalories = (TextView) findViewById(R.id.recipe_calories);
            	edtCalories.setVisibility(View.VISIBLE);
	            edtCalories.setText(""+calories);
    		}
            
    	}
		try { cursor.close(); } catch(Exception e) { }
    }
    
    /** Handle Basket link. */
    public void onBasketClick(View v) {
    	 final ContentValues values = new ContentValues();
         values.put(Recipe.BASKET, 1);
         mHandler.startUpdate(mRecipeUri, values);
         // add recipe to starred list
         onCheckedChanged(null, true);
         Toast.makeText(RecipeDetailActivity.this, getResources().getString(R.string.basket_done), Toast.LENGTH_SHORT).show();
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested
     * string resource as its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, null, false);
        indicator.setText(textRes);
        indicator.setTypeface(RecipesApplication.getInstance().getTypeface());
        return indicator;
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(token == RecipeQuery._TOKEN) {
            onRecipeQueryComplete(cursor);
        } else if(token == ProductsQuery._PRODUCTS) {
            onProductsQueryComplete(cursor);
        } else {
            cursor.close();
        }
    }

    /** Handle {@link RecipeQuery} {@link Cursor}. */
    private void onRecipeQueryComplete(Cursor cursor) {
        try {
            if (!cursor.moveToFirst()) {
            	finish();
            	return;
            }
            if(position > 0 && cursor.getCount() > position) {
            	for(int i=0; i< position; i++) {
            		cursor.moveToNext();
            	}
            }
            mTitleString = cursor.getString(RecipeQuery.NAME);
            TextView mTitle = (TextView) findViewById(R.id.recipe_title);
            mTitle.setTypeface(RecipesApplication.getInstance().getTypeface());
            mTitle.setText(mTitleString);
            mRecipe.name = mTitleString;
            StringBuilder details = new StringBuilder(64);
            String time = cursor.getString(RecipeQuery.TIME);
            String portions = cursor.getString(RecipeQuery.PORTION);
            Resources res = getResources();
            if(!TextUtils.isEmpty(time)) {
            	details.append(String.format(res.getString(R.string.cook_time), time)).append(BRS);
            }
            if(!TextUtils.isEmpty(portions) && !Z.equals(portions)) {
                details.append(String.format(res.getString(R.string.portions), portions));
            }
            TextView mSubTitle = (TextView) findViewById(R.id.recipe_subtitle);

            UIUtils.setTextMaybeHtml(mSubTitle, details.toString());
            
            // Unregister around setting checked state to avoid triggering
            // listener since change isn't user generated.
            CompoundButton mStarred = (CompoundButton) findViewById(R.id.star_button);
            mStarred.setOnCheckedChangeListener(null);
            mStarred.setChecked(cursor.getInt(RecipeQuery.STARRED) != 0);
            mStarred.setOnCheckedChangeListener(this);

            recipeId = cursor.getInt(RecipeQuery._ID);
            idp = cursor.getInt(RecipeQuery.IDP);

            View mIcon = (View) findViewById(R.id.icon1);
        	mIcon.setBackgroundDrawable(null);
            UIUtils.fetchDrawableOnThread(recipeId, mIcon, this);
        	mIcon.setVisibility(View.VISIBLE);

            final View stepsBlock = findViewById(R.id.steps_block);
            byte[] input = cursor.getBlob(RecipeQuery.STEPS);
            String steps = "";
            Inflater decompresser = new Inflater();
            try {
            	decompresser.setInput(input, 0, input.length);
    	        byte[] result = new byte[input.length*3];
    	        int resultLength = decompresser.inflate(result);
    	        steps = new String(result, 0, resultLength, UTF8);

                if (!TextUtils.isEmpty(steps)) {
                	steps = steps.replaceAll(S1, BRD);
                    TextView mSteps = (TextView) findViewById(R.id.steps);
                    mSteps.setTextSize(fontSize);
                	UIUtils.setTextMaybeHtml(mSteps, steps);
                    stepsBlock.setVisibility(View.VISIBLE);
                    mHasSummaryContent = true;
                    mRecipe.steps = steps;
                } else {
                    stepsBlock.setVisibility(View.GONE);
                }
            } catch (Exception e) {
    			e.printStackTrace();
    		}

            setupProductsTab();

            // Show empty message when all data is loaded, and nothing to show
            if(!mHasSummaryContent) {
                findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            }
        } finally {
            if(cursor != null) {
	        	cursor.close();
	            System.gc();
            }
        }
    }

    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
    	finish();
    }
    
    /** Handle "comments" title-bar action. */
    public void onCommentsClick(View v) {
    	final Intent intent = new Intent(RecipeDetailActivity.this, CommentsActivity.class);
   		CommentsService.recipeId = recipeId;
        startActivity(intent);
    }

    /** Handle "share" title-bar action. */
    public void onShareClick(View v) {
        String shareString = mRecipe.name + NT + mRecipe.steps + NT;
        StringBuilder sb = new StringBuilder(32);
        for(String product : mRecipe.getProducts()) {
        	sb.append(product).append(N);
        }
        shareString += sb.toString();
        shareString = shareString.replace(BRS, N);
        shareString = shareString.replace(BR, N);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareString);

        startActivity(Intent.createChooser(intent, getText(R.string.title_share)));
    }
    
   
    /** Handle "font" title-bar action. */
    public void onFontClick(View v) {
    	fontSize +=2;
    	if(fontSize > 30) fontSize = 16;
    	final Editor edit = prefs.edit();
		edit.putFloat(FKEY, fontSize);
		edit.commit();
    	
    	TextView mSteps = (TextView) findViewById(R.id.steps);
    	//LinearLayout mProducts = (LinearLayout) findViewById(R.id.products);
    	mSteps.setTextSize(fontSize);
    	//mProducts.setTextSize(fontSize);
    }

    /** Handle toggling of starred checkbox. */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ContentValues values = new ContentValues();
        values.put(Recipe.STARRED, isChecked ? 1 : 0);
        mHandler.startUpdate(mRecipeUri, values);
    }

    /** {@link Sessions} query parameters. */
    private interface RecipeQuery {
        int _TOKEN = 0x1;
        String[] PROJECTION = {
                BaseColumns._ID,
                Recipe.IDP,
                Recipe.NAME,
                Recipe.STEPS,
                Recipe.PORTION,
                Recipe.TIME,
                Recipe.STARRED
        };

        int _ID=0;
        int IDP=1;
        int NAME=2;
        int STEPS=3;
        int PORTION=4;
        int TIME=5;
        int STARRED=6;
    }

    private interface ProductsQuery {
    	int _PRODUCTS = 0x2;
        String[] PROJECTION = {
        		Tables.PRODUCTS + "." + ProductsColumns._ID,
                Tables.PRODUCTS + "." + ProductsColumns.NAME,
                Tables.PRODUCTS + "." + ProductsColumns.CALORIES,
                Tables.RECIPE_PRODUCT + "." + RecipeProductColumns.AMOUNT,
                Tables.ITEMS + "." + ItemsColumns.NAME
        };

        int NAME=1;
        int CALORIES=2;
        int AMOUNT=3;
        int ITEM=4;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.recipe_detail_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

        mTimer = (TextView) findViewById(R.id.title_text);
        mTimer.setText(formatTime(duration));
        mTimer.setOnClickListener(this);
        mTimer.setOnLongClickListener(this);
        mTimer.setTextSize(32);
	    mTimer.setGravity(Gravity.CENTER_HORIZONTAL);
	    mTimer.setTextColor(this.getResources().getColor(R.color.white));
    }

    private static long duration;
    
	@Override
	public void onClick(View v) {
		if(mTimer.equals(v)) {
			if(cdt != null) {
				cdt.cancel();
				cdt = null;
				duration = 0;
			} else {
				duration += 60000;
				if(duration >= 60000*60) duration = 0;
			}
			mTimer.setText(formatTime(duration));
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(mTimer.equals(v)) {
			if(cdt != null) {
				cdt.cancel();
			}
	     	cdt = new Count(duration, 100);
			cdt.start();
		}
		return true;
	}
    
	class Count extends CountDownTimer {
		public Count(long millisInFuture, long countDownInterval) {
	      super(millisInFuture, countDownInterval);
	    }
	
		public void onFinish() {
	    	try {
		    	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		        if(alert == null){
		            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		            if(alert == null){
		                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
		            }
		        }
		        if(player != null) player.release();
		        player = MediaPlayer.create(RecipeDetailActivity.this, alert);
	            player.start();
	    	} catch(Exception e) {
	    		Log.e("RecipeDetailActivity", e.toString());
	    	}
	    }
	
	    public void onTick(long millisUntilFinished) {
	    	try {
	    		mTimer.setText(formatTime(millisUntilFinished));
	    	} catch(Exception e) {
	    		cancel();
	    	}
	    }
	}
	
	public static String formatTime(long millis) {
    	String output = "00:00";
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        String secondsD = String.valueOf(seconds);
        String minutesD = String.valueOf(minutes);

        if (seconds < 10)
        	secondsD = "0" + seconds;
        if (minutes < 10)
        	minutesD = "0" + minutes;
        output = minutesD + ":" + secondsD;
        return output;
    }

	@Override 
	public boolean dispatchTouchEvent(MotionEvent mEvent){ 
	    super.dispatchTouchEvent(mEvent); 
	    return gestureDetector.onTouchEvent(mEvent); 
	}

	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	position++;
                	mHandler.startQuery(RecipeQuery._TOKEN, null, Dish.buildRecipeUri(""+idp), RecipeQuery.PROJECTION, null, null, Recipe.DEFAULT_SORT);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	position--;
                	if(position < 0) position = 0;
                	mHandler.startQuery(RecipeQuery._TOKEN, null, Dish.buildRecipeUri(""+idp), RecipeQuery.PROJECTION, null, null, Recipe.DEFAULT_SORT);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
        
        // It is necessary to return true from onDown for the onFling event to register
        @Override
        public boolean onDown(MotionEvent e) {
        	return true;
        }
    }

}

