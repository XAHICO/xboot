/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class BigDataMass implements Cloneable, Comparable<BigDataMass> {
	private static final BigInteger KILOBYTE = BigInteger.valueOf(1024);
	
	
	
	public static BigDataMass valueOf (final String string){
		int                 cursor;
		final BigDataMass   mass;
		final BigInteger    num;
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
		
		num = new BigInteger(sb.toString());
		
		type = string.substring(cursor).toLowerCase();
		
		mass = new BigDataMass();
		
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
	
	
	
	private BigInteger bytes;
	
	
	
	public BigDataMass (){
		this(BigInteger.ZERO);
	}
	
	public BigDataMass (final long bytes){
		this(BigInteger.valueOf(bytes));
	}
	
	public BigDataMass (final BigInteger bytes){
		super();
		
		this.bytes = bytes;
	}
	
	
	
	public BigInteger bytes (){
		return this.bytes;
	}
	
	public BigDataMass bytes (final BigInteger newBytes){
		this.bytes = newBytes;
		
		return BigDataMass.this;
	}
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public BigDataMass clone (){
		try {
			return (BigDataMass) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public int compareTo (final BigDataMass other){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof BigDataMass)) 
			return false;
		else {
			final BigDataMass other;
			
			other = (BigDataMass) obj;
			
			return Objects.equals(this.bytes, other.bytes);
		}
	}
	
	public BigInteger exabytes (){
		return this.petabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass exabytes (final BigInteger bx){
		return this.petabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal exabytesExact (){
		return this.petabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	public BigInteger gigabytes (){
		return this.megabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass gigabytes (final BigInteger bx){
		return this.megabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal gigabytesExact (){
		return this.megabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	@Override
	public int hashCode (){
		int hash = 5;
		hash = 83 * hash + Objects.hashCode(this.bytes);
		return hash;
	}
	
	public BigInteger kilobytes (){
		return this.bytes().divide(KILOBYTE);
	}
	
	public BigDataMass kilobytes (final BigInteger bx){
		return this.bytes(bx.multiply(KILOBYTE));
	}
	
	public BigDecimal kilobytesExact (){
		return new BigDecimal(this.bytes().divide(KILOBYTE));
	}
	
	public BigInteger megabytes (){
		return this.kilobytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass megabytes (final BigInteger bx){
		return this.kilobytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal megabytesExact (){
		return this.kilobytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	public BigInteger petabytes (){
		return this.terabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass petabytes (final BigInteger bx){
		return this.terabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal petabytesExact (){
		return this.terabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	public BigInteger terabytes (){
		return this.gigabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass terabytes (final BigInteger bx){
		return this.gigabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal terabytesExact (){
		return this.gigabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	@Override
	public String toString (){
		if (this.yottabytes().intValue() > 0) 
			return (this.yottabytes() + "YB");
		else if (this.zettabytes().intValue() > 0) 
			return (this.zettabytes() + "ZB");
		else if (this.exabytes().intValue() > 0) 
			return (this.exabytes() + "EB");
		else if (this.petabytes().intValue() > 0) 
			return (this.petabytes() + "PB");
		else if (this.terabytes().intValue() > 0) 
			return (this.terabytes() + "TB");
		else if (this.gigabytes().intValue() > 0) 
			return (this.gigabytes() + "GB");
		else if (this.megabytes().intValue() > 0) 
			return (this.megabytes().intValue() + "MB");
		else if (this.kilobytes().intValue() > 0) 
			return (this.kilobytes() + "KB");
		else {
			return (this.bytes() + "B");
		}
	}
	
	public BigInteger yottabytes (){
		return this.zettabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass yottabytes (final BigInteger bx){
		return this.zettabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal yottabytesExact (){
		return this.zettabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
	
	public BigInteger zettabytes (){
		return this.exabytes().divide(BigInteger.valueOf(1000));
	}
	
	public BigDataMass zettabytes (final BigInteger bx){
		return this.exabytes(bx.multiply(BigInteger.valueOf(1000)));
	}
	
	public BigDecimal zettabytesExact (){
		return this.exabytesExact().divide(BigDecimal.valueOf(1000.0d));
	}
}