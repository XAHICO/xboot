/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author KARBAROTTA
**/
public class BigNumericMass implements Cloneable, Comparable<BigNumericMass> {
	private BigInteger   absoluteValue;
	private MetricNumber ignoreDecimal = null;
	
	
	
	public BigNumericMass (){
		this(BigInteger.ZERO);
	}
	
	public BigNumericMass (final long absoluteValue){
		this(BigInteger.valueOf(absoluteValue));
	}
	
	public BigNumericMass (final BigInteger absoluteValue){
		super();
		
		this.absoluteValue = absoluteValue;
	}
	
	
	
	public BigInteger absoluteValue (){
		return this.absoluteValue;
	}
	
	public BigNumericMass absoluteValue (final BigInteger newSeed){
		this.absoluteValue = newSeed;
		
		return BigNumericMass.this;
	}
	
	public BigInteger billions (){
		return (this.absoluteValue.divide(MetricNumber.BILLION.ival()));
	}
	
	public BigDecimal billionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.BILLION.dval());
	}
	
	public BigDecimal billionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.BILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.BILLION.dval());
	}
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public BigNumericMass clone (){
		try {
			return (BigNumericMass) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public int compareTo (final BigNumericMass other){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof BigNumericMass)) 
			return false;
		else {
			final BigNumericMass other;
			
			other = (BigNumericMass) obj;
			
			return (this.absoluteValue.equals(other.absoluteValue));
		}
	}
	
	@Override
	public int hashCode (){
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.absoluteValue);
		return hash;
	}
	
	public BigInteger hundreds (){
		return (this.absoluteValue.divide(MetricNumber.HUNDRED.ival()));
	}
	
	public BigDecimal hundredsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.HUNDRED.dval());
	}
	
	public BigDecimal hundredsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.HUNDRED.ival().divide(BigInteger.TEN)))).divide(MetricNumber.HUNDRED.dval());
	}
	
	public MetricNumber ignoreDecimal (){
		return this.ignoreDecimal;
	}
	
	public BigNumericMass ignoreDecimal (final MetricNumber newIgnoreDecimal){
		this.ignoreDecimal = newIgnoreDecimal;
		
		return BigNumericMass.this;
	}
	
	public BigInteger millions (){
		return (this.absoluteValue.divide(MetricNumber.MILLION.ival()));
	}
	
	public BigDecimal millionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.MILLION.dval());
	}
	
	public BigDecimal millionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.MILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.MILLION.dval());
	}
	
	public BigInteger ones (){
		return this.absoluteValue;
	}
	
	public BigInteger quadrillions (){
		return (this.absoluteValue.divide(MetricNumber.QUADRILLION.ival()));
	}
	
	public BigDecimal quadrillionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.QUADRILLION.dval());
	}
	
	public BigDecimal quadrillionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.QUADRILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.QUADRILLION.dval());
	}
	
	public BigInteger quintillions (){
		return (this.absoluteValue.divide(MetricNumber.QUINTILLION.ival()));
	}
	
	public BigDecimal quintillionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.QUINTILLION.dval());
	}
	
	public BigDecimal quintillionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.QUINTILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.QUINTILLION.dval());
	}
	
	public BigInteger septillions (){
		return (this.absoluteValue.divide(MetricNumber.SEPTILLION.ival()));
	}
	
	public BigDecimal septillionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.SEPTILLION.dval());
	}
	
	public BigDecimal septillionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.SEPTILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.SEPTILLION.dval());
	}
	
	public BigInteger sextillions (){
		return (this.absoluteValue.divide(MetricNumber.SEXTILLION.ival()));
	}
	
	public BigDecimal sextillionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.SEXTILLION.dval());
	}
	
	public BigDecimal sextillionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.SEXTILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.SEXTILLION.dval());
	}
	
	public BigInteger tens (){
		return this.absoluteValue.divide(MetricNumber.TEN.ival());
	}
	
	public BigDecimal tensExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.TEN.dval());
	}
	
	public BigDecimal tensRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.TEN.ival().divide(BigInteger.TEN)))).divide(MetricNumber.TEN.dval());
	}
	
	public BigInteger thousands (){
		return this.absoluteValue.divide(MetricNumber.THOUSAND.ival());
	}
	
	public BigDecimal thousandsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.THOUSAND.dval());
	}
	
	public BigDecimal thousandsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.THOUSAND.ival().divide(BigInteger.TEN)))).divide(MetricNumber.THOUSAND.dval());
	}
	
	@Override
	public String toString (){
		final DecimalFormat format;
		
		format = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
		format.applyPattern("0.#");
		format.setGroupingUsed(true);
		
		if (this.septillions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.SEPTILLION || this.septillionsExact().compareTo(new BigDecimal(this.septillions())) == 0)
				return (this.septillions() + MetricNumber.SEPTILLION.symbol());
			else {
				return (format.format(this.septillionsRounded()) + MetricNumber.SEPTILLION.symbol());
			}
		} else if (this.sextillions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.SEXTILLION || this.sextillionsExact().compareTo(new BigDecimal(this.sextillions())) == 0)
				return (this.sextillions() + MetricNumber.SEXTILLION.symbol());
			else {
				return (format.format(this.sextillionsRounded()) + MetricNumber.SEXTILLION.symbol());
			}
		} else if (this.quintillions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.QUINTILLION || this.quintillionsExact().compareTo(new BigDecimal(this.quintillions())) == 0)
				return (this.quintillions() + MetricNumber.QUINTILLION.symbol());
			else {
				return (format.format(this.quintillionsRounded()) + MetricNumber.QUINTILLION.symbol());
			}
		} else if (this.quadrillions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.QUADRILLION || this.quadrillionsExact().compareTo(new BigDecimal(this.quadrillions())) == 0)
				return (this.quadrillions() + MetricNumber.QUADRILLION.symbol());
			else {
				return (format.format(this.quadrillionsRounded()) + MetricNumber.QUADRILLION.symbol());
			}
		} else if (this.trillions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.TRILLION || this.trillionsExact().compareTo(new BigDecimal(this.trillions())) == 0)
				return (this.trillions() + MetricNumber.TRILLION.symbol());
			else {
				return (format.format(this.trillionsRounded()) + MetricNumber.TRILLION.symbol());
			}
		} else if (this.billions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.BILLION || this.billionsExact().compareTo(new BigDecimal(this.billions())) == 0)
				return (this.billions() + MetricNumber.BILLION.symbol());
			else {
				return (format.format(this.billionsRounded()) + MetricNumber.BILLION.symbol());
			}
		} else if (this.millions().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.MILLION || this.millionsExact().compareTo(new BigDecimal(this.millions())) == 0)
				return (this.millions() + MetricNumber.MILLION.symbol());
			else {
				return (format.format(this.millionsRounded()) + MetricNumber.MILLION.symbol());
			}
		} else if (this.thousands().intValue() > 0) {
			if (this.ignoreDecimal() == MetricNumber.THOUSAND || this.thousandsExact().compareTo(new BigDecimal(this.thousands())) == 0)
				return (this.thousands() + MetricNumber.THOUSAND.symbol());
			else {
				return (format.format(this.thousandsRounded()) + MetricNumber.THOUSAND.symbol());
			}
		} else {
			return Objects.toString(this.ones());
		}
	}
	
	public BigInteger trillions (){
		return (this.absoluteValue.divide(MetricNumber.TRILLION.ival()));
	}
	
	public BigDecimal trillionsExact (){
		return new BigDecimal(this.absoluteValue).divide(MetricNumber.TRILLION.dval());
	}
	
	public BigDecimal trillionsRounded (){
		return new BigDecimal(this.absoluteValue.subtract(this.absoluteValue.mod(MetricNumber.TRILLION.ival().divide(BigInteger.TEN)))).divide(MetricNumber.TRILLION.dval());
	}
}