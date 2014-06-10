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
import org.opencv.core.CvType;
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
import org.opencv.objdetect.Objdetect;

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
    private static final Scalar    EYES_RECT_COLOR     = new Scalar(0, 0, 255, 255);
    private static final Scalar    MOUTH_RECT_COLOR    = new Scalar(255, 0, 180, 255);
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
    
    private Mat                    mResult;
    private Mat					   templateR;
    private Mat					   templateL;
    private Mat					   templateM;
    
    private File                   mCascadeFile;
    
    
    // Native detector and java detector
    private DetectionBasedTracker  mNativeDetector;
    
    // Cascade classifier files
    private CascadeClassifier      mFaceDetector;
    private CascadeClassifier	   mCascadeER;
    private CascadeClassifier	   mCascadeEL;
    private CascadeClassifier	   mCascadeM;
    
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
    
    private static final int TM_SQDIFF 			= 0;
    private static final int TM_SQDIFF_NORMED 	= 1;
    private static final int TM_CCOEFF			= 2;
    private static final int TM_CCOEFF_NORMED 	= 3;
    private static final int TM_CCORR 			= 4;
    private static final int TM_CCORR_NORMED 	= 5;
    
    public static int		 method				= 1;
    
    Point left_pupil = null;
	Point right_pupil = null;

    private long				    starttime = 0;
    private int						learn_frames = 0;
    private double					match_valuel, match_valuer;
    
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
                        
                        // create the native detector for opencv
                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    // Load left eye classifier
                    try {
                        // load cascade file from application resources
                        InputStream isEL = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEL = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFileEL = new File(cascadeDirEL, "haarcascade_lefteye_2splits.xml");
                        FileOutputStream osEL = new FileOutputStream(mCascadeFileEL);

                        byte[] bufferEL = new byte[4096];
                        int bytesReadEL;
                        while ((bytesReadEL = isEL.read(bufferEL)) != -1) {
                            osEL.write(bufferEL, 0, bytesReadEL);
                        }
                        isEL.close();
                        osEL.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mCascadeEL = new CascadeClassifier(mCascadeFileEL.getAbsolutePath());
                        if (mCascadeEL.empty()) {
                            Log.e(TAG, "Failed to load eye cascade classifier");
                            mCascadeEL = null;
                        } else
                        {
                            Log.i(TAG, "Loaded EL cascade classifier from " + mCascadeFileEL.getAbsolutePath());
                        }
                        
                        cascadeDirEL.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    // Load right eye classifier
                    try {
                        // load cascade file from application resources
                        InputStream isER = getResources().openRawResource(R.raw.haarcascade_righteye_2splits);
                        File cascadeDirER = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFileER = new File(cascadeDirER, "haarcascade_righteye_2splits.xml");
                        FileOutputStream osER = new FileOutputStream(mCascadeFileER);

                        byte[] bufferER = new byte[4096];
                        int bytesReadER;
                        while ((bytesReadER = isER.read(bufferER)) != -1) {
                            osER.write(bufferER, 0, bytesReadER);
                        }
                        isER.close();
                        osER.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mCascadeER = new CascadeClassifier(mCascadeFileER.getAbsolutePath());
                        if (mCascadeER.empty()) {
                            Log.e(TAG, "Failed to load eye cascade classifier");
                            mCascadeER = null;
                        } else
                        {
                            Log.i(TAG, "Loaded ER cascade classifier from " + mCascadeFileER.getAbsolutePath());
                        }
                        
                        cascadeDirER.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    // Load the mouth classifier                   
                    try {
                        // load cascade file from application resources
                        InputStream isM = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
                        File cascadeDirM = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFileM = new File(cascadeDirM, "haarcascade_mcs_mouth.xml");
                        FileOutputStream osM = new FileOutputStream(mCascadeFileM);

                        byte[] bufferM = new byte[4096];
                        int bytesReadM;
                        while ((bytesReadM = isM.read(bufferM)) != -1) {
                            osM.write(bufferM, 0, bytesReadM);
                        }
                        isM.close();
                        osM.close();
                        
                        // This part is for the java cascade classifier to search within region
                        mCascadeM = new CascadeClassifier(mCascadeFileM.getAbsolutePath());
                        if (mCascadeM.empty()) {
                            Log.e(TAG, "Failed to load eye cascade classifier");
                            mCascadeM = null;
                        } else
                        {
                            Log.i(TAG, "Loaded M cascade classifier from " + mCascadeFileM.getAbsolutePath());
                        }
                        
                        cascadeDirM.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    /*
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
    	mNativeDetector.detect(mGray, faces);
    	// take the most important face
        facesArray = faces.toArray();
        //Log.i("info", "faces to array length " + facesArray.length);
        if (facesArray.length > 0)
        {
        	// color the faces
        	Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);
        	
        	// draw the area for the eyes
        	Rect eyearea_left = new Rect(facesArray[0].x + facesArray[0].width/16 + (facesArray[0].width - 2 * facesArray[0].width/16)/2,(int)(facesArray[0].y + ( facesArray[0].height/4.5)),(facesArray[0].width - facesArray[0].width/8)/2,(int)(facesArray[0].height/3.5));
            Rect eyearea_right = new Rect(facesArray[0].x + facesArray[0].width/16,(int)(facesArray[0].y + (facesArray[0].height/4.5)),(facesArray[0].width - facesArray[0].width/8)/2,(int)( facesArray[0].height/3.5));
        	
            // draw rectangles for debugging
            Core.rectangle(mRgba, eyearea_left.tl(),eyearea_left.br() , EYES_RECT_COLOR, 2);
            Core.rectangle(mRgba, eyearea_right.tl(),eyearea_right.br(), EYES_RECT_COLOR, 2);
            
            // compute the mouth area
	        Rect moutharea = new Rect((facesArray[0].x + (facesArray[0].width/4)), (int)(facesArray[0].y + facesArray[0].height/1.5), (facesArray[0].width - facesArray[0].width/2),(int)(facesArray[0].height/3.0));
	        Core.rectangle(mRgba, moutharea.tl(), moutharea.br(), MOUTH_RECT_COLOR, 2);
            
	        // learn the template for the eyes
	        if(learn_frames<10)
	        {
	        	templateL = get_template(mCascadeEL,eyearea_left,24);
             	templateR = get_template(mCascadeER,eyearea_right,24);
             	// have to open mouth slightly for it to calibrate
             	templateM = get_templateMouth(mCascadeM, moutharea,24);
             	
             	learn_frames++;
            }
	        else
	        {
	        	// match_value is the cenrtainty that it is the pupil
	        	match_valuel = match_eye(eyearea_left,templateL, left_pupil, FdActivity.method); 
	        	match_valuer = match_eye(eyearea_right,templateR, right_pupil, FdActivity.method); 
	        	
	        	// TODO find out why match_valuer stays 1.0
	        	// Log.i("distance", "match left: " + match_valuel + "matchvalue right: " + match_valuer);
	        	
	        	// TODO find the distance between the 2 points
	        	
	        	
	        }
        }
        
        
       
        //RotatedRect trackeyes = null;
        //RotatedRect trackface = null;
/* 
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
		    */
        return mRgba;
    }
    
    private double  match_eye(Rect area, Mat mTemplate, Point pupil_coord, int type){
		  Point matchLoc; 
		  Mat mROI = mGray.submat(area);
	      int result_cols =  mGray.cols() - mTemplate.cols() + 1;
		  int result_rows = mGray.rows() - mTemplate.rows() + 1;
		  if(mTemplate.cols()==0 ||mTemplate.rows()==0){
			  return 0.0;
		  }
		  mResult = new Mat(result_cols,result_rows, CvType.CV_32FC1);
		  
		  switch (type){
			  case TM_SQDIFF:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF) ; 
				  break;
			  case TM_SQDIFF_NORMED:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF_NORMED) ; 
				  break;
			  case TM_CCOEFF:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF) ; 
				  break;
			  case TM_CCOEFF_NORMED:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF_NORMED) ; 
				  break;
			  case TM_CCORR:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR) ; 
				  break;
			  case TM_CCORR_NORMED:
				  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR_NORMED) ; 
				  break;
		  }
		  
		  Core.MinMaxLocResult mmres =  Core.minMaxLoc(mResult);
		  
		  if(type == TM_SQDIFF || type == TM_SQDIFF_NORMED)
		  	{ matchLoc = mmres.minLoc; }
		  else
		    { matchLoc = mmres.maxLoc; }
		  
		  Point  matchLoc_tx = new Point(matchLoc.x+area.x,matchLoc.y+area.y);
		  Point  matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x , matchLoc.y + mTemplate.rows()+area.y );
		  
		  // Save the coordinates in the pupil coordinate reserved
		  pupil_coord = new Point(matchLoc.x, matchLoc.y);
		 
		  Core.rectangle(mRgba, matchLoc_tx,matchLoc_ty, new Scalar(255, 255, 0, 255));
		 
		  if(type == TM_SQDIFF || type == TM_SQDIFF_NORMED)
		  	{ return mmres.maxVal; }
		  else
		    { return mmres.minVal; }

	    }
    
    // get template for the pupils
    private Mat  get_template(CascadeClassifier clasificator, Rect area,int size){
    	Mat template = new Mat();
    	Mat mROI = mGray.submat(area);
    	MatOfRect eyes = new MatOfRect();
    	Point iris = new Point();
    	Rect eye_template = new Rect();
    	clasificator.detectMultiScale(mROI, eyes, 1.15, 2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
    	 
    	Rect[] eyesArray = eyes.toArray();
    	for (int i = 0; i < eyesArray.length; i++){
	    	Rect e = eyesArray[i];
	    	e.x = area.x + e.x;
	    	e.y = area.y + e.y;
	    	Rect eye_only_rectangle = new Rect((int)e.tl().x,(int)( e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
	    	// reduce ROI
	    	mROI = mGray.submat(eye_only_rectangle);
	    	Mat vyrez = mRgba.submat(eye_only_rectangle);
	    	// find the darkness point
	    	Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);
	    	// draw point to visualise pupil
	    	Core.circle(vyrez, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
	    	iris.x = mmG.minLoc.x + eye_only_rectangle.x;
	    	iris.y = mmG.minLoc.y + eye_only_rectangle.y;
	    	eye_template = new Rect((int)iris.x-size/2,(int)iris.y-size/2 ,size,size);
	    	Core.rectangle(mRgba,eye_template.tl(),eye_template.br(),new Scalar(255, 0, 0, 255), 2);
	    	// copy area to template
	    	template = (mGray.submat(eye_template)).clone();
	    	return template;
	    	}
    	return template;
    	}
    
    // get template for the pupils
    private Mat  get_templateMouth(CascadeClassifier clasificator, Rect area,int size){
    	Mat template = new Mat();
    	Mat mROI = mGray.submat(area);
    	MatOfRect mouths = new MatOfRect();
    	Point lips = new Point();
    	Rect mouth_template = new Rect();
    	clasificator.detectMultiScale(mROI, mouths, 1.15, 2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
    	 
    	Rect[] mouthArray = mouths.toArray();
    	for (int i = 0; i < mouthArray.length; i++){
	    	Rect e = mouthArray[i];
	    	e.x = area.x + e.x;
	    	e.y = area.y + e.y;
	    	Rect mouth_only_rectangle = new Rect((int)e.tl().x,(int)( e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
	    	// reduce ROI
	    	mROI = mGray.submat(mouth_only_rectangle);
	    	Mat vyrez = mRgba.submat(mouth_only_rectangle);
	    	// find the darkness point
	    	Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);
	    	// draw point to visualise pupil
	    	Core.circle(vyrez, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
	    	lips.x = mmG.minLoc.x + mouth_only_rectangle.x;
	    	lips.y = mmG.minLoc.y + mouth_only_rectangle.y;
	    	mouth_template = new Rect((int)lips.x-size/2,(int)lips.y-size/2 ,size,size);
	    	Core.rectangle(mRgba,mouth_template.tl(),mouth_template.br(),new Scalar(255, 0, 0, 255), 2);
	    	// copy area to template
	    	template = (mGray.submat(mouth_template)).clone();
	    	return template;
	    	}
    	return template;
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
