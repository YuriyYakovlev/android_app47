package org.m5.ui;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.m5.provider.RecipesContract.ItemsColumns;
import org.m5.provider.RecipesContract.ProductsColumns;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.RecipeColumns;
import org.m5.provider.RecipesContract.RecipeProductColumns;
import org.m5.provider.RecipesContract.RecipeProducts;
import org.m5.provider.RecipesDatabase.Tables;
import org.m5.util.L10N;
import org.m5.util.NotifyingAsyncQueryHandler;
import org.m5.util.UIUtils;
import org.m5.util.NotifyingAsyncQueryHandler.AsyncQueryListener;

import org.m5.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * {@link ListActivity} that displays a set of {@link Products}, as requested
 * through {@link Intent#getData()}.
 */
public class BasketActivity extends ListActivity implements AsyncQueryListener, DialogInterface.OnClickListener {
	private static String KEY = "org.m5.basket.";
	private static String KB = KEY + "basket";
	private static String F = "Для";
	private static String T = "по вкусу";
	private static String P = " + ";
	private static String E = " ";
	private static String S1 = "\\$";
	private static String S1S = "$";
	private static String S2 = ";";
	private static String N = "\n";
	private static String STRIKE = "strike";
	private static String ICB = "icBackground";
	
	private BasketListAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    private SharedPreferences prefs;
    private AutoCompleteTextView edtAdd;
    private Map<Integer, Product> products;
    private SortedMap<String, Product> sorted;
    private ArrayAdapter<String> adapter;
    private Random random;
    private Builder alert;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L10N.init();
		
        products = new HashMap<Integer, Product>();
        sorted = new TreeMap<String, Product>();
        
        setContentView(R.layout.activity_basket);
        
        initOrnament();
        initTitle();
        
        final Intent intent = getIntent();
        final Uri basketUri = intent.getData();

        String[] projection;
        projection = BasketQuery.PROJECTION;
        
        edtAdd = (AutoCompleteTextView) findViewById(R.id.edtAdd);

        // Start background query to load basket
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        mHandler.startQuery(BasketQuery._TOKEN, basketUri, projection);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(BasketActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
        mAdapter = null;
        prefs = null;
        edtAdd = null;
        adapter = null;
        products = null;
        sorted = null;
        random = null;
        alert = null;

    }
    
    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if(cursor != null) {
	    	if(token == BasketQuery._TOKEN) {
				try {
	    			int count = cursor.getCount();
			    	if(count > 0) {
				    	products.clear();
				    	while(cursor.moveToNext()) {
				    		String name = cursor.getString(BasketQuery.NAME);
				    		if(name != null && !name.startsWith(F)) {
					    		Product p = new Product();
					    		p.id = cursor.getInt(BasketQuery.PRODUCT_ID);
					    		p.recipeId = cursor.getString(BasketQuery.RECIPE_ID);
					    		p.name = name;
					    		String amount = cursor.getString(BasketQuery.AMOUNT);
					    		String item = cursor.getString(BasketQuery.ITEM);
					    		StringBuilder s = new StringBuilder(8);
					    		
					    		Product tmp = products.get(p.id);
								if(tmp!=null && !"".equals(tmp.amount) && !T.equals(tmp.amount.trim())) {
									s.append(tmp.amount);
								}
								
					    		if(amount != null) {
					    			if(s.length() > 0) s.append(P);
					    			s.append(amount.trim());
					    		}
								if(item != null) s.append(E).append(item.trim());
									
								p.amount = s.toString();
								products.put(p.id, p);
				    		}
				    	}
				    	for(Product p : products.values()) {
				    		sorted.put(p.name, p);
				    	}
			    	}
			    	// Get products from basket
		    		String[] basket = prefs.getString(KB, "").split(S1);
			    	for(String item : basket) {
			    		if(!TextUtils.isEmpty(item)) {
			    			String[] inner = item.split(S2);
			    			Product p = new Product();
				    		try { 
				    			p.id = Integer.parseInt(inner[0]); 
				    			p.name = inner[1];
				    		} catch(NumberFormatException e) {
				    			p.name = inner[0];
				    		}
			    			sorted.put(item, p);
			    		}
			    	}
			    	
			    	mAdapter = new BasketListAdapter(this, new ArrayList<Product>(sorted.values()));
			    	setListAdapter(mAdapter);
	
			    	Uri mProductsUri = RecipeProducts.buildProductsUri("-1");
			    	mHandler.startQuery(ProductsQuery._PRODUCTS, mProductsUri, ProductsQuery.PROJECTION);
	
				} finally {
	        		cursor.close();
	            }
			} else {
				try {
					int count = cursor.getCount();
			    	if(count > 0) {
						String[] array = new String[count];
						int counter = 0;
						while(cursor.moveToNext()) {
			            	array[counter++] = cursor.getString(ProductsQuery.NAME);
						}
						adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, array);
				        edtAdd.setAdapter(adapter);
			    	}
				} finally {
	        		cursor.close();
	            }
	    	}
		}
    }
    
    private Random getRandom() {
    	if(random == null) {
    		random = new Random();
    	}
    	return random;
    }
    
    public void onAddClick(View v) {
    	String product = edtAdd.getText().toString();
    	if(!TextUtils.isEmpty(product)) {
    		Product p = new Product();
			p.id = -Math.abs(getRandom().nextInt()) % 1000;
    		p.name = product;
    		
    		final Editor edit = prefs.edit();
			edit.putString(KB, prefs.getString(KB, "") + S1S + p.id + S2 + product);
			edit.commit();

			edtAdd.setText("");
			mAdapter.products.add(0, p);
    		mAdapter.notifyDataSetChanged();
    		
    		try {
	    		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
	    		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    		} catch(Exception e) { }
    	}
    }
    
    private class Product {
    	public Integer id;
    	public String recipeId, name, amount;
    }

    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
    	finish();
    }

    /** Handle "share" title-bar action. */
    public void onShareClick(View v) {
        final StringBuilder share = new StringBuilder(64);
        share.append(getResources().getString(R.string.basket_share)).append(N);
        for(Entry<Integer, Product> entry : products.entrySet()) {
        	Product p = entry.getValue();
        	share.append(p.name).append(E).append(p.amount).append(N);
        }
    	String[] basket = prefs.getString(KB, "").split(S1);
    	for(String item : basket) {
    		if(!TextUtils.isEmpty(item)) {
	    		share.append(item).append(N);
    		}
    	}
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, share.toString());

        startActivity(Intent.createChooser(intent, getText(R.string.title_share)));
    }
    
    @Override
    public void onClick(DialogInterface arg0, int arg1) {
    	final ContentValues values = new ContentValues();
        values.put(Recipe.BASKET, 0);
        Set<String> recipeIds = new HashSet<String>(); 
        for(Entry<Integer, Product> entry : products.entrySet()) {
        	Product p = entry.getValue();
        	recipeIds.add(p.recipeId);
        	
        	try {
            	final Editor edit = prefs.edit();
				String k = KEY + p.id;
            	edit.remove(k);
				edit.remove(k + STRIKE);
				edit.commit();
        	} catch(Exception e) { }
        }
    	
        for(String recipeId : recipeIds) {
        	Uri uri = Recipe.buildRecipeUri(recipeId);
        	mHandler.startUpdate(uri, values);
        }
    	
    	try {
        	final Editor edit = prefs.edit();
			edit.remove(KB);
			edit.commit();
    	} catch(Exception e) { }

        products.clear();
        mAdapter.getProducts().clear();
        mAdapter.notifyDataSetChanged();
    }
    
    /** Handle "delete" title-bar action. */
    public void onDeleteClick(View v) {
    	alert = new AlertDialog.Builder(this)
        .setTitle(R.string.title_basket)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(R.string.delete_confirm)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, this)
        .setCancelable(false);
    	alert.show(); 
    }
    
    class BasketListAdapter extends BaseAdapter {
        private List<Product> products;
        private Context mContext;
        
        public BasketListAdapter(Context context, List<Product> products) {
            this.products = products;
            this.mContext = context;
        }
        
        public List<Product> getProducts() {
        	if(products == null) {
        		products = new ArrayList<Product>();
        	}
        	return products;
        }
        
        public int getCount() {
            return products.size();
        }

        public Object getItem(int position) {
            return products.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
        	final Product p = products.get(position);
	        final String sKey = KEY + p.id + STRIKE;
	        
        	LinearLayout layout = new LinearLayout(mContext);
        	SoftReference<Drawable> icBackground = RecipesApplication.getInstance().getDrawableHashMap().get(ICB);
    		if(icBackground == null) {
    			BitmapDrawable drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.background));
    			icBackground = new SoftReference<Drawable>(drawable);   
    			RecipesApplication.getInstance().getDrawableHashMap().put(ICB, icBackground);   
    		}
        	layout.setBackgroundDrawable(icBackground.get());
      		layout.setOrientation(LinearLayout.HORIZONTAL);
        	layout.setPadding(10,5,10,5);
    			
      		LinearLayout l = new LinearLayout(mContext);
      		l.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.6f));
      		l.setOrientation(LinearLayout.VERTICAL);
        	
            final TextView name = new TextView(mContext);
            if(prefs.getBoolean(sKey, false)) {
				name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            name.setTextSize(18f);
            name.setTypeface(RecipesApplication.getInstance().getTypeface());
            name.setText(p.name);
            name.setTextColor(BasketActivity.this.getResources().getColor(R.color.foreground2));
            l.addView(name);
			
			if(!TextUtils.isEmpty(p.amount)) {
	            TextView amount = new TextView(mContext);
				amount.setTextSize(16f);
				amount.setTextColor(BasketActivity.this.getResources().getColor(R.color.units_header));
				amount.setText(p.amount);
	            l.addView(amount);
			}
            layout.addView(l);
            
            layout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					if(!prefs.getBoolean(sKey, false)) {
						name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						final Editor edit = prefs.edit();
	    				edit.putBoolean(sKey, true);
	    				edit.commit();
					} else {
						name.setPaintFlags(name.getPaintFlags() - Paint.STRIKE_THRU_TEXT_FLAG);
						final Editor edit = prefs.edit();
	    				edit.putBoolean(sKey, false);
	    				edit.commit();
					}
					return false;
				}
            });
            
            CheckBox done = new CheckBox(mContext);
            done.setGravity(Gravity.CENTER);
            final String pKey = KEY + p.id;
            done.setChecked(prefs.getBoolean(pKey, false));
            done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	            @Override
				public void onCheckedChanged(CompoundButton arg0, boolean checked) {
	            	final Editor edit = prefs.edit();
    				edit.putBoolean(pKey, checked);
    				edit.commit();
				}
            });
            done.setGravity(Gravity.RIGHT);
            layout.addView(done);

	        return layout;
        }
    }

    /** {@link Sessions} query parameters. */
    private interface BasketQuery {
    	int _TOKEN = 0x1;
        String[] PROJECTION = {
        		Tables.RECIPE_PRODUCT + "." + RecipeProductColumns._ID,
        		Tables.PRODUCTS + "." + ProductsColumns._ID,
                Tables.PRODUCTS + "." + ProductsColumns.NAME,
                Tables.RECIPE_PRODUCT + "." + RecipeProductColumns.AMOUNT,
                Tables.ITEMS + "." + ItemsColumns.NAME + " item",
                Tables.RECIPE + "." + RecipeColumns._ID
        };
        int PRODUCT_ID = 1;
        int NAME = 2;
        int AMOUNT = 3;
        int ITEM = 4;
        int RECIPE_ID = 5;
    }
    
    private interface ProductsQuery {
    	int _PRODUCTS = 0x2;
        String[] PROJECTION = {
        		Tables.PRODUCTS + "." + ProductsColumns._ID,
        		Tables.PRODUCTS + "." + ProductsColumns.NAME
        };
        int NAME=1;
    }
    
    private void initOrnament() {
    	View ornament = (View) findViewById(R.id.viewOrnament);
		ornament.setBackgroundDrawable(RecipesApplication.getInstance().getOrnamentDrawable());
    }
    
    private void initTitle() {
		LinearLayout title = (LinearLayout) findViewById(R.id.basket_title);
		title.setBackgroundDrawable(RecipesApplication.getInstance().getTitleDrawable());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        TextView titleView = ((TextView) findViewById(R.id.title_text));
        titleView.setText(customTitle != null ? customTitle : getTitle());
        titleView.setTypeface(RecipesApplication.getInstance().getTypeface());
    }
    
}
