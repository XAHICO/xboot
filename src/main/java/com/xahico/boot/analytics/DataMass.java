/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class DataMass implements Cloneable, Comparable<DataMass> {
	private static final int KILOBYTE = 1024;
	
	
	
	public static DataMass valueOf (final String string){
		int                 cursor;
		final DataMass      mass;
		final long          num;
		final StringBuilder sb;
		final String        type;
		
		sb = new StringBuilder();
		
		for (cursor = 0; cursor < string.length(); cursor++) {
			final char c;
			
			c = string.charAt(cursor);
			
			if (Character.isDigit(c)) 
				sb.append(c);
			else {
				break;
			}
		}
		
		num = Long.parseLong(sb.toString());
		
		type = string.substring(cursor).toLowerCase();
		
		mass = new DataMass();
		
		if (type.equals("b")) 
			return mass.bytes(num);
		
		if (type.equals("kb")) 
			return mass.kilobytes(num);
		
		if (type.equals("mb")) 
			return mass.megabytes(num);
		
		if (type.equals("gb")) 
			return mass.gigabytes(num);
		
		if (type.equals("tb")) 
			return mass.terabytes(num);
		
		return mass.bytes(num);
	}
	
	
	
	private long bytes;
	private int  decimal = 0;
	
	
	
	public DataMass (){
		this(0L);
	}
	
	public DataMass (final long bytes){
		super();
		
		this.bytes = bytes;
	}
	
	
	
	public BigDataMass big (){
		return new BigDataMass(this.bytes);
	}
	
	public long bytes (){
		return this.bytes;
	}
	
	public DataMass bytes (final long newBytes){
		this.bytes = newBytes;
		
		return DataMass.this;
	}
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public DataMass clone (){
		try {
			return (DataMass) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public int compareTo (final DataMass other){
		return Long.compare(this.bytes, other.bytes);
	}
	
	public int decimal (){
		return this.decimal;
	}
	
	public DataMass decimal (final int newDecimal){
		this.decimal = newDecimal;
		
		return DataMass.this;
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof DataMass)) 
			return false;
		else {
			final DataMass other;
			
			other = (DataMass) obj;
			
			return (this.bytes == other.bytes);
		}
	}
	
	public long gigabytes (){
		return (this.megabytes() / 1000);
	}
	
	public DataMass gigabytes (final double bx){
		return this.megabytes(bx * 1000);
	}
	
	public double gigabytesExact (){
		return (this.megabytesExact() / 1000);
	}
	
	@Override
	public int hashCode (){
		int hash = 5;
		hash = 59 * hash + (int) (this.bytes ^ (this.bytes >>> 32));
		return hash;
	}
	
	public long kilobytes (){
		return (this.bytes() / KILOBYTE);
	}
	
	public DataMass kilobytes (final double bx){
		return this.bytes((long) bx * KILOBYTE);
	}
	
	public double kilobytesExact (){
		return (this.bytes() / KILOBYTE);
	}
	
	public long megabytes (){
		return (this.kilobytes() / 1000);
	}
	
	public DataMass megabytes (final double bx){
		return this.kilobytes(bx * 1000);
	}
	
	public double megabytesExact (){
		return (this.kilobytesExact() / 1000);
	}
	
	public long terabytes (){
		return (this.gigabytes() / 1000);
	}
	
	public DataMass terabytes (final double bx){
		return this.gigabytes(bx * 1000);
	}
	
	public double terabytesExact (){
		return (this.gigabytesExact() / 1000);
	}
	
	@Override
	public String toString (){
		final DecimalFormat format;
		
		format = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
		format.setGroupingUsed(true);
		
		if (this.decimal() > 0) 
			format.applyPattern("0." + "#".repeat(this.decimal()));
		else {
			format.applyPattern("0");
		}
		
		if (this.terabytes() > 0) {
			if (this.terabytes() == this.terabytesExact()) 
				return (this.terabytes() + "TB");
			else {
				return (format.format(this.terabytesExact()) + "TB");
			}
		}
		else if (this.gigabytes() > 0) {
			if (this.gigabytes() == this.gigabytesExact()) 
				return (this.gigabytes() + "GB");
			else {
				return (format.format(this.gigabytesExact()) + "GB");
			}
		}
		else if (this.megabytes() > 0) {
			if (this.megabytes() == this.megabytesExact()) 
				return (this.megabytes() + "MB");
			else {
				return (format.format(this.megabytesExact()) + "MB");
			}
		}
		else if (this.kilobytes() > 0) {
			if (this.kilobytes() == this.kilobytesExact()) 
				return (this.kilobytes() + "KB");
			else {
				return (format.format(this.kilobytesExact()) + "KB");
			}
		}
		else {
			return (this.bytes() + "B");
		}
	}
}