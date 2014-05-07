package org.m5.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.Deflater;

import org.m5.R;
import org.m5.ui.HomeActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


/**
 * Get Started!
 * @author yakovlev.yuriy@gmail.com
 */
public class DataHandler {
	private static final String TAG = "DataHandler";
	private SQLiteDatabase db;
	private Context context;
	private final static String SEP = ";";
    
	
	public DataHandler(Context context, SQLiteDatabase db) {
		 this.context = context;
		 this.db = db;
	}
	
	
    public void insertDish() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_dish));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.dish));
            reader = new BufferedReader(in, 1024);
            db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into dish (_id, idp, dish, count) values (?,?,?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindLong(2, Integer.valueOf(data[1]));
                	stmt.bindString(3, data[2]);
                	try { stmt.bindLong(4, Integer.valueOf(data[3])); } catch(Exception e) { stmt.bindNull(4); }
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }

    public void insertKitchen() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_kitchen));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.kitchen));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into kitchen (_id, kitchen, count) values (?,?,?)");
    		while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindString(2, data[1]);
                	try { stmt.bindLong(3, Integer.valueOf(data[2])); } catch(Exception e) { stmt.bindNull(3); }
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertProducts() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_product));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.products));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into products (_id, name, calories) values (?,?,?)");
    		while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	try { stmt.bindString(2, data[1]); } catch(Exception e) { stmt.bindNull(2); }
                	try { stmt.bindString(3, data[2]); } catch(Exception e) { stmt.bindNull(3); }
                	stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertItems() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_item));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.items));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into items (_id, name) values (?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindString(2, data[1]);
                	stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertRecipeProduct() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_recipe_product));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.recipe_product));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		
    		stmt = db.compileStatement("insert into recipe_product (_id, product_id, amount, item) values (?,?,?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	int dlen = data.length;
        			stmt.bindLong(1, Integer.valueOf(data[0]));
            		stmt.bindLong(2, Integer.valueOf(data[1]));
            		if(dlen > 2) {
            			stmt.bindString(3, data[2]);
        			} else {
        				stmt.bindNull(3);
        			}
        			if(dlen > 3) {
                		stmt.bindLong(4, Integer.valueOf(data[3]));
        			} else {
        				stmt.bindNull(4);
        			}
            		stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertRecipe() {
    	int[] recipes = {R.raw.recipe1, R.raw.recipe2, R.raw.recipe3, R.raw.recipe4, R.raw.recipe5, R.raw.recipe6, R.raw.recipe7, R.raw.recipe8, R.raw.recipe9}; 
        for(int i=1; i<recipes.length; i++) {
    		HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_recipe) + " " + i + "000");
            //migrateRecipe("r"+i+".csv");
    		insertRecipe(recipes[i]);
        }
    	
    }
    
    //private Pattern p = Pattern.compile("([x]?[0-9]+(\\,)?(\\.)?(\\-)?(\\+)?(\\/)?([0-9]+)?)+");
    private void insertRecipe(int rawId) {
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(rawId));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into recipe (_id, idp, name, steps, portion, time, kitchen, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    		byte[] buf = null;
    		byte[] ip = null;
    		while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindLong(2, Integer.valueOf(data[1]));
                	stmt.bindString(3, data[2]);
            		
	    			ip = data[3].getBytes("UTF-8");
	          		Deflater compressor = new Deflater();
	        		compressor.setLevel(Deflater.BEST_COMPRESSION);
	        		compressor.setInput(ip);
	        		compressor.finish();
	        		ByteArrayOutputStream bos = new ByteArrayOutputStream(ip.length);
	        		buf = new byte[ip.length*2];
	        		int count = compressor.deflate(buf);
	        		bos.write(buf, 0, count); 
        			bos.close();
        			stmt.bindBlob(4, bos.toByteArray());
        			stmt.bindString(5, data[4]);
        			stmt.bindString(6, data[5]);
        			try { stmt.bindLong(7, Integer.valueOf(data[6])); } catch(Exception e) { stmt.bindNull(7); }
        			
        			try { stmt.bindLong(8, Integer.valueOf(data[7])); } catch(Exception e) { stmt.bindNull(8); }
        			try { stmt.bindLong(9, Integer.valueOf(data[8])); } catch(Exception e) { stmt.bindNull(9); }
        			try { stmt.bindLong(10, Integer.valueOf(data[9])); } catch(Exception e) { stmt.bindNull(10); }
        			try { stmt.bindLong(11, Integer.valueOf(data[10])); } catch(Exception e) { stmt.bindNull(11); }
        			try { stmt.bindLong(12, Integer.valueOf(data[11])); } catch(Exception e) { stmt.bindNull(12); }
        			try { stmt.bindLong(13, Integer.valueOf(data[12])); } catch(Exception e) { stmt.bindNull(13); }
        			try { stmt.bindLong(14, Integer.valueOf(data[13])); } catch(Exception e) { stmt.bindNull(14); }
        			try { stmt.bindLong(15, Integer.valueOf(data[14])); } catch(Exception e) { stmt.bindNull(15); }
        			try { stmt.bindLong(16, Integer.valueOf(data[15])); } catch(Exception e) { stmt.bindNull(16); }
        			try { stmt.bindLong(17, Integer.valueOf(data[16])); } catch(Exception e) { stmt.bindNull(17); }
        			try { stmt.bindLong(18, Integer.valueOf(data[17])); } catch(Exception e) { stmt.bindNull(18); }
        			try { stmt.bindLong(19, Integer.valueOf(data[18])); } catch(Exception e) { stmt.bindNull(19); }
        			try { stmt.bindLong(20, Integer.valueOf(data[19])); } catch(Exception e) { stmt.bindNull(20); }
        			try { stmt.bindLong(21, Integer.valueOf(data[20])); } catch(Exception e) { stmt.bindNull(21); }
        			try { stmt.bindLong(22, Integer.valueOf(data[21])); } catch(Exception e) { stmt.bindNull(22); }
        			try { stmt.bindLong(23, Integer.valueOf(data[22])); } catch(Exception e) { stmt.bindNull(23); }
        			try { stmt.bindLong(24, Integer.valueOf(data[23])); } catch(Exception e) { stmt.bindNull(24); }
        			try { stmt.bindLong(25, Integer.valueOf(data[24])); } catch(Exception e) { stmt.bindNull(25); }
        			try { stmt.bindLong(26, Integer.valueOf(data[25])); } catch(Exception e) { stmt.bindNull(26); }
        			try { stmt.bindLong(27, Integer.valueOf(data[26])); } catch(Exception e) { stmt.bindNull(27); }
            		
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    /*private void migrateRecipe(String fileName) {
    	Log.d(TAG,"read file: " + fileName);
    	
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        SQLiteStatement stmtProducts = null;
        SQLiteStatement stmtItems = null;
        SQLiteStatement stmtRP = null;
        try{
        	in = new InputStreamReader(context.getAssets().open(fileName));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		
    		stmt = db.compileStatement("insert into recipe (_id, idp, name, steps, portion, time, kitchen, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    		stmtProducts = db.compileStatement("insert into products (_id, name) values (?,?)");
            stmtItems = db.compileStatement("insert into items (_id, name) values (?,?)");
            stmtRP = db.compileStatement("insert into recipe_product (product_id, amount, item) values (?,?,?)");
            a: while(((text = reader.readLine()) != null)) {
                //if(!(text.length() > 1024)) {
                	data = text.split(SEP);
            		stmt.bindLong(1, Integer.valueOf(data[0]));
            		stmt.bindLong(2, Integer.valueOf(data[1]));
            		stmt.bindString(3, data[2]);
            		stmt.bindString(4, data[4]);
            		try {
            			stmt.bindLong(5, Integer.valueOf(data[5]));
            		} catch(Exception e) { stmt.bindNull(5); }
            		try {
            			stmt.bindLong(6, Integer.valueOf(data[6]));
            		} catch(Exception e) { stmt.bindNull(6); }
            		try {
            			stmt.bindLong(7, Integer.valueOf(data[7]));
            		} catch(Exception e) { stmt.bindNull(7); }
            		// Process products
	    			String[] products = data[3].split("\\$");
	    			int plen = products.length;
	    			if(plen > 20) continue a;
	    			for(int counter=0; counter<20; counter++) {
	    				if(counter >= plen) {
	    					stmt.bindNull(8 + counter);
	    				} else {
		    				String product = products[counter];
		    				try {
			    				if(!"".equals(product)) {
			    					int i = product.indexOf(" - ");
				    				String name = null;
				    				String amount = null;
				    				if(i > 0) {
				    					name = product.substring(0, i).trim();
					    				amount = product.substring(i+2).trim();
				    				} else {
				    					name = product;
				    				}
				    				Cursor c = db.query(Tables.PRODUCTS, new String[]{ProductsColumns._ID, ProductsColumns.NAME}, ProductsColumns.NAME+" like '"+name+"'", null, null, null, null);
				    				long product_id = -1;
				    				if(c.getCount() > 0 && c.moveToFirst()) {
				    					product_id = c.getInt(0);
				    				}
				    				c.close();
				    				if(product_id == -1) {
				    					stmtProducts.bindNull(1);
				    					stmtProducts.bindString(2, name);
				    					product_id = stmtProducts.executeInsert();
				    				}
				    				stmtRP.bindLong(1, product_id);
			    					
				    				StringBuilder rpWhere = new StringBuilder(24);
				    				rpWhere.append(RecipeProductColumns.PRODUCT_ID+"="+product_id);
				    				// Process amount
				    				if(amount != null) {
					    				int a = amount.indexOf(" ");
					    				String _amount = null;
				    					String item = null;
				    					if(a > 0) {
					    					_amount = amount.substring(0, a).trim().replaceAll(" ", "");
						    				item = amount.substring(a+1).trim();
						    				Matcher m = p.matcher(_amount);
						    				if(!m.matches()) {
						    					item = amount;
						    					_amount = null;
						    				}
					    				} else {
					    					item = amount;
					    				}
				    					c = db.query(Tables.ITEMS, new String[]{ItemsColumns._ID, ItemsColumns.NAME}, ItemsColumns.NAME+" like '"+item+"'", null, null, null, null);
					    				long item_id = -1;
					    				if(c.getCount() > 0 && c.moveToFirst()) {
					    					item_id = c.getInt(0);
					    				}
					    				c.close();
					    				if(item_id == -1) {
					    					stmtItems.bindNull(1);
					    					stmtItems.bindString(2, item);
					    					item_id = stmtItems.executeInsert();
					    				}
				    					if(_amount == null) {
				    						stmtRP.bindNull(2);
				    					} else {
				    						stmtRP.bindString(2, _amount);
				    						rpWhere.append(" and " + RecipeProductColumns.AMOUNT+" like '"+_amount+"'");
					    					
				    					}
					    				stmtRP.bindLong(3, item_id);
					    				rpWhere.append(" and "+RecipeProductColumns.ITEM+"="+item_id);
					    			} else {
				    					stmtRP.bindNull(2);
				    					stmtRP.bindNull(3);
					    			}
				    				
				    				c = db.query(Tables.RECIPE_PRODUCT, new String[]{RecipeProductColumns._ID}, rpWhere.toString(), null, null, null, null);
				    				long rp_id = -1;
				    				if(c.getCount() > 0 && c.moveToFirst()) {
				    					rp_id = c.getInt(0);
				    				}
				    				c.close();
				    				if(rp_id == -1) {
				    					rp_id = stmtRP.executeInsert();
				    				}
				    				stmt.bindLong(8+counter, rp_id);
			    				}
		    				} catch(Exception e) {
		    		    		Log.e("#######", e.toString());
		    				}
	    				}
	    			}
	    			stmt.execute();
                //}
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
                stmtProducts.close();
                stmtItems.close();
                stmtRP.close();
                if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }*/

    public void insertUnits() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_units));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.units));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into units (idp, product, w1, w2, w3, w4, w5) values (?,?,?,?,?,?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindString(2, data[1]);
                	try { stmt.bindLong(3, Integer.valueOf(data[2])); } catch(Exception e) { stmt.bindNull(3); }
                	try { stmt.bindLong(4, Integer.valueOf(data[3])); } catch(Exception e) { stmt.bindNull(4); }
                	try { stmt.bindLong(5, Integer.valueOf(data[4])); } catch(Exception e) { stmt.bindNull(5); }
                	try { stmt.bindLong(6, Integer.valueOf(data[5])); } catch(Exception e) { stmt.bindNull(6); }
                	try { stmt.bindLong(7, Integer.valueOf(data[6])); } catch(Exception e) { stmt.bindNull(7); }
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertE() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_e));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.e));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into e (idp, type, number, name, description) values (?,?,?,?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindLong(1, Integer.valueOf(data[0]));
                	stmt.bindLong(2, Integer.valueOf(data[1]));
                	stmt.bindString(3, data[2]);
                	stmt.bindString(4, data[3]);
                	try { stmt.bindString(5, data[4]); } catch(Exception e) { stmt.bindNull(5); }
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
    public void insertReference() {
    	HomeActivity.updateProgress(context.getResources().getString(R.string.status_insert_reference));
        
    	BufferedReader reader = null;
        InputStreamReader in = null;
        SQLiteStatement stmt = null;
        try{
        	in = new InputStreamReader(context.getResources().openRawResource(R.raw.reference));
            reader = new BufferedReader(in, 1024);
        	db.beginTransaction();
    		String[] data = null;
    		String text = null;
    		stmt = db.compileStatement("insert into reference (name, description) values (?,?)");
            while(((text = reader.readLine()) != null)) {
                if(!(text.length() > 1024)) {
                	data = text.split(SEP);
                	stmt.bindString(1, data[0]);
                	stmt.bindString(2, data[1]);
        			stmt.execute();
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e) {
        	Log.e(TAG, e.toString());
        } finally {
        	try {
        		db.endTransaction();
        		stmt.close();
        		if(reader != null) {
                    reader.close();
                }
                if(in != null) {
                	in.close();
                }
            } catch(Exception e) {
            	Log.e(TAG, e.toString());
            }
        }
    }
    
}
