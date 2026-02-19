/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.geo;

/**
 * TBD.
 * 
 * @author hat
**/
public class SphericalMercator extends Mercator {
	private static final double RADIUS_MAJOR = 6378137.0;
	private static final double RADIUS_MINOR = 6356752.3142;
	
	
	
	@Override
	public double xAxisProjection (final double input){
		return (Math.toRadians(input) * RADIUS_MAJOR);
	}
	
	@Override
	public double yAxisProjection (final double input){
		return (Math.log(Math.tan(Math.PI / 4 + Math.toRadians(input) / 2)) * RADIUS_MAJOR);
	}
}