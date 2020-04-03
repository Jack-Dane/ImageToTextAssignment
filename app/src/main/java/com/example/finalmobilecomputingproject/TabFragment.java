package com.example.finalmobilecomputingproject;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class TabFragment extends Fragment {

    private static final int NUM_PAGES = 2;
    private String[] tabs = new String[] {"Translator", "Camera Roll"};
    private int[] tabIcons = new int[] {R.drawable.language, R.drawable.camera};
    private ViewPager2 viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        viewPager = view.findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position]).setIcon(tabIcons[position])
        ).attach();

        return view;
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        ScreenSlidePagerAdapter(Fragment fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch(position){
                case 0:
                    return new HomeFragment();
                case 1:
                    return new CameraRollFragment();
            }
            return new HomeFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onResume() {
        SharedPreferences result = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        viewPager.setUserInputEnabled(result.getBoolean("swipe_screens", false));
        super.onResume();
    }
}
