/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.pilot.Time;
import com.xahico.boot.util.Filter;
import com.xahico.boot.util.RepeatRunnable;
import java.util.Map;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXInstance <T extends GWXSession> {
	public final Map<String, Object> namespace;
	private final GWXResourceManager rcm;
	public final T                   session;
	public final Map<String, Object> store;
	public final int                 version;
	
	
	
	public GWXInstance (final GWXResourceManager rcm, final double version, final T session){
		super();
		
		this.rcm = rcm;
		this.version = (int) version;
		this.session = session;
		this.namespace = this.session.namespace.vars();
		this.store = this.session.store;
	}
	
	
	
	public GWXHook add_hook (final String hookId, final Runnable callback){
		return this.session.registerHook(GWXHookType.parseString(hookId), callback);
	}
	
	public void clear_auth (){
		this.session.detachUser();
	}
	
	public GWXConnectionAdapter create_connection (final String hostname, final Map<String, Object> configs){
		return GWXConnectionAdapter.create(hostname, configs);
	}
	
	public <T extends GWXObject> Filter<T> create_filter_my_objects (){
		final GWXUser user;
		
		user = this.session.user;
		
		if (null == user) 
			return (object) -> false;
		else {
			return (object) -> user.isOwnerOf(object);
		}
	}
	
	public void emit (final GWXObject eventTarget, final GWXObject eventSource, final String eventId, final Map<String, Object> eventData){
		final GWXEvent event;
		
		event = new GWXEvent();
		event.data = new JSOXVariant(eventData);
		event.id = eventId;
		event.from = Time.hostTimeMillisNow();
		event.origin = Thread.currentThread().getId();
		event.source = eventSource;
		event.target = eventTarget;
		event.version = this.version;
		
		rcm.emit(event);
	}
	
	public void execute_after (final Runnable runnable, final long millis){
		this.execute_async(new Runnable() {
			final long dispatched = System.currentTimeMillis();
			
			@Override
			public void run (){
				if ((System.currentTimeMillis() - dispatched) < millis) {
					execute_async(this);
				} else {
					runnable.run();
				}
			}
		});
	}
	
	public void execute_async (final RepeatRunnable runnable){
		this.execute_async(new Runnable() {
			@Override
			public void run (){
				final boolean repeat;
				
				repeat = runnable.run();
				
				if (repeat) {
					GWXInstance.this.execute_async(this);
				}
			}
		});
	}
	
	public void execute_async (final Runnable runnable){
		session.getExecutor().execute(runnable);
		//testExec.execute(runnable);
	}
	
	public void execute_interval (final RepeatRunnable runnable, final long intervalMillis){
		this.execute_async(new RepeatRunnable() {
			long dispatched = System.currentTimeMillis();
			
			@Override
			public boolean run (){
				final boolean repeat;
				
				if ((System.currentTimeMillis() - dispatched) < intervalMillis) {
					repeat = true;
				} else {
					repeat = runnable.run();
					
					if (repeat) {
						dispatched = System.currentTimeMillis();
					}
				}
				
				return repeat;
			}
		});
	}
	
	public GWXUser get_user (){
		return this.session.user;
	}
	
	public String get_token (){
		return this.session.getToken();
	}
	
	public void grant_access_rights (final GWXObject target, final String accessModes){
		this.session.grantAccess(target, GWXPermission.parseMultiString(accessModes));
	}
	
	public void grant_access_rights (final String target, final String accessModes){
		this.session.grantAccess(target, GWXPermission.parseMultiString(accessModes));
	}
	
	public String init_auth (final GWXUser user){
		this.session.attachUser(user);
		
		return this.session.getToken();
	}
	
	public boolean is_auth (){
		return this.session.isAuthenticated();
	}
	
	public boolean is_privileged (){
		return this.session.isPrivileged();
	}
	
	public GWXObject lookup (final String path){
		return rcm.lookup(this.session, path);
	}
	
	public boolean remove_hook (final String hookId, final Runnable callback){
		return this.session.unregisterHook(GWXHookType.parseString(hookId), callback);
	}
	
	public void revoke_access_rights (final GWXObject target, final String accessModes){
		this.session.revokeAccess(target, GWXPermission.parseMultiString(accessModes));
	}
	
	public void revoke_access_rights (final String target, final String accessModes){
		this.session.revokeAccess(target, GWXPermission.parseMultiString(accessModes));
	}
}