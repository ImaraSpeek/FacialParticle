
package com.cameraimara;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class HelloOpenCvActivity extends Activity implements CvCameraViewListener2 {
	
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	     Log.i(TAG, "called onCreate");
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.helloopencvlayout);
	     mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	     mOpenCvCameraView.setCvCameraViewListener(this);
	 }

	 @Override
	 public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	 public void onCameraViewStarted(int width, int height) {
	 }

	 public void onCameraViewStopped() {
	 }

	 public Mat onCameraFrame(Mat inputFrame) {
	     return inputFrame;
	 }
	 
	 private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		    @Override
		    public void onManagerConnected(int status) {
		        switch (status) {
		            case LoaderCallbackInterface.SUCCESS:
		            {
		                Log.i(TAG, "OpenCV loaded successfully");
		                mOpenCvCameraView.enableView();
		            } break;
		            default:
		            {
		                super.onManagerConnected(status);
		            } break;
		        }
		    }
		};

		@Override
		public void onResume()
		{
		    super.onResume();
		    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		}

		@Override
		public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			// TODO Auto-generated method stub
			return null;
		}

}
