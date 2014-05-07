package org.m5.ui;

import org.m5.R;

import android.app.TabActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;


public abstract class AdActivity extends TabActivity {
	private static String KEY = "a14ddbcb298ca1d";
	public static String REMOVE_ADS_KEY = "org.m5.remove_ads";
	private AdView adView;
	
	public void initAdLayout() {
	    final LinearLayout adLayout = (LinearLayout)findViewById(R.id.AdView1);
	    
	    /*AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
        alpha.setDuration(0); // Make animation instant
        alpha.setFillAfter(true); // Tell it to persist after the animation ends
        adLayout.startAnimation(alpha);*/
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean isPremium = preferences.getBoolean(AdActivity.REMOVE_ADS_KEY, false);
    	
	    if(adLayout != null && /*!RecipesApplication.adClicked &&*/ !isPremium) {
	   		adView = new AdView(this, AdSize.BANNER, KEY);
	   		adLayout.addView(adView);
	   		
	   		adView.loadAd(new AdRequest());
	    	adView.setAdListener(new AdListener() {
				@Override
				public void onDismissScreen(Ad arg0) {
					//RecipesApplication.adClicked = true;
					adLayout.setVisibility(View.GONE);
				}
	
				@Override
				public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
				}
	
				@Override
				public void onLeaveApplication(Ad arg0) {
					//RecipesApplication.adClicked = true;
					adLayout.setVisibility(View.GONE);
				}
	
				@Override
				public void onPresentScreen(Ad arg0) {
				}
	
				@Override
				public void onReceiveAd(Ad arg0) {
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
        	adView = null;
        }
    }
}
