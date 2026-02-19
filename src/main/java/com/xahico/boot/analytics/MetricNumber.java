/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * TBD.
 * 
 * @author KARBAROTTA
**/
public enum MetricNumber {
	BILLION	("G", "giga", 9),
	HUNDRED	("h", "hecto", 2),
	MILLION	("M", "mega", 6),
	ONE		("", "one"),
	QUADRILLION	("P", "peta", 15),
	QUINTILLION	("E", "exa", 18),
	SEPTILLION	("Y", "yotta", 24),
	SEXTILLION	("Z", "zetta", 21),
	TEN		("da", "deka", 1),
	THOUSAND	("k", "kilo", 3),
	TRILLION	("T", "tera", 12);
	
	
	
	public static MetricNumber largest (){
		return SEPTILLION;
	}
	
	public static MetricNumber smallest (){
		return TEN;
	}
	
	
	
	private final BigDecimal dval;
	private final BigInteger ival;
	private final String     prefix;
	private final String     symbol;
	private final int        zeros;
	
	
	
	MetricNumber (final String symbol, final String prefix){
		this.symbol = symbol;
		this.prefix = prefix;
		this.zeros = 0;
		this.dval = BigDecimal.ONE;
		this.ival = BigInteger.ONE;
	}
	
	MetricNumber (final String symbol, final String prefix, final int zeros){
		this.symbol = symbol;
		this.prefix = prefix;
		this.zeros = zeros;
		this.dval = new BigDecimal("1" + "0".repeat(zeros));
		this.ival = new BigInteger("1" + "0".repeat(zeros));
	}
	
	
	
	public BigDecimal dval (){
		return this.dval;
	}
	
	public boolean isGreaterThan (final MetricNumber n){
		return (this.zeros > n.zeros);
	}
	
	public boolean isLesserThan (final MetricNumber n){
		return (this.zeros < n.zeros);
	}
	
	public BigInteger ival (){
		return this.ival;
	}
	
	public String prefix (){
		return this.prefix;
	}
	
	public String symbol (){
		return this.symbol;
	}
	
	public int zeros (){
		return this.zeros;
	}
}