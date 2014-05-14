/* ======================================================================
 *  Copyright 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 * @file    LiveRecognition.java
 *
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class LiveRecognition extends Activity implements Camera.PreviewCallback{
	
	Camera cameraObj;				// Accessing the Android native Camera.
	FrameLayout preview;
	CameraSurfacePreview mPreview;	
	private int FRONT_CAMERA_INDEX= 1;
	private int BACK_CAMERA_INDEX = 0;	
	OrientationEventListener orientationListener;
	public static int displayAngle;	
	private int lastAngle = 0;
	FacialProcessing faceObj;
	public int frameWidth;
	public int frameHeight;
	public boolean cameraFacingFront = true;
	public static PREVIEW_ROTATION_ANGLE rotationAngle = PREVIEW_ROTATION_ANGLE.ROT_90;
	public DrawView drawView;
	FaceData[] faceArray;			// Array in which all the face data values will be returned for each face detected. 
	ImageView switchCameraButton;
	Vibrator vibrate;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_recognition);
		
		faceObj = FacialRecognitionActivity.faceObj;
		switchCameraButton = (ImageView) findViewById(R.id.camera_facing);
		vibrate = (Vibrator) LiveRecognition.this.getSystemService(Context.VIBRATOR_SERVICE);	
		
		orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {		// Rotate the buttons based on the display so that we do not have to open/close the camera on orientation change. 
			@Override
			public void onOrientationChanged(int arg0) {
				int prevAngle = lastAngle * 90;
				if (arg0 - prevAngle < 0) {
					arg0 += 360;
				}
				
				// Only shift if > 60 degree deviance
				if (Math.abs(arg0 - prevAngle) < 60) return;
				
				int angle = ((arg0 + 45) % 360) / 90;

				if (lastAngle == angle) 
				{
					return;
				}
				lastAngle = angle;
				
				switch(angle) {
					case 0: // portrait
						displayAngle = 0;
						break;
					case 1: // landscape right
						displayAngle = 270;
						break;
					case 2: // upside-down
						displayAngle = 180;
						break;
					case 3: // landscape left
						displayAngle = 90;
						break;
				}
			}						
		};
		
		switchCameraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				vibrate.vibrate(80);
				if(cameraFacingFront)
				{
					switchCameraButton.setImageResource(R.drawable.camera_facing_back);
					cameraFacingFront = false;
				}
				else
				{
					switchCameraButton.setImageResource(R.drawable.camera_facing_front);
					cameraFacingFront = true;
				}					
				stopCamera();
				startCamera();				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.live_recognition, menu);
		return true;
	}
	
	protected void onPause() {
        super.onPause();
        stopCamera();
    }

	protected void onDestroy(){
		super.onDestroy();
	}

	 protected void onResume() {
	        super.onResume();
	        if(cameraObj!=null)
	        {
	        	stopCamera();	        	
	        }	        
	        startCamera();
	 }
	
	/*
	 *  Stops the camera preview. Releases the camera. Make the objects null. 
	 */
	 private void stopCamera() {
		
		if(cameraObj!=null)
		{
			cameraObj.stopPreview();
            cameraObj.setPreviewCallback(null);
            preview.removeView(mPreview);	            
            cameraObj.release();
		}	
		cameraObj = null;
	}

	 /*
	  * Method that handles initialization and starting of camera. 
	  */
	private void startCamera() {
		if(cameraFacingFront)
		{
			cameraObj = Camera.open(FRONT_CAMERA_INDEX); 					// Open the Front camera  	
		}
		else
		{
			cameraObj = Camera.open(BACK_CAMERA_INDEX); 					// Open the back camera   	
		}	       		    	
		mPreview = new CameraSurfacePreview(LiveRecognition.this, cameraObj, orientationListener);	// Create a new surface on which Camera will be displayed. 
	    preview = (FrameLayout) findViewById(R.id.cameraPreview2);
	    preview.addView(mPreview);
	    cameraObj.setPreviewCallback(LiveRecognition.this); 	
	    frameWidth = cameraObj.getParameters().getPreviewSize().width;
	    frameHeight = cameraObj.getParameters().getPreviewSize().height;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		boolean result =false;
		faceObj.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
		if(cameraFacingFront)
		{
			result = faceObj.setFrame(data, frameWidth, frameHeight, true, rotationAngle);
		}
		else
		{
			result = faceObj.setFrame(data, frameWidth, frameHeight, false, rotationAngle);
		}
		if(result)
		{
			int numFaces = faceObj.getNumFaces();
			if(numFaces==0)
			{
				Log.d("TAG", "No Face Detected");
				if(drawView!=null)
				{
					preview.removeView(drawView);
					drawView = new DrawView(this, null, false);
			        preview.addView(drawView);
			    }
			}
			else
			{			
				faceArray= faceObj.getFaceData();				
				if(faceArray == null)
				{
					Log.e("TAG", "Face array is null");
				}
				else
				{	
					int surfaceWidth = mPreview.getWidth();
					int surfaceHeight = mPreview.getHeight();
					faceObj.normalizeCoordinates(surfaceWidth, surfaceHeight);	
					preview.removeView(drawView);					// Remove the previously created view to avoid unnecessary stacking of Views. 
					drawView = new DrawView(this, faceArray, true);		
			        preview.addView(drawView);
				}
			}
		}
	}

}
