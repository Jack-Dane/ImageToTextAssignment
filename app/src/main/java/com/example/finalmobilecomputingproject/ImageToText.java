package com.example.finalmobilecomputingproject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;

public class ImageToText implements OnSuccessListener<FirebaseVisionText>, OnFailureListener{

    private Observer mObserver;
    private String mImageText;

    ImageToText(Observer observer){
        mObserver = observer;
    }

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
        mObserver.updateText(mImageText);
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        mImageText = "Failed to read any text";
        mObserver.updateText(mImageText);
    }
}
