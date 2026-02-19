/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.inet;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class Internet4AddressRange implements Iterable<Inet4Address> {
	public static final Internet4AddressRange WHOLE_INTERNET = getRange("0.0.0.0", "255.255.255.255");
	
	
	
	public static Internet4AddressRange getRange (final InetAddress afrom, final InetAddress ato){
		final Inet4Address afromf;
		final Inet4Address atof;
		
		if (!(afrom instanceof Inet4Address)) {
			throw new InvalidRangeException(String.format(""));
		}
		
		if (!(ato instanceof Inet4Address)) {
			throw new InvalidRangeException(String.format(""));
		}
		
		afromf = (Inet4Address)(afrom);
		atof = (Inet4Address)(ato);
		
		return new Internet4AddressRange(afromf, atof);
	}
	
	public static Internet4AddressRange getRange (final String sfrom, final String sto){
		try {
			final InetAddress  afrom;
			final Inet4Address afromf;
			final InetAddress  ato;
			final Inet4Address atof;
			
			afrom = InetAddress.getByName(sfrom);
			
			if (!(afrom instanceof Inet4Address)) {
				throw new InvalidRangeException(String.format(""));
			}
			
			ato = InetAddress.getByName(sto);
			
			if (!(ato instanceof Inet4Address)) {
				throw new InvalidRangeException(String.format(""));
			}
			
			afromf = (Inet4Address)(afrom);
			atof = (Inet4Address)(ato);
			
			return new Internet4AddressRange(afromf, atof);
		} catch (final UnknownHostException ex) {
			throw new InvalidRangeException(String.format(""));
		}
	}
	
	public static Internet4AddressRange parseString (final String string) throws InvalidRangeException {
		final String addressFrom;
		final String addressTo;
		final int    delimiter;
		
		delimiter = string.indexOf("-");
		
		if (delimiter == -1) {
			throw new InvalidRangeException("invalid range '%s'".formatted(string));
		}
		
		addressFrom = string.substring(0, delimiter);
		
		addressTo = string.substring(delimiter + 1);
		
		return getRange(addressFrom.strip(), addressTo.strip());
	}
	
	
	
	private Inet4Address from;
	private Inet4Address to;
	
	
	
	public Internet4AddressRange (){
		super();
	}
	
	public Internet4AddressRange (final Inet4Address from, final Inet4Address to){
		super();
		
		this.from = from;
		this.to = to;
	}
	
	
	
	public boolean contains (final Internet4Address address){
		final long addressLong;
		final long addressMax;
		final long addressMin;
		
		addressLong = address.toLong();
		
		addressMax = InetUtilities.getInetLong(this.to);
		
		if (addressLong > addressMax) {
			return false;
		}
		
		addressMin = InetUtilities.getInetLong(this.from);
		
		if (addressLong < addressMin) {
			return false;
		}
		
		return true;
	}
	
	public boolean contains (final InternetAddress address){
		if (address instanceof Internet4Address) 
			return this.contains((Internet4Address)(address));
		else {
			return false;
		}
	}
	
	public Internet4AddressRange[] divide (final int n){
		long                          ifrom;
		long                          ito;
		final Internet4AddressRange[] ranges;
		final long                    total;
		
		if (this.isDivisibleBy(n)) try {
			return this.divideExact(n);
		} catch (final NotDivisibleException ex) {
			throw new InternalError(ex);
		}
		
		total = this.size();
		
		ranges = new Internet4AddressRange[n];
		
		ifrom = InetUtilities.getInetLong(this.from);
		
		for (var i = 0; i < n; i++) {
			final Internet4AddressRange range;
			
			if ((i + 1) < n) {
				ito = (ifrom + (total / n) - 1);
			} else {
				ito = InetUtilities.getInetLong(this.to);
			}
			
			range = new Internet4AddressRange();
			range.setFrom(InetUtilities.longToInet4(ifrom));
			range.setTo(InetUtilities.longToInet4(ito));
			
			ranges[i] = range;
			
			ifrom += (total / n);
		}
		
		return ranges;
	}
	
	public Internet4AddressRange[] divideExact (final int n) throws NotDivisibleException {
		long                          ifrom;
		long                          ito;
		final Internet4AddressRange[] ranges;
		final long                    total;
		
		if (! this.isDivisibleBy(n)) 
			throw new NotDivisibleException("%d not divisible by %d".formatted(this.size(), n));
		
		total = this.size();
		
		ranges = new Internet4AddressRange[n];
		
		ifrom = InetUtilities.getInetLong(this.from);
		
		for (var i = 0; i < n; i++) {
			final Internet4AddressRange range;
			
			ito = (ifrom + (total / n) - 1);
			
			range = new Internet4AddressRange();
			range.setFrom(InetUtilities.longToInet4(ifrom));
			range.setTo(InetUtilities.longToInet4(ito));
			
			ranges[i] = range;
			
			ifrom += (total / n);
		}
		
		return ranges;
	}
	
	public Internet4Address get (final long index) throws IndexOutOfBoundsException {
		final Internet4Address address;
		
		address = Internet4Address.parseLong(InetUtilities.getInetLong(Internet4AddressRange.this.from) + index);
		
		if (! this.contains(address)) 
			throw new IndexOutOfBoundsException("");
		
		return address;
	}
	
	public Internet4Address getFrom (){
		return Internet4Address.translateFromAddress(this.from);
	}
	
	public Internet4Address getTo (){
		return Internet4Address.translateFromAddress(this.to);
	}
	
	public boolean isDivisibleBy (final int n){
		final long size;
		
		size = this.size();
		
		if (n > size) {
			return false;
		} else {
			return ((((double)size) / ((double)n)) == (size / n));
		}
	}
	
	public boolean isValid (){
		final long fromlong;
		final long tolong;
		
		if (null == this.getFrom()) 
			return false;
		
		if (null == this.getTo()) 
			return false;
		
		fromlong = InetUtilities.getInetLong(this.from);
		tolong = InetUtilities.getInetLong(this.to);
		
		return (tolong >= fromlong);
	}
	
	@Override
	public Iterator<Inet4Address> iterator (){
		return new Iterator<>() {
			long count = Internet4AddressRange.this.size();
			long cursor = 0;
			
			
			@Override
			public boolean hasNext (){
				return (cursor < (count - 1));
			}
			
			@Override
			public Inet4Address next (){
				final Inet4Address address;
				
				address = InetUtilities.longToInet4(InetUtilities.getInetLong(Internet4AddressRange.this.from) + this.cursor);
				
				this.cursor++;
				
				return address;
			}
		};
	}
	
	public Internet4AddressRange reduceLeft (final long count){
		this.from = InetUtilities.longToInet4(InetUtilities.getInetLong(this.from) + count);
		
		return Internet4AddressRange.this;
	}
	
	public Internet4AddressRange reduceRight (final long count){
		this.to = InetUtilities.longToInet4(InetUtilities.getInetLong(this.to) - count);
		
		return Internet4AddressRange.this;
	}
	
	public Internet4AddressRange region (final long offset, final long size){
		return new Internet4AddressRange(
			InetUtilities.longToInet4(InetUtilities.getInetLong(this.from) + offset),
			InetUtilities.longToInet4(InetUtilities.getInetLong(this.from) + offset + size)
		);
	}
	
	public void setFrom (final Inet4Address address){
		this.from = address;
	}
	
	public void setTo (final Inet4Address address){
		this.to = address;
	}
	
	public long size (){
		return ((InetUtilities.getInetLong(this.to) - InetUtilities.getInetLong(this.from)) + 1);
	}
	
	@Override
	public String toString (){
		return String.format("%s - %s", this.from.getHostAddress(), this.to.getHostAddress());
	}
	
	public void validate () throws InvalidRangeException {
		if (! this.isValid()) {
			throw new InvalidRangeException(String.format(""));
		}
	}
}