package com.jdevelopment.imagetranslator.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseConnection extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TranslatorDatabase";
    public static final int DATABASE_VERSION = 7;

    public DataBaseConnection(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE ImageData (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TRANSLATED_MESSAGE TEXT," +
                "ORIGIN_MESSAGE TEXT," +
                "DATE TEXT," +
                "ORIGIN_LANGUAGE TEXT," +
                "TRANSLATED_LANGUAGE TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertImageData(String translatedText, String originText, String date, String originLanguage, String translatedLanguage){
        SQLiteDatabase translateDatabase = getWritableDatabase();
        ContentValues insertValued = new ContentValues();
        insertValued.put("TRANSLATED_MESSAGE", translatedText);
        insertValued.put("ORIGIN_MESSAGE", originText);
        insertValued.put("DATE", date);
        insertValued.put("ORIGIN_LANGUAGE", originLanguage);
        insertValued.put("TRANSLATED_LANGUAGE", translatedLanguage);
        translateDatabase.insert("ImageData", null, insertValued);
    }

    public ArrayList<ImageData> getAllFromDatabase(){
        SQLiteDatabase db;

        ArrayList<ImageData> imageDataList = new ArrayList<>();

        db = getReadableDatabase();
        Cursor cursor = db.query("ImageData", new String[] { "_id", "TRANSLATED_MESSAGE", "ORIGIN_MESSAGE", "DATE", "ORIGIN_LANGUAGE", "TRANSLATED_LANGUAGE"},
                null, null, null, null, "_id DESC" );

        int id;
        String translatedText;
        String date;
        String originText;
        String translatedLanguage;
        String originLanguage;

        if(cursor != null) {
            if(cursor.moveToFirst()){
                do{
                    id = Integer.parseInt(cursor.getString(0));
                    translatedText = cursor.getString(1);
                    originText = cursor.getString(2);
                    date = cursor.getString(3);
                    originLanguage = cursor.getString(4);
                    translatedLanguage = cursor.getString(5);

                    imageDataList.add(new ImageData(id, translatedText, originText, date, originLanguage, translatedLanguage));
                }while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }

        return imageDataList;
    }

    public void deleteRow(int id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("ImageData","_id = ?", new String[]{ Integer.toString(id)});
    }

}
