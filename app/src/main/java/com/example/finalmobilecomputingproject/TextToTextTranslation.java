package com.example.finalmobilecomputingproject;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextToTextTranslation implements OnSuccessListener<Void>, OnFailureListener{

    private FirebaseTranslator englishSpanishTranslator;
    private String mText;
    private Observer mObserver;

    TextToTextTranslation(Observer observer){
        mObserver = observer;


    }

    void TranslateText(String text){
        mText = text;

        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.FR)
                .build();

        englishSpanishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        englishSpanishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(this)
                .addOnFailureListener(this);
    }

    @Override
    public void onSuccess(Void aVoid) {
        englishSpanishTranslator.translate(mText)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                mObserver.updateTranslatedText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                            }
                        });
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
    }

    ArrayList<String> getAllLanguages(){

        Set<Integer> intValues = FirebaseTranslateLanguage.getAllLanguages();
        ArrayList<String> returnLanguagesList = new ArrayList<String>();

        for (int in : intValues){
            returnLanguagesList.add(FirebaseTranslateLanguage.languageCodeForLanguage(in));
        }

        return returnLanguagesList;
    }
}
