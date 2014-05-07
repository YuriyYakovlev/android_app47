package org.m5.provider;


import java.io.FileNotFoundException;
import java.util.Arrays;

import org.m5.provider.RecipesContract.Dish;
import org.m5.provider.RecipesContract.E;
import org.m5.provider.RecipesContract.Kitchen;
import org.m5.provider.RecipesContract.Profile;
import org.m5.provider.RecipesContract.Recipe;
import org.m5.provider.RecipesContract.RecipeProducts;
import org.m5.provider.RecipesContract.Reference;
import org.m5.provider.RecipesContract.SearchSuggest;
import org.m5.provider.RecipesContract.Units;
import org.m5.provider.RecipesDatabase.Tables;
import org.m5.ui.RecipesApplication;
import org.m5.util.SelectionBuilder;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 * 
 * Provider that stores {@link RecipesContract} data. Data is usually inserted
 * by {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class RecipesProvider extends ContentProvider {
    private static final String TAG = "RecipesProvider";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int DISH = 200;
    private static final int DISH_RECIPE = 201;

    private static final int RECIPE = 300;
    private static final int RECIPE_ID = 301;
    private static final int RECIPE_STARRED = 302;
    private static final int RECIPE_SEARCH = 303;
    private static final int RECIPE_ID_PRODUCTS = 304;
    
    private static final int KITCHEN = 400;
    private static final int KITCHEN_RECIPE = 401;
    
    private static final int SEARCH_SUGGEST = 500;

    private static final int BASKET = 600;
    
    private static final int UNITS = 700;
    
    private static final int _E = 800;
    
    private static final int REFERENCE = 900;
    private static final int REFERENCE_ID = 901;

    private static final int PROFILE = 1000;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecipesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "dish/*", DISH);
        matcher.addURI(authority, "dish/*/recipe", DISH_RECIPE);

        matcher.addURI(authority, "recipe", RECIPE);
        matcher.addURI(authority, "recipe/starred", RECIPE_STARRED);
        matcher.addURI(authority, "recipe/search/*", RECIPE_SEARCH);
        matcher.addURI(authority, "recipe/*", RECIPE_ID);
        matcher.addURI(authority, "recipe/*/products", RECIPE_ID_PRODUCTS);
        
        matcher.addURI(authority, "kitchen", KITCHEN);
        matcher.addURI(authority, "kitchen/*/recipe", KITCHEN_RECIPE);

        matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);

        matcher.addURI(authority, "products", BASKET);
        
        matcher.addURI(authority, "units/*", UNITS);
        
        matcher.addURI(authority, "e/*", _E);
        
        matcher.addURI(authority, "reference", REFERENCE);
        matcher.addURI(authority, "reference/*", REFERENCE_ID);

        matcher.addURI(authority, "profile", PROFILE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DISH:
                return Dish.CONTENT_TYPE;
            case DISH_RECIPE:
                return Recipe.CONTENT_TYPE;
            case RECIPE:
                return Recipe.CONTENT_TYPE;
            case RECIPE_STARRED:
                return Recipe.CONTENT_TYPE;
            case RECIPE_SEARCH:
                return Recipe.CONTENT_TYPE;
            case RECIPE_ID:
                return Recipe.CONTENT_ITEM_TYPE;
            case RECIPE_ID_PRODUCTS:
                return Recipe.CONTENT_TYPE;    
            case KITCHEN:
                return Kitchen.CONTENT_TYPE;
            case KITCHEN_RECIPE:
                return Recipe.CONTENT_TYPE;
            case BASKET:
                return RecipeProducts.CONTENT_TYPE;
            case UNITS:
                return Units.CONTENT_TYPE;
            case _E:
                return E.CONTENT_TYPE;
            case REFERENCE:
                return Reference.CONTENT_TYPE;
            case REFERENCE_ID:
                return Reference.CONTENT_TYPE;
            case PROFILE:
                return Profile.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
        final SQLiteDatabase db = RecipesApplication.getInstance().getDatabase().getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri, match);
                return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
            }
            case SEARCH_SUGGEST: {
                final SelectionBuilder builder = new SelectionBuilder();

                // Adjust incoming query to become SQL text match
                selectionArgs[0] = selectionArgs[0] + "%";
                builder.table(Tables.SEARCH_SUGGEST);
                builder.where(selection, selectionArgs);
                builder.map(SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_TEXT_1);
                projection = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY };
                final String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                return builder.query(db, projection, null, null, SearchSuggest.DEFAULT_SORT, limit);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = RecipesApplication.getInstance().getDatabase().getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH_SUGGEST: {
                db.insertOrThrow(Tables.SEARCH_SUGGEST, null, values);
                return SearchSuggest.CONTENT_URI;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = RecipesApplication.getInstance().getDatabase().getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).update(db, values);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = RecipesApplication.getInstance().getDatabase().getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).delete(db);
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    /*@Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }*/

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DISH: {
                final String dishId = Dish.getDishId(uri);
                return builder.table(Tables.DISH).where(Dish.IDP + "=?", dishId);
            }
            case DISH_RECIPE: {
            	final String dishId = Dish.getDishId(uri);
                return builder.table(Tables.RECIPE).where(Recipe.IDP + "=?", dishId);
            }
            case KITCHEN: {
                return builder.table(Tables.KITCHEN);
            }
            case KITCHEN_RECIPE: {
            	final String kitchenId = Kitchen.getKitchenId(uri);
                return builder.table(Tables.RECIPE).where(Recipe.KITCHEN + "=?", kitchenId);
            }
            case RECIPE: {
                return builder.table(Tables.RECIPE);
            }
            case RECIPE_ID: {
            	final String recipeId = Recipe.getRecipeId(uri);
                return builder.table(Tables.RECIPE).where(Recipe._ID + "=?", recipeId);
            }
            case RECIPE_ID_PRODUCTS: {
                final String recipeId = Recipe.getRecipeId(uri);
                try {
                	int id = Integer.parseInt(recipeId);
                	if(id == -1) {
                		return builder.table(Tables.PRODUCTS);
                	} else {
                		return builder.table(Tables.JOIN_RECIPE_PRODUCTS).where(Qualified.RECIPE_RECIPE_ID + "=?", ""+id);
                	}
                } catch(Exception e) {
                    return builder.table(Tables.PRODUCTS).where("name like '?%'", recipeId);
                }
            }
            case SEARCH_SUGGEST: {
                return builder.table(Tables.SEARCH_SUGGEST);
            }
            case BASKET: {
                return builder.table(Tables.RECIPE_PRODUCT);
            }
            case UNITS: {
            	final String idp = Units.getIdp(uri);
                return builder.table(Tables.UNITS).where(Units.IDP + "=?", idp);
            }
            case _E: {
            	final String idp = E.getIdp(uri);
                return builder.table(Tables.E).where(E.IDP + "=?", idp);
            }
            case REFERENCE: {
            	return builder.table(Tables.REFERENCE);
            }
            case REFERENCE_ID: {
            	final String id = Reference.getId(uri);
                return builder.table(Tables.REFERENCE).where(Reference._ID + "=?", id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case DISH: {
                final String dishId = Dish.getDishId(uri);
                return builder.table(Tables.DISH).where(Dish.IDP + "=?", dishId);
            }
            case DISH_RECIPE: {
            	final String dishId = Dish.getDishId(uri);
                return builder.table(Tables.RECIPE).where(Recipe.IDP + "=?", dishId);
            }
            case RECIPE: {
                return builder.table(Tables.RECIPE);
            }
            case RECIPE_STARRED: {
                return builder.table(Tables.RECIPE).where(Recipe.STARRED + "=1");
            }
            case RECIPE_SEARCH: {
                final String query = Uri.decode(Recipe.getSearchQuery(uri));
                String[] products = null;
                if(query.indexOf(" и ") > 0) {
                	products = query.split(" и ");
                }
                String where = "recipe._id in (select recipe._id from recipe"
	                + " LEFT OUTER JOIN recipe_product ON (recipe.p1=recipe_product._id or recipe.p2=recipe_product._id or recipe.p3=recipe_product._id"
	                + " or recipe.p4=recipe_product._id or recipe.p5=recipe_product._id or recipe.p6=recipe_product._id or recipe.p7=recipe_product._id"
	                + " or recipe.p8=recipe_product._id or recipe.p9=recipe_product._id or recipe.p10=recipe_product._id or recipe.p11=recipe_product._id"
	                + " or recipe.p12=recipe_product._id or recipe.p13=recipe_product._id or recipe.p14=recipe_product._id or recipe.p15=recipe_product._id"
	                + " or recipe.p16=recipe_product._id or recipe.p17=recipe_product._id or recipe.p18=recipe_product._id or recipe.p19=recipe_product._id"
	                + " or recipe.p20=recipe_product._id)"
	                + " left outer join products on recipe_product.product_id=products._id where ";
                StringBuilder where1 = new StringBuilder(16);
                if(products != null) {
                	for(int i=0; i<products.length; i++) {
                		if(i > 0) where1.append(" and ");
                		//if(i > 0) where1 = where1 + " or "; 
                		//where1 = where1 + "products.name like '"+products[i]+"%'";
                		where1.append(where).append("products.name like '").append(products[i]).append("%')");
                	}
                	//where1 = where + where1 + ")";
                } else {
                	String upper = query.substring(0, 1).toUpperCase() + query.substring(1, query.length() - 1);
                	where1.append(where).append(" recipe.name like '").append(query).append("%' or recipe.name like '").append(upper).append("%' or products.name like '").append(query).append("%')");
                }
                return builder.table(Tables.RECIPE).where(where1.toString());
            }
            case RECIPE_ID: {
            	final String recipeId = Recipe.getRecipeId(uri);
                return builder.table(Tables.RECIPE).where(Recipe._ID + "=?", recipeId);
            }
            case RECIPE_ID_PRODUCTS: {
                final String recipeId = Recipe.getRecipeId(uri);
                try {
                	int id = Integer.parseInt(recipeId);
                	if(id == -1) {
                		return builder.table(Tables.PRODUCTS);
                	} else {
                		return builder.table(Tables.JOIN_RECIPE_PRODUCTS).where(Qualified.RECIPE_RECIPE_ID + "=?", ""+id);
                	}
                } catch(Exception e) {
                    return builder.table(Tables.PRODUCTS).where("name like '?%'", recipeId);
                }
            }
            case KITCHEN: {
                return builder.table(Tables.KITCHEN);
            }
            case KITCHEN_RECIPE: {
            	final String kitchenId = Kitchen.getKitchenId(uri);
                return builder.table(Tables.RECIPE).where(Recipe.KITCHEN + "=?", kitchenId);
            }
            case BASKET: {
            	return builder.table(Tables.JOIN_RECIPE_PRODUCTS).where(Recipe.BASKET + ">0");
            }
            case UNITS: {
            	final String idp = Units.getIdp(uri);
                return builder.table(Tables.UNITS).where(Units.IDP + "=?", idp);
            }
            case _E: {
            	final String idp = E.getIdp(uri);
                return builder.table(Tables.E).where(E.IDP + "=?", idp);
            }
            case REFERENCE: {
            	return builder.table(Tables.REFERENCE);
            }
            case REFERENCE_ID: {
            	final String id = Reference.getId(uri);
                return builder.table(Tables.REFERENCE).where(Reference._ID + "=?", id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /**
     * {@link RecipesContract} fields that are fully qualified with a specific
     * parent {@link Tables}. Used when needed to work around SQL ambiguity.
     */
    private interface Qualified {
        String RECIPE_RECIPE_ID = Tables.RECIPE + "." + Recipe._ID;
    }
}
