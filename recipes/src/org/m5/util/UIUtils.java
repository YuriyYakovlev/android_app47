package org.m5.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;


import org.m5.ui.HomeActivity;
import org.m5.ui.RecipesApplication;
import org.m5.ui.SearchActivity;


public class UIUtils {
	private static final String TAG = "UIUtils";
    private static final String SERVER_URL = "http://getstarted.com.ua/recipes/images/";
    private static final String SERVER_URL_LARGE = "http://m5i.s3.amazonaws.com/";
    private static String S1 = "%s%d.jpg"; 
    private static String S2 = "%s%d.png"; 
	private static String LPNG = "l.png";
	private static String PNG = ".png";
	

	
    public static Drawable fetchDrawable(int id) {
    	return fetchDrawable(id, null);
    }
    
	public static Drawable fetchDrawable(int id, Activity activity) {
    	Drawable drawable = null;
    	String key = (activity != null) ? id + LPNG : id + PNG;
    	
    	// read from file cache
    	File f = null;
    	if(RecipesApplication.USE_SD) {
        	f = new File(RecipesApplication.CACHE_DIR, key);
        	Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
        	if(bitmap != null && activity != null) {
        		drawable = new BitmapDrawable(RecipesApplication.getInstance().getResources(), bitmap);
        		return drawable;
        	}
    	} else {
        	// read from memory cache
        	if(RecipesApplication.getInstance().getDrawableHashMap().containsKey(key)) {
                return RecipesApplication.getInstance().getDrawableHashMap().get(key).get();
            }
    	}
    	if(RecipesApplication.getInstance().isOnline(false)) {
	    	// fetch from URL
	        InputStream is = null;
	        try {
	            String url = (activity != null) ? String.format(S1, SERVER_URL_LARGE, id) : String.format(S2, SERVER_URL, id);
	            URLConnection con = new URL(url).openConnection();
	            con.connect();
	            int fileLength = con.getContentLength();
	            is = con.getInputStream();
	        	if(is != null) {
	        		byte[] bytes = new byte[fileLength];
	                for(int i=0; i<fileLength; i++) {
	                    bytes[i] = (byte)is.read();
	                }
	                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, fileLength);
	                //Bitmap bitmap = BitmapFactory.decodeStream(is);
			    	if(bitmap != null) {
		                drawable = new BitmapDrawable(RecipesApplication.getInstance().getResources(), bitmap);
		            	if(RecipesApplication.USE_SD) {
		            		writeFile(bitmap, f);
			            } else {
			            	if(drawable != null) {
			            		RecipesApplication.getInstance().getDrawableHashMap().put(key, new SoftReference<Drawable>(drawable));
			            	}	            	
			            }
			    	}
	        	}
	        } catch (Exception e) {
	            Log.w(TAG, "fetchDrawable failed", e);
	        } finally {
	            if(is != null) {
	                try { is.close(); } catch (IOException e) {	}
	            }
	        }
    	}
        return drawable;
    }
    
    private static void writeFile(Bitmap bmp, File f) {
    	FileOutputStream out = null;
    	try {
    	    out = new FileOutputStream(f);
    	    if(out != null) {
    	    	bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally { 
    	    try { if (out != null ) out.close(); }
    	    catch(Exception ex) {} 
    	}
   	}
    
    public static void fetchDrawableOnThread(final int id, final View imageView) {
    	fetchDrawableOnThread(id, imageView, null);
    }
    public static void fetchDrawableOnThread(final int id, final View imageView, final Activity activity) {
        if(activity == null && RecipesApplication.getInstance().getDrawableHashMap().containsKey(id)) {
            imageView.setBackgroundDrawable(RecipesApplication.getInstance().getDrawableHashMap().get(id).get());
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
            	Drawable drawable = (Drawable) message.obj;
            	if(drawable == null) {
            		imageView.setVisibility(View.GONE);
            	} else {
            		imageView.setBackgroundDrawable(drawable);
            	}
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
            	Drawable drawable = fetchDrawable(id, activity);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    
    public static void fetchDrawable(final String url, final View imageView) {
        if(RecipesApplication.getInstance().getDrawableHashMap().containsKey(url)) {
            imageView.setBackgroundDrawable(RecipesApplication.getInstance().getDrawableHashMap().get(url).get());
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
            	Drawable drawable = (Drawable) message.obj;
            	if(drawable == null) {
            		imageView.setVisibility(View.GONE);
            	} else {
            		imageView.setBackgroundDrawable(drawable);
            	}
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
            	Drawable drawable = fetchDrawable(url);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }
    
    public static Drawable fetchDrawable(String url) {
    	Drawable drawable = null;
    	if(RecipesApplication.getInstance().isOnline(false)) {
	    	// fetch from URL
	        InputStream is = null;
	        try {
	            URLConnection con = new URL(url).openConnection();
	            con.connect();
	            int fileLength = con.getContentLength();
	            is = con.getInputStream();
	        	if(is != null) {
	        		byte[] bytes = new byte[fileLength];
	                for(int i=0; i<fileLength; i++) {
	                    bytes[i] = (byte)is.read();
	                }
	                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, fileLength);
	                //Bitmap bitmap = BitmapFactory.decodeStream(is);
			    	drawable = new BitmapDrawable(RecipesApplication.getInstance().getResources(), bitmap);
            		if(drawable != null) {
	            		RecipesApplication.getInstance().getDrawableHashMap().put(url, new SoftReference<Drawable>(drawable));
	            	}	            	
		        }
	        } catch (Exception e) {
	            Log.e(TAG, "fetchDrawable failed", e);
	        } finally {
	            if(is != null) {
	                try { is.close(); } catch (IOException e) {	}
	            }
	        }
    	}
        return drawable;
    }
    
    /**
     * Invoke "home" action, returning to {@link HomeActivity}.
     */
    public static void goHome(Context context) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Invoke "search" action, triggering a default search.
     */
    public static void goSearch(Activity activity) {
    	Intent intent = new Intent(activity, SearchActivity.class);
        activity.startActivity(intent);
    	//activity.startSearch(null, false, Bundle.EMPTY, false);
    }

    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (text.contains("<") && text.contains(">")) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

}
