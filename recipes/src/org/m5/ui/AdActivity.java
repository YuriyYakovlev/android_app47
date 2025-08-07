package org.m5.ui;

import org.m5.R;

import android.app.TabActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;


public abstract class AdActivity extends TabActivity {
	private static String KEY = "a14ddbcb298ca1d";
	public static String REMOVE_ADS_KEY = "org.m5.remove_ads";
	private AdView adView;
	
	public void initAdLayout() {
	    final LinearLayout adLayout = (LinearLayout)findViewById(R.id.AdView1);
	    
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean isPremium = preferences.getBoolean(AdActivity.REMOVE_ADS_KEY, false);
    	
	    if(adLayout != null && !isPremium) {
	   		adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(KEY);
	   		adLayout.addView(adView);
	   		
	   		adView.loadAd(new AdRequest.Builder().build());
	    	adView.setAdListener(new AdListener() {
				@Override
				public void onAdClosed() {
					//RecipesApplication.adClicked = true;
					adLayout.setVisibility(View.GONE);
				}
	
				@Override
				public void onAdFailedToLoad(LoadAdError adError) {
				}
	
				@Override
				public void onAdOpened() {
					//RecipesApplication.adClicked = true;
					adLayout.setVisibility(View.GONE);
				}
	
				@Override
				public void onAdLoaded() {
					adLayout.setVisibility(View.VISIBLE);
				}
	    	});
	    }
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView != null) {
        	adView.setAdListener(null);
        	adView.destroy();
        	adView = null;
        }
    }
}