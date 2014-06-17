package org.opencv.samples.facedetect.particlefilter;

import org.opencv.core.Point;

public class Particle {

	private Point location;
	
	public void setLocation(Point l) {
		location = l;
	}
	
	public Point getLocation() {
		return location;
	}
	
	// Select a random value within range for selecting particles.
    public static double randomWithRange(double min, double max)
    {
    	double range = (max - min);
    	return (Math.random() * range) + min;
    }
	
}
