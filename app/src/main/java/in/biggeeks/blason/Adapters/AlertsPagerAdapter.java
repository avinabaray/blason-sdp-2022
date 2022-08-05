package in.biggeeks.blason.Adapters;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import in.biggeeks.blason.Fragments.AlertsRecvFragment;
import in.biggeeks.blason.Fragments.AlertsSentFragment;

public class AlertsPagerAdapter extends FragmentPagerAdapter {

    private final Activity mActivity;
    private final View rootLayout;
    private final ViewPager tabsViewPager;
    private int totalTabs;

    public AlertsPagerAdapter(Activity mActivity, View rootLayout, ViewPager tabsViewPager,
                              @NonNull FragmentManager fm, int behavior, int totalTabs) {
        super(fm, behavior);
        this.mActivity = mActivity;
        this.rootLayout = rootLayout;
        this.tabsViewPager = tabsViewPager;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AlertsRecvFragment(mActivity, rootLayout, tabsViewPager);
            case 1:
                return new AlertsSentFragment(mActivity, rootLayout);
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
