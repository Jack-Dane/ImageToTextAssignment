package com.example.finalmobilecomputingproject;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;

public class MainActivity extends FragmentActivity {

    private static final int NUM_PAGES = 3;
    private String[] tabs = new String[] {"Calendar", "Translator", "Camera Roll"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        this.setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        //create the tab layout selector
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch(position){
                case 0:
                    return new CalendarFragment();
                case 1:
                    return new HomeFragment();
                case 2:
                    return new CameraRollFragment();
            }
            return new HomeFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}
