package org.m5.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.m5.R;
import org.m5.provider.RecipesContract.Profile;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.social.Comment;
import org.m5.social.CommentsService;
import org.m5.social.Config;
import org.m5.util.L10N;
import org.m5.util.UIUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class CommentsActivity  extends Activity {
	private CommentsAdapter mAdapter;
	private CommentsService mService;
	private SharedPreferences settings;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		
        setContentView(R.layout.activity_comments);
        
        initOrnament();
        initTitle();
        
        settings = getSharedPreferences(Config.PREFS_NAME, 0);
        mAdapter = new CommentsAdapter(this); 
		mService = new CommentsService();
		
		GridView gridView = (GridView) findViewById(android.R.id.list);
		gridView.setAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		//mAdapter.getComments().addAll(mService.listComment(CommentsService.recipeId));
		new ListCommentsTask().execute();
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        mService = null;
        
    }
    
    /** Handle "back" title-bar action. */
    public void onBackClick(View v) {
    	finish();
    }
 
    /** Handle "vote" title-bar action. */
    public void onVoteClick(View v) {
    	Intent goToMarket = null;
    	goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=org.m5"));
    	startActivity(goToMarket);

    }
    
    /** Handle "send" title-bar action. */
    public void onSendClick(View v) {
    	EditText edtComment = (EditText) findViewById(R.id.edtComment);
		String text = edtComment.getText().toString();
    	if(text != null && !TextUtils.isEmpty(text.trim())) {
    		new SaveCommentTask().execute();
    		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
    	}
    }
    
    class ListCommentsTask extends AsyncTask<String, String, Void>{
    	@Override
    	protected void onPreExecute() {
    	}
    	
    	@Override
    	protected Void doInBackground(String... args) {
    		if(mAdapter != null) {
    			mAdapter.getComments().clear();
    			mAdapter.getComments().addAll(mService.listComment(CommentsService.recipeId));
    		}
    		return null;
    	}

    	@Override
    	protected void onPostExecute(Void ret) {
    		findViewById(android.R.id.empty).setVisibility(View.GONE);
    		if(mAdapter != null) {
    			mAdapter.notifyDataSetChanged();
    		}
    	}
    }
    
    class SaveCommentTask extends AsyncTask<String, String, Void>{
    	@Override
    	protected void onPreExecute() {
    		
    	}
    	
    	@Override
    	protected Void doInBackground(String... args) {
    		EditText edtComment = (EditText) findViewById(R.id.edtComment);
    		String text = edtComment.getText().toString();
        	
        	String u_id = settings.getString(Config.PROFILE_ID, "");
        	String u_name = settings.getString(Config.PROFILE_NAME, "");
        	String u_image = settings.getString(Config.PROFILE_IMAGE, "");
        	boolean isPremium = settings.getBoolean(AdActivity.REMOVE_ADS_KEY, false);
        	String u_type = isPremium ? "1" : "0";
        	int u_version = android.os.Build.VERSION.SDK_INT;
    		new CommentsService().saveComment(text.trim(), CommentsService.recipeId, u_id, u_name, u_image, u_type, u_version);
    		
    		return null;
    	}

    	@Override
    	protected void onPostExecute(Void ret) {
    		/*finish();
	    	Intent myIntent = new Intent(CommentsActivity.this, CommentsActivity.class);
	    	startActivity(myIntent);*/
    		EditText edtComment = (EditText) findViewById(R.id.edtComment);
    		edtComment.setText("");
    		
    		new ListCommentsTask().execute();
    	}
    }
    
    class DeleteCommentTask extends AsyncTask<Comment, String, Void>{
    	@Override
    	protected void onPreExecute() {
    		
    	}
    	
    	@Override
    	protected Void doInBackground(Comment... args) {
    		new CommentsService().remove(args[0]);
    		return null;
    	}

    	@Override
    	protected void onPostExecute(Void ret) {
    		/*finish();
	    	Intent myIntent = new Intent(CommentsActivity.this, CommentsActivity.class);
	    	startActivity(myIntent);*/
    		new ListCommentsTask().execute();
    	}
    }
    
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.recipe_comments_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        TextView titleView = ((TextView) findViewById(R.id.title_text));
        titleView.setText(customTitle != null ? customTitle : getTitle());
        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }
    
    
    class CommentsAdapter extends BaseAdapter {
    	private String TAG = "CommentsAdapter";
    	private static final long serialVersionUID = -5623354315863298693L;
    	private List<Comment> comments;
    	private Context mContext;
    	private LayoutInflater mInflater;

        
        public CommentsAdapter(Context c) {
            this.mContext = c;
            mInflater = LayoutInflater.from(c);
        }

        public List<Comment> getComments() {
        	if(comments == null) {
        		comments = new ArrayList<Comment>();
        	}
        	return comments;
        }
        
        public int getCount() {
            return getComments().size();
        }

        public Comment getItem(int position) {
            return getComments().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

    	private DateFormat in = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	private DateFormat out = new SimpleDateFormat("dd.MM HH:mm");
    	private String SU = "105948846620768411679";
    	
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder;
        	if(convertView == null) {
        		convertView = mInflater.inflate(R.layout.list_item_comment, null);
        		holder = new ViewHolder();
        		holder.photo = (ImageView) convertView.findViewById(R.id.photo);
        		holder.name = (TextView) convertView.findViewById(R.id.name);
        		holder.type = (TextView) convertView.findViewById(R.id.type);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.delete = (ImageButton) convertView.findViewById(R.id.delete);
        		convertView.setTag(holder);
        	} else {
        		holder = (ViewHolder) convertView.getTag();
        	}
        	
        	final Comment comment = getComments().get(position);
        	
        	final String u_id = settings.getString(Config.PROFILE_ID, "-1"); 
        	if(u_id.equals(comment.getUserId()) || SU.equals(u_id)) {
        		holder.delete.setVisibility(View.VISIBLE);
        		holder.delete.setOnClickListener(new OnClickListener() {
        			@Override
					public void onClick(View arg0) {
        				Builder alert = new AlertDialog.Builder(mContext)
    			        .setTitle(R.string.title_comments)
    			        .setIcon(android.R.drawable.ic_dialog_alert)
    			        .setMessage(R.string.delete_comment)
    			        .setNegativeButton(android.R.string.cancel, null)
    			        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface arg0, int arg1) {
    							new DeleteCommentTask().execute(comment);
    						}
    			        })
    			        .setCancelable(false);
    			    	alert.show();	
					}
        		});
        	} else {
        		holder.delete.setOnClickListener(null);
        		holder.delete.setVisibility(View.GONE);
        	}
        	
        	if(comment.getRecipeId() > 0) {
        		convertView.setOnClickListener(new OnClickListener() {
        			@Override
        			public void onClick(View arg0) {
        				if(comment.getRecipeId() > 0) {
        			         final Uri recipeUri = Recipe.buildRecipeUri(""+comment.getRecipeId());
        			         final Intent intent = new Intent(Intent.ACTION_VIEW, recipeUri);
        			         mContext.startActivity(intent);
        				}
        			}
            	});
        	} else {
        		convertView.setOnClickListener(null);
        	}

        	// set image
        	if(!TextUtils.isEmpty(comment.getUserImage())) {
        		if(SU.equals(comment.getUserId())) {
        			holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
        		} else {
        			UIUtils.fetchDrawable(comment.getUserImage(), holder.photo);
        		}
        	} else {
        		switch(comment.getUserVersion()) {
        			case 3: holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.cupcake));
        					break;
        			case 4: holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.donut));
        					break;
        			case 5:
        			case 6:
        			case 7: holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.eclair));
							break;
        			case 8: holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.froyo));
							break;
        			case 9:
        			case 10:holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gingerbread));
							break;
        			case 11:
        			case 12:
        			case 13:holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.honeycomb));
						 	break;
        			case 14:holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icecream));
        					break;
        			default: holder.photo.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.profile)); 	
        					break;
        		}
        	}
        	holder.photo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
	        		final Intent intent = new Intent(Intent.ACTION_VIEW, Profile.CONTENT_URI);
			        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_profile));
			        startActivity(intent);
			        finish();
			    }
        	});

        	// set name
        	if(!TextUtils.isEmpty(comment.getUserName())) {
        		if(SU.equals(comment.getUserId())) {
        			holder.name.setText(getResources().getString(R.string.app_name));
        		} else {
        			holder.name.setText(comment.getUserName());
        		}
        		holder.name.setVisibility(View.VISIBLE);
        	} else {
        		holder.name.setVisibility(View.GONE);
        	}
        	if("1".equals(comment.getUserType()) && !SU.equals(comment.getUserId())) {
        		holder.type.setVisibility(View.VISIBLE);
        		holder.type.setText(getResources().getString(R.string.premium_account));
        	} else {
        		holder.type.setVisibility(View.GONE);
        	}
        	
        	// set timestamp
        	String time = "";
	    	try {  
	    		Date date = (Date)in.parse(comment.getTimestamp());
	    		time = out.format(date);
	    	} catch(ParseException e) {
	    		Log.e(TAG, e.toString());
	    	}
	    	holder.date.setText(time);
	    	
	    	// set comment
	    	holder.text.setText(comment.getText().trim());
	    	
	    	if(SU.equals(comment.getUserId())) {
	    		holder.text.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bubble_pan));
	    	} else {
	    		if(comment.getRecipeId() > 0) {
	        		holder.text.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bubble_default));
	        	} else {
	        		holder.text.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bubble_grey));
	        	}
	    	}

	    	return convertView;
        }
    }

    static class ViewHolder {
        ImageView photo;
        TextView name;
        TextView date;
        TextView text;
        TextView type;
        ImageButton delete;
    }

}
  
