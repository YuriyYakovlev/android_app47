package org.m5.social;

import org.m5.R;
import org.m5.ui.RecipesApplication;
import org.m5.util.L10N;
import org.m5.util.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public final class ProfileActivity extends Activity {
	protected CharSequence[] _options = {"English", "Русский"};
	protected boolean[] _selections =  {true, false};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		L10N.init();
		setContentView(R.layout.activity_profile);
		initOrnament();
		initTitle();
		showDialog(0);
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
					    UIUtils.goHome(ProfileActivity.this);
					} 
					break;
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Get profile id, displayName and image
		final SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
		String name = settings.getString(Config.PROFILE_NAME, "");
		String url = settings.getString(Config.PROFILE_IMAGE, "");
		if(!TextUtils.isEmpty(name)) {
			((TextView)findViewById(R.id.name)).setText(name);
		}
		if(!TextUtils.isEmpty(url)) {
			UIUtils.fetchDrawable(url, (ImageView)findViewById(R.id.photo));
		}
	}

	private String APP_MARKET_URL = "market://details?id=org.m5.plus"; 
	public void onUpgrade(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW);  
    	intent.setData(Uri.parse(APP_MARKET_URL));
    	startActivity(intent);
	}
	
	public void onNickSave(View v) {
		EditText name = ((EditText)findViewById(R.id.nick_name));
		if(!TextUtils.isEmpty(name.getText())) {
			// Save profile id, displayName and image
			try {
				final SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);
				final SharedPreferences.Editor editor = settings.edit();
	        	editor.putString(Config.PROFILE_NAME, name.getText().toString());
	        	editor.commit();
			} catch(Exception e) {
				Log.e(this.getClass().getName(), "can't save user data:"+e.toString());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		finish();
	}
	
	private void initOrnament() {
		View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
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