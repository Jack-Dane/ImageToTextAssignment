package com.jdevelopment.imagetranslator.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jdevelopment.imagetranslator.R;
import com.jdevelopment.imagetranslator.ui.CameraRollFragment;
import com.jdevelopment.imagetranslator.ui.HomeFragment;

import java.util.Objects;

public class TabFragment extends Fragment {

    private static final int NUM_PAGES = 2;
    private String[] mTabs = new String[] {"Translator", "Camera Roll"};
    private int[] mTabIcons = new int[] {R.drawable.language, R.drawable.camera};
    private ViewPager2 mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewPager = view.findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);

        mViewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> tab.setText(mTabs[position]).setIcon(mTabIcons[position])
        ).attach();
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
        mViewPager.setUserInputEnabled(result.getBoolean("swipe_screens", false));
        super.onResume();
    }
}
