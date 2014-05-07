package org.m5.provider;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class CustomContext extends ContextWrapper {

    public CustomContext(Context base) {
       super(base);
    }

    /**
     * We override the default open/create as it doesn't work on OS 1.6 and
     * causes issues on other devices too.
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
       File f = new File(name);
       SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(f, factory);
       return db;
    }

 }