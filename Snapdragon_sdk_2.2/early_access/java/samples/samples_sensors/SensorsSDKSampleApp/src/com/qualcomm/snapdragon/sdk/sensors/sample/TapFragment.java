/* ======================================================================
 *  Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 */
package com.qualcomm.snapdragon.sdk.sensors.sample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.qualcomm.snapdragon.sdk.sensors.SscConnectionStatusListener;
import com.qualcomm.snapdragon.sdk.sensors.SscEventListener;
import com.qualcomm.snapdragon.sdk.sensors.SscManager;
import com.qualcomm.snapdragon.sdk.sensors.SscManager.SensorAccuracy;
import com.qualcomm.snapdragon.sdk.sensors.SscManager.SensorFeature;

public class TapFragment extends Fragment implements SscEventListener, SscConnectionStatusListener {
    private static final String    TAG                  = "TapFragment";
    private long                   mLastSCTimestamp, mLastACTimestamp;
    public static boolean          mDisableUIDataUpdate = false;
    private View                   mFragView;
    private ArrayAdapter<String>   mHistoryList;
    public static SscEventListener sscListener          = null;
    private List<String>           mSensorItemsList     = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragView = inflater.inflate(R.layout.fragment_layout, container, false);
        /***
         * CHECK IF QC SENSOR FEATURE IS SUPPORTED ON THE DEVICE
         ***/
        Button sensorsBtn = (Button) mFragView.findViewById(R.id.sensorstext);
        if (false == MainActivity.tapFeatureFlag) {
            sensorsBtn.setText("Feature Not Supported");
        } else {
            sscListener = this;
            registerListenerforTap();
        }

        mHistoryList = new ArrayAdapter<String>(MainActivity.getContext(), android.R.layout.test_list_item);
        ListView hlv = (ListView) mFragView.findViewById(R.id.eventList);
        hlv.setAdapter(mHistoryList);

        return mFragView;
    }

    @Override
    public void onAccuracyChanged(SensorFeature arg0, SensorAccuracy arg1) {
        // To avoid redrawing the screen too frequently, we limit the total
        // number of updates per second,
        // based on the number of sensors currently streaming
        long currentTimestamp = Calendar.getInstance().getTime().getTime();
        if (!mDisableUIDataUpdate && currentTimestamp > this.mLastACTimestamp) {
            this.mLastACTimestamp = currentTimestamp;
        }

    }

    @Override
    public void onErrorReceived(SensorFeature feature) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSscEventReceived(SensorFeature featureType, Bundle tapBundle) {
        if (featureType == SscManager.SensorFeature.FEATURE_TAP) {
            Log.i(TAG,
                    "onSscEventReceived type:" + featureType + "TAPOutput:"
                            + tapBundle.getString(SscManager.MESSAGE_BUNDLE_TAP_OUTPUT));
            // To avoid redrawing the screen too frequently, we limit the total
            // number of updates per second,
            // based on the number of sensors currently streaming
            long currentTimestamp = Calendar.getInstance().getTime().getTime();
            if (!mDisableUIDataUpdate && currentTimestamp > this.mLastSCTimestamp) {
                this.updateView(tapBundle);

                this.mLastSCTimestamp = currentTimestamp;
            }
        }
    }

    private void updateHistory() {
        mHistoryList.clear();
        List<String> messages = getEventHistory();
        for (String message : messages) {
            mHistoryList.add(message);
        }
        mHistoryList.notifyDataSetChanged();
    }

    private void updateView(Bundle tapBundle) {
        Button tapBtn = (Button) mFragView.findViewById(R.id.sensorstext);
        String sensorText = "";

        SscManager.SensorTapState tapState = SscManager.SensorTapState.valueOf(tapBundle
                .getString(SscManager.MESSAGE_BUNDLE_TAP_OUTPUT));
        if (tapState == SscManager.SensorTapState.EVENT_SENSOR_GESTURES_TAP_LEFT) {
            sensorText = "TAP LEFT";
        } else if (tapState == SscManager.SensorTapState.EVENT_SENSOR_GESTURES_TAP_RIGHT) {
            sensorText = "TAP RIGHT";
        } else if (tapState == SscManager.SensorTapState.EVENT_SENSOR_GESTURES_TAP_TOP) {
            sensorText = "TAP TOP";
        } else if (tapState == SscManager.SensorTapState.EVENT_SENSOR_GESTURES_TAP_BOTTOM) {
            sensorText = "TAP BOTTOM";
        }
        tapBtn.setText("");
        tapBtn.setText(sensorText);

        mSensorItemsList.add(sensorText);
        this.updateHistory();
    }

    public synchronized List<String> getEventHistory() {
        List<String> clone = new ArrayList<String>(mSensorItemsList.size());
        for (String sensorItem : mSensorItemsList) {
            clone.add(sensorItem);
        }
        return clone;
    }

    @Override
    public void onConnectionLost() {
    }

    public static void registerListenerforTap() {
        try {
            if (false == MainActivity.sscManager.registerListener(SensorFeature.FEATURE_TAP, sscListener)) {
                Log.e(TAG, "registerListener failed for FEATURE_TAP");
            } else {
                MainActivity.mSscEventListenerList.add(sscListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
