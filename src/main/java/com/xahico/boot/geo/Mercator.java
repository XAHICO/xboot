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
public abstract class Mercator {
	public abstract double yAxisProjection (final double input);
	public abstract double xAxisProjection (final double input);
}