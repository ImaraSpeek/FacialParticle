package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
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
import org.opencv.highgui.Highgui;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


@SuppressWarnings("unused")
public class AnnotateActivity extends Activity{

    private static final String    TAG                 = "OCVSample::Activity";
    private static final String	   TagD				   = "OCVSample::Debugging";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final Scalar    EYES_RECT_COLOR     = new Scalar(0, 0, 255, 255);
    private static final Scalar    MOUTH_RECT_COLOR    = new Scalar(255, 0, 180, 255);
    private static final Scalar    LINE_COLOR    	   = new Scalar(255, 120, 0, 0);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;

    private Mat                    mRgba;
    private Mat                    mGray;
    public Mat					   mImage1;
    public Mat					   mImage2;
    public Mat					   mImage3;
    public Mat					   mImage4;
    public Mat					   mImage5;
    public Mat					   mImage6;
    public Mat					   mImage7;
    public Mat					   mImage8;
    public Mat					   mImage9;
    public Mat					   mImage10;
    
    
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
    private CascadeClassifier	   mCascadeFace;
    
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
    
    private long				    starttime = 0;
    private int						learn_frames = 0;
    private double					match_valuel, match_valuer, match_valuem;
    
    // viewing the images
    private int						mViewmode = 0;
    private static final int REAL_TIME			= 0;
    private static final int IMAGE_1			= 1;
    private static final int IMAGE_2			= 2;
    private static final int IMAGE_3			= 3;
    private static final int IMAGE_4			= 4;
    private static final int IMAGE_5			= 5;
    private static final int IMAGE_6			= 6;
    private static final int IMAGE_7			= 7;
    private static final int IMAGE_8			= 8;
    private static final int IMAGE_9			= 9;
    private static final int IMAGE_10			= 10;
    private int 					mPictures 	= 0;
    private int click = 0;

    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");
                                     
                    // enable the opencv camera
                    //mOpenCvCameraView.enableView();
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
    
    
    public AnnotateActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        //System.loadLibrary("detection_based_tracker");
        
        load_cascade();     

        // Set the layout
        setContentView(R.layout.annotate_surface_view);
        
        // read image to both matrixes as it cant load to an empty matrix
		mRgba = loadImageFromFile("imara.png");
		mGray = loadImageFromFile("imara.png");
		// transfer image to gray MAt
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
		
		// Convert the loaded image to bitmap
		Bitmap resultBitmap = Bitmap.createBitmap(mRgba.cols(),  mRgba.rows(),Bitmap.Config.ARGB_8888);;
		Utils.matToBitmap(mRgba, resultBitmap);
		// and display
    	ImageView img = (ImageView) findViewById(R.id.Image);
    	img.setImageBitmap(resultBitmap);
    	
    	// Detect and colour the faces
    	//detecting(mRgba, mGray);

