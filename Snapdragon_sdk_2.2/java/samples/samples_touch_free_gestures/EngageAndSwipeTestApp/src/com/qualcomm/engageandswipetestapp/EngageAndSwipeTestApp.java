/**
 * Copyright (c) 2014 Qualcomm Technologies, Inc.All Rights Reserved.
 * Qualcomm Technologies Confidential and Proprietary.
 */

package com.qualcomm.engageandswipetestapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.qualcomm.snapdragon.sdk.gestures.EngagementListener;
import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager;
import com.qualcomm.snapdragon.sdk.gestures.GestureMode;
import com.qualcomm.snapdragon.sdk.gestures.OverlayPreferences;
import com.qualcomm.snapdragon.sdk.gestures.SwipeListener;

/**
 * The Class EngageAndSwipeTestApp. This application is set to use the
 * ENGAGMENT_SWIPE mode. The OverlayPreferences is set to display engagement,
 * left, and right only.
 */
public class EngageAndSwipeTestApp extends Activity implements
        EngagementListener, SwipeListener {

    /** The Constant TAG. */
    private static final String TAG = "EngageAndSwipeTestApp";

    /** The gesture mgr. */
    private GestureDeviceManager mGestureMgr;

    /** The value textbox. */
    private TextView mTextView;

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     *            the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // create Gesture manager
        mGestureMgr = new GestureDeviceManager(this,
                GestureMode.ENGAGEMENT_GESTURE);
        mGestureMgr.setOverlayPreferences(new OverlayPreferences(true,
                OverlayPreferences.LEFT | OverlayPreferences.RIGHT
                        | OverlayPreferences.ENGAGEMENT));

        // Set the SwipeListener and Engagement Listener
        mGestureMgr.setSwipeListener(this);
        mGestureMgr.setEngagementListener(this);

        // Textview to display engagement value.
        mTextView = (TextView) findViewById(R.id.value);
        mTextView.setText("0.0");
    }

    // ///////////////////////////////////
    // Listeners
    // ////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.qualcomm.gesturesdk.listeners.SwipeListener#onLeftSwipe(com.qualcomm
     * .gesturesdk.listeners.SwipeListener.SwipeEvent)
     */
    @Override
    public void onLeftSwipe(SwipeEvent e) {
        Log.d(TAG, "onLeftSwipe");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.qualcomm.gesturesdk.listeners.SwipeListener#onRightSwipe(com.qualcomm
     * .gesturesdk.listeners.SwipeListener.SwipeEvent)
     */
    @Override
    public void onRightSwipe(SwipeEvent e) {
        Log.d(TAG, "onRightSwipe");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.qualcomm.gesturesdk.listeners.SwipeListener#onUpSwipe(com.qualcomm
     * .gesturesdk.listeners.SwipeListener.SwipeEvent)
     */
    @Override
    public void onUpSwipe(SwipeEvent e) {
        Log.d(TAG, "onUpSwipe");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.qualcomm.gesturesdk.listeners.SwipeListener#onDownSwipe(com.qualcomm
     * .gesturesdk.listeners.SwipeListener.SwipeEvent)
     */
    @Override
    public void onDownSwipe(SwipeEvent e) {
        Log.d(TAG, "onDownSwipe");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.qualcomm.gesturesdk.listeners.EngagementListener#onEngagementChanged
     * (com.qualcomm.gesturesdk.listeners.EngagementListener.EngagementEvent)
     */
    @Override
    public void onEngagementChanged(EngagementEvent e) {
        Log.d(TAG, "onEngagementChanged");
        mTextView.setText(String.valueOf(e.engagePercent));
    }
}