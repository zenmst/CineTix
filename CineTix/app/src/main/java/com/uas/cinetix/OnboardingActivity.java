package com.uas.cinetix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_onboarding);

        mViewPager = findViewById (R.id.pager);
        mViewPager.setAdapter (new ViewPagerAdapter(
                getSupportFragmentManager ()
        ));

    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(FragmentManager fm){super(fm);}

        @Override
        public Fragment getItem(int position) {
            return new Onboarding1Fragment();
        }
        public int getCount(){return 1;}
    }
}
