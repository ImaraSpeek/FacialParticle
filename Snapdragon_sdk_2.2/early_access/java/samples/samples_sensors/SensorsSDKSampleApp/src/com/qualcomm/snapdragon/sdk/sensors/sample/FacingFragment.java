/* ======================================================================
 *  Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 */
package com.qualcomm.snapdragon.sdk.sensors.sample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
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

public class FacingFragment extends Fragment implements SscEventListener, SscConnectionStatusListener {
    public static final String     TAG                  = "FacingFragment";
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
        if (false == MainActivity.facingFeatureFlag) {
            sensorsBtn.setText("Feature Not Supported");
        } else {
            sscListener = this;
            registerListenerforFacing();
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
    public void onSscEventReceived(SensorFeature featureType, Bundle facingBundle) {
        if (featureType == SscManager.SensorFeature.FEATURE_FACING) {
            Log.i(TAG,
                    "onSscEventReceived type:" + featureType + " FacingOutput:"
                            + facingBundle.getString(SscManager.MESSAGE_BUNDLE_FACING_OUTPUT));
            // To avoid redrawing the screen too frequently, we limit the total
            // number of updates per second,
            // based on the number of sensors currently streaming
            long currentTimestamp = Calendar.getInstance().getTime().getTime();
            if (!mDisableUIDataUpdate && currentTimestamp > this.mLastSCTimestamp) {
                this.updateView(facingBundle);

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

    private void updateView(Bundle facingBundle) {
        Button facingBtn = (Button) mFragView.findViewById(R.id.sensorstext);
        String sensorText = "";
        SscManager.SensorFacingState facingState = SscManager.SensorFacingState.valueOf(facingBundle
                .getString(SscManager.MESSAGE_BUNDLE_FACING_OUTPUT));
        if (facingState == SscManager.SensorFacingState.EVENT_SENSOR_GESTURES_FACING_UP) {
            sensorText = "FACING UP";
        } else if (facingState == SscManager.SensorFacingState.EVENT_SENSOR_GESTURES_FACING_DOWN) {
            MediaPlayer player = MediaPlayer
                    .create(MainActivity.getContext(), Settings.System.DEFAULT_NOTIFICATION_URI);
            player.start();
            sensorText = "FACING DOWN";
        }
        facingBtn.setText("");
        facingBtn.setText(sensorText);

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

    public static void registerListenerforFacing() {
        try {
            if (false == MainActivity.sscManager.registerListener(SensorFeature.FEATURE_FACING, sscListener)) {
                Log.e(TAG, "registerListener failed for FEATURE_FACING");
            } else {
                MainActivity.mSscEventListenerList.add(sscListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
