/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class TimeUtilities {
	private static final String  FORMAT_DATE = "dd/MM/yyyy";
	private static final String  FORMAT_DATE_TIME = "dd/MM/yyyy, HH:mm";
	private static final String  FORMAT_DATE_TIME_SPECIFIC = "dd/MM/yyyy, HH:mm:ss";
	private static final String  FORMAT_TIME = "HH:mm";
	private static final String  FORMAT_TIME_SPECIFIC = "HH:mm:ss";
	
	public static final TimeZone TIMEZONE_HOST = TimeZone.getTimeZone("UTC");
	public static final TimeZone TIMEZONE_LOCAL = TimeZone.getDefault();
	public static final ZoneId   ZONE_HOST = ZoneId.of("UTC");
	public static final ZoneId   ZONE_LOCAL = ZoneId.systemDefault();
	
	
	
	public static String formatHostDate (final long timeMillis){
		final Date       date;
		final DateFormat format;
		final String     string;
		
		date = new Date(timeMillis);
		
		format = new SimpleDateFormat(FORMAT_DATE);
		format.setTimeZone(TIMEZONE_HOST);
		
		string = format.format(date);
		
		return string;
	}
	
	public static String formatHostDateTime (final long timeMillis){
		final Date       date;
		final DateFormat format;
		final String     string;
		
		date = new Date(timeMillis);

		format = new SimpleDateFormat(FORMAT_DATE_TIME);
		format.setTimeZone(TIMEZONE_HOST);
		
		string = format.format(date);
		
		return string;
	}
	
	public static long formatHostDateTimeToLong (final String dateTimeString) throws ParseException {
		final Date       date;
		final DateFormat format;
		final long       timeMillis;
		
		format = new SimpleDateFormat(FORMAT_DATE_TIME);
		format.setTimeZone(TIMEZONE_HOST);
		
		date = format.parse(dateTimeString);
		
		timeMillis = date.getTime();
		
		return timeMillis;
	}
	
	public static long formatHostDateToLong (final String dateString) throws ParseException {
		final Date       date;
		final DateFormat format;
		final long       timeMillis;
		
		format = new SimpleDateFormat(FORMAT_DATE);
		format.setTimeZone(TIMEZONE_HOST);
		
		date = format.parse(dateString);
		
		timeMillis = date.getTime();
		
		return timeMillis;
	}
	
	public static String formatLocalDate (final long timeMillis){
		final Date       date;
		final DateFormat format;
		final String     string;
		
		date = new Date(timeMillis);
		
		format = new SimpleDateFormat(FORMAT_DATE);
		format.setTimeZone(TIMEZONE_LOCAL);
		
		string = format.format(date);
		
		return string;
	}
	
	public static String formatLocalDateTime (final long timeMillis){
		final Date       date;
		final DateFormat format;
		final String     string;
		
		date = new Date(timeMillis);

		format = new SimpleDateFormat(FORMAT_DATE_TIME);
		format.setTimeZone(TIMEZONE_LOCAL);
		
		string = format.format(date);
		
		return string;
	}
	
	public static long formatLocalDateTimeToLong (final String dateTimeString) throws ParseException {
		final Date       date;
		final DateFormat format;
		final long       timeMillis;
		
		format = new SimpleDateFormat(FORMAT_DATE_TIME);
		format.setTimeZone(TIMEZONE_LOCAL);
		
		date = format.parse(dateTimeString);
		
		timeMillis = date.getTime();
		
		return timeMillis;
	}
	
	public static long formatLocalDateToLong (final String dateString) throws ParseException {
		final Date       date;
		final DateFormat format;
		final long       timeMillis;
		
		format = new SimpleDateFormat(FORMAT_DATE);
		format.setTimeZone(TIMEZONE_LOCAL);
		
		date = format.parse(dateString);
		
		timeMillis = date.getTime();
		
		return timeMillis;
	}
	
	public static long hostTimeMillisNow (){
		return hostTimeNow().toInstant().toEpochMilli();
	}
	
	public static long hostTimeMillisToLocalTimeMillis (final long hostTimeMillis){
		final ZonedDateTime hostDateTime;
		
		hostDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(hostTimeMillis), ZONE_HOST);
		
		return hostTimeToLocalTimeMillis(hostDateTime);
	}
	
	public static ZonedDateTime hostTimeNow (){
		return ZonedDateTime.now(ZONE_HOST);
	}
	
	public static LocalDateTime hostTimeToLocalTime (final ZonedDateTime hostTime){
		return hostTime.withZoneSameInstant(ZONE_LOCAL).toLocalDateTime();
	}
	
	public static long hostTimeToLocalTimeMillis (final ZonedDateTime hostTime){
		return hostTimeToLocalTime(hostTime).atZone(ZONE_LOCAL).toInstant().toEpochMilli();
	}
	
	public static long localTimeMillisNow (){
		return localTimeNow().toInstant().toEpochMilli();
	}
	
	public static long localTimeMillisToHostTimeMillis (final long localTimeMillis){
		final LocalDateTime localDateTime;
		
		localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(localTimeMillis), ZONE_LOCAL);
		
		return localTimeToHostTimeMillis(localDateTime);
	}
	
	public static ZonedDateTime localTimeNow (){
		return ZonedDateTime.now(ZONE_LOCAL);
	}
	
	public static ZonedDateTime localTimeToHostTime (final LocalDateTime ldt){
		final ZonedDateTime hdtZoned;
		final ZonedDateTime ldtZoned;
		
		ldtZoned = ldt.atZone(ZONE_LOCAL);
		hdtZoned = ldtZoned.withZoneSameInstant(ZONE_HOST);
		
		return hdtZoned;
	}
	
	public static long localTimeToHostTimeMillis (final LocalDateTime localTime){
		return localTimeToHostTime(localTime).toInstant().toEpochMilli();
	}
	
	public static long transformDaysToMilliseconds (final int days){
		return (transformHoursToMilliseconds(days * 24));
	}
	
	public static long transformHoursToMilliseconds (final int hours){
		return (transformMinutesToMilliseconds(hours * 60));
	}
	
	public static int transformMillisecondsToDays (final long milliseconds){
		return (transformMillisecondsToHours(milliseconds) / 24);
	}
	
	public static int transformMillisecondsToHours (final long milliseconds){
		return (transformMillisecondsToMinutes(milliseconds) / 60);
	}
	
	public static int transformMillisecondsToMinutes (final long milliseconds){
		return (transformMillisecondsToSeconds(milliseconds) / 60);
	}
	
	public static int transformMillisecondsToSeconds (final long milliseconds){
		return (int)(milliseconds * 1000);
	}
	
	public static long transformMinutesToMilliseconds (final int minutes){
		return (transformSecondsToMilliseconds(minutes * 60));
	}
	
	public static long transformSecondsToMilliseconds (final int seconds){
		return (seconds * 1000);
	}
	
	
	
	private TimeUtilities (){
		throw new UnsupportedOperationException("Not supported");
	}
}