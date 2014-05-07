package org.m5.ui;

import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.E;
import org.m5.provider.RecipesContract.Units;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import org.m5.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link Dish}, as requested
 * through {@link Intent#getData()}.
 */
public class EActivity extends ListActivity implements AsyncQueryListener {
    private EAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_e);
        initOrnament();
        initTitle();
        initCategories();
        
        mAdapter = new EAdapter(this);
        setListAdapter(mAdapter);

        final Uri eUri = E.buildEUri("1");
        
        // Start background query to load units
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(eUri, EQuery.PROJECTION, null, null, Units.DEFAULT_SORT);
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(cursor != null &&  mAdapter != null) {
	    	startManagingCursor(cursor);
	        mAdapter.changeCursor(cursor);
	        System.gc();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Null out the group cursor. This will cause the group cursor and all of the child cursors to be closed.
        //mAdapter.changeCursor(null);
        mAdapter = null;
        mHandler = null;
    }

    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
    	finish();
    }

    
    //private static String T = "-";
    /**
     * {@link CursorAdapter} that renders a {@link DishQuery}.
     */
    private class EAdapter extends CursorAdapter {
        public EAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_e, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        	final TextView number = (TextView) view.findViewById(R.id.number);
            number.setText(cursor.getString(EQuery.NUMBER));
            number.setTypeface(RecipesApplication.getInstance().getTypeface());

            final TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(cursor.getString(EQuery.NAME));

            final TextView description = (TextView) view.findViewById(R.id.description);
            StringBuilder desc = new StringBuilder(32); 
            
        	int type = cursor.getInt(EQuery.TYPE);
        	final LinearLayout item = (LinearLayout) view.findViewById(R.id.item);
        	switch(type) {
        		case 1:
        			item.setBackgroundColor(getResources().getColor(R.color.red));
        			desc.append(getResources().getString(R.string.e_f1)).append(". ");  
        			break;
        		case 2:
        			item.setBackgroundColor(getResources().getColor(R.color.orange));
        			desc.append(getResources().getString(R.string.e_f2)).append(". ");  
    				break;
        		case 3:
        			item.setBackgroundColor(getResources().getColor(R.color.yellow));
        			desc.append(getResources().getString(R.string.e_f3)).append(". ");  
    				break;
        		case 4:
        			item.setBackgroundColor(getResources().getColor(R.color.blue));
        			desc.append(getResources().getString(R.string.e_f4)).append(". ");  
    				break;
    			default: 
    				item.setBackgroundColor(0xffffffff);
    				break;
        	}
        	if(cursor.getString(EQuery.DESCRIPTION)!= null) { 
        		desc.append(cursor.getString(EQuery.DESCRIPTION));
        	}
            description.setText(desc.toString());
        }
    }

    /** {@link Dish} query parameters. */
    private interface EQuery {
        String[] PROJECTION = {
        		E._ID,
        		E.IDP,
                E.TYPE,
                E.NUMBER,
                E.NAME,
                E.DESCRIPTION           
        };
        int TYPE = 2;
        int NUMBER = 3;
        int NAME = 4;
        int DESCRIPTION = 5;
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
    
    private void initCategories() {
	    Spinner categories = (Spinner) findViewById(R.id.spCategories);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
	    for(Category cat : Category.values()) {
	    	adapter.add(this.getResources().getString(cat.getName()));
		}
	    categories.setAdapter(adapter);
	    categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    		 mHandler.startQuery(E.buildEUri("" + (pos+1)), EQuery.PROJECTION, null, null, E.DEFAULT_SORT);
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    categories.setSelection(0);
    }
    
    private static enum Category {
    	C1 (R.string.e_c1),
    	C2 (R.string.e_c2),
    	C3 (R.string.e_c3),
    	C4 (R.string.e_c4),
    	C5 (R.string.e_c5),
    	C6 (R.string.e_c6);
    	
    	private int name;
    	
    	Category(int name) {
    		this.name = name;
    	}
    	public int getName() {
    		return name;
    	}
    }

}
