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
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.objdetect.Objdetect;
import org.opencv.samples.facedetect.particlefilter.Particle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.sql.Array;
import java.util.*;


@SuppressWarnings("unused")
public class FdActivity extends Activity implements CvCameraViewListener2 {
	
	
	public static final String PREFS_NAME = "MyTrainedFace";

    private static final String    TAG                 = "OCVSample::Activity";
    private static final String	   TagD				   = "OCVSample::Debugging";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final Scalar    EYES_RECT_COLOR     = new Scalar(0, 0, 255, 255);
    private static final Scalar    MOUTH_RECT_COLOR    = new Scalar(255, 0, 180, 255);
    private static final Scalar    LINE_COLOR    	   = new Scalar(255, 120, 0, 0);
    private static final Scalar    LEFT_PIXEL_COLOR    = new Scalar(39, 219, 195, 255);
    private static final Scalar    RIGHT_PIXEL_COLOR   = new Scalar(219, 39, 156, 255);
    private static final Scalar    MOUTH_PIXEL_COLOR   = new Scalar(219, 90, 39, 255);
    
    private static final Scalar    PUPIL_COLOR		   = new Scalar(255,255,255,255);
    
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem			   mItemTrain;

    private Mat                    mRgba;
    private Mat                    mGray;
    
    private Mat                    mResult;
    private Mat					   mResultL;
    private Mat					   mResultR;
    private Mat					   mResultM;
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
    
    private static final int RETURN_EYE_LEFT		= 0;
    private static final int RETURN_EYE_RIGHT		= 1;
    private static final int RETURN_MOUTH			= 2;
    
    public static int		 method				= 1;
    
    private long				    starttime = 0;
    private int						learn_frames = 0;
    
    // Particle filter variables
    private int nParticles = 1000;
    private Particle[] particlesR = new Particle[nParticles];
    private Particle[] particlesL = new Particle[nParticles];
    private Particle[] particlesM = new Particle[nParticles];
    
    // variable to save previous face location for motion model
    private Point prevFace = null;
    private double deviation = 10; 
    
    private boolean train = false;
    
    // a array of size 2 to save the mean and the sigma of the ratios for training
    private double[] ratios = new double[2];
    private int traincounter = 0;
    private int nSamples = 100;
    private double[] traindata = new double[nSamples];

    
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    load_cascade();
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
        
        Log.i("LOG", "size of frame is: " + mRgba.cols() + " x " + mRgba.rows());
        
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
            
	        

        	//********************************************************************************************************************************/
        	//                                                          INITIALIZATION                                                        /
        	//********************************************************************************************************************************/
	        
