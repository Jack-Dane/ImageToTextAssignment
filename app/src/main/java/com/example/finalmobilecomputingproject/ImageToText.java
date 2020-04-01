package com.example.finalmobilecomputingproject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.ArrayList;

public class ImageToText implements OnSuccessListener<FirebaseVisionText>, OnFailureListener, Observable{

    private String mImageText;
    private ArrayList<Observer> mObservers;

    ImageToText() { mObservers = new ArrayList<Observer>(); }

    void convertImage(Bitmap bp){
        FirebaseVisionImage image;
        image = FirebaseVisionImage.fromBitmap(bp);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        final Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(this)
                        .addOnFailureListener(this);
    }

    @Override
    public void onSuccess(FirebaseVisionText firebaseVisionText) {
        mImageText = firebaseVisionText.getText();
        notifyObservers();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        mImageText = "";
        notifyObservers();
    }

    @Override
    public void addObserver(Observer o) {
        mObservers.add(o);
    }

    @Override
    public void removerObserver(Observer o) {
        mObservers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for(Observer o : mObservers){
            o.updateText(mImageText);
        }
    }
}
