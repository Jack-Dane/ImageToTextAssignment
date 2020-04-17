package com.example.finalmobilecomputingproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
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

public class HomeFragment extends Fragment implements ImageToTextObserver, TextToTextTranslationObserver {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 3;
    private static final int OPEN_CAMERA_PERMISSION = 10;
    private static final int OPEN_EDIT_CHOOSER_PERMISSION = 11;
    private static final String ORIGIN_LANGUAGE_STATE = "originLanguage";
    private static final String TRANSLATED_LANGUAGE_STATE = "translatedLanguage";
    private static final String TAKEN_PICTURE_STATE = "takenPicture";
    private static final String ORIGIN_TEXT_STATE = "mOriginText";
    private static final String TRANSLATED_TEXT_STATE = "translatedText";
    private static final String PHOTO_FILE_PATH = "photoPath";

    //UI elements
    private ImageButton uiTakePictureImageButton;
    private TextView uiMessageTextView;
    private Spinner uiOriginLanguageSpinner;
    private Spinner uiDestinationLanguageSpinner;
    private Button uiSaveButton;

    //Misc
    private String mCurrentPhotoPath;
    private File mCurrentPhotoFile;
    private String mCurrentTranslationLanguage;
    private String mCurrentOriginLanguage;
    private TextToTextTranslation mTextToTextTranslation;
    private ImageToText mImageToText;
    private String mCurrentlyTranslatedText;
    private DataBaseConnection mDBConnection;
    private String mOriginText;
    private boolean mImageCaptured = false;
    private boolean mImageTranslated = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDBConnection = new DataBaseConnection(view.getContext());

        uiTakePictureImageButton = view.findViewById(R.id.ImageScreenTextView);
        uiMessageTextView = view.findViewById(R.id.TextResultTextView);
        uiOriginLanguageSpinner = view.findViewById(R.id.uiLanguageOriginSelectSpinner);
        uiDestinationLanguageSpinner = view.findViewById(R.id.uiLanguageDestinationSelectSpinner);
        uiSaveButton = view.findViewById(R.id.uiSaveButton);
        Button uiShareButton = view.findViewById(R.id.uiShareButton);

        mImageToText = ImageToText.getInstance();
        mImageToText.addObserver(this);

        mTextToTextTranslation = TextToTextTranslation.getInstance();
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

            mOriginText = savedInstanceState.getString(ORIGIN_TEXT_STATE);
            mCurrentlyTranslatedText = savedInstanceState.getString(TRANSLATED_TEXT_STATE);
            uiMessageTextView.setText(mCurrentlyTranslatedText);

            mCurrentPhotoPath = savedInstanceState.getString(PHOTO_FILE_PATH);
            mCurrentPhotoFile = new File(mCurrentPhotoPath);

