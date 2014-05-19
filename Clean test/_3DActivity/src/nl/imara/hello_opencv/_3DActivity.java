package nl.imara.hello_opencv;
/*  
  * Working demo of face detection (remember to put the camera in horizontal)  
  * using OpenCV/CascadeClassifier.  
  */  
 import java.io.File;  
 import java.io.FileOutputStream;  
 import java.io.IOException;  
 import java.io.InputStream;  
 import org.opencv.android.BaseLoaderCallback;  
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;  
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;  
 import org.opencv.android.LoaderCallbackInterface;  
 import org.opencv.android.OpenCVLoader;   
 import org.opencv.core.Core;  
 import org.opencv.core.CvType;  
 import org.opencv.core.Mat;  
 import org.opencv.core.MatOfRect;  
 import org.opencv.core.Scalar;  
 import org.opencv.core.Size;  
 import org.opencv.imgproc.Imgproc;  
 import org.opencv.objdetect.CascadeClassifier;  
 import org.opencv.core.Point;  
 import org.opencv.core.Rect;  
 import android.app.Activity;  
 import android.content.Context;   
 import android.os.Bundle;  
 import android.util.Log;  
 import android.view.Menu;  
 import android.view.MenuItem;  
 import android.view.SurfaceView;  
 import android.view.WindowManager;  
 
 public class _3DActivity extends Activity implements CvCameraViewListener2 {  
   private static final int         VIEW_MODE_CAMERA  = 0;  
   private static final int         VIEW_MODE_GRAY   = 1;  
   private static final int         VIEW_MODE_FACES  = 2;  
   private MenuItem             mItemPreviewRGBA;  
   private MenuItem             mItemPreviewGrey;  
   private MenuItem             mItemPreviewFaces;  
   private int               mViewMode;  
   private Mat               mRgba;  
   private Mat               mGrey;  
   private int                              screen_w, screen_h;  
   private CascadeClassifier           face_cascade;  
   private Tutorial3View            mOpenCvCameraView;   
   private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {  
   
	   @Override  
     public void onManagerConnected(int status) {  
       switch (status) {  
         case LoaderCallbackInterface.SUCCESS:  
         {  
           // Load native library after(!) OpenCV initialization  
           mOpenCvCameraView.enableView();        
           load_cascade();  
         } break;  
         default:  
         {  
           super.onManagerConnected(status);  
         } break;  
       }  
     }  
   };  
   public _3DActivity() {  
   }  
   
   /** Called when the activity is first created. */  
   @Override  
   public void onCreate(Bundle savedInstanceState) {  
     super.onCreate(savedInstanceState);  
     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
     setContentView(R.layout.tutorial2_surface_view);  
     mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial2_activity_surface_view);  
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
   @Override  
   public void onResume()  
   {  
     super.onResume();  
     OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);  
   }  
   public void onDestroy() {  
     super.onDestroy();  
     if (mOpenCvCameraView != null)  
       mOpenCvCameraView.disableView();  
   }  
   public void onCameraViewStarted(int width, int height) {  
     screen_w=width;  
     screen_h=height;  
     mRgba = new Mat(screen_w, screen_h, CvType.CV_8UC4);  
     mGrey = new Mat(screen_w, screen_h, CvType.CV_8UC1);  
     Log.v("MyActivity","Height: "+height+" Width: "+width);  
   }  
   public void onCameraViewStopped() {  
     mRgba.release();  
     mGrey.release();  
   }  
   public Mat onCameraFrame(CvCameraViewFrame inputFrame) {  
        long startTime = System.nanoTime();  
        long endTime;  
        boolean show=true;  
        MatOfRect faces = new MatOfRect();  
        mRgba=inputFrame.rgba();  
        if (mViewMode==VIEW_MODE_CAMERA) {  
             endTime = System.nanoTime();  
          if (show==true) Log.v("MyActivity","Elapsed time: "+ (float)(endTime - startTime)/1000000+"ms");  
             return mRgba;  
        }  
        Imgproc.cvtColor( mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);   
        if (mViewMode==VIEW_MODE_GRAY){             
             endTime = System.nanoTime();  
          if (show==true) Log.v("MyActivity","Elapsed time: "+ (float)(endTime - startTime)/1000000+"ms");  
             return mGrey;  
        }  
        Mat low_res = new Mat(screen_w, screen_h, CvType.CV_8UC1);  
        // 1280 x 720  
        Log.v("MyActivity","width: "+screen_w+" height: "+screen_h);  
        Imgproc.resize(mGrey,low_res,new Size(),0.25,0.25,Imgproc.INTER_LINEAR);  
        Imgproc.equalizeHist( low_res, low_res );   
           face_cascade.detectMultiScale(low_res, faces);  
           if (show==true) Log.v("MyActivity","Detected "+faces.toArray().length+" faces");  
           for(Rect rect:faces.toArray())  
           {  
                Point center= new Point(4*rect.x + 4*rect.width*0.5, 4*rect.y + 4*rect.height*0.5 );  
                Core.ellipse( mRgba, new Point(center.x,center.y), new Size( rect.width*2, rect.height*2), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );  
           }  
           if (mViewMode==VIEW_MODE_FACES) {  
                endTime = System.nanoTime();  
                if (show==true) Log.v("MyActivity","Elapsed time: "+ (float)(endTime - startTime)/1000000+"ms");  
             return mRgba;  
                //return low_res;  
        }  
           return mRgba;  
    }  
   @Override  
   public boolean onCreateOptionsMenu(Menu menu) {  
     mItemPreviewRGBA = menu.add("RGBA");  
     mItemPreviewGrey = menu.add("Grey");  
     mItemPreviewFaces = menu.add("Faces");  
     return true;  
   }  
   public boolean onOptionsItemSelected(MenuItem item) {  
     if (item == mItemPreviewRGBA) {  
       mViewMode = VIEW_MODE_CAMERA;  
     } else if (item == mItemPreviewGrey) {  
       mViewMode = VIEW_MODE_GRAY;  
     } else if (item == mItemPreviewFaces) {  
       mViewMode = VIEW_MODE_FACES;  
     }  
     return true;  
   }    
   private void load_cascade(){  
        try {  
             // LOAD FROM ASSET  
             InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);  
             //InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);  
             File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);  
             File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");  
             FileOutputStream os = new FileOutputStream(mCascadeFile);  
             byte[] buffer = new byte[4096];  
             int bytesRead;  
             while ((bytesRead = is.read(buffer)) != -1) {  
                  os.write(buffer, 0, bytesRead);  
             }  
             is.close();  
             os.close();  
             face_cascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());  
             if(face_cascade.empty())  
             {  
                  Log.v("MyActivity","--(!)Error loading A\n");  
                  return;  
             }  
             else  
             {  
                  Log.v("MyActivity",  
                            "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());  
             }  
        } catch (IOException e) {  
             e.printStackTrace();  
             Log.v("MyActivity", "Failed to load cascade. Exception thrown: " + e);  
        }  
   }  
 }  