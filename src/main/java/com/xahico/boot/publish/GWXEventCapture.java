/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GWXEventCapture {
	public static GWXEventCapture createEventCapture (final GWXSession session, final GWXResourceManager rcm, final GWXEventListener listener){
		return new GWXEventCapture(session, rcm, listener.path(), listener.type());
	}
	
	
	
	private volatile boolean               draining = false;
	public final GWXPath                   listenerPath;
	public final GWXEventListener.Type     listenerType;
	private final Deque<GWXEvent>          queue = new ConcurrentLinkedDeque();
	private final GWXResourceManager       rcm;
	private final GWXSession               session;
	public final long                      since = System.currentTimeMillis();
	private final Map<GWXEvent, GWXObject> sources = new ConcurrentHashMap<>();
	private GWXEventSubscription           subscription = null;
	
	
	
	private GWXEventCapture (final GWXSession session, final GWXResourceManager rcm, final GWXPath listenerPath, final GWXEventListener.Type listenerType){
		super();
		
		this.session = session;
		this.rcm = rcm;
		this.listenerPath = listenerPath;
		this.listenerType = listenerType;
	}
	
	
	
	public void activate (){
		this.subscription = rcm.listen(this.session, this.listenerPath, (path, source, event) -> {
			this.sources.put(event, source);
			
			this.queue.add(event);
		});
	}
	
	public void deactivate (){
		if (null != this.subscription) {
			this.subscription.cancel();
			this.subscription = null;
		}
	}
	
	public void destroy (){
		this.deactivate();
		
		if (! this.draining) {
			this.queue.clear();
			
			this.sources.clear();
		}
	}
	
	public void drainTo (final GWXEventListener listener){
		this.draining = true;
		
		for (;;) {
			final GWXEvent  event;
			final GWXObject eventSource;
			
			event = this.queue.poll();
			
			if (null == event) {
				break;
			}
			
			eventSource = this.sources.remove(event);
			
			listener.call(eventSource, event);
		}
		
		this.sources.clear();
	}
	
	public void log (final GWXEvent event){
		this.queue.add(event);
	}
}