package com.jdevelopment.imagetranslator;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TextToTextTranslation implements TextToTextSubject {

    private static TextToTextTranslation textToTextTranslation;
    private String mText;
    private String mOriginLanguage;
    private String mTranslatedLanguage;
    private ArrayList<TextToTextTranslationObserver> mObservers;
    private boolean mSuccessfulTranslation = false;

    final private String API_KEY = "AIzaSyCH-emgcqcZFsbKnB34yRkVN-nLR-6v0_g";

    public static TextToTextTranslation getInstance(){
        if(textToTextTranslation == null){
            textToTextTranslation = new TextToTextTranslation();
        }
        return textToTextTranslation;
    }

    private TextToTextTranslation(){
        mObservers = new ArrayList<TextToTextTranslationObserver>();
    }

    void TranslateText(String text, String fromLanguage, String toLanguage, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);

        mOriginLanguage = fromLanguage;
        mTranslatedLanguage = toLanguage;

        String url = Uri.parse("https://www.googleapis.com/language/translate/v2")
                .buildUpon()
                .appendQueryParameter("key", API_KEY)
                .appendQueryParameter("source", fromLanguage)
                .appendQueryParameter("target", toLanguage)
                .appendQueryParameter("format", "text")
                .appendQueryParameter("q", text)
                .build().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject data = json.getJSONObject("data");
                            JSONArray array = data.getJSONArray("translations");
                            if(array.getJSONObject(0).has("detectedSourceLanguage")){
                                mOriginLanguage = array.getJSONObject(0).getString("detectedSourceLanguage");
                            }
                            mText = array.getJSONObject(0).getString("translatedText");
                            mSuccessfulTranslation = true;
                            notifyObservers();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSuccessfulTranslation = false;
                mText = "";
                notifyObservers();
            }
        });

        queue.add(stringRequest);
    }

    @Override
    public void addObserver(TextToTextTranslationObserver o) {
        if(!mObservers.contains(o)){
            mObservers.add(o);
        }
    }

    @Override
    public void removeObserver(TextToTextTranslationObserver o) {
        mObservers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (TextToTextTranslationObserver o: mObservers) {
            if(mSuccessfulTranslation){
                o.updateTranslatedText(mText, mOriginLanguage, mTranslatedLanguage);
            }else{
                o.updateTranslatedTextError();
            }
        }
    }
}