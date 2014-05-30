/* ======================================================================
 *  Copyright © 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 *  
 * @file:   CameraSurfacePreview.java
 *
 */


package com.qualcomm.snapdragon.sdk.sample;

import java.io.IOException;

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mHolder;
    private Camera mCamera;
    Context mContext;
    FacialProcessing mFaceProc;
    
    //Facial Values
    int numFaces = 0;
    int smileValue = 0;
    
    //Supported Preview Sizes for the camera
    int mSupportedWidth;
    int mSupportedHeight;

    

	public CameraSurfacePreview(Context context, Camera camera, FacialProcessing faceProc) {
		super(context);
		mCamera = camera;
		mContext = context;
		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFaceProc= faceProc;
    
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
        try 
        {
        	mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();                
        } 
        catch (IOException e) 
        {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {	
		
		
	}


}
