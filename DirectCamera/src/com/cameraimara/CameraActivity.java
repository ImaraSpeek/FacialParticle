package com.cameraimara;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
		private static final String TAG = "CameraActivity";
		private Camera camera;
		private Camera front;
		
		
		
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
			holder.setFixedSize(400, 300);
		}
		
		public void surfaceCreated(SurfaceHolder holder) 
		{
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
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
