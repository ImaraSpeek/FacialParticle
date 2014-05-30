#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

#include "objdetect.hpp"
#include "highgui.hpp"
#include "imgproc.hpp"
#include "core.hpp"
#include "imgproc_c.h"

#include <iostream>
#include <stdio.h>

using namespace std;
using namespace cv;

extern "C" {
/** Function Headers */
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tuturial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_detectAndDisplay(JNIEnv*, jobject, jlong addrGray, jlong addrRgba, CascadeClassifier face_cascade);


// Global variables
// Copy this file from opencv/data/haarscascades to target folder
//string face_cascade_name = "/home/imara/University/SmartPhoneSensing/IN4254/OpenCV-2.4.4-android-sdk/sdk/etc/haarcascades/haarcascade_frontalface_alt.xml";
//string face_cascade_name = "/home/imara/University/SmartPhoneSensing/IN4254/OpenCV-2.4.4-android-sdk/sdk/etc/lbpcascade/lbpcascade_frontalface.xml";
//string eyes_cascade_name = "/home/imara/University/SmartPhoneSensing/IN4254/OpenCV-2.4.4-android-sdk/sdk/etc/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
//CascadeClassifier face_cascade, eyes_cascade;
string window_name = "Capture - Face detection";
//int filenumber; // Number of file to be saved
//string filename;

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}


/**
* @function detectAndDisplay
*/
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_detectAndDisplay(JNIEnv*, jobject, jlong addrGray, jlong addrRgba, CascadeClassifier face_cascade)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    std::vector<Rect> faces;
    //Mat frame_gray;

    //cvtColor( mRgb, frame_gray, CV_BGR2GRAY );

    // cvtColor( frame, frame_gray, CV_BGR2GRAY );
    //equalizeHist( frame_gray, frame_gray );

    /*
    // Load the cascade
      if (!face_cascade.load(face_cascade_name) || !eyes_cascade.load(eyes_cascade_name) )
      {
          printf("--(!)Error loading\n");
          int ch = std::cin.get();
          //return (-1);
      };
	*/

    //-- Detect faces
    //face_cascade.detectMultiScale( mGr, faces, 1.1, 2, 0, Size(80, 80) );
    face_cascade.detectMultiScale( mGr, faces );

/*
    vector<cv::Rect>::const_iterator r;
    printf("--r is created\n");

    for( r = faces.begin(); r != faces.end(); r++ )
    {
    	printf("--r iteration\n");

    	//Mat faceROI = mGr( *r );
    	//std::vector<Rect> eyes;

    	//const KeyPoint& kp = r;
    	//-- Draw the face - point to center of face and draw
    	//Point center( r->x + r->width/2, r->y + r->height/2 );
    	//ellipse( mRgb, center, Size( r->width/2, r->height/2 ), 0, 0, 360, Scalar( 255, 0, 0 ), 2, 8, 0 );
    	circle(mRgb, Point(r->x, r->y), 10, Scalar(255,0,0,255), 5);
    	//circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }/*

    	Mat faceROI = frame_gray( *r );
        std::vector<Rect> eyes;

        //-- In each face, detect eyes
        eyes_cascade.detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
        if( eyes.size() == 2)
        {
            //-- Draw the face
            Point center( r->x + r->width/2, r->y + r->height/2 );
            ellipse( frame, center, Size( r->width/2, r->height/2 ), 0, 0, 360, Scalar( 255, 0, 0 ), 2, 8, 0 );

            for( vector<cv::Rect>::const_iterator s = eyes.begin() ; s != eyes.end() ; s++ )
            { //-- Draw the eyes
                Point eye_center( r->x + s->x + s->width/2, r->y + s->y + s->height/2 );
                int radius = cvRound( (s->width + s->height)*0.25 );
                circle( frame, eye_center, radius, Scalar( 255, 0, 255 ), 3, 8, 0 );
            }
        }

    }
*/
    //-- Show what you got
    //imshow( window_name, mRgb );
    //cvWaitKey(0);

}

// Perform face detection on the input image, using the given Haar Cascade.
// Returns a rectangle for the detected region in the given image.
//CvRect detectFaceInImage(IplImage *inputImg, CvHaarClassifierCascade* cascade)
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_detectFaceInImage(JNIEnv*, jobject, IplImage *inputImg, CvHaarClassifierCascade* cascade)
{
	// Smallest face size.
	CvSize minFeatureSize = cvSize(20, 20);
	// Only search for 1 face.
	int flags = CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH;
	// How detailed should the search be.
	float search_scale_factor = 1.1f;
	IplImage *detectImg;
	IplImage *greyImg = 0;
	CvMemStorage* storage;
	CvRect rc;
	double t;
	CvSeq* rects;
	CvSize size;
	int i, ms, nFaces;

	storage = cvCreateMemStorage(0);
	cvClearMemStorage( storage );


	// If the image is color, use a greyscale copy of the image.
	detectImg = (IplImage*)inputImg;
	if (inputImg->nChannels > 1) {
		size = cvSize(inputImg->width, inputImg->height);
		greyImg = cvCreateImage(size, IPL_DEPTH_8U, 1 );
		cvCvtColor( inputImg, greyImg, CV_BGR2GRAY );
		detectImg = greyImg;	// Use the greyscale image.
	}

	// Detect all the faces in the greyscale image.
	t = (double)cvGetTickCount();
	rects = cvHaarDetectObjects( detectImg, cascade, storage,
			search_scale_factor, 3, flags, minFeatureSize);
	t = (double)cvGetTickCount() - t;
	ms = cvRound( t / ((double)cvGetTickFrequency() * 1000.0) );
	nFaces = rects->total;
	printf("Face Detection took %d ms and found %d objects\n", ms, nFaces);

	// Get the first detected face (the biggest).
	if (nFaces > 0)
		rc = *(CvRect*)cvGetSeqElem( rects, 0 );
	else
		rc = cvRect(-1,-1,-1,-1);	// Couldn't find the face.

	if (greyImg)
		cvReleaseImage( &greyImg );
	cvReleaseMemStorage( &storage );
	//cvReleaseHaarClassifierCascade( &cascade );

	//return rc;	// Return the biggest face found, or (-1,-1,-1,-1).
}


}
