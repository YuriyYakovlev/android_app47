package org.m5.ui;

import org.m5.provider.RecipesContract.Dish;
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
public class UnitsActivity extends ListActivity implements AsyncQueryListener {
    private UnitsAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_units);
        initOrnament();
        initTitle();
        initCategories();
        
        mAdapter = new UnitsAdapter(this);
        setListAdapter(mAdapter);

        final Uri unitsUri = Units.buildUnitsUri("0");
        
        // Start background query to load units
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(unitsUri, UnitsQuery.PROJECTION, null, null, Units.DEFAULT_SORT);
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

    
    private static String T = "-";
    /**
     * {@link CursorAdapter} that renders a {@link DishQuery}.
     */
    private class UnitsAdapter extends CursorAdapter {
        public UnitsAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_unit, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView name = (TextView) view.findViewById(R.id.product);
            name.setText(cursor.getString(UnitsQuery.PRODUCT));
            name.setTypeface(RecipesApplication.getInstance().getTypeface());

            int w1 = cursor.getInt(UnitsQuery.W1);
            final TextView tw1 = (TextView) view.findViewById(R.id.w1);
	        if(w1 > 0) {
	            tw1.setText("" + w1);
	            tw1.setTypeface(RecipesApplication.getInstance().getTypeface());
            } else {
            	tw1.setText(T);
            }
            int w2 = cursor.getInt(UnitsQuery.W2);
            final TextView tw2 = (TextView) view.findViewById(R.id.w2);
            if(w2 > 0) {
	            tw2.setText("" + w2);
	            tw2.setTypeface(RecipesApplication.getInstance().getTypeface());
            } else {
            	tw2.setText(T);
            }
            int w3 = cursor.getInt(UnitsQuery.W3);
            final TextView tw3 = (TextView) view.findViewById(R.id.w3);
            if(w3 > 0) {
	            tw3.setText("" + w3);
	            tw3.setTypeface(RecipesApplication.getInstance().getTypeface());
            } else {
            	tw3.setText(T);
            }
            int w4 = cursor.getInt(UnitsQuery.W4);
            final TextView tw4 = (TextView) view.findViewById(R.id.w4);
            if(w4 > 0) {
	            tw4.setText("" + w4);
	            tw4.setTypeface(RecipesApplication.getInstance().getTypeface());
            } else {
            	tw4.setText(T);
            }
            int w5 = cursor.getInt(UnitsQuery.W5);
            final TextView tw5 = (TextView) view.findViewById(R.id.w5);
            if(w5 > 0) {
	            tw5.setText("" + w5);
	            tw5.setTypeface(RecipesApplication.getInstance().getTypeface());
            } else {
                tw5.setText(T);
            }
        }
    }

    /** {@link Dish} query parameters. */
    private interface UnitsQuery {
        String[] PROJECTION = {
        		Units._ID,
        		Units.PRODUCT,
                Units.W1,
                Units.W2,
                Units.W3,
                Units.W4,
                Units.W5                
        };

        int PRODUCT = 1;
        int W1 = 2;
        int W2 = 3;
        int W3 = 4;
        int W4 = 5;
        int W5 = 6;
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
	    		 mHandler.startQuery(Units.buildUnitsUri("" + pos), UnitsQuery.PROJECTION, null, null, Units.DEFAULT_SORT);
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    categories.setSelection(0);
    }
    
    private static enum Category {
    	VEG (R.string.unit_veg),
    	FRU (R.string.unit_fru),
    	NUT (R.string.unit_nut),
    	MIL (R.string.unit_mil),
    	FAT (R.string.unit_fat),
    	BOB (R.string.unit_bob),
    	DRI (R.string.unit_dri),
    	SWE (R.string.unit_swe),
    	OTH (R.string.unit_oth);
    	
    	private int name;
    	
    	Category(int name) {
    		this.name = name;
    	}
    	public int getName() {
    		return name;
    	}
    }
    
}
