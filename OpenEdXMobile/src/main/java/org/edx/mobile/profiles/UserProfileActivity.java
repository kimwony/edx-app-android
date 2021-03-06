package org.edx.mobile.profiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.Config;

public class UserProfileActivity extends BaseSingleFragmentActivity {
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_SHOW_NAVIGATION_DRAWER = "showNavigationDrawer";

    @Inject
    private Config config;

    public static Intent newIntent(@NonNull Context context, @NonNull String username, boolean showNavigationDrawer) {
        return new Intent(context, UserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username)
                .putExtra(EXTRA_SHOW_NAVIGATION_DRAWER, showNavigationDrawer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideToolbarShadow();
        if (!config.isTabsLayoutEnabled()) {
            if (getIntent().getBooleanExtra(EXTRA_SHOW_NAVIGATION_DRAWER, false)) {
                addDrawer();
            } else {
                blockDrawerFromOpening();
            }
        }
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROFILE_VIEW);
    }

    @Override
    public Fragment getFirstFragment() {
        return UserProfileFragment.newInstance(getIntent().getStringExtra(EXTRA_USERNAME));
    }
}
