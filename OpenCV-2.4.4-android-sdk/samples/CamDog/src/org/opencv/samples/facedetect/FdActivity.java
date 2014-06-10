package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;


@SuppressWarnings("unused")
public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final String	   TagD				   = "OCVSample::Debugging";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    
    /*
    public static File working_Dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/opencv");
    static File fileC;
    static {
    	working_Dir.mkdirs();
    	 fileC = new File(FdActivity.working_Dir,"csv.txt");
    
    }
	public static boolean pictureTaken,recognized;
	*/

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;

    private Mat                    mRgba;
    private Mat                    mGray;
    
    private File                   mCascadeFile;
    
    // Native detector and java detector
    private DetectionBasedTracker  mNativeDetector;
    private CascadeClassifier      mFaceDetector;
    
    
    private int                    mDetectorType       = NATIVE_DETECTOR;
    private String[]               mDetectorName;
    private static String		   current_name 		= "Imara";

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    // Native library for the camera
    private CameraBridgeViewBase   mOpenCvCameraView;
    
    // for tracking the face
    CamShifting cs;
    CamShifting cseyes;

    private long				   starttime = 0;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                    	// initialize new camshift
                    	//cs = new CamShifting();                    	
                    	
                        // load cascade file from application resources - lpbcascade is faster than haarcascade but not as robust
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // This part is for the java cascade classifier 
                        mFaceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mFaceDetector.empty()) {
                            Log.e(TAG, "Failed to load Face cascade classifier");
                            mFaceDetector = null;
                        } else
                        {
                            Log.i(TAG, "Loaded Face cascade classifier from " + mCascadeFile.getAbsolutePath());
                        }
                        
                        // create the native detector for opencv
                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    /*
                    try {
                        // load cascade file from application resources
                        InputStream iseye = getResources().openRawResource(R.raw.haarcascade_eye);
                        File cascadeDireye = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileeye = new File(cascadeDireye, "haarcascade_eye.xml");
                        FileOutputStream oseye = new FileOutputStream(mCascadeFileeye);

                        byte[] buffereye = new byte[4096];
                        int bytesReadeye;
                        while ((bytesReadeye = iseye.read(buffereye)) != -1) {
                            oseye.write(buffereye, 0, bytesReadeye);
                        }
                        iseye.close();
                        oseye.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mEyeDetector = new CascadeClassifier(mCascadeFileeye.getAbsolutePath());
                        if (mEyeDetector.empty()) {
                            Log.e(TAG, "Failed to load eye cascade classifier");
                            mEyeDetector = null;
                        } else
                        {
                            Log.i(TAG, "Loaded eye cascade classifier from " + mCascadeFileeye.getAbsolutePath());
                        }

                        // create detector for the eyes
                        //mNativeDetectoreye = new DetectionBasedTracker(mCascadeFileeye.getAbsolutePath(), 0);

                        cascadeDireye.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    try {
                        // load cascade file from application resources
                        InputStream ismouth = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
                        File cascadeDirmouth = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFilemouth = new File(cascadeDirmouth, "haarcascade_mcs_mouth.xml");
                        FileOutputStream osmouth = new FileOutputStream(mCascadeFilemouth);

                        byte[] buffermouth = new byte[4096];
                        int bytesReadmouth;
                        while ((bytesReadmouth = ismouth.read(buffermouth)) != -1) {
                            osmouth.write(buffermouth, 0, bytesReadmouth);
                        }
                        ismouth.close();
                        osmouth.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mMouthDetector = new CascadeClassifier(mCascadeFilemouth.getAbsolutePath());
                        if (mMouthDetector.empty()) {
                            Log.e(TAG, "Failed to load mouth cascade classifier");
                            mMouthDetector = null;
                        } else
                        {
                            Log.i(TAG, "Loaded eye cascade classifier from " + mCascadeFilemouth.getAbsolutePath());
                        }

                        // create detector mouth
                        //mNativeDetectormouth = new DetectionBasedTracker(mCascadeFilemouth.getAbsolutePath(), 0);

                        cascadeDirmouth.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    try {
                        // load cascade file from application resources
                        InputStream isnose = getResources().openRawResource(R.raw.haarcascade_mcs_nose);
                        File cascadeDirnose = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFilenose = new File(cascadeDirnose, "haarcascade_mcs_nose.xml");
                        FileOutputStream osnose = new FileOutputStream(mCascadeFilenose);

                        byte[] buffernose = new byte[4096];
                        int bytesReadnose;
                        while ((bytesReadnose = isnose.read(buffernose)) != -1) {
                            osnose.write(buffernose, 0, bytesReadnose);
                        }
                        isnose.close();
                        osnose.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mNoseDetector = new CascadeClassifier(mCascadeFilenose.getAbsolutePath());
                        if (mNoseDetector.empty()) {
                            Log.e(TAG, "Failed to load nose cascade classifier");
                            mNoseDetector = null;
                        } else
                        {
                            Log.i(TAG, "Loaded nose cascade classifier from " + mCascadeFilenose.getAbsolutePath());
                        }

                        //mNativeDetectornose = new DetectionBasedTracker(mCascadeFilenose.getAbsolutePath(), 0);

                        cascadeDirnose.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    */

                    // enable the opencv camera
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    // A button listener for all the buttons
    private class ButtonListener implements View.OnClickListener{
    	Intent i;
    	FileOutputStream out;
    	
		public void onClick(View v) {
			
		}
    }
    
    
    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
       

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        MatOfRect 	faces = new MatOfRect();
        Rect[] 		facesArray = null;

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }
        
    	
    	// detect the faces using opencv
    	//mNativeDetector.detect(mGray, faces);
   		mFaceDetector.detectMultiScale(mGray, faces, 1.5,2,2,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
    	
        facesArray = faces.toArray();
        for (int j = 0; j < facesArray.length; j++){
        	Core.rectangle(mRgba, facesArray[j].tl(), facesArray[j].br(), FACE_RECT_COLOR, 3);
        }


        /*
        MatOfRect initfaces = new MatOfRect();
        MatOfRect faces = new MatOfRect();
        MatOfRect trackfaces = new MatOfRect();
        
        MatOfRect eyes = new MatOfRect();
        MatOfRect lefteye = new MatOfRect();
        MatOfRect righteye = new MatOfRect();
        
        MatOfRect mouths = new MatOfRect();
        MatOfRect noses = new MatOfRect();
        
        Point leftEye, rightEye;    // Position of the detected eyes.
        
        Rect[] facesArray = null;
        Rect[] eyesArray = null;
        Rect[] eyeleftArray = null;
        Rect[] eyerightArray = null;
        Rect[] mouthsArray = null;
        Rect[] nosesArray = null;
        
        RotatedRect trackeyes = null;
        RotatedRect trackface = null;
        
        // create a new region to look for the eyes
        Mat mEyeGrayLeft = new Mat();
        Mat mEyeRgbaLeft = new Mat();
        Mat mEyeGrayRight = new Mat();
        Mat mEyeRgbaRight = new Mat();
        Mat mNoseGray = new Mat();
        Mat mNoseRgba = new Mat();

        Mat mMouthGray = new Mat();
        Mat mMouthRgba = new Mat();
        
        
        
        Mat faceImg = null;
        int facetoprow, facebottomrow, faceleftcolumn, facerightcolumn;
        
        Rect trackhue = null;

        // If no face has been detected yet, detect the face
        // TODO add a way to falsify more than 1 face
        
        // if a face has already been detected, we should track that face until it is lost
        if (facedetected)
        {
            // track the face in the new frame
            trackface = cs.camshift_track_face(mRgba, facesArray, cs);            
            // Convert the rotated rectangle from cam shifting to a regular rectangle 
            trackhue = trackface.boundingRect();
            //outline face with rectangle
        	Core.rectangle(mRgba, trackhue.tl(), trackhue.br(), HUE_RECT_COLOR, 3);	           
            //outline the tracked eclipse
            Core.ellipse(mRgba, trackface, NOSE_RECT_COLOR, 3);
            
            // check whether the face is still a valid detection, else check again
            if (trackhue.width < (mGray.width()/5))
            {
            	facedetected = false;
            	facelost = true;
            }
        }
        // if the face is not tracked, we should detect it again
        if(!facedetected) 
        {	
        	
	    		// detect the faces using opencv
        		mNativeDetector.detect(mGray, faces);
	   			//mFaceDetector.detectMultiScale(mGray, faces, 2,2,2,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
	        
	            // check if there is a face detected and assign them to the array
	            if (!faces.empty())
	            {
	            	facesArray = faces.toArray();  
	            	facedetected = true;
	            	facelost = false;
	            	Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);
	            
	                // When face is detected, start tracking it using camshifting
	                cs.create_tracked_object(mRgba,facesArray,cs);
	                // track the face in the new frame
	                trackface = cs.camshift_track_face(mRgba, facesArray, cs);            
	                // Convert the rotated rectangle from cam shifting to a regular rectangle 
	                trackhue = trackface.boundingRect();
	            }
	        }   
	       
		    if (facedetected)
		    	
		    {
		    	//if(trackhue.x > 0 && trackhue.y > 0)
		    	if (trackhue.width > 0 && trackhue.height > 0)
	        	{
		    	
		            // compute the eye area
		           	//Rect eyearea = new Rect(trackhue.x +trackhue.width/8,(int)(trackhue.y + (trackhue.height/4.5)),trackhue.width - trackhue.width/8,(int)( trackhue.height/2.0));
		            // split it
		            Rect eyearea_right = new Rect(trackhue.x +trackhue.width/16,(int)(trackhue.y + (trackhue.height/4.5)),(trackhue.width - trackhue.width/16)/2,(int)( trackhue.height/4));
		            Rect eyearea_left = new Rect(trackhue.x +trackhue.width/16 + (trackhue.width - 2*trackhue.width/16)/2,(int)(trackhue.y + (trackhue.height/4.5)),(trackhue.width - trackhue.width/16)/2,(int)( trackhue.height/4));
		            // draw the area - mGray is working grayscale mat, if you want to see area in rgb preview, change mGray to mRgba
		            Core.rectangle(mRgba, eyearea_left.tl(),eyearea_left.br() , DEBUG_RECT_COLOR, 2);
		            Core.rectangle(mRgba, eyearea_right.tl(),eyearea_right.br(), DEBUG_RECT_COLOR, 2);
		            
		            
		            mEyeGrayLeft = mGray.adjustROI((eyearea_left.y - eyearea_left.height), eyearea_left.y, eyearea_left.x, (eyearea_left.x + eyearea_left.width));
		            mEyeDetector.detectMultiScale(mEyeGrayLeft, lefteye, 1.1,2,2,new Size(), new Size());
		            eyeleftArray = lefteye.toArray();
		            for (int j = 0; j < eyeleftArray.length; j++){
		            	Core.rectangle(mEyeRgbaLeft, eyeleftArray[0].tl(), eyeleftArray[0].br(), EYES_RECT_COLOR, 3);
		            }
		            
		            
		            mEyeGrayLeft = mGray.submat(eyearea_left);
		            mEyeRgbaLeft = mRgba.submat(eyearea_left);
		            mEyeGrayRight = mGray.submat(eyearea_right);
		            mEyeRgbaRight = mRgba.submat(eyearea_right);
		            
		            // Java detector performs betters, detect eyes
		            mEyeDetector.detectMultiScale(mEyeGrayLeft, lefteye, 1.1,2,2,new Size(), new Size());
		            mEyeDetector.detectMultiScale(mEyeGrayRight, righteye, 1.1,2,2,new Size(), new Size());
		            
		            // transform to arrays and print the eyes
		            eyeleftArray = lefteye.toArray();
		            eyerightArray = righteye.toArray();
		            for (int j = 0; j < eyeleftArray.length; j++){
		            	Core.rectangle(mEyeRgbaLeft, eyeleftArray[0].tl(), eyeleftArray[0].br(), EYES_RECT_COLOR, 3);
		            }
		            for (int j = 0; j < eyerightArray.length; j++){
		            	Core.rectangle(mEyeRgbaRight, eyerightArray[0].tl(), eyerightArray[0].br(), EYES_RECT_COLOR, 3);
		            }	            
		            
		            
		            // compute the mouth area
			        Rect moutharea = new Rect(trackhue.x +trackhue.width/8,(int)(trackhue.y + trackhue.height/2),trackhue.width - trackhue.width/8,(int)( trackhue.height/3.0));
			        Core.rectangle(mRgba, moutharea.tl(), moutharea.br(), DEBUG_RECT_COLOR, 2);
		            
			        mMouthGray = mGray.submat(moutharea);
		            mMouthRgba = mRgba.submat(moutharea);
		            
		            mMouthDetector.detectMultiScale(mMouthGray, mouths, 1.1, 3, 2, new Size(), new Size());
		        	//mNativeDetectormouth.detect(mGray, mouths);
		        	mouthsArray = mouths.toArray();
		            //for (int j = 0; j < mouthsArray.length; j++)
		            //{
		            //	Core.rectangle(mMouthRgba, mouthsArray[j].tl(), mouthsArray[j].br(), MOUTH_RECT_COLOR, 3);            	
		            //}
		        	if (mouthsArray.length > 0)
		        	{
		        		Core.rectangle(mMouthRgba, mouthsArray[0].tl(), mouthsArray[0].br(), MOUTH_RECT_COLOR, 3);            	
		        	}
		           
			        
			        /*
			        // compute the nose area
			        Rect nosearea = new Rect(trackhue.x +trackhue.width/8,(int)(trackhue.y + trackhue.height/2),trackhue.width - trackhue.width/4,(int)( trackhue.height/3.5));
			        Core.rectangle(mRgba, moutharea.tl(), moutharea.br(), DEBUG_RECT_COLOR, 2);
		            
			        mNoseGray = mGray.submat(nosearea);
			        mNoseRgba = mRgba.submat(nosearea);
			        mNoseDetector.detectMultiScale(mNoseGray, noses, 1.1,2,2,new Size(), new Size());
		            
		        	//mNativeDetectornose.detect(mGray, noses);
		        	nosesArray = noses.toArray();
		            for (int j = 0; j < nosesArray.length; j++)
		            {
		            	Core.rectangle(mNoseRgba, nosesArray[j].tl(), nosesArray[j].br(), NOSE_RECT_COLOR, 3);            	
		            }
		            
	        	}	      
		    }
		    */
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

}
