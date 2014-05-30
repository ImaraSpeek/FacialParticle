/**
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Confidential and Proprietary.
 */

package com.qualcomm.gesturephototaking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.qualcomm.snapdragon.sdk.gestures.EngagementListener;
import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager;
import com.qualcomm.snapdragon.sdk.gestures.GestureDeviceManager.CameraSource;

/**
 * The Class MainActivity.
 */
public class MainActivity extends Activity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, EngagementListener {

    /** The Constant TAG. */
    private final static String TAG = "GesturePhotoTaking";

    /** The gesture device manager. */
    private GestureDeviceManager mGestureDeviceManager = null;

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
    private CameraSource mCameraSource = CameraSource.SOURCE_APP;

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

    /**
     * The Interface ActionComplete. Callback for actions. This can be used for
     * any action complete, such a countdown view.
     * @see OnActionCompleteEvent
     */
    interface OnActionCompleteListener {

        /**
         * On complete.
         */
        public void onComplete();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Do not dim screen while running this
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGestureDeviceManager = new GestureDeviceManager(this);
        mGestureDeviceManager.setCameraSource(CameraSource.SOURCE_APP);
        mGestureDeviceManager.setEngagementListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.qualcomm.snapdragon.sdk.gestures.EngagementListener#onEngagementChanged
     * (com.qualcomm.snapdragon.sdk.gestures.EngagementListener.EngagementEvent)
     */
    @Override
    public void onEngagementChanged(EngagementEvent e) {
        Log.d(TAG, "engaged : " + e.engagePercent);
        // Run the camera
        if (e.engagePercent >= 100f && mCameraSource == CameraSource.SOURCE_APP
                && !mCountdownView.isRunning()) {
            mCountdownView.start(5, mTakePicture);
        }
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

        // Release camera resource when exiting application.
        stopCamera();

        // Stop any pending countdowns
        mCountdownView.stop();
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
        mGestureDeviceManager.setFrame(data, mPreviewWidth, mPreviewHeight,
                mCameraOrientation, mIsMirrored);
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
     * Start camera, adjust display display based on camera orientation, start
     * up preview.
     */
    private void startCamera() {
        try {
            mCamera = getCameraInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception opening camera: " + e.getMessage());
        }
        try {
            if (mCamera != null) {

                Camera.Parameters params = mCamera.getParameters();

                // Retrieve camera orientation info and rotate camera surface as
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

                // Start preview and set callback.
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Camera not available!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception in startCamera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop camera.
     */
    private void stopCamera() {
        if (null != mCamera) {
            // Stop the camera and cancel preview callback. Release resource.
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

        // Return null if there is no camera!
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
         * @param listener
         *            the listener
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
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                        "yy-MM-dd HH:mm:ss", Locale.getDefault());

                String timeStamp = simpleDateFormat.format(new Date());
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

}
