package org.opencv.samples.facedetect;



public class FaceRecognizer {

    private static native long createFisherFaceRecognizer(String cascadeName, int minFaceSize);
}

