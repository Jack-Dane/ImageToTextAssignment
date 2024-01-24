package com.jdevelopment.imagetranslator;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DatabaseTextView extends Fragment {

    private TextView uiTranslatedTextView;
    private TextView uiOriginTextView;
    private TextView uiDateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_database_text_view, container, false);

        uiTranslatedTextView = rootView.findViewById(R.id.uiTranslatedTextDisplayTextView);
        uiOriginTextView = rootView.findViewById(R.id.uiOriginTextDisplayTextView);
        uiDateTextView = rootView.findViewById(R.id.uiDateDisplayTextView);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            uiTranslatedTextView.setText(bundle.getString("translatedText"));
            uiOriginTextView.setText(bundle.getString("originText"));
            uiDateTextView.setText(bundle.getString("date"));
        }

        return rootView;
    }
}
