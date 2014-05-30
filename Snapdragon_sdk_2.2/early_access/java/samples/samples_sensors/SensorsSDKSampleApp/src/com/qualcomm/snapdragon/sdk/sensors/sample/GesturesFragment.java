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

public class GesturesFragment extends Fragment implements SscEventListener, SscConnectionStatusListener {
    public static final String     TAG                  = "GesturesFragment";
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
        if (false == MainActivity.gesturesFeatureFlag) {
            sensorsBtn.setText("Feature Not Supported");
        } else {
            sscListener = this;
            registerListenerforGestures();
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
    public void onErrorReceived(SensorFeature arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSscEventReceived(SensorFeature featureType, Bundle gesturesBundle) {
        if (featureType == SscManager.SensorFeature.FEATURE_GESTURES) {
            Log.i(TAG,
                    "onSscEventReceived type:" + featureType + " FacingOutput:"
                            + gesturesBundle.getString(SscManager.MESSAGE_BUNDLE_GESTURES_OUTPUT));
            // To avoid redrawing the screen too frequently, we limit the total
            // number of updates per second,
            // based on the number of sensors currently streaming
            long currentTimestamp = Calendar.getInstance().getTime().getTime();
            if (!mDisableUIDataUpdate && currentTimestamp > this.mLastSCTimestamp) {
                this.updateView(gesturesBundle);

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

    private void updateView(Bundle gesturesBundle) {
        Button activityBtn = (Button) mFragView.findViewById(R.id.sensorstext);
        String sensorText = "";
        SscManager.SensorGestureState gesturesState = SscManager.SensorGestureState.valueOf(gesturesBundle
                .getString(SscManager.MESSAGE_BUNDLE_GESTURES_OUTPUT));
        if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_PULL) {
            sensorText = "PULL";
        } else if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_PUSH) {
            sensorText = "PUSH";
        } else if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_SHAKE_BOTTOM) {
            sensorText = "SHAKE BOTTOM";
        } else if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_SHAKE_LEFT) {
            sensorText = "SHAKE LEFT";
        } else if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_SHAKE_RIGHT) {
            sensorText = "SHAKE RIGHT";
        } else if (gesturesState == SscManager.SensorGestureState.EVENT_SENSOR_GESTURES_SHAKE_TOP) {
            sensorText = "SHAKE TOP";
        }

        activityBtn.setText("");
        activityBtn.setText(sensorText);

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
        // TODO Auto-generated method stub

    }

    public static void registerListenerforGestures() {
        try {
            if (false == MainActivity.sscManager.registerListener(SensorFeature.FEATURE_GESTURES, sscListener)) {
                Log.e(TAG, "registerListener failed for FEATURE_GESTURES");
            } else {
                MainActivity.mSscEventListenerList.add(sscListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
