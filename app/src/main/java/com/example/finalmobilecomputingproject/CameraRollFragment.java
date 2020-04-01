package com.example.finalmobilecomputingproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class CameraRollFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private DataBaseConnection dataBaseConnection;
    private DatabaseArrayAdapter adapter;
    private ListView uiDatabaseListView;
    private SwipeRefreshLayout uiSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_camera_roll, container, false);

        dataBaseConnection = new DataBaseConnection(rootView.getContext());

        uiDatabaseListView = rootView.findViewById(R.id.uiDatabaseListViewData);

        uiSwipeRefreshLayout = rootView.findViewById(R.id.uiswipeRefreshLayout);
        uiSwipeRefreshLayout.setOnRefreshListener(this);

        refresh();
        return rootView;
    }

    public void refresh(){
        ArrayList<ImageData> storedImages = dataBaseConnection.getAllFromDatabase();

        adapter = new DatabaseArrayAdapter(storedImages, getContext());

        uiDatabaseListView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        //when the user has refreshed
        refresh();
        uiSwipeRefreshLayout.setRefreshing(false);
    }
}
