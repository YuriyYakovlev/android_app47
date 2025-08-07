package org.m5.provider;

import java.util.ArrayList;
import java.util.List;

import org.m5.io.DataHandler;
import org.m5.provider.RecipesContract.EColumns;
import org.m5.provider.RecipesContract.ItemsColumns;
import org.m5.provider.RecipesContract.KitchenColumns;
import org.m5.provider.RecipesContract.ProductsColumns;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.RecipeColumns;
import org.m5.provider.RecipesContract.DishColumns;
import org.m5.provider.RecipesContract.RecipeProductColumns;
import org.m5.provider.RecipesContract.RefColumns;
import org.m5.provider.RecipesContract.UnitsColumns;
import org.m5.util.L10N;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link RecipesProvider}.
 */
public class RecipesDatabase extends SQLiteOpenHelper {
    private static final String TAG = "RecipesDatabase";
    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.
    // released version: 5
    private static final int DATABASE_VERSION = 12;
    
    private DataHandler mDataHandler;
    private Context context;
	

    public interface Tables {
        String DISH = "dish";
        String KITCHEN = "kitchen";
        String RECIPE = "recipe";
        String PRODUCTS = "products";
        String ITEMS = "items";
        String UNITS = "units";
        String E = "e";
        String REFERENCE = "reference";
        
        String RECIPE_PRODUCT = "recipe_product";
        
        String RECIPE_SEARCH = "recipe_search";
        String KITCHEN_SEARCH = "kitchen_search";

        String SEARCH_SUGGEST = "search_suggest";

        String JOIN_RECIPE_PRODUCTS = "recipe"
            + " LEFT OUTER JOIN recipe_product ON (recipe.p1=recipe_product._id or recipe.p2=recipe_product._id or recipe.p3=recipe_product._id"
            + " or recipe.p4=recipe_product._id or recipe.p5=recipe_product._id or recipe.p6=recipe_product._id or recipe.p7=recipe_product._id"
            + " or recipe.p8=recipe_product._id or recipe.p9=recipe_product._id or recipe.p10=recipe_product._id or recipe.p11=recipe_product._id"
            + " or recipe.p12=recipe_product._id or recipe.p13=recipe_product._id or recipe.p14=recipe_product._id or recipe.p15=recipe_product._id"
            + " or recipe.p16=recipe_product._id or recipe.p17=recipe_product._id or recipe.p18=recipe_product._id or recipe.p19=recipe_product._id"
            + " or recipe.p20=recipe_product._id)"
            + " LEFT OUTER JOIN products ON recipe_product.product_id=products._id"
            + " LEFT OUTER JOIN items ON recipe_product.item=items._id";
    }