	        // learn the template for the features
	        if(learn_frames<5)
	        {
	        	templateL = get_template(mCascadeEL,eyearea_left,24);
             	templateR = get_template(mCascadeER,eyearea_right,24);
             	// have to open mouth slightly for it to calibrate
             	templateM = get_templateMouth(mCascadeM, moutharea,24);
             	
             	learn_frames++;
             	// TODO make sure that templates are correct 	
             	
             	if(learn_frames == 4) {
             		// Initialize particles for right eye
             		for (int i = 0; i<particlesR.length; i++) {
             			
             			// RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT
             			particlesR[i] = new Particle();
             			double xmin = (double)eyearea_right.x;
             			double xmax = (double)(eyearea_right.x + eyearea_right.width);
             			double ymin = (double)eyearea_right.y;
             			double ymax = (double)(eyearea_right.y + eyearea_right.height);
             			//Log.i("WD", xmin + "," + xmax + "," + ymin + "," + ymax);
             			Point pr = new Point(Particle.randomWithRange(xmin, xmax), 
             								Particle.randomWithRange(ymin, ymax));
             			Core.circle(mRgba, pr, 2, RIGHT_PIXEL_COLOR);
             			particlesR[i].setLocation(pr);
             			particlesR[i].setWeight(1.0/particlesR.length);
             			
             			// LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT
             			particlesL[i] = new Particle();
             			double lxmin = (double)eyearea_left.x;
             			double lxmax = (double)(eyearea_left.x + eyearea_left.width);
             			double lymin = (double)eyearea_left.y;
             			double lymax = (double)(eyearea_left.y + eyearea_left.height);
             			//Log.i("WD", xmin + "," + xmax + "," + ymin + "," + ymax);
             			Point pl = new Point(Particle.randomWithRange(lxmin, lxmax), 
             								Particle.randomWithRange(lymin, lymax));
             			Core.circle(mRgba, pl, 2, LEFT_PIXEL_COLOR);
             			particlesL[i].setLocation(pl);
             			particlesL[i].setWeight(1.0/particlesL.length);
             			
             			// 	MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH
             			particlesM[i] = new Particle();
             			double mxmin = (double)moutharea.x;
             			double mxmax = (double)(moutharea.x + moutharea.width);
             			double mymin = (double)moutharea.y;
             			double mymax = (double)(moutharea.y + moutharea.height);
             			//Log.i("WD", xmin + "," + xmax + "," + ymin + "," + ymax);
             			Point pm = new Point(Particle.randomWithRange(mxmin, mxmax), 
             								Particle.randomWithRange(mymin, mymax));
             			Core.circle(mRgba, pm, 2, MOUTH_PIXEL_COLOR);
             			particlesM[i].setLocation(pm);
             			particlesM[i].setWeight(1.0/particlesM.length);

             		}
             	}
            }
	        else
	        {
	        	//********************************************************************************************************************************/
	        	//                                                          RESAMPLING                                                            /
	        	//********************************************************************************************************************************/
	        	
	        	// Resample the particles according to their assigned weight
	        	
	        	// RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT
	        	Particle[] newParticlesR = new Particle[nParticles];
	        	int iParticleR = 0;
	        	double sumWeightsR = particlesR[0].getWeight();
	        	
	        	int pr = 0;
	            while (pr<nParticles) 
	            {
			        //Log.i("WD", ((1.0*i)/nParticles) + " " + sumWeights + " " + particles[iParticle].getWeight());
	            	if (((1.0*pr)/nParticles) <= sumWeightsR || (iParticleR+1) == nParticles) {
			        //if (((1.0*p)/nParticles) <= sumWeights) {
				        newParticlesR[pr] = new Particle();
				        newParticlesR[pr].setWeight(1.0/nParticles);
				        newParticlesR[pr].setLocation(particlesR[iParticleR].getLocation());
				        pr++;
				        //Log.i("WD", "Yes");
				    }
			        else 
			        {
				        iParticleR++;
				        sumWeightsR += particlesR[iParticleR].getWeight();
				        //Log.i("WD", "No");
			       }
	            }
	            particlesR = newParticlesR;
	        	newParticlesR = null;
	        	
	        	// LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT
	        	Particle[] newParticlesL = new Particle[nParticles];
	        	int iParticleL = 0;
	        	double sumWeightsL = particlesL[0].getWeight();
	        	
	        	int pl = 0;
	            while (pl<nParticles) 
	            {
			        //Log.i("WD", ((1.0*i)/nParticles) + " " + sumWeights + " " + particles[iParticle].getWeight());
	            	if (((1.0*pl)/nParticles) <= sumWeightsL || (iParticleL+1) == nParticles) {
			        //if (((1.0*p)/nParticles) <= sumWeights) {
				        newParticlesL[pl] = new Particle();
				        newParticlesL[pl].setWeight(1.0/nParticles);
				        newParticlesL[pl].setLocation(particlesL[iParticleL].getLocation());
				        pl++;
				        //Log.i("WD", "Yes");
				    }
			        else 
			        {
				        iParticleL++;
				        sumWeightsL += particlesL[iParticleL].getWeight();
				        //Log.i("WD", "No");
			       }
	            }
	            particlesL = newParticlesL;
	        	newParticlesL = null;
	        	
	        	
	        	// MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH
	        	Particle[] newParticlesM = new Particle[nParticles];
	        	int iParticleM = 0;
	        	double sumWeightsM = particlesM[0].getWeight();
	        	
	        	int pm = 0;
	            while (pm<nParticles) 
	            {
			        //Log.i("WD", ((1.0*i)/nParticles) + " " + sumWeights + " " + particles[iParticle].getWeight());
	            	if (((1.0*pm)/nParticles) <= sumWeightsM || (iParticleM+1) == nParticles) {
			        //if (((1.0*p)/nParticles) <= sumWeights) {
				        newParticlesM[pm] = new Particle();
				        newParticlesM[pm].setWeight(1.0/nParticles);
				        newParticlesM[pm].setLocation(particlesM[iParticleM].getLocation());
				        pm++;
				        //Log.i("WD", "Yes");
				    }
			        else 
			        {
				        iParticleM++;
				        sumWeightsM += particlesM[iParticleM].getWeight();
				        //Log.i("WD", "No");
			       }
	            }
	            particlesM = newParticlesM;
	        	newParticlesM = null;
	        	
	        	
	        	

	        	//********************************************************************************************************************************/
	        	//                                                        MOTION MODEL                                                            /
	        	//********************************************************************************************************************************/
	        	
	        	// Motion model 
	        	for (int i = 0; i < nParticles; i++)
	        	{
	        		Point transPointR, transPointL, transPointM;
		        	if (prevFace == null)
		        	{
		        		// set translated points for right, left and mouth
		        		transPointR = particlesR[i].getLocation();
		        		transPointL = particlesL[i].getLocation();
		        		transPointM = particlesM[i].getLocation();
		        	}
		        	else
		        	{
		        		transPointR = new Point();
		        		transPointL = new Point();
		        		transPointM = new Point();
		        		
		        		// Right
		        		transPointR.x = particlesR[i].getLocation().x + (facesArray[0].x + facesArray[0].width/2 - prevFace.x);
		        		transPointR.y = particlesR[i].getLocation().y + (facesArray[0].y + facesArray[0].height/2 - prevFace.y);
		        		// Left
		        		transPointL.x = particlesL[i].getLocation().x + (facesArray[0].x + facesArray[0].width/2 - prevFace.x);
		        		transPointL.y = particlesL[i].getLocation().y + (facesArray[0].y + facesArray[0].height/2 - prevFace.y);
		        		// Mouth
		        		transPointM.x = particlesM[i].getLocation().x + (facesArray[0].x + facesArray[0].width/2 - prevFace.x);
		        		transPointM.y = particlesM[i].getLocation().y + (facesArray[0].y + facesArray[0].height/2 - prevFace.y);
		        	}
		        	
		        	double gaussDisp = new Random().nextGaussian()*deviation;
		        	double angle = Math.random()*2*Math.PI;
		        	
		        	Point newPartPointR = new Point();
		        	Point newPartPointL = new Point();
		        	Point newPartPointM = new Point();
		        	// Right
		        	newPartPointR.x = transPointR.x + Math.sin(angle)*gaussDisp;
		        	newPartPointR.y = transPointR.y + Math.cos(angle)*gaussDisp;
		        	// Left
		        	newPartPointL.x = transPointL.x + Math.sin(angle)*gaussDisp;
		        	newPartPointL.y = transPointL.y + Math.cos(angle)*gaussDisp;
		        	// Mouth
		        	newPartPointM.x = transPointM.x + Math.sin(angle)*gaussDisp;
		        	newPartPointM.y = transPointM.y + Math.cos(angle)*gaussDisp;
		        	
		        	//Core.circle(mRgba, newPartPointR, 2, RIGHT_PIXEL_COLOR);
		        	//Core.circle(mRgba, newPartPointL, 2, LEFT_PIXEL_COLOR);
		        	//Core.circle(mRgba, newPartPointM, 2, MOUTH_PIXEL_COLOR);
		        	
		        	particlesR[i].setLocation(newPartPointR);
		        	particlesL[i].setLocation(newPartPointL);
		        	particlesM[i].setLocation(newPartPointM);
	        	}
	        	
	        	

	        	//********************************************************************************************************************************/
	        	//                                                          OBSERVATION                                                           /
	        	//********************************************************************************************************************************/
	        	
	        		// Measure all the features 
		        	// create points locally to use here
		        	Point left_pupil = new Point();
		        	Point right_pupil = new Point();
		        	Point lips = new Point();
		        	// Dummy point to calculate distance from eyes to middle lips
		        	Point middle_pupil = new Point();
		        	
		        	// match_value is the squared difference normalized across the area
		        	// use the TM_CCOEFF_NORMED to return high values for better matches
		        	double match_valuel = match_eye(eyearea_left,templateL, left_pupil, TM_CCOEFF_NORMED, RETURN_EYE_LEFT); 
		        	double match_valuer = match_eye(eyearea_right,templateR, right_pupil, TM_CCOEFF_NORMED, RETURN_EYE_RIGHT); 
		        	double match_valuem = match_eye(moutharea, templateM, lips, TM_CCOEFF_NORMED, RETURN_MOUTH);
		        
		        	Log.i("MV", "match_valuer: " + match_valuer + " match value l: " + match_valuel + " match mouth: " + match_valuem);
			        

		        //********************************************************************************************************************************/
		        //                                                          ASSIGN WEIGHTS                                                        /
		        //********************************************************************************************************************************/    
			       
		        	double sumweightR = 0.0;   
		        	double sumweightL = 0.0;   
		        	double sumweightM = 0.0;   
		        	// assign weights according to observation for right eye
			        for (int i = 0; i< nParticles; i++)
			        {
			        	// distance between particle and the eye
			        	double distanceR = Math.sqrt(Math.pow(particlesR[i].getLocation().x - right_pupil.x, 2) + Math.pow(particlesR[i].getLocation().y - right_pupil.y, 2));
			        	double distanceL = Math.sqrt(Math.pow(particlesL[i].getLocation().x - left_pupil.x, 2) + Math.pow(particlesL[i].getLocation().y - left_pupil.y, 2));
			        	double distanceM = Math.sqrt(Math.pow(particlesM[i].getLocation().x - lips.x, 2) + Math.pow(particlesM[i].getLocation().y - lips.y, 2));
				        
			        	// likelihood determinization
			        	double[] likelihoodR = new double[1];
			        	double[] likelihoodL = new double[1];
			        	double[] likelihoodM = new double[1];
			        	
			        	// make sure that all weights are assigned actual numbers, as Result matrix does not return numbers for
			        	// any values outside of the region of interest
			        
			        	
			        	// RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT
			        	double weightR = 0.0;
			        	if (mResultR != null)
				        {
			        		// check if the particle is within the region, if not set weight to zero
			        		if (particlesR[i].getLocation().x < eyearea_right.x || particlesR[i].getLocation().x > eyearea_right.x + eyearea_right.width || 
			        				particlesR[i].getLocation().y < eyearea_right.y || particlesR[i].getLocation().y > eyearea_right.y + eyearea_right.height)
			        		{
			        			// leave weight at 0.0;
			        		}
			        		else 
			        		{
					        	//double[] test = mResultR.get(eyearea_right.y/2, eyearea_right.x/2);
					        	//Log.i("DEBUG", "test double for value outside result: " + test[0] );
					        	likelihoodR = mResultR.get((int)(particlesR[i].getLocation().y - eyearea_right.y), (int)(particlesR[i].getLocation().x - eyearea_right.x)); 
					        	if (likelihoodR != null && likelihoodR[0] > 0.0)
					        	{
					        		weightR = likelihoodR[0];
					        	}
			        		}
				        }
				        
				        // LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT 
			        	if (mResultL != null)
				        {
				        	likelihoodL = mResultL.get((int)(particlesL[i].getLocation().y - eyearea_left.y), (int)(particlesL[i].getLocation().x - eyearea_left.x)); 
				        }
			        	double weightL = 0.0;
				        if (likelihoodL != null)
			        	{
				        	//Log.i("DEBUG", " particle[" + i + "], likelihood = " + likelihood[0]);
				        	if (likelihoodL[0] <= 0.0)
				        	{
				        		weightL = 0.0;
				        	}
				        	else 
				        	{
				        		weightL = likelihoodL[0];
				        	}
			        	}
				        
				        // MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH MOUTH
			        	if (mResultM != null)
				        {
				        	likelihoodM = mResultM.get((int)(particlesM[i].getLocation().y - moutharea.y), (int)(particlesM[i].getLocation().x - moutharea.x)); 
				        }
			        	double weightM = 0.0;
				        if (likelihoodM != null)
			        	{
				        	//Log.i("DEBUG", " particle[" + i + "], likelihood = " + likelihood[0]);
				        	if (likelihoodM[0] <= 0.0)
				        	{
				        		weightM = 0.0;
				        	}
				        	else 
				        	{
				        		weightM = likelihoodM[0];
				        	}
			        	}
				        
				        // add the Gaussian distrubutian of the most likely pupil
			        	weightR += Particle.weightGauss(distanceR) * 200;
			        	weightL += Particle.weightGauss(distanceL) * 200;
			        	weightM += Particle.weightGauss(distanceM) * 200;
			        	particlesR[i].setWeight(weightR);
			        	particlesL[i].setWeight(weightL);
			        	particlesM[i].setWeight(weightM);
			        	sumweightR += weightR;
			        	sumweightL += weightL;
			        	sumweightM += weightM;
			        }
			        // normalize the weight
			        for (int i = 0; i< nParticles; i++)
			        {
			        	particlesR[i].setWeight(particlesR[i].getWeight() / sumweightR);
			        	particlesL[i].setWeight(particlesL[i].getWeight() / sumweightL);
			        	particlesM[i].setWeight(particlesM[i].getWeight() / sumweightM);
			        	Core.circle(mRgba, particlesR[i].getLocation(), (int)(particlesR[i].getWeight()* 1000), RIGHT_PIXEL_COLOR);
			        	Core.circle(mRgba, particlesL[i].getLocation(), (int)(particlesL[i].getWeight()* 1000), LEFT_PIXEL_COLOR);
			        	Core.circle(mRgba, particlesM[i].getLocation(), (int)(particlesM[i].getWeight()* 1000), MOUTH_PIXEL_COLOR);
			        }
		        

		        
	        	//********************************************************************************************************************************/
	        	//                                                          ESTIMATE                                                              /
	        	//********************************************************************************************************************************/
		        
		        // Sort the particles to make an estimation from the top particles' average
		        Arrays.sort(particlesR);
		        Arrays.sort(particlesL);
		        Arrays.sort(particlesM);
		        //Log.i("DEBUG", "particle[0]: " + particlesR[0].getWeight() + " particle[999]: " + particlesR[999].getWeight());
		        
		        // Make a proper estimation based on the particles
		        Point estimateR = new Point();
		        Point estimateL = new Point();
		        Point estimateM = new Point();
		        double weightnormR = 0.0;
		        double weightnormL = 0.0;
		        double weightnormM = 0.0;
		        // set the average for the best 100 particles
		        for (int i = 0; i < nParticles/10; i ++)
		        {
		        	// Right
		        	estimateR.x += (particlesR[i].getLocation().x * particlesR[i].getWeight());
		        	estimateR.y += (particlesR[i].getLocation().y * particlesR[i].getWeight());
		        	weightnormR += particlesR[i].getWeight();
		        	// Left
		        	estimateL.x += (particlesL[i].getLocation().x * particlesL[i].getWeight());
		        	estimateL.y += (particlesL[i].getLocation().y * particlesL[i].getWeight());
		        	weightnormL += particlesL[i].getWeight();
		        	// Mouth
		        	estimateM.x += (particlesM[i].getLocation().x * particlesM[i].getWeight());
		        	estimateM.y += (particlesM[i].getLocation().y * particlesM[i].getWeight());
		        	weightnormM += particlesM[i].getWeight();
		        }
		        // average the position of the estimate
		        // Right
		        estimateR.x = estimateR.x / weightnormR;
		        estimateR.y = estimateR.y / weightnormR;
		        // Left
		        estimateL.x = estimateL.x / weightnormL;
		        estimateL.y = estimateL.y / weightnormL;
		        // Mouth
		        estimateM.x = estimateM.x / weightnormM;
		        estimateM.y = estimateM.y / weightnormM;
		        Core.circle(mRgba, estimateR, 5, PUPIL_COLOR, 4);
		        Core.circle(mRgba, estimateL, 5, PUPIL_COLOR, 4);
		        Core.circle(mRgba, estimateM, 5, PUPIL_COLOR, 4);
		        
		        
		        

	        	//********************************************************************************************************************************/
	        	//                                                          MEASUREMENTS                                                          /
	        	//********************************************************************************************************************************/
		        
	        	// Measure distances and ratios
		        // TODO set these distances based on the estimate values from particle filter
	        	
	        	//Log.i("distance", "match left: " + match_valuel + "match value right: " + match_valuer);
	        	
		        //Log.i("distance", "x: " + left_pupil.x + ", y: " + left_pupil.y);
		        //Log.i("distance", "left x: " + left_pupil.x + " right x: " + right_pupil.x);
		        Log.i("distance", "left y: " + left_pupil.y + " right y: " + right_pupil.y);
		        
		        // determine horizontal and vertical distances
		        double eyex = Math.abs(estimateL.x - estimateR.x);
		        double eyey = Math.abs(estimateL.y - estimateR.y);
		        // determine the x by subtracting half of the width from the farthest x coordinate
		        middle_pupil.x = estimateL.x - (eyex / 2);
		        middle_pupil.y = estimateL.y - (eyey / 2);
		        
		        Core.line(mRgba, estimateL, estimateR, LINE_COLOR, 2);
		        
		        // determine the horizontal and vertical distances from middle of the eyes to the mouth
		        double mouthx = Math.abs(Math.max(middle_pupil.x, estimateM.x) - Math.min(middle_pupil.x, estimateM.x));
		        double mouthy = Math.abs(Math.max(middle_pupil.y, estimateM.y) - Math.min(middle_pupil.y, estimateM.y));
	
		        Core.line(mRgba, middle_pupil, estimateM, LINE_COLOR, 2);
		        	
		        // pythagoras
		        double interoccular = Math.sqrt((eyex * eyex) + (eyey * eyey));
		        double moutheyes = Math.sqrt((mouthx * mouthx) + (mouthy * mouthy));	     
		        
		        // actual ratio
		        double ratio = interoccular / moutheyes;
		        	
		        Log.i("distance", "distance eyes: " + interoccular + " distance eyes to mouth: " + moutheyes + "ratio: " + ratio);
		        

	        	//********************************************************************************************************************************/
	        	//                                                             VERIFICATION                                                       /
	        	//********************************************************************************************************************************/
		        
		        // TODO compare the training dataset with the current observations if there is a dataset available
		        if (traindata[nSamples - 1] != 0.0)
		        {
		        	
		        }
		        else 
		        {
		        	// I dont think toast works in oncamera frame
		        	/*
            		Context context = getApplicationContext();
            		CharSequence text = "first train!";
            		int duration = Toast.LENGTH_LONG;

            		Toast toast = Toast.makeText(context, text, duration);
            		toast.show();
		        	train = true;
		        	*/
		        }
		        
		        
		        
		        
		        
		        
		        
		        
		        
		        
		        
		        
	        	//********************************************************************************************************************************/
	        	//                                                             TRAIN                                                              /
	        	//********************************************************************************************************************************/
		        
		        // TODO save the values in a database
		        if (train)
		        {
		        	Log.i("DEBUG", "im in the train loop");
		        	if (traincounter >= nSamples)
		        	{
		        		double average = 0.0;
		        		// enough training samples collected, determine the mean
		        		for (int r = 0; r < nSamples; r++)
		        		{
		        			average =+ traindata[r];
		        		}
		        		average = average / nSamples;
		        		
		        		// determine variance
		                double temp = 0;
		                for(int r = 0; r < nSamples; r++)
		                {
		                    temp += (average-traindata[r])*(average-traindata[r]);
		                }
		                double variance = temp / (nSamples - 1);
		        		
		                Log.i("ratio", "mean: " + average + "variance: " + variance);
		                // set training variables back to 0
		        		traincounter = 0;
		        		train = false;
		        	}
		        	else
		        	{
			        	traindata[traincounter] = ratio;
			        	Log.i("ratio", "traindata ratio[" + traincounter + "]: " + ratio);
			        	traincounter++;
		        	}
		        }
	        }
	        
	        // save the previous face center point
	       prevFace = new Point(facesArray[0].x + facesArray[0].width/2, facesArray[0].y + facesArray[0].height/2);       
        }
        
        return mRgba;
    }

    
    
    private double  match_eye(Rect area, Mat mTemplate, Point pupil_coord, int type, int feature){
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
		  // Save the return matrix with a normalized distribution 
		  switch (feature){
		  case RETURN_EYE_LEFT:
			  mResultL = new Mat(result_cols,result_rows, CvType.CV_32FC1);
			  mResultL = mResult;
			  break;
		  case RETURN_EYE_RIGHT:
			  mResultR = new Mat(result_cols,result_rows, CvType.CV_32FC1);
			  mResultR = mResult;
			  break;
		  case RETURN_MOUTH:
			  mResultM = new Mat(result_cols,result_rows, CvType.CV_32FC1);
			  mResultM = mResult;
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
        mItemTrain = menu.add("Train");
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
        else if (item == mItemTrain)
        {
        	train = true;
        }
        else
        {
        	train = false;
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    public void load_cascade()
    {

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

    	
    }
}