            mImageCaptured = savedInstanceState.getBoolean(TAKEN_PICTURE_STATE);
            if(mImageCaptured){
                uiTakePictureImageButton.setImageDrawable(null);
                uiTakePictureImageButton.setImageURI(Uri.parse(mCurrentPhotoPath));
            }
        }else{
            SharedPreferences result = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
            if(uiDestinationLanguageSpinner.getSelectedItemPosition() == 0){
                String language = result.getString("translated_language", "");
                int index = Arrays.asList((getResources().getStringArray(R.array.languages_array))).indexOf(language);
                uiDestinationLanguageSpinner.setSelection(index);
            }

            if(uiOriginLanguageSpinner.getSelectedItemPosition() == 0){
                String language = result.getString("origin_language", "");
                int index = Arrays.asList((getResources().getStringArray(R.array.languages_array_origin))).indexOf(language);
                uiOriginLanguageSpinner.setSelection(index);
            }

            createImageFile();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's choices
        super.onSaveInstanceState(savedInstanceState);
        int translatedIndex = uiDestinationLanguageSpinner.getSelectedItemPosition();
        int originIndex = uiOriginLanguageSpinner.getSelectedItemPosition();
        boolean savedImage = uiTakePictureImageButton.getDrawable() != null;

        savedInstanceState.putString(PHOTO_FILE_PATH, mCurrentPhotoPath);
        savedInstanceState.putInt(ORIGIN_LANGUAGE_STATE, originIndex);
        savedInstanceState.putInt(TRANSLATED_LANGUAGE_STATE, translatedIndex);
        savedInstanceState.putBoolean(TAKEN_PICTURE_STATE, savedImage);
        savedInstanceState.putString(ORIGIN_TEXT_STATE, mOriginText);
        savedInstanceState.putString(TRANSLATED_TEXT_STATE, mCurrentlyTranslatedText);
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
            createImageFile();
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    "com.example.finalmobilecomputingproject.provider",
                    mCurrentPhotoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this.getContext(), "Please enable the camera to take pictures", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareButtonPress(View view){
        if(mImageTranslated){
            Intent shareIntent = new Intent();
            Uri imageUri = FileProvider.getUriForFile(
                    Objects.requireNonNull(getActivity()),
                    "com.example.finalmobilecomputingproject.provider",
                    mCurrentPhotoFile);
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mCurrentlyTranslatedText);
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
        if(mImageCaptured){
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
        CropImage.activity(Uri.fromFile(mCurrentPhotoFile))
                .start(getContext(), this);
    }

    private void chooseImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void save() {
        if(mImageTranslated){
            //check to see if the user has taken a picture

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY", Locale.UK);
            String date = simpleDateFormat.format(calendar.getTime());

            mDBConnection.insertImageData(mCurrentlyTranslatedText, mOriginText, date, mCurrentOriginLanguage, mCurrentTranslationLanguage);
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
            mImageTranslated = false;
            mImageCaptured = true;
            if(requestCode == PICK_IMAGE){
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};
                assert selectedImageUri != null;
                Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(selectedImageUri, projection, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(projection[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                mCurrentPhotoPath = picturePath;
                mCurrentPhotoFile = new File(picturePath);
                Log.d("Path", mCurrentPhotoPath);
            }
            if(requestCode == REQUEST_IMAGE_CAPTURE || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                    createImageFile();

                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    File source = new File(result.getUri().getPath());

                    try (InputStream in = new FileInputStream(source)) {
                        try (OutputStream out = new FileOutputStream(mCurrentPhotoFile)) {
                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                galleryAddPic();
            }
            uiTakePictureImageButton.setImageDrawable(null);//refresh
            uiTakePictureImageButton.setImageURI(Uri.parse(mCurrentPhotoPath));
            try {
                readImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createImageFile(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK);
        String timeStamp = simpleDateFormat.format(new Date());
        String imageFileName = "Translator_App_Image" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Translator");

        if(!storageDir.exists()){
            new File(storageDir.getPath()).mkdir();
        }
        File image = new File(storageDir, imageFileName + ".jpg");

        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoFile = image;
    }

    private void readImage() throws IOException{
        if(uiDestinationLanguageSpinner.getSelectedItemPosition() != 0 && mImageCaptured){
            Log.d("Image",  "Read");
            Bitmap bp = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), Uri.fromFile(mCurrentPhotoFile));
            mImageToText.convertImage(bp);
        }
    }

    @Override
    public void updateText(String text) {
        if(uiDestinationLanguageSpinner.getSelectedItemPosition() != 0) {
            String fromLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiOriginLanguageSpinner.getSelectedItemPosition());
            String toLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).get(uiDestinationLanguageSpinner.getSelectedItemPosition());
            mOriginText = text;

            mTextToTextTranslation.TranslateText(text, fromLanguage, toLanguage, getContext());
        }else if(text.equals("")){
            Toast.makeText(getContext(),"Please select a translation language", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateTextError() {
        //clear data
        Toast.makeText(getContext(),"No text can be read from the image, please make sure to take a clear photo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateTranslatedText(String text, String originLanguage, String translatedLanguage) {
        mCurrentlyTranslatedText = text;
        uiMessageTextView.setText(text);

        int originLanguageIndex = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).indexOf(originLanguage);
        int translatedLanguageIndex = Arrays.asList((getResources().getStringArray(R.array.languages_array_value))).indexOf(translatedLanguage);

        if(originLanguageIndex == -1){
            mCurrentOriginLanguage = originLanguage;
        }else{
            mCurrentOriginLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array))).get(originLanguageIndex);
        }

        if(translatedLanguageIndex == -1){
            mCurrentTranslationLanguage = translatedLanguage;
        }else{
            mCurrentTranslationLanguage = Arrays.asList((getResources().getStringArray(R.array.languages_array))).get(translatedLanguageIndex);
        }

        mImageTranslated = true;
    }

    @Override
    public void updateTranslatedTextError() {
        //clear data
        Toast.makeText(getContext(),"The text cannot be translated", Toast.LENGTH_SHORT).show();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        Objects.requireNonNull(getContext()).sendBroadcast(mediaScanIntent);
    }
}