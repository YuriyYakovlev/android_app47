package org.m5.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.m5.provider.CommentsProvider;
import org.m5.ui.HomeActivity;

import android.util.Log;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 */
public class CommentsService extends CommentsProvider {
	public static int recipeId;
	private String serviceURL;
	private static String ID = "id";
	private static String TEXT = "text";
	private static String RID = "recipe_id";
	private static String D = "date";
	private static String T = "timestamp";
	private static String U_ID = "user_id";
	private static String U_NAME = "user_name";
	private static String U_IMAGE = "user_image";
	private static String U_TYPE = "user_type";
	private static String U_VER = "user_ver";
	
	
	public String getServiceURL() {
		if(serviceURL == null) { 
			serviceURL = HomeActivity.SERVER_URL + "CommentsDAO.php";
		}
		return serviceURL;
	}
	
	public List<Comment> listComment(int recipe) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		if(recipe > 0) {
			params.put(RID, recipe);
		}
		List<Comment> ret = list(params);
		return ret;
	}
	
	public String saveComment(String comment, int recipe, String userId, String userName, String userImage, String userType, int userVer) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(RID, recipe);
		params.put(TEXT, comment);
		params.put(D, "");
		params.put(U_ID, userId);
		params.put(U_NAME, userName);
		params.put(U_IMAGE, userImage);
		params.put(U_TYPE, userType);
		params.put(U_VER, userVer);
		String id = saveData(params);
		return id;
	}

	public void remove(Comment ticket) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(ID, ticket.getId());
		removeData(params);
	}
	
	public List<Comment> list(Map<String, Object> params) {

		List<Comment> ret = new ArrayList<Comment>();
		String data = listData(params);
		if(data != null) {
			try {
		        JSONArray jArray = new JSONArray(data);
		        for(int i=0; i < jArray.length(); i++) {
		           ret.add(buildTicket(jArray.getJSONObject(i)));
		        }
			} catch(JSONException e) {
	        	Log.e(this.getClass().getName(), e.toString());
			}
		}
		return ret;
	}
	
	private Comment buildTicket(JSONObject jsonObject) throws JSONException {
		Comment coment = new Comment();
		coment.setId(jsonObject.getInt(ID));
		coment.setText(jsonObject.getString(TEXT));
		coment.setRecipeId(jsonObject.getInt(RID));
		coment.setTimestamp(jsonObject.getString(T));
		coment.setUserId(jsonObject.getString(U_ID));
		coment.setUserName(jsonObject.getString(U_NAME));
		coment.setUserImage(jsonObject.getString(U_IMAGE));
		coment.setUserType(jsonObject.getString(U_TYPE));
		coment.setUserVersion(jsonObject.getInt(U_VER));
		return coment;
	}

}
