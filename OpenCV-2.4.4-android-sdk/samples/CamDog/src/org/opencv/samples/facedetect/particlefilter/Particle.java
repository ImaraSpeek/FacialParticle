package org.opencv.samples.facedetect.particlefilter;

import org.opencv.core.Point;

public class Particle implements Comparable<Particle> {

	private Point location;
	private double weight;
	private static double deviation = 10;
	
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
    
    public static double weightGauss(double distance)
    {
    	return (1/(deviation * Math.sqrt(2 * Math.PI))) * Math.exp(- Math.pow(distance, 2) / (2 * Math.pow(deviation, 2)));
    }

	@Override
	public int compareTo(Particle another) {
		// TODO Auto-generated method stub
		return (int)(this.weight - another.weight);
	}
    
	
}
