/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author KARBAROTTA
**/
public class NumericMass implements Cloneable, Comparable<NumericMass> {
	private long         absoluteValue;
	private MetricNumber ignoreDecimal = null;
	
	
	
	public NumericMass (){
		this(0L);
	}
	
	public NumericMass (final long absoluteValue){
		super();
		
		this.absoluteValue = absoluteValue;
	}
	
	
	
	public long absoluteValue (){
		return this.absoluteValue;
	}
	
	public NumericMass absoluteValue (final long newSeed){
		this.absoluteValue = newSeed;
		
		return NumericMass.this;
	}
	
	public BigNumericMass big (){
		return new BigNumericMass(this.absoluteValue);
	}
	
	public long billions (){
		return (this.absoluteValue / MetricNumber.BILLION.ival().longValue());
	}
	
	public double billionsExact (){
		return (this.absoluteValue / MetricNumber.BILLION.dval().doubleValue());
	}
	
	public double billionsRounded (){
		return ((this.absoluteValue - (this.absoluteValue % MetricNumber.BILLION.dval().longValue() / 10)) / MetricNumber.BILLION.dval().doubleValue());
	}
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public NumericMass clone (){
		try {
			return (NumericMass) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public int compareTo (final NumericMass other){
		return Long.compare(this.absoluteValue, other.absoluteValue);
	}
	
	public void decrement (){
		this.absoluteValue++;
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof NumericMass)) 
			return false;
		else {
			final NumericMass other;
			
			other = (NumericMass) obj;
			
			return (this.absoluteValue == other.absoluteValue);
		}
	}
	
	@Override
	public int hashCode (){
		int hash = 7;
		hash = 11 * hash + (int) (this.absoluteValue ^ (this.absoluteValue >>> 32));
		return hash;
	}
	
	public long hundreds (){
		return (this.absoluteValue / MetricNumber.HUNDRED.ival().longValue());
	}
	
	public double hundredsExact (){
		return (this.absoluteValue / MetricNumber.HUNDRED.dval().doubleValue());
	}
	
	public double hundredsRounded (){
		return ((this.absoluteValue - (this.absoluteValue % MetricNumber.HUNDRED.dval().longValue() / 10)) / MetricNumber.HUNDRED.dval().doubleValue());
	}
	
	public MetricNumber ignoreDecimal (){
		return this.ignoreDecimal;
	}
	
	public NumericMass ignoreDecimal (final MetricNumber newIgnoreDecimal){
		this.ignoreDecimal = newIgnoreDecimal;
		
		return NumericMass.this;
	}
	
	public void increment (){
		this.absoluteValue++;
	}
	
	public long millions (){
		return (this.absoluteValue / MetricNumber.MILLION.ival().longValue());
	}
	
	public double millionsExact (){
		return (this.absoluteValue / MetricNumber.MILLION.dval().doubleValue());
	}
	
	public double millionsRounded (){
		return ((this.absoluteValue - (this.absoluteValue % MetricNumber.MILLION.dval().longValue() / 10)) / MetricNumber.MILLION.dval().doubleValue());
	}
	
	public long ones (){
		return this.absoluteValue;
	}
	
	public long tens (){
		return (this.absoluteValue / MetricNumber.TEN.ival().longValue());
	}
	
	public double tensExact (){
		return (this.absoluteValue / MetricNumber.TEN.dval().doubleValue());
	}
	
	public double tensRounded (){
		return ((this.absoluteValue - (this.absoluteValue % MetricNumber.TEN.dval().longValue() / 10)) / MetricNumber.TEN.dval().doubleValue());
	}
	
	public long thousands (){
		return (this.absoluteValue / MetricNumber.THOUSAND.ival().longValue());
	}
	
	public double thousandsExact (){
		return (this.absoluteValue / MetricNumber.THOUSAND.dval().doubleValue());
	}
	
	public double thousandsRounded (){
		return ((this.absoluteValue - (this.absoluteValue % MetricNumber.THOUSAND.dval().longValue() / 10)) / MetricNumber.THOUSAND.dval().doubleValue());
	}
	
	@Override
	public String toString (){
		final DecimalFormat format;
		
		format = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
		format.applyPattern("0.#");
		format.setGroupingUsed(true);
		
		if (this.billions() > 0) {
			if (this.ignoreDecimal() == MetricNumber.BILLION || this.billionsExact() == this.billions()) 
				return (this.billions() + MetricNumber.BILLION.symbol());
			else {
				return (format.format(this.billionsRounded()) + MetricNumber.BILLION.symbol());
			}
		} else if (this.millions() > 0) {
			if (this.ignoreDecimal() == MetricNumber.MILLION || this.millionsExact() == this.millions()) 
				return (this.millions() + MetricNumber.MILLION.symbol());
			else {
				return (format.format(this.millionsRounded()) + MetricNumber.MILLION.symbol());
			}
		} else if (this.thousands() > 0) {
			if (this.ignoreDecimal() == MetricNumber.THOUSAND || this.thousandsExact() == this.thousands()) 
				return (this.thousands() + MetricNumber.THOUSAND.symbol());
			else {
				return (format.format(this.thousandsRounded()) + MetricNumber.THOUSAND.symbol());
			}
		} else {
			return Objects.toString(this.ones());
		}
	}
}
//		return ((this.absoluteValue() - (this.absoluteValue() % 100000)) / 1000000.0d);
