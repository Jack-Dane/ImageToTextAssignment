package com.jdevelopment.imagetranslator;


public class ImageData{
    private int mId;
    private String mDate;
    private String mOriginText;
    private String mTranslatedText;
    private String mOriginLanguage;
    private String mTranslatedLanguage;

    public ImageData(int id, String translatedText, String originText, String date, String originLanguage, String translatedLanguage){
        mId = id;
        mDate = date;
        mTranslatedText = translatedText;
        mOriginText = originText;
        mOriginLanguage = originLanguage;
        mTranslatedLanguage = translatedLanguage;
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

    public String getmOriginLanguage(){
        return mOriginLanguage;
    }

    public String getmTranslatedLanguage(){
        return mTranslatedLanguage;
    }
}
