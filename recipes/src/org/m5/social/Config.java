package org.m5.social;

public interface Config {
	  String PREFS_NAME = "org.m5.r";
	  String PREF_ACCOUNT_NAME = "accountName";
	  String PREF_TOKEN = "accessToken";
	  // This scope lets you use the user id 'me' in API requests.
	  // The Android AccountManager has experimental support for OAuth 2.0. You simply need to
	  // add oauth2:https://www.googleapis.com/auth/plus.me when setting the AUTH_TOKEN_TYPE.
	  // This isn't ideal because that string will be displayed in the authorization dialog.
	  // For this reason, you can use the human-readable alias assigned below instead.
	  // For more information, see http://code.google.com/p/google-api-java-client/wiki/Android
	  String PLUS_ME_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.me"; //"Получение ваших публичных данных из Google+";
	  String ACCOUNT_TYPE = "com.google";
	  String API_KEY = "AIzaSyAl0-z8h47ZsJWDFaxrksPQOIbgco6n-xE";
	  
	  String PROFILE_ID = "profileId";
	  String PROFILE_NAME = "profileName";
	  String PROFILE_IMAGE = "profileImage";

	  String GPLUS_URL = "market://details?id=com.google.android.apps.plus";
}
