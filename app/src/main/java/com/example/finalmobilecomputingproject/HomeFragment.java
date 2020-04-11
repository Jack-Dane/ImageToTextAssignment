package com.example.finalmobilecomputingproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.common.util.IOUtils.copyStream;

public class HomeFragment extends Fragment implements Observer{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_EDIT = 2;
    private static final int PICK_IMAGE = 3;
    private static final int OPEN_CAMERA_PERMISSION = 10;
    private static final int OPEN_EDIT_CHOOSER_PERMISSION = 11;
    private static final String ORIGIN_LANGUAGE_STATE = "originLanguage";
    private static final String TRANSLATED_LANGUAGE_STATE = "translatedLanguage";
    private static final String TAKEN_PICTURE_STATE = "takenPicture";
    private static final String ORIGIN_TEXT_STATE = "originText";
    private static final String TRANSLATED_TEXT_STATE = "translatedText";
    private static final String PHOTO_FILE_PATH = "photoPath";

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
                longClickImage();
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

        uiOriginLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(uiTakePictureImageButton.getDrawable() != null){
                    //user has taken a picture
                    try {
                        readImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uiDestinationLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(uiTakePictureImageButton.getDrawable() != null){
                    //user has taken a picture
                    try {
                        readImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(savedInstanceState != null){
            uiOriginLanguageSpinner.setSelection(savedInstanceState.getInt(ORIGIN_LANGUAGE_STATE));
            uiDestinationLanguageSpinner.setSelection(savedInstanceState.getInt(TRANSLATED_LANGUAGE_STATE));

            originText = savedInstanceState.getString(ORIGIN_TEXT_STATE);
            currentlyTranslatedText = savedInstanceState.getString(TRANSLATED_TEXT_STATE);
            uiMessageTextView.setText(currentlyTranslatedText);

            currentPhotoPath = savedInstanceState.getString(PHOTO_FILE_PATH);
            currentPhotoFile = new File(currentPhotoPath);

            if(savedInstanceState.getBoolean(TAKEN_PICTURE_STATE)){
                uiTakePictureImageButton.setImageDrawable(null);
                uiTakePictureImageButton.setImageURI(Uri.parse(currentPhotoPath));
            }
        }else{
            try {
                createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's choices
        super.onSaveInstanceState(savedInstanceState);
        int translatedIndex = uiDestinationLanguageSpinner.getSelectedItemPosition();
        int originIndex = uiOriginLanguageSpinner.getSelectedItemPosition();
        boolean savedImage = uiTakePictureImageButton.getDrawable() != null;

        savedInstanceState.putString(PHOTO_FILE_PATH, currentPhotoPath);
        savedInstanceState.putInt(ORIGIN_LANGUAGE_STATE, originIndex);
        savedInstanceState.putInt(TRANSLATED_LANGUAGE_STATE, translatedIndex);
        savedInstanceState.putBoolean(TAKEN_PICTURE_STATE, savedImage);
        savedInstanceState.putString(ORIGIN_TEXT_STATE, originText);
        savedInstanceState.putString(TRANSLATED_TEXT_STATE, currentlyTranslatedText);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case OPEN_CAMERA_PERMISSION:
                if(grantResults.length>0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                        openCameraApp();
                    }
                }
                break;
            case OPEN_EDIT_CHOOSER_PERMISSION:
                if(grantResults.length>0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                        openEditChooser();
                    }
                }
                break;
        }
    }

    private void openCamera(View view){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    OPEN_CAMERA_PERMISSION);
        }else{
            openCameraApp();
        }
    }

    private void openCameraApp() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            try {
                createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    "com.example.finalmobilecomputingproject.provider",
                    currentPhotoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
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

    private void longClickImage(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    OPEN_EDIT_CHOOSER_PERMISSION);
        }else{
            openEditChooser();
        }
    }

    private void openEditChooser(){
        if(uiTakePictureImageButton.getDrawable() != null){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            dialogBuilder.setTitle("Do you want to edit or choose another image?");

            dialogBuilder.setItems(new CharSequence[]
                            {"Edit", "Select", "Cancel"},
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                editImage();
                                break;
                            case 1:
                                chooseImage();
                                break;
                        }
                    });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }else{
            chooseImage();
        }
    }

    private void editImage(){
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(Uri.parse(currentPhotoPath), "image/*");
        editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(editIntent, REQUEST_IMAGE_EDIT);
    }

    private void chooseImage(){
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(Uri.parse(currentPhotoPath), "image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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
        if (resultCode == RESULT_OK){
            if(requestCode == PICK_IMAGE){
                try{
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(currentPhotoFile);

                    FileOutputStream outputStream = new FileOutputStream(currentPhotoFile);
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            uiTakePictureImageButton.setImageDrawable(null);//refresh
            uiTakePictureImageButton.setImageURI(Uri.parse(currentPhotoPath));
            galleryAddPic();
            try {
                readImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Translator_App_Image" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Translator");

        if(!storageDir.exists()){
            new File(storageDir.getPath()).mkdir();
        }
        File image = new File(storageDir, imageFileName + ".jpg");

        currentPhotoPath = image.getAbsolutePath();
        currentPhotoFile = image;
    }

    //TODO IF IMAGE HAS BEEN TAKEN AND BOTH SPINNERS INDEX'S DON'T = 0, THEN CONTINUE, ELSE DONT READ IMAGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void readImage() throws IOException{
        Bitmap bp = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), Uri.fromFile(currentPhotoFile));
        imageToText.convertImage(bp);
    }

    @Override
    public void updateText(String text) {
        if(uiOriginLanguageSpinner.getSelectedItemPosition() != 0 && uiDestinationLanguageSpinner.getSelectedItemPosition() != 0 && !text.equals("")) {
            String fromLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiOriginLanguageSpinner.getSelectedItemPosition());
            String toLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiDestinationLanguageSpinner.getSelectedItemPosition());
            originText = text;

            mTextToTextTranslation.TranslateText(text, fromLanguage, toLanguage, getContext());
        }else if(text.equals("")){
            Toast.makeText(getContext(),"No text could be read from the image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateTranslatedText(String text) {
        currentlyTranslatedText = text;
        uiMessageTextView.setText(text);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
    }
}