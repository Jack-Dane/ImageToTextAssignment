package com.example.finalmobilecomputingproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class DatabaseArrayAdapter extends ArrayAdapter<ImageData> implements View.OnClickListener{

    private Context context;
    private List<ImageData> records;
    private DataBaseConnection dbConnection;

    public DatabaseArrayAdapter(List<ImageData> objects, Context context) {
        super(context, R.layout.fragment_database_text_view, objects);
        records = objects;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        dbConnection = new DataBaseConnection(getContext());

        int position=(Integer) v.getTag();
        ImageData dataModel=getItem(position);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        dialogBuilder.setMessage("Are you sure you want to delete this?")
                .setTitle("Warning");

        dialogBuilder.setPositiveButton("Yes", (dialog, which) -> {
            assert dataModel != null;
            dbConnection.deleteRow(dataModel.getmId());
            records.remove(dataModel);

            notifyDataSetChanged();
        });

        dialogBuilder.setNegativeButton("No", (dialog, which) -> {
            //do nothing
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public static class ViewHolder{
        TextView date;
        TextView originText;
        TextView translatedText;
        ImageButton deleteButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ImageData textRecord = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater= LayoutInflater.from(getContext());

            convertView = inflater.inflate(R.layout.fragment_database_text_view, parent, false);
            viewHolder.date = convertView.findViewById(R.id.uiDateDisplayTextView);
            viewHolder.originText = convertView.findViewById(R.id.uiOriginTextDisplayTextView);
            viewHolder.translatedText = convertView.findViewById(R.id.uiTranslatedDisplayTextView);
            viewHolder.deleteButton = convertView.findViewById(R.id.uiDeleteButton);

            result = convertView;

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.date.setText(textRecord.getDate());
        viewHolder.originText.setText(textRecord.getmOriginText());
        viewHolder.translatedText.setText(textRecord.getmTranslatedText());
        viewHolder.deleteButton.setOnClickListener(this);
        viewHolder.deleteButton.setTag(position);

        return convertView;
    }
}
