package com.example.finalmobilecomputingproject;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_EDIT = 2;

    private ImageButton uiTakePictureImageButton;
    private TextView uiMessageTextView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        uiTakePictureImageButton = findViewById(R.id.ImageScreenTextView);
        uiMessageTextView = findViewById(R.id.TextResultTextView);

        //long click to edit images
        uiTakePictureImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editImage();
                return true;
            }
        });
    }

    //open camera intent
    public void openCamera(View view){
        if(askForCameraPermission()){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File sourceFile;
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                sourceFile = null;
                try{
                    sourceFile = createImageFile();
                }catch (IOException ex){
                    //error
                }
                if(sourceFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.finalmobilecomputingproject.provider",
                            sourceFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    public void editImage(){
        if(currentPhotoPath != null){
            Intent editIntent = new Intent(Intent.ACTION_EDIT);
            editIntent.setDataAndType(Uri.parse(currentPhotoPath), "image/*");
            editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(editIntent, REQUEST_IMAGE_EDIT);
        }else{
            //TODO alert to say "need to take picture first before you can edit"
        }
    }

    private boolean askForCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    100);
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
        }else{
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get the image which was taken back from the user
        if (resultCode == RESULT_OK && (requestCode == REQUEST_IMAGE_EDIT || requestCode == REQUEST_IMAGE_CAPTURE)) {
            uiTakePictureImageButton.setImageDrawable(null);//refresh
            uiTakePictureImageButton.setImageURI(Uri.parse(currentPhotoPath));
        }else{
            Log.d("URI", currentPhotoPath);
        }
    }

    //TODO create static class and make this static method
    private File createImageFile() throws IOException {//from android development page: https://developer.android.com/training/camera/photobasics
        // Create an image file name
        String imageFileName = "TEMP_IMG";
        Log.d("fileName", imageFileName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void readImage(View view) {
        FirebaseVisionImage image;
        try{
            image = FirebaseVisionImage.fromFilePath(this, Uri.fromFile(new File(currentPhotoPath)));
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // ...
                                    String result = firebaseVisionText.getText();
                                    uiMessageTextView.setText(result);
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
