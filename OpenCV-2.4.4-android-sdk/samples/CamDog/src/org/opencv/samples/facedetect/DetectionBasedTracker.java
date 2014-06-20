package org.opencv.samples.facedetect;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }
    
    /*
    public void detectregion(Mat imageGray, MatOfRect faces, MatOfRect objects) {
        nativeDetectInRegion(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr(), objects.getNativeObjAddr());
    }
    
    
    public void track(MatOfRect faces){
    	nativeTrack(mNativeObj, faces.getNativeObjAddr());
    }
    */

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long inputImage, long faces);
    private static native void nativeDetectInRegion(long thiz, long inputImage, long faces, long objects);
    private static native void nativeTrack(long thiz, long faces);
}
