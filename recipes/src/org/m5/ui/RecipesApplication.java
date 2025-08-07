package org.m5.ui;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;

import org.m5.R;
import org.m5.provider.CustomContext;
import org.m5.provider.RecipesDatabase;
import org.m5.util.LruCacheLinkedHashMap;

//import com.loopme.LoopMe;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 */
public class RecipesApplication extends Application {
    public static boolean USE_SD = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	public static String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.pangalushka/";

	private static RecipesApplication instance; 
	private static Map<Object, SoftReference<Drawable>> drawableHashMap;
	private static Typeface typeface;
	public static boolean adClicked;
	private static String TAG = "RecipesApplication";
	private RecipesDatabase mOpenHelper;

	
	public static RecipesApplication getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		drawableHashMap = Collections.synchronizedMap(new LruCacheLinkedHashMap(100));
		
        /*
        LoopMe loopMe = LoopMe.getInstance(getApplicationContext());
        loopMe.setAppKey("063a50db-168c-47c1-8065-1c1ca196f0d2");
        loopMe.sendAppOpen();
        */
	}
	
	public RecipesDatabase getDatabase() {
		if(mOpenHelper == null) {
			if(RecipesApplication.USE_SD) {
		        // remove in memory db if exists
	        	try {
					File old = getDatabasePath(getResources().getString(R.string.database_name));
					if (old.exists()) {
						SQLiteDatabase.openDatabase(old.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
						old.delete(); // delete old instance to recreate db on sdcard
					}
			    } catch (Exception e) { // database doesn't exist yet.
			    	Log.d(TAG, "db does not exist yet", e);
			    }
	        }
	        if(RecipesApplication.USE_SD) {
	        	File preparedDir = new File(RecipesApplication.CACHE_DIR);
	            if(!preparedDir.exists()) {
	            	if (!preparedDir.mkdirs()) {
						USE_SD = false;
					}
	            }
				if (USE_SD) {
					mOpenHelper = new RecipesDatabase(new CustomContext(this), preparedDir.getAbsolutePath() + "/" + getResources().getString(R.string.database_name));
				}
	        }

			if (mOpenHelper == null) {
		        mOpenHelper = new RecipesDatabase(this, getResources().getString(R.string.database_name));
	        }
		}
		return mOpenHelper;
	}
	
    public void resetDatabase() {
		if(mOpenHelper != null) {
			mOpenHelper.close();
			mOpenHelper = null;
		}
	}
    
	public Map<Object, SoftReference<Drawable>> getDrawableHashMap() {
		return drawableHashMap;
	}
	
	public Drawable getOrnamentDrawable() {
		SoftReference<Drawable> icOrnament = getDrawableHashMap().get("icOrnament");
		if(icOrnament == null) {
			BitmapDrawable drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.ornament));
			drawable.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
			icOrnament = new SoftReference<Drawable>(drawable);   
			getDrawableHashMap().put("icOrnament", icOrnament);   
		}
		return icOrnament.get();
	}
	
	public Drawable getTitleDrawable() {
		SoftReference<Drawable> icTitle = getDrawableHashMap().get("icTitle");
		if(icTitle == null) {
			BitmapDrawable drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.title_background));
			drawable.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.MIRROR);
			icTitle = new SoftReference<Drawable>(drawable);   
			getDrawableHashMap().put("icTitle", icTitle);   
		}
		return icTitle.get();
	}
	
	public Typeface getTypeface() {
		if(typeface == null) {
			typeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/a.ttf");
		}
		return typeface;
	}


    private Boolean isOnline;
    public boolean isOnline(boolean force) {
		if(isOnline == null || force) {
			try {
				final ConnectivityManager conMgr = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
				if(activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
				    isOnline = true;
				} else {
				    isOnline = false;
				} 
			} catch(Exception e) {
				Log.e(TAG, e.toString());
				isOnline = false;
			}
		}
		return isOnline;
	}
    
}

