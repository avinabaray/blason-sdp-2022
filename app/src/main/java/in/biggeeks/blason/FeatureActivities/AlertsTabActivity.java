package in.biggeeks.blason.FeatureActivities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import in.biggeeks.blason.Adapters.AlertsPagerAdapter;
import in.biggeeks.blason.CustomViews.NonSwipeableViewPager;
import in.biggeeks.blason.R;

public class AlertsTabActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout alertsTypeTabLay;
    private TabItem receivedTabItem, sentTabItem;
    private NonSwipeableViewPager tabsViewPager;
    private Activity mActivity;

    private AlertsPagerAdapter pagerAdapter;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_tab);
        mActivity = this;

        initViews();
        initPagerAdapter();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        toolbar = findViewById(R.id.toolbar);
        alertsTypeTabLay = findViewById(R.id.alertsTypeTabLay);
        receivedTabItem = findViewById(R.id.receivedTabItem);
        sentTabItem = findViewById(R.id.sentTabItem);
        tabsViewPager = findViewById(R.id.tabsViewPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(getString(R.string.all_alerts));
    }

    public void initPagerAdapter() {
        pagerAdapter = new AlertsPagerAdapter(mActivity,
                rootLayout,
                tabsViewPager,
                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                alertsTypeTabLay.getTabCount());
        tabsViewPager.setAdapter(pagerAdapter);

        alertsTypeTabLay.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabsViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(alertsTypeTabLay));
    }

}