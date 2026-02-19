/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.event.EventQueue;
import com.xahico.boot.net.URIA;
import com.xahico.boot.net.web.http.HttpServiceClient;
import com.xahico.boot.net.web.http.HttpServiceExchange;
import com.xahico.boot.pilot.Session;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionField;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GlassSession <T extends GlassServiceBase> extends Session {
	private static final ReflectionField masterField;
	
	
	
	static {
		try {
			final Reflection<GlassSession> reflection;
			
			reflection = Reflection.of(GlassSession.class);
			
			masterField = reflection.getField("master");
		} catch (final IllegalAccessException | NoSuchFieldException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private HttpServiceClient                         client = null;
	private String                                    defaultPath = "";
	private volatile boolean                          dead = false;
	private GlassDocumentLoader                       documentLoader = null;
	private final Map<Thread, EventQueue<GlassEvent>> eventQueues = new ConcurrentHashMap<>();
	private final String                              id = UUID.randomUUID().toString();
	private long                                      idleTimeout = 30000;
	private URIA                                      location = null;
	protected final T                                 master = null;
	private GlassNamespace                            namespace = null;
	private final long                                whenCreated = System.currentTimeMillis();
	private long                                      whenDied = -1;
	private long                                      whenUpdated = this.whenCreated;
	
	
	
	protected GlassSession (){
		super();
	}
	
	
	
	final void attachEventHandler (){
		final Thread thread;
		
		thread = Thread.currentThread();
		
		eventQueues.put(thread, new EventQueue<>());
		
		//this.call(() -> eventQueues.put(thread, new EventQueue<>()));
	}
	
	public final void clearDefaultPath (){
		this.defaultPath = "";
	}
	
	protected final GlassNamespace createTemporaryNamespace (){
		return new GlassNamespace(this.namespace);
	}
	
	final void detachEventHandler (){
		final Thread thread;
		
		thread = Thread.currentThread();
		
		eventQueues.remove(thread);
		
		//this.call(() -> eventQueues.remove(thread));
	}
	
	public final long firstContact (){
		return this.whenCreated;
	}
	
	public final String getDefaultPath (){
		return this.defaultPath;
	}
	
	protected final GlassDocumentLoader getDocumentLoader (){
		return this.documentLoader;
	}
	
	final GlassEvent getEvent (final long timeoutMillis) throws InterruptedException, TimeoutException {
		final Thread thread;
		final long   timeMarked;
		
		thread = Thread.currentThread();
		
		timeMarked = System.currentTimeMillis();
		
		for (;;) {
			final EventQueue<GlassEvent> eventQueue;
			//final Future<GlassEvent> future;
			final GlassEvent         result;
			final long               timeNow;
			
			timeNow = System.currentTimeMillis();
			
			if ((timeNow - timeMarked) >= timeoutMillis) {
				throw new TimeoutException();
			}
			
			eventQueue = eventQueues.get(thread);
			
			/*
			future = this.call(() -> {
				final EventQueue<GlassEvent> eventQueue;
				
				eventQueue = eventQueues.get(thread);
				
				return eventQueue.pop();
			});
			
			try {
				result = future.get();
			} catch (final ExecutionException ex) {
				throw new Error(ex);
			}
			*/
			result = eventQueue.pop();
			
			if (null != result) {
				return result;
			}
		}
	}
	
	public final long getIdleTimeout (){
		return this.idleTimeout;
	}
	
	public final URIA getLocation (){
		return this.location;
	}
	
	public final GlassNamespace getNamespace (){
		return this.namespace;
	}
	
	public final String getSessionID (){
		return this.id;
	}
	
	public final HttpServiceClient getSource (){
		return this.client;
	}
	
	public final String getSourceName (){
		if (null != this.client) 
			return this.client.getUsableAddress();
		else {
			return "unknown";
		}
	}
	
	final void initDocumentLoader (final GlassDocumentLoader loader){
		this.documentLoader = loader;
	}
	
	final void initMaster (final T master){
		masterField.set(this, master);
	}
	
	final void initNamespace (final GlassNamespace globalNamespace){
		this.namespace = new GlassNamespace(globalNamespace);
	}
	
	public abstract boolean isAuthenticated ();
	
	public final boolean isDead (){
		return this.dead;
	}
	
	public final boolean isDefaultPath (){
		return !this.defaultPath.isBlank();
	}
	
	public final boolean isIdleTimedOut (){
		if (this.idleTimeout == 0) 
			return false;
		else {
			return ((System.currentTimeMillis() - this.whenUpdated) >= this.idleTimeout);
		}
	}
	
	public final void kill (){
		if (! this.dead) try {
			this.dead = true;
		} finally {
			this.whenDied = System.currentTimeMillis();
		}
	}
	
	public final long lastContact (){
		return this.whenUpdated;
	}
	
	@Override
	protected void onCreate (){
		
	}
	
	@Override
	protected void onDestroy (){
		
	}
	
	protected void onLocationChanged (final URIA from, final URIA to){
		
	}
	
	protected void onLocationChanging (final URIA from, final URIA to){
		
	}
	
	protected boolean onUnhandledAction (final HttpServiceExchange exchange){
		return false;
	}
	
	public final void postEvent (final GlassEvent event){
		for (final var eventQueue : eventQueues.values()) {
			eventQueue.post(event);
		}
	}
	
	final void relocate (final URIA newLocation){
		final URIA oldLocation;
		
		oldLocation = this.location;
		
		this.location = newLocation;
		
		this.onLocationChanged(oldLocation, newLocation);
	}
	
	final void relocateFirst (final URIA newLocation){
		this.onLocationChanging(this.location, newLocation);
	}
	
	public final void setDefaultPath (final String path){
		this.defaultPath = path;
	}
	
	public final void setIdleTimeout (final long timeoutMillis){
		this.idleTimeout = timeoutMillis;
	}
	
	public final long timeOfDeath (){
		return this.whenDied;
	}
	
	final void update (final HttpServiceClient client){
		try {
			this.client = client;
		} finally {
			this.whenUpdated = System.currentTimeMillis();
		}
	}
	
	final void updateContact (){
		this.whenUpdated = System.currentTimeMillis();
	}
}