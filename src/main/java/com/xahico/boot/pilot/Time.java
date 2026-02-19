/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.pilot;

import com.xahico.boot.util.DateTime;
import com.xahico.boot.util.TimeUtilities;
import java.util.TimeZone;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Time {
	private static volatile TimeZone tzHost = TimeZone.getTimeZone("UTC");
	private static final TimeZone    tzLocal = TimeZone.getDefault();
	
	
	
	public static TimeZone getHostTimeZone (){
		return tzHost;
	}
	
	public static TimeZone getLocalTimeZone (){
		return tzLocal;
	}
	
	public static long hostTimeMillisNow (){
		return hostTimeNow().getTime();
	}
	
	public static DateTime hostTimeNow (){
		return DateTime.now().at(tzHost);
	}
	
	public static long localTimeMillisNow (){
		return localTimeNow().getTime();
	}
	
	public static DateTime localTimeNow (){
		return DateTime.now();
	}
	
	public static void setHostTimeZone (final TimeZone timeZone){
		tzHost = timeZone;
	}
	
	
	
	private Time (){
		throw new UnsupportedOperationException("Not supported.");
	}
}