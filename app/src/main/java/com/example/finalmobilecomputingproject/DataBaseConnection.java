package com.example.finalmobilecomputingproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.ConnectException;
import java.util.ArrayList;

public class DataBaseConnection extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TranslatorDatabase";
    public static final int DATABASE_VERSION = 2;

    public DataBaseConnection(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE ImageData (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TRANSLATED_MESSAGE TEXT," +
                "ORIGIN_MESSAGE TEXT," +
                "DATE TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertImageData(String translatedText, String originText, String date){
        SQLiteDatabase translateDatabase = getWritableDatabase();
        ContentValues insertValued = new ContentValues();
        insertValued.put("TRANSLATED_MESSAGE", translatedText);
        insertValued.put("ORIGIN_MESSAGE", originText);
        insertValued.put("DATE", date);
        translateDatabase.insert("ImageData", null, insertValued);
    }

    public ArrayList<ImageData> getAllFromDatabase(){
        SQLiteDatabase db;

        /*
        //DELETE DATABASE CONTENT
        db = getWritableDatabase();
        db.execSQL("delete from ImageData");
         */

        ArrayList<ImageData> imageDataList = new ArrayList<>();

        db = getReadableDatabase();
        Cursor cursor = db.query("ImageData", new String[] { "_id", "TRANSLATED_MESSAGE", "ORIGIN_MESSAGE", "DATE"},
                null, null, null, null, "DATE" );

        int id;
        String translatedText;
        String date;
        String originText;

        if(cursor != null) {
            if(cursor.moveToFirst()){
                do{
                    id = Integer.parseInt(cursor.getString(0));
                    translatedText = cursor.getString(1);
                    originText = cursor.getString(2);
                    date = cursor.getString(3);

                    imageDataList.add(new ImageData(id, translatedText, originText, date));
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
