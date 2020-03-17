package com.example.finalmobilecomputingproject;

import android.content.Context;
import android.net.Uri;
import android.util.JsonToken;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextToTextTranslation{

    private FirebaseTranslator englishSpanishTranslator;
    private String mText;
    private Observer mObserver;
    final private String API_KEY = "AIzaSyCH-emgcqcZFsbKnB34yRkVN-nLR-6v0_g";

    TextToTextTranslation(Observer observer){
        mObserver = observer;
    }

    ArrayList<String> getAllLanguages(){

        //TODO needs to be changed to another method, maybe manually write
        Set<Integer> intValues = FirebaseTranslateLanguage.getAllLanguages();
        ArrayList<String> returnLanguagesList = new ArrayList<String>();

        for (int in : intValues){
            returnLanguagesList.add(FirebaseTranslateLanguage.languageCodeForLanguage(in));
        }

        return returnLanguagesList;
    }

    void TranslateText(String text, String fromLanguage, String toLanguage, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Uri.parse("https://www.googleapis.com/language/translate/v2")
                .buildUpon()
                .appendQueryParameter("key", API_KEY)
                .appendQueryParameter("source", fromLanguage)
                .appendQueryParameter("target", toLanguage)
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
                            mObserver.updateTranslatedText(array.getJSONObject(0).get("translatedText").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "Error");
                //TODO error
            }
        });

        queue.add(stringRequest);
    }

}
