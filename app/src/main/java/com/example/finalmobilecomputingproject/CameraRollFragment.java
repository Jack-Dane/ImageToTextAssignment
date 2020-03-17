package com.example.finalmobilecomputingproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class CameraRollFragment extends Fragment {

    private DataBaseConnection dataBaseConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera_roll, container, false);
        dataBaseConnection = new DataBaseConnection(rootView.getContext());
        refresh();
        return rootView;
    }

    public void refresh(){
        ArrayList<ImageData> storedImages = dataBaseConnection.getAllFromDatabase();

        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        DatabaseTextView databaseTextView;

        for(ImageData imgData : storedImages) {
            Log.d("Data", imgData.mTranslatedText);
            databaseTextView = new DatabaseTextView();

            Bundle bundle = new Bundle();
            bundle.putString("translatedText", imgData.mTranslatedText);
            bundle.putString("originText", imgData.mOriginText);
            bundle.putString("date", imgData.mDate);
            databaseTextView.setArguments(bundle);

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.uiFragmentCameraRollLinearLayout, databaseTextView);
            fragmentTransaction.commit();
        }
    }
}
