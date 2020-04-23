package com.example.finalmobilecomputingproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private DataBaseConnection mDataBaseConnection;
    private DatabaseArrayAdapter mAdapter;
    private ListView uiDatabaseListView;
    private SwipeRefreshLayout uiSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_roll, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDataBaseConnection = new DataBaseConnection(view.getContext());

        uiDatabaseListView = view.findViewById(R.id.uiDatabaseListViewData);

        uiSwipeRefreshLayout = view.findViewById(R.id.uiswipeRefreshLayout);
        uiSwipeRefreshLayout.setOnRefreshListener(this);

        refresh();
    }

    public void refresh(){
        ArrayList<ImageData> storedImages = mDataBaseConnection.getAllFromDatabase();

        mAdapter = new DatabaseArrayAdapter(storedImages, getContext());

        uiDatabaseListView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        //when the user switches screens, automatically update the adapter
        super.onResume();
        refresh();
    }

    @Override
    public void onRefresh() {
        //when the user has refreshed
        refresh();
        uiSwipeRefreshLayout.setRefreshing(false);
    }
}
