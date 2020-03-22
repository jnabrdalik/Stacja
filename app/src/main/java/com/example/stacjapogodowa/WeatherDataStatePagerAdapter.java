package com.example.stacjapogodowa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataStatePagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();

    public WeatherDataStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }

    public boolean contains(String cityName) {
        for (Fragment f : fragmentList) {
            Bundle args = f.getArguments();
            String fragmentCityName = args.getString("city");
            if (cityName.equals(fragmentCityName))
                return true;
        }

        return false;
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public void updateAll() {
        for (Fragment f : fragmentList)
            ((WeatherFragment) f).updateData();
    }
}
