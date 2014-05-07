package org.m5.provider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
public abstract class CommentsProvider {
	private static String A = "action"; 
	private static String L = "list"; 
	private static String C = "count"; 
	private static String S = "save"; 
	private static String R = "remove"; 
	private static String S_URL = "http://getstarted.com.ua/recipes/CommentsDAO.php";
	private static String UTF8 = "UTF-8";
	
	
	public String listData(Map<String, Object> params) {
		if(params == null) params = new HashMap<String, Object>();
		params.put(A, L);
		return execute(params);
	}
		
	public String countData() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(A, C);
		return execute(params);
	}
	
	public String saveData(Map<String, Object> params) {
		params.put(A, S);
		return execute(params);
	}

	public void removeData(Map<String, Object> params) {
		params.put(A, R);
		execute(params);
	}
	
	public abstract String getServiceURL();


	
	// PRIVATE METHODS
	
	private String execute(Map<String, Object> params) {
		String result = "";
		if(params != null && params.size() > 0) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for(Map.Entry<String, Object> entry : params.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
			
			InputStream is = null; 
			// http post
			try {
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpPost httppost = new HttpPost(S_URL);
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, UTF8));
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        is = entity.getContent();
			} catch(Exception e) {
	        	Log.e(this.getClass().getName(), e.toString());
			}
			
			// convert response to string
			try {
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF8), 8);
		        StringBuilder sb = new StringBuilder();
		        String line = null;
		        while((line = reader.readLine()) != null) {
		        	sb.append(line).append("\n");
		        }
		        is.close();
		        result = sb.toString();
			} catch(Exception e) {
	        	Log.e(this.getClass().getName(), e.toString());
			}
		}
		return result;
	}
	
}
