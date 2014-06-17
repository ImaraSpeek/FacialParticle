
NUI Chapter 7. Face Detection and Tracking

From the website:

  Killer Game Programming in Java
  http://fivedots.coe.psu.ac.th/~ad/jg

  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention my name, and include a link
to the website.

Thanks,
  Andrew

============================

This directory contains 2 Java files:
  * FaceTracker.java, FacePanel.java

There is 1 image file, used by the application:
  * crosshairs.png

There is 1 XML file, containing a Haar classifier for faces:
  * haarcascade_frontalface_alt.xml
         -- this was copied from the OpenCV directory: 
            <OpenCV>\data\haarcascades\

There are 2 subdirectories:
  * JavaCV Examples\    -- holds the JavaCV face detection example;
                           see the readme.txt in that directory
                           for details

  * savedFaces\        -- holds the images saved when the user presses "Save Face"
                          in the GUI of the face tracker

There are 2 batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JavaCV and OpenCV


----------------------------
Before Compilation/Execution:

You need to have a webcam connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.5-bin.zip
             and installed it in d:\javacv-bin

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run FaceTracker
    // you must have JavaCV and OpenCV installed

----------------------------
Last updated: 10th July 2013
