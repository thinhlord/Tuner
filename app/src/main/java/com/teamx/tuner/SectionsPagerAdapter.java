package com.teamx.tuner;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * Created by Thinh on 16/05/2016.
 * Project: Tuner
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    Fragment fragment1, fragment2, fragment3;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        fragment1 = new Fragment();
        fragment2 = ToneFragment.newInstance();
        fragment3 = new Fragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return fragment1;
            case 1:
                return fragment2;
            default:
                return fragment3;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Category";
            case 1:
                return "Featured";
            case 2:
                return "Popular";
        }
        return null;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                fragment1 = f;
                break;
            case 1:
                fragment2 = f;
                break;
            case 2:
                fragment3 = f;
                break;
        }
        return f;
    }
}


