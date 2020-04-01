package com.example.finalmobilecomputingproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageData{
    private int mId;
    private String mDate;
    private String mOriginText;
    private String mTranslatedText;

    public ImageData(int id, String translatedText, String originText, String date){
        mId = id;
        mDate = date;
        mTranslatedText = translatedText;
        mOriginText = originText;
    }

    public void setmDate(String date){
        mDate = date;
    }

    public int getmId(){
        return mId;
    }

    public String getDate(){
        return mDate;
    }

    public String getmOriginText(){
        return mOriginText;
    }

    public String getmTranslatedText(){
        return mTranslatedText;
    }
}
