package org.m5.social;

import java.io.Serializable;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 */
public class Comment implements Serializable {
	private static final long serialVersionUID = 7631715626468205590L;
	private int id;
	private int recipeId;
	private String text;
	private String timestamp;	
	private String userId;	
	private String userName;	
	private String userImage;	
	private String userType;
	private int userVersion;
	
	
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public Comment() {}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRecipeId() {
		return recipeId;
	}
	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getUserVersion() {
		return userVersion;
	}
	public void setUserVersion(int version) {
		this.userVersion = version;
	}
}
