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
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.ArrayList;

public class ImageToText implements OnSuccessListener<FirebaseVisionText>, OnFailureListener, ImageToTextSubject{

    private static ImageToText imageToText;
    private String mImageText;
    private ArrayList<ImageToTextObserver> mObservers;
    private boolean mSuccessfulRead = false;

    public static ImageToText getInstance(){
        if(imageToText == null){
            imageToText = new ImageToText();
        }
        return imageToText;
    }

    private ImageToText() { mObservers = new ArrayList<ImageToTextObserver>(); }

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
        if(!mImageText.equals("")){
            mSuccessfulRead = true;
        }else{
            mSuccessfulRead = false;
        }
        notifyObservers();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
        mImageText = "";
        mSuccessfulRead = false;
        notifyObservers();
    }

    @Override
    public void addObserver(ImageToTextObserver o) {
        if(!mObservers.contains(o)){
            mObservers.add(o);
        }
    }

    @Override
    public void removeObserver(ImageToTextObserver o) {
        mObservers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for(ImageToTextObserver o : mObservers){
            if(mSuccessfulRead){
                o.updateText(mImageText);
            }else{
                o.updateTextError();
            }
        }
    }
}
