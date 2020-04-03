package com.example.finalmobilecomputingproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements Observer{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_EDIT = 2;
    private static final String ORIGIN_LANGUAGE_STATE = "originLanguage";
    private static final String TRANSLATED_LANGUAGE_STATE = "translatedLanguage";
    private static final String TAKEN_PICTURE_STATE = "takenPicture";

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

        imageToText = new ImageToText();
        imageToText.addObserver(this);

        mTextToTextTranslation = new TextToTextTranslation();
        mTextToTextTranslation.addObserver(this);

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
                save();
            }
        });

        if(savedInstanceState != null){
            uiOriginLanguageSpinner.setSelection(savedInstanceState.getInt(ORIGIN_LANGUAGE_STATE));
            uiDestinationLanguageSpinner.setSelection(savedInstanceState.getInt(TRANSLATED_LANGUAGE_STATE));
            if(savedInstanceState.getBoolean(TAKEN_PICTURE_STATE)){
                uiTakePictureImageButton.setImageDrawable(null);
                uiTakePictureImageButton.setImageURI(Uri.parse(currentPhotoPath));
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's choices
        int translatedIndex = uiDestinationLanguageSpinner.getSelectedItemPosition();
        int originIndex = uiOriginLanguageSpinner.getSelectedItemPosition();
        boolean savedImage = !(uiTakePictureImageButton.getDrawable() == null);

        savedInstanceState.putInt(ORIGIN_LANGUAGE_STATE, originIndex);
        savedInstanceState.putInt(TRANSLATED_LANGUAGE_STATE, translatedIndex);
        savedInstanceState.putBoolean(TAKEN_PICTURE_STATE, savedImage);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        SharedPreferences result = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        String language;
        int index;

        if(uiOriginLanguageSpinner.getSelectedItemPosition() == 0){
            language = result.getString("origin_language", "");
            index = Arrays.asList((getResources().getStringArray(R.array.languages_array))).indexOf(language);
            uiOriginLanguageSpinner.setSelection(index);
        }

        if(uiDestinationLanguageSpinner.getSelectedItemPosition() == 0){
            language = result.getString("translated_language", "");
            index = Arrays.asList((getResources().getStringArray(R.array.languages_array))).indexOf(language);
            uiDestinationLanguageSpinner.setSelection(index);
        }

        super.onResume();
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
            Toast.makeText(this.getContext(), "Please enable the camera to take pictures", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareButtonPress(View view){
        if(currentlyTranslatedText != null && originText != null){
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
        }else{
            Toast.makeText(this.getContext(), "You need to take a picture before you can share", Toast.LENGTH_SHORT).show();
        }
    }

    private void editImage(){
        if(currentPhotoPath != null){
            Intent editIntent = new Intent(Intent.ACTION_EDIT);
            editIntent.setDataAndType(Uri.parse(currentPhotoPath), "image/*");
            editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(editIntent, REQUEST_IMAGE_EDIT);
        }else{
            Toast.makeText(this.getContext(), "Please take a picture first by click the square", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean askForCameraPermission(){
        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    100);
            return (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
        }else{
            //if already given permission, return true
            return true;
        }
    }

    private void save() {
        if(currentlyTranslatedText != null && originText != null){
            //check to see if the user has taken a picture

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY", Locale.UK);
            String date = simpleDateFormat.format(calendar.getTime());

            String originLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array))).get(uiOriginLanguageSpinner.getSelectedItemPosition());
            String translateLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array))).get(uiDestinationLanguageSpinner.getSelectedItemPosition());

            dbConnection.insertImageData(currentlyTranslatedText, originText, date, originLanguage, translateLanguage);
            Toast.makeText(this.getContext(), "Your image data has been saved", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this.getContext(), "A picture needs to be taken before you can save", Toast.LENGTH_SHORT).show();
        }
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

    private void createImageFile(){
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
        if(!(uiOriginLanguageSpinner.getSelectedItemPosition() == 0 || uiDestinationLanguageSpinner.getSelectedItemPosition() == 0)){
            String fromLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiOriginLanguageSpinner.getSelectedItemPosition());
            String toLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiDestinationLanguageSpinner.getSelectedItemPosition());
            originText = text;

            mTextToTextTranslation.TranslateText(text, fromLanguage, toLanguage,getContext());
        }else{
            Toast.makeText(getContext(),"Please select items before translating", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateTranslatedText(String text) {
        currentlyTranslatedText = text;
        uiMessageTextView.setText(text);
    }
}