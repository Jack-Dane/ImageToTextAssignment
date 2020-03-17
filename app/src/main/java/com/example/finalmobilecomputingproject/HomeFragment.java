package com.example.finalmobilecomputingproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements Observer{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_EDIT = 2;

    //UI elements
    private ImageButton uiTakePictureImageButton;
    private TextView uiMessageTextView;
    private Spinner uiOriginLanguageSpinner;
    private Spinner uiDestinationLanguageSpinner;

    //Misc
    private String currentPhotoPath;
    private File currentPhotoFile;
    private TextToTextTranslation mTextToTextTranslation;
    private ImageToText imageToText;
    private String currentlyTranslatedText;
    private Button uiSaveButton;
    private DataBaseConnection dbConnection;
    private String originText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        createImageFile();

        dbConnection = new DataBaseConnection(rootView.getContext());

        uiTakePictureImageButton = rootView.findViewById(R.id.ImageScreenTextView);
        uiMessageTextView = rootView.findViewById(R.id.TextResultTextView);
        uiOriginLanguageSpinner = rootView.findViewById(R.id.uiLanguageOriginSelectSpinner);
        uiDestinationLanguageSpinner = rootView.findViewById(R.id.uiLanguageDestinationSelectSpinner);
        uiSaveButton = rootView.findViewById(R.id.uiSaveButton);
        Button uiShareButton = rootView.findViewById(R.id.uiShareButton);

        imageToText = new ImageToText(this);

        mTextToTextTranslation = new TextToTextTranslation(this);
        ArrayList<String> availableLanguages = mTextToTextTranslation.getAllLanguages();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(), R.layout.support_simple_spinner_dropdown_item, availableLanguages);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        uiOriginLanguageSpinner.setAdapter(adapter);
        uiDestinationLanguageSpinner.setAdapter(adapter);

        //long click to edit images
        uiTakePictureImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editImage();
                return true;
            }
        });

        uiTakePictureImageButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                openCamera(v);
            }
        });

        uiShareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                shareButtonPress(v);
            }
        });

        uiSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    save();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    private void openCamera(View view){
        if(askForCameraPermission()){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.finalmobilecomputingproject.provider",
                        currentPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else{
            //TODO "Need to allow permission for app to take pictures" TOAST class!!!!
        }
    }

    private void shareButtonPress(View view){
        //TODO share image and text via different methods
        Intent shareIntent = new Intent();
        Uri imageUri = FileProvider.getUriForFile(
                Objects.requireNonNull(getActivity()),
                "com.example.finalmobilecomputingproject.provider",
                currentPhotoFile);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentlyTranslatedText);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, null));
    }

    private void editImage(){
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
        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    100);
            return (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
        }else{
            return true;
        }
    }

    private void save() throws IOException {
        //TODO error checks

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = simpleDateFormat.format(calendar.getTime());

        dbConnection.insertImageData(currentlyTranslatedText, originText, date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get the image which was taken back from the user
        if (resultCode == RESULT_OK && (requestCode == REQUEST_IMAGE_EDIT || requestCode == REQUEST_IMAGE_CAPTURE)) {
            uiTakePictureImageButton.setImageDrawable(null);//refresh
            uiTakePictureImageButton.setImageURI(Uri.parse(currentPhotoPath));
            try {
                readImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //move out of main activity
    private void createImageFile(){//from android development page: https://developer.android.com/training/camera/photobasics
        // Create an image file name
        String imageFileName = "TEMP_IMG";
        File storageDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentPhotoFile = image;
    }

    private void readImage() throws IOException{
        Bitmap bp = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), Uri.fromFile(currentPhotoFile));
        imageToText.convertImage(bp);
    }

    @Override
    public void updateText(String text) {
        String fromLanguage = uiOriginLanguageSpinner.getSelectedItem().toString();
        String toLanguage = uiDestinationLanguageSpinner.getSelectedItem().toString();
        originText = text;

        mTextToTextTranslation.TranslateText(text, fromLanguage, toLanguage,getContext());
    }

    @Override
    public void updateTranslatedText(String text) {
        currentlyTranslatedText = text;

        uiMessageTextView.setText(text);
    }



}
