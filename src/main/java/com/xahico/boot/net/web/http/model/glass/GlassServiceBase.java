/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.net.InvalidTokenException;
import com.xahico.boot.net.web.http.HttpServiceClient;
import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.synchronicity.SynchronizationProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GlassServiceBase <T extends GlassSession> extends SynchronizationProvider {
	private ClassFactory<T>      sessionFactory = null;
	private final ThreadLocal<T> sessionLts = new ThreadLocal<>();
	private final List<T>        sessions = new ArrayList<>();
	
	
	
	protected GlassServiceBase (){
		super();
	}
	
	
	
	protected final T currentSession (){
		return sessionLts.get();
	}
	
	@Override
	public final int getCoreCount (){
		return this.sessions.size();
	}
	
	public final T getSessionByID (final String id) throws InterruptedException, InvalidTokenException {
		final Future<T> future;
		final T         result;
		
		future = this.call(() -> this.getSessionInternal(id));
		
		try {
			result = future.get();
		} catch (final ExecutionException ex) {
			if (ex.getCause() instanceof InvalidTokenException) 
				throw (InvalidTokenException) ex.getCause();
			else {
				throw new Error(ex);
			}
		}
		
		if (null == result) 
			throw new InternalError();
		else {
			return result;
		}
	}
	
	private T getSessionInternal (final String id) throws InvalidTokenException {
		for (final var session : sessions) {
			if (session.getSessionID().equalsIgnoreCase(id)) {
				return session;
			}
		}
		
		throw new InvalidTokenException();
	}
	
	public final List<T> getSessions (){
		return sessions;
	}
	
	private void initSession (final T session){
		sessionLts.set(session);
	}
	
	final void initSessionFactory (final ClassFactory<T> sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public boolean isIdle (){
		return this.sessions.isEmpty();
	}
	
	@Override
	protected final void onClock (){
		final Iterator<T> it;
		
		it = sessions.iterator();
		
		while (it.hasNext()) {
			final T session;
			
			session = it.next();
			
			if (session.isIdleTimedOut()) {
				session.kill();
			}
			
			if (session.isDead()) {
				session.relocateFirst(null);
				session.relocate(null);
				
				session.onDestroy();
				
				it.remove();
			}
		}
	}
	
	protected File rootDirectory (){
		return null;
	}
	
	final T spawnSession (final HttpServiceClient client, final String token, final GlassNamespace globalNamespace){
		final Future<T> future;
		final T         result;
		
		future = this.call(() -> {
			try {
				final T session;
				
				session = this.getSessionInternal(token);
				session.update(client);
				
				return session;
			} catch (final InvalidTokenException ex) {
				final T session;
				
				session = this.sessionFactory.newInstance();
				session.initNamespace(globalNamespace);
				session.setExecutor(this);
				session.setIdleTimeout(30000);
				session.initMaster(this);
				session.update(client);
				session.onCreate();
				
				sessions.add(session);
				
				return session;
			}
		});
		
		try {
			result = future.get();
			
			this.initSession(result);
		} catch (final ExecutionException | InterruptedException ex) {
			throw new Error(ex);
		}
		
		this.initSession(result);
		
		return result;
	}
}