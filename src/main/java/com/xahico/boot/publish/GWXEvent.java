/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.util.TimeUtilities;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXEvent {
	private static final AtomicLong COUNTER = new AtomicLong(0);
	
	
	
	public JSOXVariant data; // abstract data, likely snapshot
	public long        from; // timestamp
	public String      id; // event id
	public long        origin; // origin thread
	public final long  sequence = COUNTER.incrementAndGet(); // event num
	public GWXObject   source; // object that caused the event
	public GWXObject   target; // object where the event occurred
	public int         version; // source interface version
	
	
	
	public String path (){
		return (this.source.path() + "." + this.id);
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("[");
		sb.append(this.sequence);
		sb.append("#");
		sb.append(this.origin);
		sb.append("] v");
		sb.append(this.version);
		sb.append(" | ");
		sb.append(this.source.path());
		sb.append(".");
		sb.append(this.id);
		sb.append(" FROM ");
		sb.append(this.target.path());
		sb.append(" @ ");
		sb.append(TimeUtilities.formatHostDateTime(this.from));
		
		if (null != this.data) {
			sb.append("\n");
			sb.append(this.data.toJSONString());
		}
		
		return sb.toString();
	}
}