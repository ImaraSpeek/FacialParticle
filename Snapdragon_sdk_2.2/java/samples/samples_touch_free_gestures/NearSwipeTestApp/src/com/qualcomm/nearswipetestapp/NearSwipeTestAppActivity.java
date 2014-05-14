/**
 * Copyright (c) 2014 Qualcomm Technologies, Inc.All Rights Reserved.
 * Qualcomm Technologies Confidential and Proprietary.
 */

package com.qualcomm.nearswipetestapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager;
import com.qualcomm.snapdragon.sdk.gestures.GestureMode;
import com.qualcomm.snapdragon.sdk.gestures.SwipeListener;

/**
 * The Class NearSwipeTestAppActivity. This Activity sets the Near Swipe mode
 * and sets up a SwipeListener.
 */
public class NearSwipeTestAppActivity extends Activity implements SwipeListener {

    /** The Constant TAG. */
    private static final String TAG = "NearSwipeTestAppActivity";

    /** The Gesture Device Manager instance. */
    private GestureDeviceManager mGestureMgr;

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     *            the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create Gesture manager and set the mode in the constructor.
        mGestureMgr = new GestureDeviceManager(this, GestureMode.NEAR_GESTURE);

        // Set the SwipeListener.
        mGestureMgr.setSwipeListener(this);
    }

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
         * com.qualcomm.gesturesdk.listeners.SwipeListener#onRightSwipe(com.
         * qualcomm.gesturesdk.listeners.SwipeListener.SwipeEvent)
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

}