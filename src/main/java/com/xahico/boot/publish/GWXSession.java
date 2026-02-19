/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.distributed.identification.IDx;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.ObjectDelegate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXSession <T extends GWXUser> {
	private final Map<String, GWXResourceAccessStore> accessStores = new ConcurrentHashMap<>();
	private Callbacks                                 callbacks;
	private GWXCallTable                              callTable;
	private final AtomicBoolean                       destroyed = new AtomicBoolean(false);
	private Set<GWXEventCapture>                      eventCaptures = null;
	private Executor                                  executor = null;
	private final Map<GWXHookType, List<Runnable>>    hooks = new ConcurrentHashMap<>();
	private final Set<GWXAPIInterface.Handle>         interfaces = new CopyOnWriteArraySet<>();
	private final AtomicLong                          lastContact = new AtomicLong(System.currentTimeMillis());
	private final Set<GWXEventListener>               listeners = new CopyOnWriteArraySet<>();
	protected final GWXNodeTree                       locals = new GWXNodeTree();
	public GWXNamespace                               namespace = null;
	private boolean                                   retainEvents = false;
	private boolean                                   retainHandles = false;
	protected final Map<String, Object>               store = new ConcurrentHashMap<>();
	private int                                       timeout = 0;
	private volatile String                           token = null;
	protected volatile T                              user = null;
	
	
	
	protected GWXSession (){
		super();
	}
	
	
	
	public final void assignCallbacks (final Callbacks callbacks){
		this.callbacks = callbacks;
	}
	
	public final GWXAPIInterface.Handle assignInterface (final GWXAPIInterface iface){
		final GWXAPIInterface.Handle ifaceHandle;
		
		ifaceHandle = iface.require();
		
		if (this.retainHandles) {
			this.interfaces.add(ifaceHandle);
		}
		
		return ifaceHandle;
	}
	
	public final void attachEventListener (final GWXEventListener listener){
		final GWXEventCapture capture;
		
		this.listeners.add(listener);
		
		this.onEventListenerConnect(listener);
		
		this.callbacks.onEventListenerConnect(this, listener);
		
		capture = this.getEventCapture(listener.path(), listener.type());
		
		if (null != capture) try {
			// deactivates the capture (so it no longer listens)
			capture.deactivate();
			
			// drain to listener, it's a match - probably reconnecting listener
			capture.drainTo(listener);
		} finally {
			// destroy the capture (free used memory)
			capture.destroy();
		}
		
		if (this.listeners.size() == 1) {
			this.fireHookSet(GWXHookType.WAKE);
		}
	}
	
	public final void attachExecutor (final Executor executor){
		this.executor = executor;
	}
	
	final synchronized void attachUser (final T user){
		this.token = IDx.random().toString();
		
		this.user = user;
		
		this.onAuthenticate();
		
		this.callbacks.onAuthenticate(this);
		
		this.fireHookSet(GWXHookType.AUTH);
	}
	
	final boolean canObserve (final GWXObject target){
		return true;
	}
	
	public final boolean checkAccess (final GWXObject target, final GWXPermission... accessModes){
		if (this.user.isOwnerOf(target)) {
			return true;
		} else {
			return this.checkAccess(target.path(), accessModes);
		}
	}
	
	public final boolean checkAccess (final String path, final GWXPermission... accessModes){
		final GWXResourceAccessStore accessStore;
		
		if (! this.isAuthenticated()) 
			return false;
		
		if (this.isPrivileged()) 
			return true;
		
		if ((accessModes.length == 1) && (accessModes[0] == GWXPermission.OBSERVE) && (path.equals("*") || path.equals("/*"))) {
			return true;
		}
		
		accessStore = this.accessStores.get(path);
		
		if (null == accessStore) 
			return false;
		
		for (final var accessMode : accessModes) {
			if (! accessStore.contains(accessMode)) {
				return false;
			}
		}
		
		return true;
	}
	
	final void cleanEventCaptures (final boolean force){
		if (null != this.eventCaptures) {
			final Set<GWXEventCapture> remove;
			
			remove = new HashSet<>();
			
			for (final var capture : this.eventCaptures) {
				// check if capture is stale => discard
				if (force || ((System.currentTimeMillis() - capture.since) >= this.timeout)) try {
					capture.destroy();
				} finally {
					remove.add(capture);
				}
			}
			
			if (! remove.isEmpty()) {
				this.eventCaptures.removeAll(remove);
			}
		}
	}
	
	public final void create (){
		this.onCreate();
	}
	
	public final void destroy (){
		if (this.destroyed.get() == false) {
			this.destroyed.set(true);
			
			if (! this.interfaces.isEmpty()) {
				for (final var handle : this.interfaces) {
					handle.release();
				}
				
				this.interfaces.clear();
			}
			
			this.cleanEventCaptures(true);
			
			this.locals.destroy();
			
			this.onDestroy();
			
			this.callbacks.onDestroy(this);
			
			this.fireHookSet(GWXHookType.DESTROY);
			
			this.hooks.values().forEach(hookSet -> hookSet.clear());
			this.hooks.clear();
		}
	}
	
	final synchronized void detachUser (){
		if (null != this.user) {
			this.onAuthenticateReset();
			
			this.callbacks.onAuthenticateReset(this);
			
			this.token = null;
			
			this.user = null;
		}
	}
	
	public final void detachEventListener (final GWXEventListener listener){
		if (this.listeners.remove(listener)) {
			this.onEventListenerDisconnect(listener);
			
			this.callbacks.onEventListenerDisconnect(this, listener);
			
			if (null != this.eventCaptures) {
				final GWXEventCapture capture;
				
				// obtain the capture of this listener
				capture = listener.capture();
				
				// activate this capture for post-mortem event capture
				capture.activate();
				
				this.eventCaptures.add(capture);
			}
			
			if (this.listeners.isEmpty()) {
				this.fireHookSet(GWXHookType.IDLE);
			}
		}
	}
	
	private void fireHookSet (final GWXHookType hookType){
		final List<Runnable> hooks;
		
		hooks = this.hooks.get(hookType);
		
		if (null != hooks) {
			hooks.forEach((hook) -> {
				try {
					hook.run();
				} catch (final Throwable t) {
					Exceptions.ignore(t);
				}
			});
		}
	}
	
	final GWXEventCapture getEventCapture (final GWXPath listenerPath, final GWXEventListener.Type listenerType){
		if (null != this.eventCaptures) {
			for (final var capture : this.eventCaptures) {
				if (capture.listenerType != listenerType) 
					continue;

				if (capture.listenerPath.equals(listenerPath)) {
					this.eventCaptures.remove(capture);
					
					return capture;
				}
			}
		}
		
		return null;
	}
	
	final Executor getExecutor (){
		return this.executor;
	}
	
	public final GWXAPIInterface.Handle getInterface (final double version, final ObjectDelegate<GWXAPIInterface> delegate){
		GWXAPIInterface.Handle select = null;
		
		if (this.retainHandles) {
			for (final var handle : this.interfaces) {
				if (handle.version() != version) 
					continue;

				if ((null == select) || (handle.build() > select.build())) {
					select = handle;
				}
			}
		}
		
		if (select == null) {
			final GWXAPIInterface useInterface;
			
			useInterface = delegate.delegate();
			
			if (null != useInterface) {
				select = this.assignInterface(useInterface);
			}
		}
		
		return select;
	}
	
	public final long getLastContact (){
		return this.lastContact.get();
	}
	
	public final String getToken (){
		return this.token;
	}
	
	public final T getUser (){
		return this.user;
	}
	
	final boolean grantAccess (final GWXObject target, final Set<GWXPermission> accessModes){
		return this.grantAccess(target.path(), accessModes);
	}
	
	final boolean grantAccess (final String path, final Set<GWXPermission> accessModes){
		final GWXResourceAccessStore accessStore;
		
		accessStore = this.accessStores.computeIfAbsent(path, (__) -> new GWXResourceAccessStore());
		
		accessStore.addAll(accessModes);
		
		return true;
	}
	
	public final boolean hasInterface (final double version){
		for (final var handle : this.interfaces) {
			if (handle.version() == version) {
				return true;
			}
		}
		
		return false;
	}
	
	public final void initLocals (final GWXResourceManager rcm){
		this.executor.execute(() -> {
			rcm.injectLocals(this, this.locals);
		});
	}
	
	public final void initNamespace (final GWXNamespace globalNamespace){
		this.namespace = new GWXNamespace(globalNamespace);
	}
	
	final boolean isAllowedToMonitor (final GWXPath eventId){
		if (eventId.toString().equals("**")) {
			return true;
		} else {
			return true;
		}
	}
	
	public final boolean isAuthenticated (){
		return (null != this.token);
	}
	
	public final boolean isOwnerOf (final GWXObject object){
		if (! this.isAuthenticated()) {
			return false;
		} else {
			return this.user.isOwnerOf(object);
		}
	}
	
	public final boolean isPrivileged (){
		if (this.isAuthenticated()) {
			return this.user.isPrivileged();
		} else {
			return false;
		}
	}
	
	public final boolean isEventListenerPresent (){
		return !this.listeners.isEmpty();
	}
	
	public final long lastContactSecondsElapsed (){
		return ((System.currentTimeMillis() - this.lastContact.get()) / 1000);
	}
	
	protected abstract void onAuthenticate ();
	
	protected abstract void onAuthenticateReset ();
	
	protected abstract void onBroadcast (final GWXEvent event);
	
	protected abstract void onCreate ();
	
	protected abstract void onDestroy ();
	
	protected abstract void onEventListenerConnect (final GWXEventListener listener);
	
	protected abstract void onEventListenerDisconnect (final GWXEventListener listener);
	
	public final GWXHook registerHook (final GWXHookType hookType, final Runnable callback){
		final List<Runnable> hookSet;
		
		hookSet = this.hooks.computeIfAbsent(hookType, (__) -> new CopyOnWriteArrayList());
		
		hookSet.add(callback);
		
		return new GWXHook(this, hookType, callback);
	}
	
	final boolean revokeAccess (final GWXObject target, final Set<GWXPermission> accessModes){
		return this.revokeAccess(target.path(), accessModes);
	}
	
	final boolean revokeAccess (final String path, final Set<GWXPermission> accessModes){
		final GWXResourceAccessStore accessStore;
		
		accessStore = this.accessStores.get(path);
		
		if (null == accessStore) 
			return false;
		
		accessStore.removeAll(accessModes);
		
		return true;
	}
	
	public final void setRetainEvents (final boolean retainEvents){
		this.retainEvents = retainEvents;
		
		if (retainEvents) {
			this.eventCaptures = new CopyOnWriteArraySet<>();
		}
	}
	
	public final void setRetainHandles (final boolean retainHandles){
		this.retainHandles = retainHandles;
	}
	
	public final int timeout (){
		return this.timeout;
	}
	
	public final void timeout (final int timeout){
		this.timeout = timeout;
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" ");
		
		if (this.isAuthenticated()) {
			sb.append("[");
			sb.append(this.token);
			sb.append("]");
		} else {
			sb.append("[No-Auth]");
		}
		
		return sb.toString();
	}
	
	public final boolean unregisterHook (final GWXHookType hookType, final Runnable callback){
		final List<Runnable> hookSet;
		
		hookSet = this.hooks.get(hookType);
		
		if (null == hookSet) 
			return false;
		
		if (! hookSet.remove(callback)) 
			return false;
		
		if (hookSet.isEmpty()) {
			this.hooks.remove(hookType);
		}
		
		return true;
	}
	
	public final void updateLastContact (){
		this.lastContact.set(System.currentTimeMillis());
		
		this.cleanEventCaptures(false);
	}
	
	
	
	public static interface Callbacks {
		void onAuthenticate (final GWXSession instance);
		
		void onAuthenticateReset (final GWXSession instance);
		
		void onDestroy (final GWXSession instance);
		
		void onEventListenerConnect (final GWXSession instance, final GWXEventListener listener);
		
		void onEventListenerDisconnect (final GWXSession instance, final GWXEventListener listener);
	}
}