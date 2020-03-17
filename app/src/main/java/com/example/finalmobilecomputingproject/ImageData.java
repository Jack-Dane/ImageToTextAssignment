package com.example.finalmobilecomputingproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageData{
    public int mId;
    public String mDate;
    public String mOriginText;
    public String mTranslatedText;

    public ImageData(int id, String translatedText, String originText, String date){
        mId = id;
        mDate = date;
        mTranslatedText = translatedText;
        mOriginText = originText;
    }

}
