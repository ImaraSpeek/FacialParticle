package com.cameraimara;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
		private static final String TAG = "CameraActivity";
		private Camera camera;
		//private Camera front;
		private boolean isPreviewRunning = false;
		
		
		
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			
			//front = openFrontFacingCamera();
			
			SurfaceView surface = (SurfaceView)findViewById(R.id.stream);
			SurfaceHolder holder = surface.getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			
			// Can set the size of the camera view here
			//holder.setFixedSize(400, 300);
		}
		
		public void surfaceCreated(SurfaceHolder holder) 
		{
			
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
				//isPreviewRunning = true;
				// TODO Draw over the preview if required.
			} 
			catch (IOException e) 
			{
				Log.d(TAG, "IO Exception", e);
			}
			
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) 
		{
			camera.stopPreview();
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format,int width, int height) 
		{
			
			if (isPreviewRunning)
	        {
	            camera.stopPreview();
	        }

	        Parameters parameters = camera.getParameters();
	        //Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

	        if(display.getRotation() == Surface.ROTATION_0)
	        {
	            parameters.setPreviewSize(height, width);                           
	            camera.setDisplayOrientation(90);
	        }

	        if(display.getRotation() == Surface.ROTATION_90)
	        {
	            parameters.setPreviewSize(width, height);                           
	        }

	        if(display.getRotation() == Surface.ROTATION_180)
	        {
	            parameters.setPreviewSize(height, width);               
	        }

	        if(display.getRotation() == Surface.ROTATION_270)
	        {
	            parameters.setPreviewSize(width, height);
	            camera.setDisplayOrientation(180);
	        }

	        camera.setParameters(parameters);
	        previewCamera(holder);      
			
		}
		
		
		public void previewCamera(SurfaceHolder holder)
		{        
		    try 
		    {           
		        camera.setPreviewDisplay(holder);          
		        camera.startPreview();
		        isPreviewRunning = true;
		    }
		    catch(Exception e)
		    {
		        Log.d(TAG, "Cannot start preview", e);    
		    }
		}
		
		
		@Override
		protected void onPause() 
		{
			super.onPause();
			camera.release();
		}
		
		@Override
		protected void onResume() 
		{
			super.onResume();
			//camera = Camera.open();
			camera = openFrontFacingCamera();
		}
		
		private Camera openFrontFacingCamera() 
		{
		    int cameraCount = 0;
		    Camera cam = null;
		    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		    cameraCount = Camera.getNumberOfCameras();
		    for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
		        Camera.getCameraInfo( camIdx, cameraInfo );
		        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
		            try {
		                cam = Camera.open( camIdx );
		            } catch (RuntimeException e) {
		                Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
		            }
		        }
		    }

		    return cam;
		}


}