        // capture the current image
        Button Return = (Button)findViewById(R.id.Return);
        Return.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent returnIntent = new Intent(getApplicationContext(), TrainingActivity.class);
            	startActivity(returnIntent);
            }
        });
        
        MatOfRect 	faces = new MatOfRect();
        Rect[] 		facesArray = null;
        
        // Native Detector doesnt work for some reason
        //mCascadeFace.detectMultiScale(mGray, faces);
        // detect the faces using opencv
        
        
        mNativeDetector.detect(mGray, faces);
       	/*
        // take the most important face
        facesArray = faces.toArray();
        //Log.i("info", "faces to array length " + facesArray.length);
        // TODO sense if more than 1 face and give an error
        if (facesArray.length > 0)
        {
        	// color the faces
        	Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);
        }
        */
        
    }

       
    public void SaveImage (Mat mat) {
        Mat mIntermediateMat = new Mat();

        Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File root = Environment.getExternalStorageDirectory();
        String filename = "imara.png";
        File file = new File(root, filename);
        
        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, mIntermediateMat);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }
    
    public Mat loadImageFromFile(String fileName) {	    
    	Mat rgbLoadedImage = null;

        File root = Environment.getExternalStorageDirectory();
        File file = new File(root, fileName);

        // this should be in BGR format according to the
        // documentation.
        Mat image = Highgui.imread(file.getAbsolutePath());
        //Mat image = Highgui.imread("R.raw.gwen_stefani10_20_20_70_70");

        if (image.width() > 0) {
            rgbLoadedImage = new Mat(image.size(), image.type());
            Imgproc.cvtColor(image, rgbLoadedImage, Imgproc.COLOR_BGR2RGB);
            Log.d("photo", "Succes loading image");
            /*
            if (DEBUG)
                Log.d(TAG, "loadedImage: " + "chans: " + image.channels()
                        + ", (" + image.width() + ", " + image.height() + ")");
			*/
            image.release();
            image = null;
        }
        else
        {
        	Log.d("photo", "Failed loading image");
        }
        return rgbLoadedImage;
    }
    
    public void detecting(Mat mRgbaim, Mat mGrayim)
    {
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
        // TODO sense if more than 1 face and give an error
        if (facesArray.length > 0)
        {
        	// color the faces
        	Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);
        }
    	
    }
    
    /*
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        // Switch on viewmode based on button
        final int viewmode = mViewmode;
       
        
        /*
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
        // TODO sense if more than 1 face and give an error
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
            
	        // learn the template for the features
	        if(learn_frames<5)
	        {
	        	templateL = get_template(mCascadeEL,eyearea_left,24);
             	templateR = get_template(mCascadeER,eyearea_right,24);
             	// have to open mouth slightly for it to calibrate
             	templateM = get_templateMouth(mCascadeM, moutharea,24);
             	
             	learn_frames++;
             	// TODO make sure that templates are correct
            }
	        else
	        {
	        	// create points locally to use here
	        	Point left_pupil = new Point();
	        	Point right_pupil = new Point();
	        	Point lips = new Point();
	        	// Dummy point to calculate distance from eyes to middle lips
	        	Point middle_pupil = new Point();
	        	
	        	// match_value is the certainty that it is the pupil
	        	match_valuel = match_eye(eyearea_left,templateL, left_pupil, TrainingActivity.method); 
	        	match_valuer = match_eye(eyearea_right,templateR, right_pupil, TrainingActivity.method); 
	        	match_valuem = match_eye(moutharea, templateM, lips, TrainingActivity.method);
	        	
	        	//Log.i("distance", "match left: " + match_valuel + "match value right: " + match_valuer);

	        	//Log.i("distance", "x: " + left_pupil.x + ", y: " + left_pupil.y);
	        	//Log.i("distance", "left x: " + left_pupil.x + " right x: " + right_pupil.x);
	        	Log.i("distance", "left y: " + left_pupil.y + " right y: " + right_pupil.y);
	        	
	        	// determine horizontal and vertical distances
	        	double eyex = Math.abs(left_pupil.x - right_pupil.x);
	        	double eyey = Math.abs(left_pupil.y - right_pupil.y);
	        	// determine the x by subtracting half of the width from the farthest x coordinate
	        	middle_pupil.x = left_pupil.x - (eyex / 2);
	        	middle_pupil.y = left_pupil.y - (eyey / 2);
	        	
	        	Core.line(mRgba, left_pupil, right_pupil, LINE_COLOR, 2);
	        	
	        	// determine the horizontal and vertical distances from middle of the eyes to the mouth
	        	double mouthx = Math.abs(Math.max(middle_pupil.x, lips.x) - Math.min(middle_pupil.x, lips.x));
	        	double mouthy = Math.abs(Math.max(middle_pupil.y, lips.y) - Math.min(middle_pupil.y, lips.y));

	        	Core.line(mRgba, middle_pupil, lips, LINE_COLOR, 2);
	        	
	        	// pythagoras
	        	double interoccular = Math.sqrt((eyex * eyex) + (eyey * eyey));
	        	double moutheyes = Math.sqrt((mouthx * mouthx) + (mouthy * mouthy));	        	
	        	
	        	Log.i("distance", "distance eyes: " + interoccular + " distance eyes to mouth: " + moutheyes);
	        	
	        }
        }
        
        return mRgba;
    }
    */
    
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
		  
		  //Log.i("distance", pupil_coord.toString() + " matchLoc x: " + matchLoc.x + " matchloc y:" + matchLoc.y + "area x, y: " + area.x + " " + area.y );
		  
		  // Save the coordinates in the pupil coordinate reserved
		  // These coordinates have to be relative to the face area the mRgba area
		  pupil_coord.x = matchLoc.x + (mTemplate.cols() / 2) + area.x;
		  pupil_coord.y = matchLoc.y + (mTemplate.rows() / 2) + area.y;		  
		 
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
    
    public void load_cascade(){
    try {
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
        
        mCascadeFace = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        if (mCascadeFace.empty()) {
            Log.e(TAG, "Failed to load face cascade classifier");
            mCascadeFace = null;
        } else
        {
            Log.i(TAG, "Loaded Face cascade classifier from " + mCascadeFile.getAbsolutePath());
        }

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
    }
    

}
