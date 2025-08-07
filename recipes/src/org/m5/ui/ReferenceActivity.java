package org.m5.ui;

import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.Reference;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import org.m5.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link Dish}, as requested
 * through {@link Intent#getData()}.
 */
public class ReferenceActivity extends ListActivity implements AsyncQueryListener {
    private ReferenceAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		setContentView(R.layout.activity_reference);
        initOrnament();
        initTitle();
        
        mAdapter = new ReferenceAdapter(this);
        setListAdapter(mAdapter);

        // Start background query to load units
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(Reference.CONTENT_URI, ReferenceQuery.PROJECTION, null, null, Reference.DEFAULT_SORT);
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
     * {@link CursorAdapter} that renders a {@link org.m5.provider.RecipesContract.Recipe}.
     */
    private class ReferenceAdapter extends CursorAdapter /*implements SectionIndexer*/ {
    	//AlphabetIndexer alphaIndexer;
    	
        public ReferenceAdapter(Context context) {
            super(context, null);
            //alphaIndexer=new AlphabetIndexer(null, ReferenceQuery.NAME, " АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ");
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
        	//alphaIndexer.setCursor(cursor);
            return getLayoutInflater().inflate(R.layout.list_item_reference, parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(cursor.getString(ReferenceQuery.NAME));
            name.setTypeface(RecipesApplication.getInstance().getTypeface(), Typeface.BOLD);

            final TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(cursor.getString(ReferenceQuery.DESCRIPTION));
        }
        
        /*@Override
		public int getPositionForSection(int section) {
			return alphaIndexer.getPositionForSection(section); //use the indexer
		}

		@Override
		public int getSectionForPosition(int position) {
			return alphaIndexer.getSectionForPosition(position); //use the indexer
		}

		@Override
		public Object[] getSections() {
			return alphaIndexer.getSections(); //use the indexer
		}*/
    }

    /** {@link Dish} query parameters. */
    private interface ReferenceQuery {
        String[] PROJECTION = {
        		Reference._ID,
        		Reference.NAME,
                Reference.DESCRIPTION           
        };
        int NAME = 1;
        int DESCRIPTION = 2;
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
