package org.opencv.samples.facedetect.particlefilter;

import org.opencv.core.Point;

public class Particle {

	private Point location;
	private double weight;
	
	public void setLocation(Point l) {
		location = l;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setWeight(double w) {
		weight = w;
	}
	
	public double getWeight() {
		return weight;
	}
	
	// Select a random value within range for selecting particles.
    public static double randomWithRange(double min, double max)
    {
    	double range = (max - min);
    	return (Math.random() * range) + min;
    }
	
}
