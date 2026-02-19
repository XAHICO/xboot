/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 *
 * Copyright (C) 2022 Tuomas Kontiainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public class TimeFormatter implements Cloneable {
	public static final int DISPLAY_MAX = 8;
	
	
	
	private int           displayAccuracy = DISPLAY_MAX;
	private boolean       displayDays = true;
	private boolean       displayHours = true;
	private boolean       displayMilliseconds = true;
	private boolean       displayMinutes = true;
	private boolean       displayMonths = true;
	private boolean       displaySeconds = true;
	private boolean       displayWeeks = true;
	private boolean       displayYears = true;
	private TimeFormatter helper = null;
	private long          millis;
	
	
	
	public TimeFormatter (){
		super();
	}
	
	public TimeFormatter (final long seed){
		super();
		
		this.millis = seed;
	}
	
	
	
	public TimeFormatter addDays (final long add){
		return this.addHours(add * 24);
	}
	
	public TimeFormatter addHours (final long add){
		return this.addMinutes(add * 60);
	}
	
	public TimeFormatter addMilliseconds (final long add){
		this.millis += add;
		
		return TimeFormatter.this;
	}
	
	public TimeFormatter addMinutes (final long add){
		return this.addSeconds(add * 60);
	}
	
	public TimeFormatter addMonths (final long add){
		return this.addDays(add * 30);
	}
	
	public TimeFormatter addSeconds (final long add){
		return this.addMilliseconds(add * 1000);
	}
	
	public TimeFormatter addWeeks (final long add){
		return this.addDays(add * 7);
	}
	
	public TimeFormatter addYears (final long add){
		return this.addDays(add * 365);
	}
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public TimeFormatter clone (){
		try {
			return (TimeFormatter) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	public long days (){
		return (this.hours() / 24);
	}
	
	public double daysExact (){
		return (this.hoursExact() / 24.0d);
	}
	
	public int displayAccuracy (){
		return this.displayAccuracy;
	}
	
	public TimeFormatter displayAccuracy (final int newDisplayAccuracy){
		this.displayAccuracy = newDisplayAccuracy;
		
		return TimeFormatter.this;
	}
	
	public boolean displayDays (){
		return this.displayDays;
	}
	
	public TimeFormatter displayDays (final boolean display){
		this.displayDays = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayHours (){
		return this.displayHours;
	}
	
	public TimeFormatter displayHours (final boolean display){
		this.displayHours = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayMilliseconds (){
		return this.displayMilliseconds;
	}
	
	public TimeFormatter displayMilliseconds (final boolean display){
		this.displayMilliseconds = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayMinutes (){
		return this.displayMinutes;
	}
	
	public TimeFormatter displayMinutes (final boolean display){
		this.displayMinutes = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayMonths (){
		return this.displayMonths;
	}
	
	public TimeFormatter displayMonths (final boolean display){
		this.displayMonths = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displaySeconds (){
		return this.displaySeconds;
	}
	
	public TimeFormatter displaySeconds (final boolean display){
		this.displaySeconds = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayWeeks (){
		return this.displayWeeks;
	}
	
	public TimeFormatter displayWeeks (final boolean display){
		this.displayWeeks = display;
		
		return TimeFormatter.this;
	}
	
	public boolean displayYears (){
		return this.displayYears;
	}
	
	public TimeFormatter displayYears (final boolean display){
		this.displayYears = display;
		
		return TimeFormatter.this;
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof TimeFormatter)) 
			return false;
		else {
			final TimeFormatter other;
			
			other = (TimeFormatter) obj;
			
			return (this.millis == other.millis);
		}
	}
	
	@Override
	public int hashCode (){
		int hash = 3;
		hash = 67 * hash + (int) (this.millis ^ (this.millis >>> 32));
		return hash;
	}
	
	public long hours (){
		return (this.minutes() / 60);
	}
	
	public double hoursExact (){
		return (this.minutesExact() / 60.0d);
	}
	
	public long milliseconds (){
		return this.millis;
	}
	
	public long minutes (){
		return (this.seconds() / 60);
	}
	
	public double minutesExact (){
		return (this.secondsExact() / 60.0d);
	}
	
	public long months (){
		return (this.days() / 30);
	}
	
	public double monthsExact (){
		return (this.daysExact() / 30.0d);
	}
	
	public long seconds (){
		return (int)(this.millis / 1000L);
	}
	
	public double secondsExact (){
		return (this.millis / 1000.0d);
	}
	
	public long seed (){
		return this.millis;
	}
	
	public void seed (final long newSeed){
		this.millis = newSeed;
	}
	
	public TimeFormatter subtractDays (final long sub){
		return this.subtractHours(sub * 24);
	}
	
	public TimeFormatter subtractHours (final long sub){
		return this.subtractMinutes(sub * 60);
	}
	
	public TimeFormatter subtractMilliseconds (final long sub){
		this.millis -= sub;
		
		return TimeFormatter.this;
	}
	
	public TimeFormatter subtractMinutes (final long sub){
		return this.subtractSeconds(sub * 60);
	}
	
	public TimeFormatter subtractMonths (final long sub){
		return this.subtractDays(sub * 30);
	}
	
	public TimeFormatter subtractSeconds (final long sub){
		return this.subtractMilliseconds(sub * 1000);
	}
	
	public TimeFormatter subtractWeeks (final long sub){
		return this.subtractDays(sub * 7);
	}
	
	public TimeFormatter subtractYears (final long sub){
		return this.subtractDays(sub * 365);
	}
	
	public long weeks (){
		return (this.days() / 7);
	}
	
	public double weeksExact (){
		return (this.daysExact() / 7.0d);
	}
	
	public long years (){
		return (this.days() / 365);
	}
	
	public double yearsExact (){
		return (this.days() / 365.0d);
	}
	
	@Override
	public String toString (){
		int                 displays;
		final StringBuilder sb;
		
		displays = 0;
		
		if (null == this.helper)
			this.helper = this.clone();
		else {
			this.helper.seed(this.seed());
		}
		
		sb = new StringBuilder();
		
		if (displays <= this.displayAccuracy() && this.displayYears()) {
			if (helper.years() > 0) {
				displays++;

				if (helper.years() == 1) 
					sb.append("1 year");
				else {
					sb.append(helper.years())
					  .append(' ')
					  .append("years");
				}

				helper.subtractYears(helper.years());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayMonths()) {
			if (helper.months() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.months() == 1) 
					sb.append("1 month");
				else {
					sb.append(helper.months())
					  .append(' ')
					  .append("months");
				}

				helper.subtractMonths(helper.months());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayWeeks()) {
			if (helper.weeks() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.weeks() == 1) 
					sb.append("1 week");
				else {
					sb.append(helper.weeks())
					  .append(' ')
					  .append("weeks");
				}

				helper.subtractWeeks(helper.weeks());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayDays()) {
			if (helper.days() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.days() == 1) 
					sb.append("1 day");
				else {
					sb.append(helper.days())
					  .append(' ')
					  .append("days");
				}

				helper.subtractDays(helper.days());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayHours()) {
			if (helper.hours() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.hours() == 1) 
					sb.append("1 hour");
				else {
					sb.append(helper.hours())
					  .append(' ')
					  .append("hours");
				}

				helper.subtractHours(helper.hours());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayMinutes()) {
			if (helper.minutes() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.minutes() == 1) 
					sb.append("1 minute");
				else {
					sb.append(helper.minutes())
					  .append(' ')
					  .append("minutes");
				}
				
				helper.subtractMinutes(helper.minutes());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displaySeconds()) {
			if (helper.seconds() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.seconds() == 1) 
					sb.append("1 second");
				else {
					sb.append(helper.seconds())
					  .append(' ')
					  .append("seconds");
				}

				helper.subtractSeconds(helper.seconds());
			}
		}
		
		if (displays <= this.displayAccuracy() && this.displayMilliseconds()) {
			if (helper.milliseconds() > 0) {
				if (sb.length() > 0) 
					sb.append(", ");

				displays++;

				if (helper.milliseconds() == 1) 
					sb.append("1 millisecond");
				else {
					sb.append(helper.milliseconds())
					  .append(' ')
					  .append("milliseconds");
				}

				helper.subtractMilliseconds(helper.milliseconds());
			}
		}
		
		if (displays == 0) {
			sb.append("0");
		}
		
		return sb.toString();
	}
}