    public RecipesDatabase(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL("CREATE TABLE " + Tables.DISH + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DishColumns.IDP + " INTEGER,"
                + DishColumns.NAME + " TEXT,"
                + DishColumns.COUNT + " INTEGER,"
                + "UNIQUE (" + DishColumns.NAME + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.KITCHEN + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KitchenColumns.KITCHEN + " TEXT,"
                + KitchenColumns.COUNT + " INTEGER,"
                + "UNIQUE (" + KitchenColumns.KITCHEN + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.RECIPE + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RecipeColumns.IDP + " INTEGER NOT NULL,"
                + RecipeColumns.NAME + " TEXT,"
                + RecipeColumns.STEPS + " TEXT,"
                + RecipeColumns.PORTION + " INTEGER,"
                + RecipeColumns.TIME + " INTEGER,"
                + RecipeColumns.KITCHEN + " INTEGER,"
                + RecipeColumns.P1 + " INTEGER,"
                + RecipeColumns.P2 + " INTEGER,"
                + RecipeColumns.P3 + " INTEGER,"
                + RecipeColumns.P4 + " INTEGER,"
                + RecipeColumns.P5 + " INTEGER,"
                + RecipeColumns.P6 + " INTEGER,"
                + RecipeColumns.P7 + " INTEGER,"
                + RecipeColumns.P8 + " INTEGER,"
                + RecipeColumns.P9 + " INTEGER,"
                + RecipeColumns.P10 + " INTEGER,"
                + RecipeColumns.P11 + " INTEGER,"
                + RecipeColumns.P12 + " INTEGER,"
                + RecipeColumns.P13 + " INTEGER,"
                + RecipeColumns.P14 + " INTEGER,"
                + RecipeColumns.P15 + " INTEGER,"
                + RecipeColumns.P16 + " INTEGER,"
                + RecipeColumns.P17 + " INTEGER,"
                + RecipeColumns.P18 + " INTEGER,"
                + RecipeColumns.P19 + " INTEGER,"
                + RecipeColumns.P20 + " INTEGER,"
                + RecipeColumns.STARRED + " INTEGER,"
        		+ RecipeColumns.BASKET + " INTEGER,"
                + "UNIQUE (" + RecipeColumns.NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.PRODUCTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProductsColumns.NAME + " TEXT,"
                + ProductsColumns.CALORIES + " INTEGER,"
                + "UNIQUE (" + ProductsColumns.NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsColumns.NAME + " TEXT,"
                + "UNIQUE (" + ItemsColumns.NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.RECIPE_PRODUCT + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RecipeProductColumns.PRODUCT_ID + " INTEGER,"
                + RecipeProductColumns.AMOUNT + " TEXT," 
                + RecipeProductColumns.ITEM + " INTEGER)");

        db.execSQL("CREATE TABLE " + Tables.SEARCH_SUGGEST + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");
        
        db.execSQL("CREATE TABLE " + Tables.UNITS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UnitsColumns.IDP + " INTEGER,"
                + UnitsColumns.PRODUCT + " TEXT,"
                + UnitsColumns.W1 + " INTEGER,"
                + UnitsColumns.W2 + " INTEGER,"
                + UnitsColumns.W3 + " INTEGER,"
                + UnitsColumns.W4 + " INTEGER,"
                + UnitsColumns.W5 + " INTEGER,"
                + "UNIQUE (" + UnitsColumns.PRODUCT + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.E + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EColumns.IDP + " INTEGER,"
                + EColumns.TYPE + " INTEGER,"
                + EColumns.NUMBER + " TEXT,"
                + EColumns.NAME + " TEXT,"
                + EColumns.DESCRIPTION + " INTEGER,"
                + "UNIQUE (" + EColumns.NUMBER + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.REFERENCE + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RefColumns.NAME + " TEXT,"
                + RefColumns.DESCRIPTION + " INTEGER,"
                + "UNIQUE (" + RefColumns.NAME + ") ON CONFLICT REPLACE)");

        // Insert initial data
        mDataHandler = new DataHandler(context, db);
        
        mDataHandler.insertDish();
        mDataHandler.insertKitchen();
        mDataHandler.insertProducts();
        mDataHandler.insertItems();
        mDataHandler.insertRecipeProduct();
        mDataHandler.insertRecipe();
        mDataHandler.insertUnits();
        mDataHandler.insertE();
        mDataHandler.insertReference();
        
        db.execSQL("CREATE INDEX dish_idp_idx ON dish(idp)");
        db.execSQL("CREATE INDEX recipe_idp_idx ON recipe(idp)");
        db.execSQL("CREATE INDEX recipe_kitchen_idx ON recipe(kitchen)");
        db.execSQL("CREATE INDEX recipe_starred_idx ON recipe(starred)");
        db.execSQL("CREATE INDEX recipe_basket_idx ON recipe(basket)");
        db.execSQL("CREATE INDEX recipe_product_p_idx ON recipe_product(product_id)");
        db.execSQL("CREATE INDEX units_idp_idx ON units(idp)");
        db.execSQL("CREATE INDEX e_idp_idx ON e(idp)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        if(oldVersion != DATABASE_VERSION) {
        	List<Integer> starred = new ArrayList<Integer>();
    		Cursor c = null;
    		try {
    			c = db.query(Tables.RECIPE, new String[]{RecipeColumns._ID}, RecipeColumns.STARRED+"=1", null, null, null, null);
    			int count = c.getCount();
    	    	if(count > 0) {
    		    	while(c.moveToNext()) {
    		    		starred.add(c.getInt(0));
    		    	}
    	    	}
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            } finally {
            	if(c != null) {
            		c.close();
            	}
            }
            
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DISH);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.KITCHEN);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.RECIPE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.RECIPE_PRODUCT);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.RECIPE_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.KITCHEN_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SEARCH_SUGGEST);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.UNITS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.E);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.REFERENCE);

            db.execSQL("DROP INDEX IF EXISTS dish_idp_idx");
            db.execSQL("DROP INDEX IF EXISTS recipe_idp_idx");
            db.execSQL("DROP INDEX IF EXISTS recipe_kitchen_idx");
            db.execSQL("DROP INDEX IF EXISTS recipe_starred_idx");
            db.execSQL("DROP INDEX IF EXISTS recipe_basket_idx");
            db.execSQL("DROP INDEX IF EXISTS recipe_product_p_idx");
            db.execSQL("DROP INDEX IF EXISTS units_idp_idx");
            db.execSQL("DROP INDEX IF EXISTS e_idp_idx");
            
            onCreate(db);

            if(starred.size() > 0) {
           	 final ContentValues values = new ContentValues();
                values.put(Recipe.STARRED, 1);
                for(Integer id : starred) {
               	 try {
               		 db.update(Tables.RECIPE, values, "_id="+id, null);
               	 } catch(Exception e) {
                    	Log.e(TAG, e.toString());
                    }
                }
           }
        }
    }
}
