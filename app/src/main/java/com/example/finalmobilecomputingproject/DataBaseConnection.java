package com.example.finalmobilecomputingproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.net.ConnectException;

public class DataBaseConnection extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TranslatorDatabase";
    public static final int DATABASE_VERSION = 1;

    public DataBaseConnection(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE ImageData (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TRANSLATED_MESSAGE TEXT," +
                "IMAGE BLOB," +
                "DATE TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertImageData(String translatedText, byte[] image, String date){
        SQLiteDatabase translateDatabase = getWritableDatabase();
        ContentValues insertValued = new ContentValues();
        insertValued.put("TRANSLATED_TEXT", translatedText);
        insertValued.put("IMAGE", image);
        insertValued.put("DATE", date);
        translateDatabase.insert("ImageData", null, insertValued);
    }

    public void getAllFromDatabase(){

    }

}
