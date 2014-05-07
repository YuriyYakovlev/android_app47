package org.m5.provider;

import org.m5.provider.RecipesDatabase.Tables;
import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 *
 * Contract class for interacting with {@link RecipesProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link ContentProvider} assumes that {@link Uri} are generated
 * using stronger {@link String} identifiers, instead of {@code int}
 * {@link BaseColumns#_ID} values, which are prone to shuffle during sync.
 */
public class RecipesContract {

    public interface DishColumns extends BaseColumns {
        /** Unique string identifying parent dish. */
    	String IDP = "idp";
        /** Name describing this dish. */
        String NAME = "dish";
        String COUNT = "count";
    }

    public interface KitchenColumns extends BaseColumns {
        /** Name of the kitchen. */
        String KITCHEN = "kitchen";
        String COUNT = "count";
    }
    
    public interface ProductsColumns extends BaseColumns {
        /** Name of the product. */
        String NAME = "name";
        String CALORIES = "calories";
    }

    public interface ItemsColumns extends BaseColumns {
        /** Name of the product. */
        String NAME = "name";
    }

    public interface RecipeProductColumns extends BaseColumns {
        String PRODUCT_ID = "product_id";
        String AMOUNT = "amount";
        String ITEM = "item";
    }
    
    public interface RecipeColumns extends BaseColumns {
    	/** Unique id identifying this recipe parent. */
        String IDP = "idp";
        /** Name describing this recipe. */
        String NAME = "name";
        /** Recipe make. */
        String STEPS = "steps";
        /** Recipe portion. */
        String PORTION = "portion";
        /** Recipe time. */
        String TIME = "time";
        /** Recipe kitchen. */
        String KITCHEN = "kitchen";
        /** Product IDs */
        String P1 = "p1";
        String P2 = "p2";
        String P3 = "p3";
        String P4 = "p4";
        String P5 = "p5";
        String P6 = "p6";
        String P7 = "p7";
        String P8 = "p8";
        String P9 = "p9";
        String P10 = "p10";
        String P11 = "p11";
        String P12 = "p12";
        String P13 = "p13";
        String P14 = "p14";
        String P15 = "p15";
        String P16 = "p16";
        String P17 = "p17";
        String P18 = "p18";
        String P19 = "p19";
        String P20 = "p20";
        /** User-specific flag indicating starred status. */
        String STARRED = "starred";
        String BASKET = "basket";
    }

    public interface UnitsColumns extends BaseColumns {
        String IDP = "idp";
        String PRODUCT = "product";
        String W1 = "w1";
        String W2 = "w2";
        String W3 = "w3";
        String W4 = "w4";
        String W5 = "w5";
    }
    
    public interface EColumns extends BaseColumns {
        String IDP = "idp";
        String TYPE = "type";
        String NUMBER = "number";
        String NAME = "name";
        String DESCRIPTION = "description";
    }
    
    public interface RefColumns extends BaseColumns {
        String NAME = "name";
        String DESCRIPTION = "description";
    }
    
    public static final String CONTENT_AUTHORITY = "org.m5";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_DISH = "dish";
    private static final String PATH_KITCHEN = "kitchen";
    private static final String PATH_RECIPE = "recipe";
    private static final String PATH_STARRED = "starred";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";
    private static final String PATH_PRODUCTS = "products";
    private static final String PATH_UNITS = "units";
    private static final String PATH_E = "e";
    private static final String PATH_REF = "reference";
    private static final String PATH_PROFILE = "profile";
    

    /**
     * Dish are overall categories for {@link Recipes}
     */
    public static class Dish implements DishColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.m5.dish";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = DishColumns.NAME + " ASC";

        /** Build {@link Uri} for requested {@link #ID}. */
        public static Uri buildDishUri(String dishId) {
            return CONTENT_URI.buildUpon().appendPath(dishId).build();
        }

        /**
         * Build {@link Uri} that references any {@link Recipe} associated
         * with the requested {@link #ID}.
         */
        public static Uri buildRecipeUri(String dishId) {
            return CONTENT_URI.buildUpon().appendPath(dishId).appendPath(PATH_RECIPE).build();
        }

        /** Read {@link #ID} from {@link Dish} {@link Uri}. */
        public static String getDishId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Each recipe is a block that has instructions and ingredients
     */
    public static class Recipe implements RecipeColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();
        public static final Uri CONTENT_STARRED_URI =
        		CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.m5.recipe";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.m5.recipe";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = Tables.RECIPE + "." + RecipeColumns.NAME + " COLLATE NOCASE ASC";
        
        /** Build {@link Uri} for requested {@link #RECIPE_ID}. */
        public static Uri buildRecipeUri(String recipeId) {
            return CONTENT_URI.buildUpon().appendPath(recipeId).build();
        }

        public static Uri buildSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static boolean isSearchUri(Uri uri) {
            try {
            	return PATH_SEARCH.equals(uri.getPathSegments().get(1));
            } catch(Exception e) {
            	return true;
            }
        }

        public static boolean isStarredUri(Uri uri) {
            return PATH_STARRED.equals(uri.getPathSegments().get(1));
        }
        
        /** Read {@link #_ID} from {@link Recipe} {@link Uri}. */
        public static String getRecipeId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static class RecipeProducts implements RecipeProductColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();
        public static final Uri CONTENT_PRODUCTS_URI =
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCTS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.m5.basket";
        
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT =  Tables.PRODUCTS + "." + ProductsColumns.NAME + " COLLATE NOCASE ASC";
        
        /** Build {@link Uri} for requested {@link #RECIPE_ID}. */
        public static Uri buildProductsUri(String recipeId) {
            return CONTENT_URI.buildUpon().appendPath(recipeId).appendPath(PATH_PRODUCTS).build();
        }
        
        /** Read {@link #_ID} from {@link Recipe} {@link Uri}. */
        public static String getRecipeId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Each Kitchen is a set of associated recipes.
     */
    public static class Kitchen implements KitchenColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_KITCHEN).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.m5.kitchen";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.m5.kitchen";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = KitchenColumns.KITCHEN + " COLLATE NOCASE ASC";

        public static Uri buildRecipeUri(String kitchenId) {
            return CONTENT_URI.buildUpon().appendPath(kitchenId).appendPath(PATH_RECIPE).build();
        }

        public static Uri buildSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static boolean isSearchUri(Uri uri) {
            return PATH_SEARCH.equals(uri.getPathSegments().get(1));
        }

        /** Read {@link #KITCHEN} from {@link KITCHEN} {@link Uri}. */
        public static String getKitchenId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static class SearchSuggest {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();

        public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
                + " COLLATE NOCASE ASC";
    }

    public static class Units implements UnitsColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_UNITS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.m5.units";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = UnitsColumns.PRODUCT + " ASC";

        /** Build {@link Uri} for requested {@link #ID}. */
        public static Uri buildUnitsUri(String idp) {
            return CONTENT_URI.buildUpon().appendPath(idp).build();
        }
        
        public static String getIdp(Uri uri) {
        	String ret = "0";
        	try { ret = uri.getPathSegments().get(1); } catch(Exception e) {}
        	return ret; 
        }
    }
    
    public static class E implements EColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_E).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.m5.e";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = EColumns.NUMBER + " ASC";

        /** Build {@link Uri} for requested {@link #ID}. */
        public static Uri buildEUri(String idp) {
            return CONTENT_URI.buildUpon().appendPath(idp).build();
        }
        
        public static String getIdp(Uri uri) {
        	String ret = "0";
        	try { ret = uri.getPathSegments().get(1); } catch(Exception e) {}
        	return ret; 
        }
    }
    
    public static class Reference implements RefColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REF).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.m5.reference";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = RefColumns.NAME + " ASC";

        /** Build {@link Uri} for requested {@link #ID}. */
        public static Uri buildRefUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
        
        public static String getId(Uri uri) {
        	return uri.getPathSegments().get(1);
        }
    }
    
    public static class Profile {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.m5.profile";
    }
    
    private RecipesContract() {
    }
}
