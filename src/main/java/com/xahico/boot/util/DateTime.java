/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class DateTime {
	private static final String FORMAT_DATE_TIME = "dd/MM/yyyy, HH:mm:ss";
	
	
	
	public static DateTime now (){
		return new DateTime(TimeZone.getDefault(), TimeUtilities.localTimeMillisNow());
	}
	
	
	
	private String   format = FORMAT_DATE_TIME;
	private long     time;
	private TimeZone timeZone;
	
	
	
	public DateTime (){
		super();
		
		this.time = 0;
	}
	
	public DateTime (final TimeZone timeZone, final long time){
		super();
		
		this.timeZone = timeZone;
		this.time = time;
	}
	
	
	
	public DateTime addDays (final int count){
		return this.set(this.toZonedDateTime().plusDays(count));
	}
	
	public DateTime addHours (final int count){
		return this.set(this.toZonedDateTime().plusHours(count));
	}
	
	public DateTime addMilliseconds (final long count){
		this.time += count;
		
		return DateTime.this;
	}
	
	public DateTime addMinutes (final int count){
		return this.set(this.toZonedDateTime().plusMinutes(count));
	}
	
	public DateTime addMonths (final int count){
		return this.set(this.toZonedDateTime().plusMonths(count));
	}
	
	public DateTime addSeconds (final int count){
		return this.set(this.toZonedDateTime().plusSeconds(count));
	}
	
	public DateTime addYears (final int count){
		return this.set(this.toZonedDateTime().plusYears(count));
	}
	
	public DateTime at (final TimeZone timeZone){
		final DateTime dt;
		
		dt = new DateTime();
		dt.setTime(this.toZonedDateTime(timeZone).toInstant().toEpochMilli());
		dt.setTimeZone(timeZone);
		
		return dt;
	}
	
	public int getDayOfMonth (){
		return this.toZonedDateTime().getDayOfMonth();
	}
	
	public int getDayOfYear (){
		return this.toZonedDateTime().getDayOfYear();
	}
	
	public int getHour (){
		return this.toZonedDateTime().getHour();
	}
	
	public int getMinute (){
		return this.toZonedDateTime().getMinute();
	}
	
	public Month getMonth (){
		return this.toZonedDateTime().getMonth();
	}
	
	public int getMonthIndex (){
		return this.toZonedDateTime().getMonthValue();
	}
	
	public int getSecond (){
		return this.toZonedDateTime().getSecond();
	}
	
	public long getTime (){
		return this.time;
	}
	
	public int getYear (){
		return this.toZonedDateTime().getYear();
	}
	
	public boolean isAfter (final DateTime other){
		return this.toZonedDateTime().isAfter(other.toZonedDateTime());
	}
	
	public boolean isBefore (final DateTime other){
		return this.toZonedDateTime().isBefore(other.toZonedDateTime());
	}
	
	public boolean isEqual (final DateTime other){
		return this.toZonedDateTime().isEqual(other.toZonedDateTime());
	}
	
	public DateTime set (final ZonedDateTime zoned){
		this.setTimeZone(TimeZone.getTimeZone(zoned.getZone()));
		this.setTime(zoned.toInstant().toEpochMilli());
		
		return DateTime.this;
	}
	
	public DateTime setFormat (final String format){
		this.format = format;
		
		return DateTime.this;
	}
	
	public DateTime setTime (final long timeMillis){
		this.time = timeMillis;
		
		return DateTime.this;
	}
	
	public DateTime setTimeZone (final TimeZone timeZone){
		this.timeZone = timeZone;
		
		return DateTime.this;
	}
	
	public DateTime subDays (final int count){
		return this.set(this.toZonedDateTime().minusDays(count));
	}
	
	public DateTime subHours (final int count){
		return this.set(this.toZonedDateTime().minusHours(count));
	}
	
	public DateTime subMilliseconds (final long count){
		this.time -= count;
		
		return DateTime.this;
	}
	
	public DateTime subMinutes (final int count){
		return this.set(this.toZonedDateTime().minusMinutes(count));
	}
	
	public DateTime subMonths (final int count){
		return this.set(this.toZonedDateTime().minusMonths(count));
	}
	
	public DateTime subSeconds (final int count){
		return this.set(this.toZonedDateTime().minusSeconds(count));
	}
	
	public DateTime subYears (final int count){
		return this.set(this.toZonedDateTime().minusYears(count));
	}
	
	@Override
	public String toString (){
		return this.toString(this.format);
	}
	
	public String toString (final String format){
		final Date       date;
		final DateFormat dateFormat;
		
		date = new Date(this.time);
		
		dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(this.timeZone);
		
		return dateFormat.format(date);
	}
	
	public ZonedDateTime toZonedDateTime (){
		return this.toZonedDateTime(this.timeZone);
	}
	
	public ZonedDateTime toZonedDateTime (final TimeZone timeZone){
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.time), timeZone.toZoneId());
	}
}