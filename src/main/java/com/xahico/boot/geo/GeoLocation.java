/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.geo;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GeoLocation {
	public static GeoLocation get (final double latitude, final double longitude){
		return new GeoLocation(latitude, longitude);
	}
	
	
	
	private double latitude;
	private double longitude;
	
	
	
	public GeoLocation (){
		super();
		
		this.latitude = 0;
		this.longitude = 0;
	}
	
	public GeoLocation (final double latitude, final double longitude){
		super();
		
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj) 
			return false;
		
		if (!(obj instanceof GeoLocation)) 
			return false;
		
		final GeoLocation other;
		
		other = (GeoLocation)(obj);
		
		return (this.latitude == other.latitude) 
			  &&
			 (this.longitude == other.longitude);
	}
	
	public double getLatitude (){
		return latitude;
	}
	
	public double getLongitude (){
		return longitude;
	}
	
	@Override
	public int hashCode (){
		int hash = 7;
		hash = 89 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
		hash = 89 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
		return hash;
	}
}