package org.m5.util;

import java.util.Locale;

import org.m5.ui.RecipesApplication;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.preference.PreferenceManager;


public class L10N {
	public static Locale locale;
	public static final String LANG_KEY = "org.m5.lang";
	private static SharedPreferences prefs;
	
	
	public static void init() {
		String locale = getPreferences().getString(L10N.LANG_KEY, null);
		if(locale != null) {
			L10N.locale = new Locale(locale);
	     	Configuration conf = RecipesApplication.getInstance().getResources().getConfiguration();
			if(!conf.locale.equals(L10N.locale)) {
				conf.locale = L10N.locale;
				RecipesApplication.getInstance().getResources().updateConfiguration(conf, RecipesApplication.getInstance().getResources().getDisplayMetrics());
			}
		}
	}
	
	public static void changeLocale(String locale) {
		L10N.locale = new Locale(locale);
    	Editor edit = getPreferences().edit();
		edit.putString(L10N.LANG_KEY, locale);
		edit.commit();
		Configuration conf = RecipesApplication.getInstance().getResources().getConfiguration();
		if(!conf.locale.equals(L10N.locale)) {
			conf.locale = L10N.locale;
			RecipesApplication.getInstance().getResources().updateConfiguration(conf, RecipesApplication.getInstance().getResources().getDisplayMetrics());
			RecipesApplication.getInstance().resetDatabase();
		}
	}
	
	public static String getLocale() {
		String ret = getPreferences().getString(L10N.LANG_KEY, null);
		if(ret == null) {
			Configuration conf = RecipesApplication.getInstance().getResources().getConfiguration();
			ret = conf.locale.getLanguage();
		}
		return ret;
	}
	
	public static SharedPreferences getPreferences() {
		if(prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(RecipesApplication.getInstance());
		}
		return prefs;
	}
	
}
