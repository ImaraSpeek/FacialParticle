package org.opencv.samples.facedetect;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import android.util.Log;


@SuppressWarnings("unused")
public class CamShifting
{
	private TrackedObj obj;
	int hist_bins;           //number of histogram bins
    int hist_range[]= {0,180};//histogram range
    int range;
    Mat bgr;
  

	public CamShifting()
	{
	obj=new TrackedObj();
	//hist_range[]= {0,180};
	hist_bins=30;
	//bgr=new Mat();
	
	//range=hist_range;
	}
	
	public void create_tracked_object(Mat mRgba, Rect[] region, CamShifting cs)
	{
		cs.obj.hsv	= new Mat(mRgba.size(),CvType.CV_8UC3);
		cs.obj.mask	= new Mat(mRgba.size(),CvType.CV_8UC1);
		cs.obj.hue	= new Mat(mRgba.size(),CvType.CV_8UC1);
		cs.obj.prob	= new Mat(mRgba.size(),CvType.CV_8UC1);
		
		
		update_hue_image(mRgba,region,cs);
		
		
		float max_val = 0.f;
		
		//create a histogram representation for the face
		//Rect roi = new Rect((int)region[0].tl().x,(int)(region[0].tl().y),region[0].width,region[0].height);//imran 
		Mat tempmask=new Mat(cs.obj.mask.size(),CvType.CV_8UC1);		
		
		//tempmask=cs.obj.mask.submat(roi);
		tempmask = cs.obj.mask.submat(region[0]);
		  
		  
		 // Log.i("CamShifting","Mask Size"+tempmask.size());
		  //cant use mask here as method will not take
		  MatOfFloat ranges = new MatOfFloat(0f, 256f);
		  MatOfInt histSize = new MatOfInt(25);
		  //List<Mat> histList = Arrays.asList( new Mat[] {new Mat(), new Mat(), new Mat()} );
		 // Imgproc.calcHist(cs.obj.huearray, new MatOfInt(0),cs.obj.mask, cs.obj.hist, histSize, ranges);
		 // List<Mat> images = Arrays.asList(cs.obj.hsv.submat(roi));
		  List<Mat> images = Arrays.asList(cs.obj.huearray.get(0).submat(region[0]));
		  Imgproc.calcHist(images, new MatOfInt(0),tempmask, cs.obj.hist, histSize, ranges);
		  
		  Core.normalize(cs.obj.hist, cs.obj.hist);
		  //Core.normalize(cs.obj.hist, cs.obj.hist, 0,255,Core.NORM_MINMAX);
		  cs.obj.prev_rect = region[0];
		  Log.i("Normalized Histogram","Normalized Histogram Starting"+cs.obj.hist);
		
		
	}
	
	public void update_hue_image(Mat mRgba, Rect[] region, CamShifting cs)
	{
		
		  int vmin = 65, vmax = 256, smin = 55;
		  
		  //converting RGBA to BGR (Blue Green Red (least significant)
		  bgr	= new Mat(mRgba.size(),CvType.CV_8UC3);
		  Imgproc.cvtColor(mRgba,bgr,Imgproc.COLOR_RGBA2BGR);

		  //convert to HSV color model (Hue Saturation Level)
		  Imgproc.cvtColor(bgr,cs.obj.hsv,Imgproc.COLOR_BGR2HSV);
		  
		  //mask out-of-range values
		  //Core.inRange(cs.obj.hsv, new Scalar(0, smin,Math.min(vmin,vmax)),new Scalar(180, 256,Math.max(vmin, vmax)), cs.obj.mask);
		  Core.inRange(cs.obj.hsv, new Scalar(0., 51., 89.),new Scalar(17., 140., 255.), cs.obj.mask);
		  //Core.inRange(cs.obj.hsv, new Scalar(H, S, V), new Scalar(H, S, V), dst);
		  
		  cs.obj.hsvarray.clear();
		  cs.obj.huearray.clear();
		  
		  // now have new arrays 
		  cs.obj.hsvarray.add(cs.obj.hsv);
		  cs.obj.huearray.add(cs.obj.hue);
		
		  // create new empty matrices
		  MatOfInt from_to = new MatOfInt(0,0);
		  
		  //extract the hue channel, split: src, dest channels
		  Core.mixChannels(cs.obj.hsvarray,cs.obj.huearray,from_to);
	}
	
	/*
	RotatedRect camshift_track_face(Mat mRgba, Rect[] region, CamShifting cs)
	{
		
		MatOfFloat ranges = new MatOfFloat(0f, 256f);
		//ConnectedComp components;
		update_hue_image(mRgba,region,cs);
		Imgproc.calcBackProject(cs.obj.huearray, new MatOfInt(0),cs.obj.hist,cs.obj.prob, ranges,255);
		Core.bitwise_and(cs.obj.prob,cs.obj.mask,cs.obj.prob,new Mat());
		
		cs.obj.curr_box=Video.CamShift(cs.obj.prob, cs.obj.prev_rect, new TermCriteria(TermCriteria.EPS,10,1));	
		Log.i("Tracked Rectangle","Tracked Rectangle"+cs.obj.prev_rect);
		Log.i("Tracked Rectangle","New Rectangle"+cs.obj.curr_box.boundingRect());
		cs.obj.prev_rect=cs.obj.curr_box.boundingRect();
		cs.obj.curr_box.angle=-cs.obj.curr_box.angle;
		return cs.obj.curr_box;
	}
	*/
	
	RotatedRect camshift_track_face(Mat mRgba, Rect[] region, CamShifting cs)
	{
		
		MatOfFloat ranges = new MatOfFloat(0f, 256f);
		//ConnectedComp components;
		update_hue_image(mRgba,region,cs);
		Imgproc.calcBackProject(cs.obj.huearray, new MatOfInt(0),cs.obj.hist,cs.obj.prob, ranges,255);
		Core.bitwise_and(cs.obj.prob,cs.obj.mask,cs.obj.prob,new Mat());
		
		cs.obj.curr_box = Video.CamShift(cs.obj.prob, cs.obj.prev_rect, new TermCriteria(TermCriteria.EPS,10,1));	
		Log.i("Tracked Rectangle","Tracked Rectangle"+cs.obj.prev_rect);
		Log.i("Tracked Rectangle","New Rectangle"+cs.obj.curr_box.boundingRect());
		
		// assign current rect to previous rectangle
		cs.obj.prev_rect = cs.obj.curr_box.boundingRect();
		cs.obj.curr_box.angle = -cs.obj.curr_box.angle;
		return cs.obj.curr_box;
	}
	
	
	/*
	CvBox2D camshift_track_face (IplImage* image, TrackedObj* obj) {
		CvConnectedComp components;
		 
		//create a new hue image
		update_hue_image(image, obj);
		 
		//create a probability image based on the face histogram
		cvCalcBackProject(&obj->hue, obj->prob, obj->hist);
		cvAnd(obj->prob, obj->mask, obj->prob, 0);
		 
		//use CamShift to find the center of the new face probability
		cvCamShift(obj->prob, obj->prev_rect,
		cvTermCriteria(CV_TERMCRIT_EPS | CV_TERMCRIT_ITER, 10, 1),
		&components, &obj->curr_box);
		 
		//update face location and angle
		obj->prev_rect = components.rect;
		obj->curr_box.angle = -obj->curr_box.angle;
		 
		return obj->curr_box;
		}
		*/
	
	
	
}
