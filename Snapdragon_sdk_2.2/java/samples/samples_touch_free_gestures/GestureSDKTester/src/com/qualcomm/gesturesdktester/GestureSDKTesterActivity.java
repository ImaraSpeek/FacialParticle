/**
 * Copyright (c) 2014 Qualcomm Technologies, Inc.All Rights Reserved.
 * Qualcomm Technologies Confidential and Proprietary.
 */

package com.qualcomm.gesturesdktester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.snapdragon.sdk.gestures.CircleListener;
import com.qualcomm.snapdragon.sdk.gestures.CoverListener;
import com.qualcomm.snapdragon.sdk.gestures.EngagementListener;
import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager;
import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager.CameraSource;
import com.qualcomm.snapdragon.sdk.gestures.GestureMode;
import com.qualcomm.snapdragon.sdk.gestures.HandDetectionListener;
import com.qualcomm.snapdragon.sdk.gestures.OverlayPreferences;
import com.qualcomm.snapdragon.sdk.gestures.SwipeListener;
import com.qualcomm.snapdragon.sdk.gestures.SwipeListener.SwipeEvent.SwipeType;

/**
 * The Class GestureSDKTesterActivity.
 */
public class GestureSDKTesterActivity extends Activity implements
        SurfaceHolder.Callback, Camera.PreviewCallback {

    /** The VERSION. */
    private final String VERSION = "v4.1.26";

    /** The LOG TAG. */
    private final static String TAG = "GestureSDKTesterActivity";

    /** The Constant GestureMode Property. */
    public static final String GESTURE_MODE = "CameraGestureModeProperty";

    /** The gesture device manager. */
    private GestureDeviceManager mGestureDeviceManager = null;

    // ///////////////////////////////
    // UI elements
    // ///////////////////////////////

    /** Overlay flags. */
    private int mOverlayFlag = 0;

    /** Overlay enabled flag. */
    private boolean mOverlayEnabled = true;

    /** The overlay preferences. */
    private OverlayPreferences mOverlayPrefs = new OverlayPreferences();

    /** The callback text. */
    private TextView mCallbackText = null;

    /** The engagement value. */
    private TextView mEngagementValue = null;

    /** The instruction textview. */
    private TextView mInstruct = null;

    /** The polling period in ms. */
    private long POLL_PERIOD_MSEC = 2000; // updater

    /** The test polling handler. */
    private Handler mTestPropTimerHandler = null;

    /** The current mode. */
    private GestureMode mCurrentMode;

    /** The last mode string. */
    private String mLastModeString = "";

    /** The simple date format. */
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
            "yy-MM-dd HH:mm:ss",  Locale.getDefault());

    /** The last event timestamp. */
    private long mLastEvent = 0;

    /** The last event time. */
    private Date mLastEventTime = null;

    /** The number tests passed. */
    private int mTestsPassed = 0;

    /** The test mode flag. */
    private boolean mInTestMode = false;

    /** The number of tests to run per test iteration. */
    private int mTestsToRun = 0;

    /** Flag to track if touch injection is enabled. */
    private boolean mEnableTouchInjection;

    /** Event Listeners. */
    private MySwipeListener mSwipeListener = null;

    /** The m hand detection listener. */
    private MyHandDetectionListener mHandDetectionListener = null;

    /** The m engagement listener. */
    private MyEngagementListener mEngagementListener = null;

    /**
     * Dynamic queue of running tests. This object increases and decreases based
     * on user interaction with the unit tests.
     **/
    private Queue<TestObject> mRunningTests = new ConcurrentLinkedQueue<TestObject>();

    /** Handle to a dialog button. **/
    private Button mDialogButton = null;

    /** Bounding box view */
    public BoundingBoxView mBBView = null;

    /** Handle to a bounding box button */
    private CheckBox mBoundingBoxButton = null;


    // /////////////////////////////////
    // Camera Objects
    // ///////////////////////////////

    /** The surface view. */
    private SurfaceView mSurfaceView;

    /** The surface holder. */
    private SurfaceHolder mSurfaceHolder;

    /** The camera. */
    private Camera mCamera;

    /** Id of camera. */
    private static int mCameraId = 1;

    /** The m camera source. */
    private CameraSource mCameraSource = CameraSource.SOURCE_SYS;

    /** The camera width. */
    private int mPreviewWidth = 0;

    /** The camera height. */
    private int mPreviewHeight = 0;

    /** Countdown overlay. */
    private CountDownView mCountdownView;

    /** Save camera orientation info. */
    private int mCameraOrientation = 0;

    /** Mirror flag. **/
    private boolean mIsMirrored;


    /** The following are used to save and restore instances.
     *  This is necessary when a device is rotated or restarted
     *  by the system for some reason. */
    private static final String CAMERA_SOURCE = "CAMERA_SOURCE";
    private static final String HAND_DETECTION_LISTENER = "HAND_DETECTION_LISTENER";
    private static final String SWIPE_LISTENER = "SWIPE_LISTENER";
    private static final String ENGAGEMENT_LISTENER = "ENGAGEMENT_LISTENER";
    private static final String OVERLAY_FLAG = "OVERLAY_FLAG";
    private static final String ENABLE_TOUCH_INJECTION = "ENABLE_TOUCH_INJECTION";
    private static final String CURRENT_GESTURE_MODE = "CURRENT_GESTURE_MODE";

    // ///////////////////////////////
    // Tests
    // ///////////////////////////////

    /**
     * Placeholder for mode before tests are run. After tests are completed, the
     * system will restore this mode.
     **/
    private GestureMode mSavedMode;

    /** The unit tests. */
    private TestObject[] mTests = {
            new TestObject(GestureMode.NEAR_GESTURE, TestOutcome.LEFT_SWIPE,
                    "NEAR_GESTURE: LEFT"),
            new TestObject(GestureMode.NEAR_GESTURE, TestOutcome.RIGHT_SWIPE,
                    "NEAR_GESTURE: RIGHT"),
            new TestObject(GestureMode.ENGAGEMENT_GESTURE,
                    TestOutcome.LEFT_SWIPE, "ENGAGEMENT_GESTURE: LEFT"),
            new TestObject(GestureMode.ENGAGEMENT_GESTURE,
                    TestOutcome.RIGHT_SWIPE, "ENGAGEMENT_GESTURE: RIGHT"),
            new TestObject(GestureMode.ENGAGEMENT_GESTURE,
                    TestOutcome.UP_SWIPE, "ENGAGEMENT_GESTURE: UP"),
            new TestObject(GestureMode.ENGAGEMENT_GESTURE,
                    TestOutcome.DOWN_SWIPE, "ENGAGEMENT_GESTURE: DOWN"),
            new TestObject(GestureMode.ENGAGEMENT_DETECTION,
                    TestOutcome.ENGAGEMENT, "ENGAGEMENT_DETECTION"),
            new TestObject(GestureMode.POINTER, TestOutcome.ENGAGEMENT,
                    "POINTER"),
            new TestObject(GestureMode.POINTER, TestOutcome.ENGAGEMENT,
                    "POINTER"),
            new TestObject(GestureMode.NEAR_AND_POINTER,
                    TestOutcome.LEFT_SWIPE, "NEAR_AND_POINTER: LEFT"),
            new TestObject(GestureMode.NEAR_AND_POINTER,
                    TestOutcome.RIGHT_SWIPE, "NEAR_AND_POINTER: RIGHT"),
            new TestObject(GestureMode.NEAR_AND_ENGAGEMENT_GESTURE,
                    TestOutcome.LEFT_SWIPE, "NEAR_ENGAGEMENT_GESTURE: LEFT"),
            new TestObject(GestureMode.NEAR_AND_ENGAGEMENT_GESTURE,
                    TestOutcome.RIGHT_SWIPE, "NEAR_ENGAGEMENT_GESTURE: RIGHT"),
            new TestObject(GestureMode.NEAR_AND_ENGAGEMENT_GESTURE,
                    TestOutcome.UP_SWIPE, "NEAR_ENGAGEMENT_GESTURE: UP"),
            new TestObject(GestureMode.NEAR_AND_ENGAGEMENT_GESTURE,
                    TestOutcome.DOWN_SWIPE, "NEAR_ENGAGEMENT_GESTURE: DOWN") };

    /**
     * The Interface ActionComplete. Callback for actions. This can be used for
     * any action complete, such a countdown view.
     */
    interface OnActionCompleteListener {

        /**
         * On complete.
         */
        public void onComplete();
    }

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

        // setup gesture device mgr. The mode is OFF.
        mGestureDeviceManager = new GestureDeviceManager(this);
        mLastEventTime = new Date();
        setMode(GestureMode.OFF);

        // Create Gesture Mode Map.
        createModeMap();

        // If gestures are not supported, show toast.
        if (!mGestureDeviceManager.isCameraGestureSupported()) {
            showNotSupportedAlert();
        }

        // Set default camera id
        mGestureDeviceManager.setCameraId(mCameraId);

        mGestureDeviceManager.setCoverListener(new CoverListener() {

            @Override
            public void onCover(int id, long ts) {
                onCallback(
                        "onCover: " + mSimpleDateFormat.format(new Date(ts)),
                        ts);

            }

            @Override
            public void onUncover(int id, long ts) {
                onCallback(
                        "onUncover: " + mSimpleDateFormat.format(new Date(ts)),
                        ts);

            }

        });

        mGestureDeviceManager.setCircleListener(new CircleListener() {

            @Override
            public void OnCircle(CircleEvent e) {

                onCallback("OnCircle: direction=" + e.direction.toString()
                        + ", center(" + e.center.x + ", " + e.center.y
                        + "), point(" + e.point.x + ", " + e.point.y + ")"
                        + mSimpleDateFormat.format(new Date(e.timestamp)),
                        e.timestamp);

                onCallback("Start point: " + e.start.x + ", " + e.start.y,
                        e.timestamp);

            }

            @Override
            public void OnCircleMove(CircleEvent e) {
                onCallback("OnCircleMove: direction=" + e.direction.toString()
                        + ", center(" + e.center.x + ", " + e.center.y
                        + "), point(" + e.point.x + ", " + e.point.y + ")"
                        + mSimpleDateFormat.format(new Date(e.timestamp)),
                        e.timestamp);

                onCallback("Total angle: " + e.angle + " Step: " + e.step,
                        e.timestamp);
            }

            @Override
            public void OnCircleStop(CircleEvent e) {
                onCallback("OnCircleStop: direction=" + e.direction.toString()
                        + ", center(" + e.center.x + ", " + e.center.y
                        + "), point(" + e.point.x + ", " + e.point.y + ")"
                        + mSimpleDateFormat.format(new Date(e.timestamp)),
                        e.timestamp);

            }

        });

        // The setprop and callback handler
        mTestPropTimerHandler = new Handler();
        setupViews();

        this.setTitle(getString(R.string.app_name) + " " + VERSION);

        // Setup camera surface
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        // Setup view for camera countdown
        mCountdownView = new CountDownView(this);
        getWindow().addContentView(
                mCountdownView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mBBView = new BoundingBoxView(this);
        getWindow().addContentView(
                mBBView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Setup bounding box view
        this.mBoundingBoxButton = (CheckBox) this
                .findViewById(R.id.boundingBoxButton);
        this.mBoundingBoxButton
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        mBBView.setBbFlag(isChecked);
                        Log.d(TAG, "Bounding box view "
                                + (isChecked ? "enabled" : "disabled"));
                        mBBView.invalidate();
                    }
                });
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState");

        savedInstanceState.putString(CURRENT_GESTURE_MODE,
                getGestureModeString(mCurrentMode));
        savedInstanceState.putInt(OVERLAY_FLAG, mOverlayFlag);
        savedInstanceState.putBoolean(SWIPE_LISTENER,
                (mSwipeListener != null));
        savedInstanceState.putBoolean(ENGAGEMENT_LISTENER,
                (mEngagementListener != null));
        savedInstanceState.putBoolean(HAND_DETECTION_LISTENER,
                (mHandDetectionListener != null));
        savedInstanceState.putBoolean(ENABLE_TOUCH_INJECTION,
                mEnableTouchInjection);
        savedInstanceState.putInt(CAMERA_SOURCE,
                mCameraSource.value());

        super.onSaveInstanceState(savedInstanceState);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");

        super.onRestoreInstanceState(savedInstanceState);

        setMode(getModeFromString(savedInstanceState.getString(CURRENT_GESTURE_MODE)));

        mOverlayFlag = savedInstanceState.getInt(OVERLAY_FLAG);
        mSwipeListener = savedInstanceState.getBoolean(SWIPE_LISTENER) ? new MySwipeListener()
                : null;
        mGestureDeviceManager.setSwipeListener(mSwipeListener);
        mEngagementListener = savedInstanceState
                .getBoolean(ENGAGEMENT_LISTENER) ? new MyEngagementListener()
                : null;
        mGestureDeviceManager.setEngagementListener(mEngagementListener);
        mHandDetectionListener = savedInstanceState
                .getBoolean(HAND_DETECTION_LISTENER) ? new MyHandDetectionListener()
                : null;

        mGestureDeviceManager.setHandDetectionListener(mHandDetectionListener);

        mEnableTouchInjection = savedInstanceState
                .getBoolean(ENABLE_TOUCH_INJECTION);

        mCameraSource = CameraSource.getCameraSource(savedInstanceState.getInt(CAMERA_SOURCE));
        mGestureDeviceManager.setCameraSource(mCameraSource);

        final TextView t = (TextView) findViewById(R.id.currentMode);
                            t.setText(mGestureDeviceManager.getGestureMode()
                                    .toString()
                                    + " ["
                                    + mCameraSource.toString() + "]");
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Turn on the properties handler
        if (mTestPropTimerHandler != null) {
            mTestPropTimerHandler.postDelayed(mTestPropPollingTask,
                    POLL_PERIOD_MSEC);
        }

        // Set the callback text to empty until an event is received.
        if (mCallbackText != null) {
            mCallbackText.setText("");
        }

        // Restart camera if in direct mode.
        if (mCameraSource == CameraSource.SOURCE_APP) {
            startCamera();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mTestPropTimerHandler != null) {
            mTestPropTimerHandler.removeCallbacks(mTestPropPollingTask);
        }

        // Release camera resource when exiting application.
        stopCamera();
    }

    /**
     * Show not supported alert.
     */
    private void showNotSupportedAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set title
        alertDialogBuilder.setTitle("Camera Gestures Error");

        // Set dialog message
        alertDialogBuilder.setMessage("Camera Gestures is not supported.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Setup views.
     */
    private void setupViews() {
        // Set up the overlay settings. Everything is initially enabled.
        mOverlayEnabled = true;
        mOverlayFlag = 0xFF;

        // Engagement info
        mEngagementValue = (TextView) findViewById(R.id.engagementValue);

        // Initialize preferences
        updateOverlayPrefs();

        // Setup event listeners
        mCallbackText = (TextView) findViewById(R.id.callbackText);

        // Update event listeners
        mSwipeListener = new MySwipeListener();

        mGestureDeviceManager.setSwipeListener(mSwipeListener);

        mEngagementListener = new MyEngagementListener();

        mGestureDeviceManager.setEngagementListener(mEngagementListener);

        mHandDetectionListener = new MyHandDetectionListener();

        mGestureDeviceManager.setHandDetectionListener(mHandDetectionListener);

    }

    /**
     * Update overlay prefs.
     */
    private void updateOverlayPrefs() {
        mOverlayPrefs.update(mOverlayEnabled, mOverlayFlag);
        mGestureDeviceManager.setOverlayPreferences(mOverlayPrefs);
    }

    /**
     * On overlay settings.
     *
     * @param v
     *            the v
     */
    public void OnOverlaySettings(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean[] checkedItems = { mOverlayPrefs.isEnabled(),
                mOverlayPrefs.showEngagement(), mOverlayPrefs.showLeft(),
                mOverlayPrefs.showRight(), mOverlayPrefs.showUp(),
                mOverlayPrefs.showDown() };

        final int[] flags = { 0, OverlayPreferences.ENGAGEMENT,
                OverlayPreferences.LEFT, OverlayPreferences.RIGHT,
                OverlayPreferences.UP, OverlayPreferences.DOWN };

        builder.setTitle(R.string.overlay_settings)
                .setMultiChoiceItems(R.array.overlay_settings, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which, boolean isChecked) {
                                if (which == 0) {
                                    mOverlayEnabled = isChecked;
                                } else if (which < flags.length) {
                                    // Set the flag
                                    mOverlayFlag = isChecked ? mOverlayFlag
                                            | flags[which] : mOverlayFlag
                                            & ~flags[which];

                                }
                            }

                        })
                .setPositiveButton(R.string.okay,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                updateOverlayPrefs();

                            }

                        });
        builder.create();
        builder.show();
    }

    /**
     * On listener settings.
     *
     * @param v
     *            the v
     */
    public void OnListenerSettings(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean[] checkedItems = { mSwipeListener != null,
                mEngagementListener != null, mHandDetectionListener != null };

        builder.setTitle(R.string.overlay_settings)
                .setMultiChoiceItems(R.array.listener_settings, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which, boolean isChecked) {
                                // ugly
                                switch (which) {
                                case 0:
                                    mSwipeListener = isChecked ? new MySwipeListener()
                                            : null;
                                    mGestureDeviceManager
                                            .setSwipeListener(mSwipeListener);
                                    break;
                                case 1:
                                    mEngagementListener = isChecked ? new MyEngagementListener()
                                            : null;
                                    mGestureDeviceManager
                                            .setEngagementListener(mEngagementListener);
                                    if (!isChecked) {
                                        mEngagementValue.setText("Unknown");
                                    }
                                    break;
                                case 2:
                                    mHandDetectionListener = isChecked ? new MyHandDetectionListener()
                                            : null;
                                    mGestureDeviceManager
                                            .setHandDetectionListener(mHandDetectionListener);
                                    break;
                                }

                            }

                        })
                .setPositiveButton(R.string.okay,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }

                        });
        builder.create();
        builder.show();
    }

    /**
     * On select output.
     *
     * @param v
     *            the v
     */
    public void OnSelectOutput(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_output).setItems(R.array.outputs,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mEnableTouchInjection = (which > 0);
                        mGestureDeviceManager
                                .enableTouchInjection(mEnableTouchInjection);

                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * On set camera source source.
     *
     * @param v
     *            the v
     */
    public void OnSetCameraSourceSource(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_camera_source).setItems(
                R.array.camera_sources, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        // Defaults to system if not valid
                        mCameraSource = (which == 1) ? CameraSource.SOURCE_APP
                                : CameraSource.SOURCE_SYS;

                        // Switch the camera source
                        if (mCameraSource != mGestureDeviceManager
                                .getCameraSource()) {

                            // Stop the camera if it is running
                            stopCamera();

                            // Restart gestures
                            mGestureDeviceManager
                                    .setCameraSource(mCameraSource);

                            // Toggle the camera
                            if (mCameraSource == CameraSource.SOURCE_APP) {
                                startCamera();
                            } else {
                                // Stop in case there is a countdown going
                                mCountdownView.stop();
                            }

                            final TextView t = (TextView) findViewById(R.id.currentMode);
                            t.setText(mGestureDeviceManager.getGestureMode()
                                    .toString()
                                    + " ["
                                    + mCameraSource.toString() + "]");

                            mSurfaceView
                                    .setVisibility(mCameraSource == CameraSource.SOURCE_APP ? View.VISIBLE
                                            : View.INVISIBLE);
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * On set camera id.
     *
     * @param v
     *            the v
     */
    public void OnSetCameraId(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.set_camera_id));
        alert.setMessage(R.string.set_camera_id_msg);

        // Create EditText to be used. Only accepts numerical input.
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true);
        alert.setView(input);

        alert.setPositiveButton(R.string.okay,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        try {
                            Integer v = Integer.valueOf(value.toString());

                            if (mCameraId != v.intValue()) {
                                mCameraId = v.intValue();

                                mGestureDeviceManager.setCameraId(mCameraId);

                                if (mCameraSource == CameraSource.SOURCE_APP) {
                                    stopCamera();
                                    startCamera();
                                }
                            }
                            Log.d(TAG, "Camera Id set to " + mCameraId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG,
                                    "Error setting camera id to "
                                            + value.toString());
                        }
                    }
                });

        alert.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
    }

    /**
     * On select mode.
     *
     * @param v
     *            the v
     */
    public void OnSelectMode(View v) {
        try {
            Log.d(TAG, "OnSelectMode");

            if (mCameraSource == CameraSource.SOURCE_APP) {
                return;
            }

            // Create array adapter for mode selection.
            final ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter
                    .createFromResource(this, R.array.modes,
                            android.R.layout.simple_list_item_1);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_mode).setItems(R.array.modes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Get the new configuration.
                            String selectedMode = modeAdapter.getItem(which)
                                    .toString();
                            setMode(getModeFromString(selectedMode));

                        }
                    });
            builder.create();
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the mode.
     *
     * @param mode
     *            the new mode
     */
    private void setMode(GestureMode mode) {
        mGestureDeviceManager.setMode(mode);
        mCurrentMode = mode;

        final TextView t = (TextView) findViewById(R.id.currentMode);
        t.setText(mode.toString() + " [" + mCameraSource.toString() + "]");
    }

    /**
     * Gets the mode from string.
     *
     * @param selectedMode
     *            the selected mode
     * @return the mode from string
     */
    public GestureMode getModeFromString(String selectedMode) {
        if (selectedMode != null && sModeMap.containsKey(selectedMode)) {
            return sModeMap.get(selectedMode);
        }

        return GestureMode.OFF;
    }

    /**
     * Gets the gesture mode string. Returns mode_none's string if not found.
     *
     * @param value
     *            the value
     * @return the gesture mode string
     */
    public String getGestureModeString(GestureMode value) {
        for (Entry<String, GestureMode> entry : sModeMap.entrySet()) {
            if (entry.getValue() == value) {
                return (entry.getKey());
            }
        }

        return getString(R.string.mode_none);
    }

    /** The gesture mode map. */
    private static HashMap<String, GestureMode> sModeMap;

    /**
     * Creates the mode map.
     */
    private void createModeMap() {
        sModeMap = new HashMap<String, GestureMode>();
        sModeMap.put(getString(R.string.mode_near_swipe),
                GestureMode.NEAR_GESTURE);
        sModeMap.put(getString(R.string.mode_engagement_gesture),
                GestureMode.ENGAGEMENT_GESTURE);
        sModeMap.put(getString(R.string.mode_pointer), GestureMode.POINTER);
        sModeMap.put(getString(R.string.mode_near_pointer),
                GestureMode.NEAR_AND_POINTER);
        sModeMap.put(getString(R.string.mode_near_engagement_gesture),
                GestureMode.NEAR_AND_ENGAGEMENT_GESTURE);
        sModeMap.put(getString(R.string.mode_engagement_only),
                GestureMode.ENGAGEMENT_DETECTION);
        sModeMap.put(getString(R.string.mode_click_and_drag),
                GestureMode.CLICK_AND_DRAG);
        sModeMap.put(getString(R.string.mode_circle), GestureMode.CIRCLE);
        sModeMap.put(getString(R.string.mode_handzoom), GestureMode.HAND_ZOOM);
        sModeMap.put(getString(R.string.mode_circle_and_click_drag), GestureMode.CIRCLE_AND_CLICK_DRAG);
    }

    /**
     * The listener interface for receiving myEngagement events. The class that
     * is interested in processing a myEngagement event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addMyEngagementListener<code> method. When
     * the myEngagement event occurs, that object's appropriate
     * method is invoked.
     *
     * @see MyEngagementEvent
     */
    public class MyEngagementListener implements EngagementListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * com.qualcomm.gesturesdk.listeners.EngagementListener#onEngagementChanged
         * (
         * com.qualcomm.gesturesdk.listeners.EngagementListener.EngagementEvent)
         */
        @Override
        public void onEngagementChanged(EngagementEvent e) {
            checkTest(TestOutcome.ENGAGEMENT);
            onCallback(
                    "onEngagementChanged: "
                            + mSimpleDateFormat.format(new Date(e.timestamp))
                            + "\n" + e.engagePercent + ", " + e.pose.toString(),
                    e.timestamp);
            mEngagementValue.setText(String.valueOf(e.engagePercent));

            // Run the camera and take a picture.
            //if (e.engagePercent >= 100f
            //        && mCameraSource == CameraSource.SOURCE_APP
            //        && !mCountdownView.isRunning()) {
            //    mCountdownView.start(5, mTakePicture);
            //}

            if (e.engagePercent == 0.0) {
                RectF f = new RectF(0, 0, 0, 0);
                mBBView.setBbRectF(f);
                mBBView.setDepthBar(f);
                mBBView.setRelDepthBar(f);
                mBBView.setDepthOutline(f);
                mBBView.invalidate();
                Log.d(TAG, "Engagement 0%, set bounding box and depth bar to 0");
            }
        }
    }

    /**
     * Returns true if in a dual swipe mode.
     *
     * @return true, if is dual mode
     */
    private boolean inDualSwipeMode() {
        return (mCurrentMode == GestureMode.NEAR_AND_ENGAGEMENT_GESTURE || mCurrentMode == GestureMode.NEAR_AND_POINTER);
    }

    /**
     * The listener interface for receiving mySwipe events. The class that is
     * interested in processing a mySwipe event implements this interface, and
     * the object created with that class is registered with a component using
     * the component's <code>addMySwipeListener<code> method. When
     * the mySwipe event occurs, that object's appropriate
     * method is invoked.
     *
     * @see MySwipeEvent
     */
    public class MySwipeListener implements SwipeListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * com.qualcomm.gesturesdk.listeners.SwipeListener#onLeftSwipe(com.qualcomm
         * .gesturesdk.listeners.SwipeListener.SwipeEvent)
         */
        @Override
        public void onLeftSwipe(SwipeEvent e) {

            checkTest(TestOutcome.LEFT_SWIPE);

            StringBuilder str = new StringBuilder();
            str.append("onLeftSwipe: ");
            str.append(mSimpleDateFormat.format(new Date(e.timestamp)));

            if (inDualSwipeMode()) {
                str.append(e.swipe_type == SwipeType.FAR ? " [Far]" : " [Near]");
            }

            onCallback(str.toString(), e.timestamp);
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

            checkTest(TestOutcome.RIGHT_SWIPE);

            StringBuilder str = new StringBuilder();
            str.append("onRightSwipe: ");
            str.append(mSimpleDateFormat.format(new Date(e.timestamp)));

            if (inDualSwipeMode()) {
                str.append(e.swipe_type == SwipeType.FAR ? " [Far]" : " [Near]");
            }

            onCallback(str.toString(), e.timestamp);
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
            checkTest(TestOutcome.UP_SWIPE);

            StringBuilder str = new StringBuilder();
            str.append("onUpSwipe: ");
            str.append(mSimpleDateFormat.format(new Date(e.timestamp)));

            if (inDualSwipeMode()) {
                str.append(e.swipe_type == SwipeType.FAR ? " [Far]" : " [Near]");
            }

            onCallback(str.toString(), e.timestamp);

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
            checkTest(TestOutcome.DOWN_SWIPE);

            StringBuilder str = new StringBuilder();
            str.append("onDownSwipe: ");
            str.append(mSimpleDateFormat.format(new Date(e.timestamp)));

            if (inDualSwipeMode()) {
                str.append(e.swipe_type == SwipeType.FAR ? " [Far]" : " [Near]");
            }

            onCallback(str.toString(), e.timestamp);
        }

    }

    /**
     * The listener interface for receiving myHandDetection events. The class
     * that is interested in processing a myHandDetection event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addMyHandDetectionListener<code> method. When
     * the myHandDetection event occurs, that object's appropriate
     * method is invoked.
     *
     * @see MyHandDetectionEvent
     */
    public class MyHandDetectionListener implements HandDetectionListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * com.qualcomm.gesturesdk.listeners.HandDetectionListener#onHandDetected
         * (com.qualcomm.gesturesdk.listeners.HandDetectionListener.
         * HandDetectionEvent)
         */

        @Override
        public void onHandDetected(HandDetectionEvent e) {
            checkTest(TestOutcome.HAND_DETECTION);
            int orientation = getResources().getConfiguration().orientation;
            StringBuilder text = new StringBuilder();

            text.append("onHandDetected: "
                    + mSimpleDateFormat.format(new Date(e.timestamp)));
            text.append((orientation == Configuration.ORIENTATION_PORTRAIT) ? "\n"
                    : " ");
            text.append("[" + e.boundingBox.left + ", " + e.boundingBox.top
                    + ", " + e.boundingBox.right + ", " + e.boundingBox.bottom
                    + "] ");
            text.append("True Z: " + String.format("%.3f", e.Z) + ", ");
            text.append("BB View is " + mBBView.getBbFlag() + ", ");
            text.append("Init Z is: " + String.format("%.3f", e.initZ) + " ");
            text.append("Relative Z is: " + String.format("%.3f", e.relZ)
                    + " ");
            text.append(e.pose.toString());
            onCallback(text.toString(), e.timestamp);

            /*
             * Displaying bounding box and depth bar. Depth bar is located at
             * the right side of the screen with width of 0.01*screen width.
             * Depth value is between 0 and 1. Vertically mapping display depth
             * bar ranging from 0 to 300 (or max height) pixels.
             */

            RectF depthOutline = new RectF();
            RectF depthBar = new RectF();
            RectF relDepthBar = new RectF();

            try {
                float depthBarWidth = mBBView.getWidth() / 100;
                float outlineHeight = Math.min(300, mBBView.getHeight());
                float depthBarHeight = e.Z * outlineHeight;
                float relDepthBarHeight = e.relZ * outlineHeight;
                depthOutline.set(mBBView.getWidth() - depthBarWidth, 0,
                        mBBView.getWidth(), outlineHeight);
                depthBar.set(mBBView.getWidth() - depthBarWidth, 0,
                        mBBView.getWidth(), depthBarHeight);
                relDepthBar.set(mBBView.getWidth() - 2 * depthBarWidth, 0,
                        mBBView.getWidth() - depthBarWidth, relDepthBarHeight);

                mBBView.setBbRectF(e.boundingBox);
                mBBView.setDepthBar(depthBar);
                mBBView.setRelDepthBar(relDepthBar);
                mBBView.setDepthOutline(depthOutline);

                mBBView.invalidate();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    /**
     * On callback.
     *
     * @param txt
     *            the txt
     * @param timestamp
     *            the timestamp
     */
    private void onCallback(String txt, long timestamp) {
        try {
            Log.d(TAG, txt);

            StringBuilder str = new StringBuilder();
            str.append(txt);

            if (timestamp == mLastEvent) {
                str.append("\n" + mCallbackText.getText());
            }

            mCallbackText.setText(str.toString());
            mLastEventTime = new Date();

            mLastEvent = timestamp;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception in onCallback");
        }
    }

    /** This task polls the properties at an interval. */
    private Runnable mTestPropPollingTask = new Runnable() {
        @Override
        public void run() {

            // Check last event time. If the event is stale, clear the text.
            Date now = new Date();
            if (now.getTime() - mLastEventTime.getTime() > POLL_PERIOD_MSEC) {
                mCallbackText.setText("");
            }

            // If mode has changed recently, update with new mode
            if (mLastModeString == null
                    || !mGestureDeviceManager.getGestureMode().toString()
                            .equals(mLastModeString)) {

                if (!mLastModeString.isEmpty()) {
                    updateMode(mLastModeString);
                }
            }

            // Use reflection to check gesture mode property
            @SuppressWarnings("rawtypes")
            Class clazz = null;
            try {
                clazz = Class.forName("android.os.SystemProperties");
                @SuppressWarnings("unchecked")
                Method method = clazz.getDeclaredMethod("get", String.class);
                String prop = (String) method.invoke(null, GESTURE_MODE);
                // Log.d(TAG, "GestureModeProperty read is: <" + prop + ">");

                if (!prop.equals(mLastModeString)) {
                    mLastModeString = prop;
                    updateMode(mLastModeString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Next poll
            mTestPropTimerHandler.postDelayed(mTestPropPollingTask,
                    POLL_PERIOD_MSEC);

        }
    };

    /**
     * Update mode.
     *
     * @param line
     *            the line
     */
    private void updateMode(String line) {
        try {
            if (line.equals(getString(R.string.mode_none))) {
                line = GestureMode.OFF.toString();
            }

            GestureMode mode = GestureMode.valueOf(line);
            Log.d(TAG, "Mode changed to " + mode.toString());
            setMode(mode);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Not a valid gesturemode value read" + line);
        } catch (Exception e) {

            e.printStackTrace();
            Log.d(TAG, "Property mode change failed. Bad mode name.");
        }
    }

    /**
     * Gets the mode string name.
     *
     * @param mode
     *            the mode
     * @return the mode string name
     */
    @SuppressWarnings("unused")
    private String getModeStringName(GestureMode mode) {

        for (Entry<String, GestureMode> entry : sModeMap.entrySet()) {
            if (entry.getValue() == mode) {
                return (entry.getKey());
            }
        }

        return getString(R.string.mode_none);

    }

    // ////////////////////////////////////////////
    // Unit Tests
    // ///////////////////////////////////////////

    /**
     * The Enum TestOutcome.
     */
    private enum TestOutcome {

        /** The LEFT swipe. */
        LEFT_SWIPE,
        /** The RIGHT swipe. */
        RIGHT_SWIPE,
        /** The UP swipe. */
        UP_SWIPE,
        /** The DOWN swipe. */
        DOWN_SWIPE,
        /** The ENGAGEMENT. */
        ENGAGEMENT,
        /** The HAND detection. */
        HAND_DETECTION
    }

    /**
     * The Class TestObject. A test object represents a single unit test. It
     * contains information for the test including which mode to set, the
     * expected outcome, the name of the test, and any UI elements for this
     * test.
     */
    private class TestObject {

        /** The gesture mode. */
        private GestureMode mGestureMode;

        /** The expected outcome. */
        private TestOutcome mExpectedOutcome;

        /** The test name. */
        private String mTestName;

        /** The checkbox to display test completion. */
        private Object mCheckBox;

        /**
         * Instantiates a new test object.
         *
         * @param mode
         *            the mode
         * @param outcome
         *            the outcome
         * @param test
         *            the test
         */
        public TestObject(GestureMode mode, TestOutcome outcome, String test) {
            mGestureMode = mode;
            mExpectedOutcome = outcome;
            mTestName = test;
        }

        /**
         * Gets the gesture mode.
         *
         * @return the gesture mode
         */
        public GestureMode getGestureMode() {
            return mGestureMode;
        }

        /**
         * Gets the expected outcome.
         *
         * @return the expected outcome
         */
        public TestOutcome getExpectedOutcome() {
            return mExpectedOutcome;
        }

        /**
         * Gets the test name.
         *
         * @return the test name
         */
        public String getTestName() {
            return mTestName;
        }

        /**
         * Gets the checkbox.
         *
         * @return the checkbox
         */
        public CheckBox getCheckbox() {
            return (CheckBox) mCheckBox;
        }
    }

    /**
     * Run tests. This method creates a dialog containing a list of tests. If
     * the list of tests is valid, the testing begins immediately.
     *
     * @param v
     *            the v
     */
    public void runTests(View v) {
        Log.d(TAG, "runTests()");
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.unit_test);
        dialog.setTitle(getString(R.string.unit_tests));

        // Setup the tests
        LinearLayout mainLayout = (LinearLayout) dialog
                .findViewById(R.id.test_list);

        // Dialog Button
        mDialogButton = (Button) dialog.findViewById(R.id.startButton);
        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.VISIBLE);

        Set<GestureMode> modes = new HashSet<GestureMode>();
        final Set<GestureMode> selectedModes = new HashSet<GestureMode>();
        for (int i = 0; i < mTests.length; ++i) {
            if (modes.add(mTests[i].getGestureMode())) {
                final CheckBox checkbox = new CheckBox(this);
                checkbox.setText(mTests[i].getGestureMode().toString());
                mainLayout.addView(checkbox);
                checkbox.setTag(mTests[i].getGestureMode());

                checkbox.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (checkbox.getTag() instanceof GestureMode) {
                            GestureMode mode = (GestureMode) checkbox.getTag();
                            if (checkbox.isChecked()) {
                                selectedModes.add(mode);

                            } else {
                                selectedModes.remove(mode);
                            }

                            mDialogButton
                                    .setVisibility(selectedModes.size() > 0 ? View.VISIBLE
                                            : View.INVISIBLE);
                        }
                    }
                });
            }
        }

        // If Cancel button is clicked then close the dialog.

        mDialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTests(selectedModes);
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        mInTestMode = true;
    }

    /**
     * Start tests.
     *
     * @param selectedModes
     *            the selected modes
     */
    private void startTests(Set<GestureMode> selectedModes) {
        Log.d(TAG, "startTests()");

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.unit_test);
        dialog.setTitle(getString(R.string.unit_tests));

        // Setup the tests
        LinearLayout mainLayout = (LinearLayout) dialog
                .findViewById(R.id.test_list);

        // Instruction text
        mInstruct = (TextView) dialog.findViewById(R.id.instruction);

        // Save the current mode before running the tests
        mSavedMode = mCurrentMode;

        // Dialog button
        mDialogButton = (Button) dialog.findViewById(R.id.cancelButton);

        // Build tests list
        if (mainLayout != null && mTests.length > 0) {

            // Setup each test with a checkbox
            for (int i = 0; i < mTests.length; ++i) {
                if (selectedModes.contains(mTests[i].getGestureMode())) {
                    CheckBox checkbox = new CheckBox(this);
                    checkbox.setText(mTests[i].getTestName());
                    mainLayout.addView(checkbox);
                    mTests[i].mCheckBox = checkbox;
                    mRunningTests.add(mTests[i]);
                }
            }

            TestObject firstTest = getCurrentTest();
            mTestsToRun = mRunningTests.size();

            if (firstTest != null) {

                // Initialize to the first test

                mInstruct.setText(firstTest.getTestName());
                // mCurrentTest = 0;
                mTestsPassed = 0;

                // Set the initial mode
                if (mGestureDeviceManager != null) {
                    Log.d(TAG,
                            "Setting first test to "
                                    + firstTest.getGestureMode());
                    mGestureDeviceManager.setMode(firstTest.getGestureMode());
                    updateMode(firstTest.getGestureMode().toString());
                }

                // If Cancel button is clicked then close the dialog.

                mDialogButton.setVisibility(View.VISIBLE);

                mDialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                        testEnded();
                    }

                });
            }
        }

        dialog.show();
        mInTestMode = true;
    }

    /**
     * User closes the dialog. Reset the values.
     */
    private void testEnded() {
        mInTestMode = false;
        mTestsToRun = 0;

        // Tests are completed. Return to previous state.
        if (mCurrentMode != mSavedMode) {
            updateMode(mSavedMode.toString());
        }

        // Reset UI handles.
        mInstruct = null;
        mDialogButton = null;
    }

    /**
     * Gets the current test.
     *
     * @return the current test
     */
    private TestObject getCurrentTest() {
        if (mRunningTests != null && mRunningTests.size() > 0) {
            return mRunningTests.peek();
        }

        return null;
    }

    /**
     * This method is called when some event has been received. Check if tests
     * are running and if so, compare the expected result with the current
     * result.
     *
     * @param outcome
     *            the outcome
     */
    public void checkTest(TestOutcome outcome) {
        // Log.d(TAG, "CheckTest");
        if (mInTestMode && mInstruct != null) {
            TestObject test = getCurrentTest();
            if (test != null
                    && test.getExpectedOutcome() == outcome
                    && mGestureDeviceManager != null
                    && mGestureDeviceManager.getGestureMode() == test
                            .getGestureMode()) {
                if (test.mCheckBox != null) {
                    test.getCheckbox().setChecked(true);
                    ++mTestsPassed;

                }

                mRunningTests.remove();

                // Check if there are anymore tests. If there are, change the
                // mode to the next test, otherwise, print out the results.
                if (mRunningTests.size() > 0) {
                    TestObject nextTest = getCurrentTest();
                    if (mGestureDeviceManager.getGestureMode() != nextTest
                            .getGestureMode()) {
                        mGestureDeviceManager
                                .setMode(nextTest.getGestureMode());
                        updateMode(nextTest.getGestureMode().toString());
                    }

                    mInstruct.setText(nextTest.getTestName());
                } else {

                    mInstruct.setText(mTestsPassed + "/" + mTestsToRun
                            + " passed");

                    if (mDialogButton != null) {
                        mDialogButton.setText(getString(R.string.done));
                    }

                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[],
     * android.hardware.Camera)
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Log.d(TAG, "onPreviewFrame on thread " + android.os.Process.myTid());
        mGestureDeviceManager.setFrame(data, mPreviewWidth,
                mPreviewHeight, mCameraOrientation, mIsMirrored);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
     * , int, int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

        Log.d(TAG, "surfaceChanged");
        if (mSurfaceHolder.getSurface() == null || mCamera == null) {
            // preview surface does not exist
            return;
        }

        // Restart the camera if necessary.
        try {
            if (mCamera != null) {
                stopCamera();
                startCamera();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
     * )
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mSurfaceHolder = holder;
    }

     /**
     * Gets the display rotation.
     *
     * @param activity
     *            the activity
     * @return the display rotation
     */
    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
        case Surface.ROTATION_0:
            return 0;
        case Surface.ROTATION_90:
            return 90;
        case Surface.ROTATION_180:
            return 180;
        case Surface.ROTATION_270:
            return 270;
        }
        return 0;
    }

    /**
     * Start camera.
     */
    private void startCamera() {
        try {
            mCamera = getCameraInstance();
            if (mCamera != null) {

                Camera.Parameters params = mCamera.getParameters();

                // Retrieve camera rotation info and rotate camera surface as
                // necessary.
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(mCameraId, cameraInfo);

                Log.d(TAG, "Camera Id " + mCameraId + " rotation is "
                        + cameraInfo.orientation);

                mCameraOrientation = cameraInfo.orientation;

                // Rotate display in respect to camera orientation.
                int degrees = getDisplayRotation();
                int result;
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mIsMirrored = true;
                    result = (cameraInfo.orientation + degrees) % 360;
                    result = (360 - result) % 360;
                } else {
                    mIsMirrored = false;
                    result = (cameraInfo.orientation - degrees + 360) % 360;
                }

                Log.d(TAG, "Setting display orientation to: " + result);
                mCamera.setDisplayOrientation(result);

                // Setting a preview of 800x480
                params.setPreviewSize(800, 480);
                mCamera.setParameters(params);

                mPreviewWidth = params.getPreviewSize().width;
                mPreviewHeight = params.getPreviewSize().height;

                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                 Log.d(TAG, "Camera has started.");

            } else {
                Toast.makeText(getApplicationContext(),
                        "Camera not available!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception opening camera " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop camera.
     */
    private void stopCamera() {
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "Camera is released.");
        }
    }

    /**
     * Gets the camera instance.
     *
     * @return the camera instance
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(mCameraId);
        } catch (Exception e) {
            Log.d(TAG, "Exception getting camera instance. " + e.getMessage());
        }

        return c;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
     * SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");

        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    /**
     * Gets the text height.
     *
     * @param p
     *            the paint
     * @return the text height
     */
    private float getTextHeight(Paint p) {
        return p.getFontMetrics().descent - p.getFontMetrics().ascent
                + p.getFontMetrics().leading;
    }

    /**
     * Gets the text width.
     *
     * @param text
     *            the text
     * @param p
     *            the paint
     * @return the text width
     */
    private float getTextWidth(String text, Paint p) {
        return p.measureText(text);
    }

    /**
     * The Class CountDownView. This view draws the countdown text.
     */
    class CountDownView extends View {

        /** The countdown. */
        private int countdown = 0;

        /** The m text paint. */
        private Paint mTextPaint;

        /** The handler. */
        private Handler handler;

        /** On countdown complete, this listener will be invoked. */
        private OnActionCompleteListener mListener;

        /**
         * Instantiates a new count down view.
         *
         * @param context
         *            the context
         */
        public CountDownView(Context context) {
            super(context);

            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTextSize(100);
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setColor(Color.GREEN);

            handler = new Handler();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.View#onDraw(android.graphics.Canvas)
         */
        @Override
        protected void onDraw(Canvas canvas) {
            // Draw the countdown text.
            if (countdown > 0) {
                canvas.drawText(String.valueOf(countdown), getWidth() / 2
                        - getTextHeight(mTextPaint) / 2, this.getHeight() / 2
                        - getTextWidth(String.valueOf(countdown), mTextPaint)
                        / 2, mTextPaint);
            }
        }

        /**
         * Start.
         *
         * @param timeout
         *            the timeout in seconds
         */
        public void start(int timeout, OnActionCompleteListener listener) {
            countdown = timeout;
            mListener = listener;
            invalidate();
            countdown();
        }

        /**
         * Stop.
         */
        public void stop() {
            countdown = 0;
            invalidate();
        }

        /**
         * Checks if is running.
         *
         * @return true, if is running
         */
        public boolean isRunning() {
            return (countdown > 0);
        }

        /**
         * Countdown one and update UI. If countdown is complete, take a
         * photograph.
         */
        private void countdown() {
            // Countdown to 0 only
            if (countdown <= 0) {
                return;
            }

            // Next second
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    --countdown;
                    invalidate();

                    if (countdown == 0 && mListener != null) {
                        mListener.onComplete();
                    } else {
                        countdown();
                    }
                }

            }, 1000);
        }
    }

    /**
     * Take a picture.
     */
    private OnActionCompleteListener mTakePicture = new OnActionCompleteListener() {

        @Override
        public void onComplete() {
            if (mCamera == null) {
                Log.e(TAG, "Camera is null! Cannot takePicture()");
                return;
            }

            try {
                mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        // Have this callback so the system plays the sound.
                        Log.d(TAG, "onShutter");
                    }
                }, null, mSavePicture);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception in takePicture() " + e.getMessage());
            }
        }
    };

    /*
     * The callback interface used to supply image data from a photo capture.
     * Saves the photo to the SD card if one is mounted under the
     * Pictures/Gestures directory.
     */
    private PictureCallback mSavePicture = new PictureCallback() {

        /*
         * Save the jpeg image to the SD card.
         *
         * (non-Javadoc)
         *
         * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[],
         * android.hardware.Camera)
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken()");
            File pictureFile = getOutputMediaFile();

            if (pictureFile == null) {
                Log.d(TAG,
                        "Error creating media file, check permissions and if SD card is installed.");
                return;
            }

            try {
                // Save the file
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception onPictureTaken(): " + e.getMessage());
            }

            // Restart the camera
            stopCamera();
            startCamera();
        }

        /*
         * Check SD card status and return file to write to.
         */
        private File getOutputMediaFile() {

            try {
                // Check if SD card is mounted.
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Log.d(TAG, "SD Card is not mounted. The state is "
                            + Environment.getExternalStorageState());
                    return null;
                }

                // File will be stored in Pictures/Gestures directory.
                File mediaStorageDir = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        getString(R.string.gestures));

                // Create the directory if it does not exist
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d(TAG, "Failed to create directory!");
                        return null;
                    }
                }

                // Create a file name with timestamp.
                String timeStamp = mSimpleDateFormat.format(new Date());
                File mediaFile = new File(mediaStorageDir.getPath()
                        + File.separator + "IMG_" + timeStamp + ".jpg");

                return mediaFile;
            } catch (Exception e) {
                Log.d(TAG,
                        "Exception in getOutputMediaFile(): " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    };

    public class BoundingBoxView extends View {

        /* Bounding box rectangle */
        private RectF bbRectF = new RectF(0, 0, 0, 0);

        /* Depth bar rectangle */
        private RectF depthBar = new RectF(0, 0, 0, 0);

        /* Depth bar rectangle */
        private RectF relDepthBar = new RectF(0, 0, 0, 0);

        /* Depth bar outline rectangle */
        private RectF depthOutline = new RectF(0, 0, 0, 0);

        /* Bounding box paint */
        private Paint bbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /* Depth bar paint */
        private Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /* Relative depth bar paint */
        private Paint relBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /* Depth bar outline paint */
        private Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /* Bounding box and depth bar flag */
        private boolean bbFlag = true;

        public void setBbRectF(RectF bbRectF) {
            this.bbRectF = bbRectF;
        }

        public void setDepthBar(RectF depthBar) {
            this.depthBar = depthBar;
        }

        public void setRelDepthBar(RectF relDepthBar) {
            this.relDepthBar = relDepthBar;
        }

        public void setDepthOutline(RectF depthOutline) {
            this.depthOutline = depthOutline;
        }

        public boolean getBbFlag() {
            return bbFlag;
        }

        public void setBbFlag(boolean bbFlag) {
            this.bbFlag = bbFlag;
        }

        public BoundingBoxView(Context context) {
            super(context);
            bbPaint.setColor(Color.GREEN);
            bbPaint.setStyle(Style.STROKE);
            bbPaint.setStrokeWidth(2);

            barPaint.setColor(Color.RED);
            barPaint.setStyle(Style.FILL);

            relBarPaint.setColor(Color.YELLOW);
            relBarPaint.setStyle(Style.FILL);

            outlinePaint.setColor(Color.RED);
            outlinePaint.setStyle(Style.STROKE);
        }

        @Override
        public void onDraw(Canvas canvas) {

            if (bbFlag) {
                canvas.drawRect(bbRectF, bbPaint);
                canvas.drawRect(depthBar, barPaint);
                canvas.drawRect(relDepthBar, relBarPaint);
                canvas.drawRect(depthOutline, outlinePaint);
            } else
                canvas.drawColor(Color.TRANSPARENT);
        }
    }
}