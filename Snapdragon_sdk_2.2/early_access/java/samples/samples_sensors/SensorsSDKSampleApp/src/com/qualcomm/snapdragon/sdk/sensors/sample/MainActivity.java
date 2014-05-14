/* ======================================================================
 *  Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 */
package com.qualcomm.snapdragon.sdk.sensors.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.qualcomm.snapdragon.sdk.sensors.SscEventListener;
import com.qualcomm.snapdragon.sdk.sensors.SscManager;
import com.qualcomm.snapdragon.sdk.sensors.SscManager.SensorFeature;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String          TAG                      = "SensorsSampleActivity";
    private ViewPager                    mViewPager;
    private TabsPagerAdapter             mAdapter;
    private ActionBar                    actionBar;

    public static SscManager             sscManager;
    private static Context               mContext;
    public static boolean                amdFeatureFlag           = false;
    public static boolean                rmdFeatureFlag           = false;
    public static boolean                facingFeatureFlag        = false;
    public static boolean                gesturesFeatureFlag      = false;
    public static boolean                activityRecogFeatureFlag = false;
    public static boolean                tapFeatureFlag           = false;
    public static boolean                tiltFeatureFlag          = false;

    public static List<SscEventListener> mSscEventListenerList    = new ArrayList<SscEventListener>();
    // Tab titles
    private final String[]                     tabs                     = { "AMD", "Activity Recog", "Facing", "Gestures",
            "RMD", "Tap", "Tilt"                                 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_FACING)) {
            facingFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_ABSOLUTE_MOTION_DETECTION)) {
            amdFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_RELATIVE_MOTION_DETECTION)) {
            rmdFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_ACTIVITY_RECOGNITION)) {
            activityRecogFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_GESTURES)) {
            gesturesFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_TAP)) {
            tapFeatureFlag = true;
        }
        if (SscManager.isFeatureSupported(SensorFeature.FEATURE_TILT)) {
            tiltFeatureFlag = true;
        }

        sscManager = SscManager.getInstance();
        MainActivity.mContext = this;

        // Initilization
        mViewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(7);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }

        /**
         * on swiping the viewpager make respective tab selected
         * */
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        mViewPager.setCurrentItem(tab.getPosition());

        Log.d(TAG, "Tushar Inside onTabSelected:" + tab.getPosition());
        switch (tab.getPosition()) {
        case 0: {
            if (null != sscManager) {
                if (null != FacingFragment.sscListener) {
                    AMDFragment.registerListenerforAMD();
                }
            }
        }
        case 1: {
            if (null != sscManager) {
                if (null != FacingFragment.sscListener) {
                    ActivityRecognitionFragment.registerListenerforActivityRecog();
                }
            }
            else{
                Log.d(TAG, "sscManager is null");
            }
        }
        case 2: {
            if (null != sscManager) {
                if (null != FacingFragment.sscListener) {
                    FacingFragment.registerListenerforFacing();
                }
            }
        }
        case 3: {
            if (null != sscManager) {
                if (null != GesturesFragment.sscListener) {
                    GesturesFragment.registerListenerforGestures();
                }
            }
        }
        case 4: {
            if (null != sscManager) {
                if (null != RMDFragment.sscListener) {
                    RMDFragment.registerListenerforRMD();
                }
            }
        }
        case 5: {
            if (null != sscManager) {
                if (null != TapFragment.sscListener) {
                    TapFragment.registerListenerforTap();
                }
            }
        }
        case 6: {
            if (null != sscManager) {
                if (null != TiltFragment.sscListener) {
                    TiltFragment.registerListenerforTilt();
                }
            }
        }

        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        Log.d(TAG, "Tushar Inside onTabUnselected:" + tab.getPosition());
        switch (tab.getPosition()) {
        case 0: {
            if (null != sscManager) {
                if (null != AMDFragment.sscListener) {
                    mSscEventListenerList.remove(AMDFragment.sscListener);
                    try {
                        sscManager.unregisterListener(AMDFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        case 1: {
            if (null != sscManager) {
                if (null != ActivityRecognitionFragment.sscListener) {
                    mSscEventListenerList.remove(ActivityRecognitionFragment.sscListener);
                    try {
                        sscManager.unregisterListener(ActivityRecognitionFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        case 2: {
            if (null != sscManager) {
                if (null != FacingFragment.sscListener) {
                    mSscEventListenerList.remove(FacingFragment.sscListener);
                    try {
                        sscManager.unregisterListener(FacingFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        case 3: {
            if (null != sscManager) {
                if (null != GesturesFragment.sscListener) {
                    mSscEventListenerList.remove(GesturesFragment.sscListener);
                    try {
                        sscManager.unregisterListener(GesturesFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        case 4: {
            if (null != sscManager) {
                if (null != RMDFragment.sscListener) {
                    mSscEventListenerList.remove(RMDFragment.sscListener);
                    try {
                        sscManager.unregisterListener(RMDFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        case 5: {
            if (null != sscManager) {
                if (null != TapFragment.sscListener) {
                    try {
                        sscManager.unregisterListener(TapFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mSscEventListenerList.remove(TapFragment.sscListener);
                }
            }
        }
        case 6: {
            if (null != sscManager) {
                if (null != TiltFragment.sscListener) {

                    try {
                        sscManager.unregisterListener(TiltFragment.sscListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mSscEventListenerList.remove(TiltFragment.sscListener);
                }
            }
        }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        clearRegisteredSensorListeners();
    }

    @Override
    public void onPause() {
        super.onStop();
        clearRegisteredSensorListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearRegisteredSensorListeners();
    }

    private void clearRegisteredSensorListeners() {
        if (!mSscEventListenerList.isEmpty()) {
            for (SscEventListener sscListener : mSscEventListenerList) {
                try {
                    sscManager.unregisterListener(sscListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mSscEventListenerList.clear();
        }
    }

    public static Context getContext() {
        return MainActivity.mContext;
    }

}
