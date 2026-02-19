/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.pilot.Session;
import com.xahico.boot.util.OrderedEnumerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class TCPSessionBasedServiceProvider <T extends Session> extends TCPServiceProvider {
	protected final List<T> sessions = Collections.synchronizedList(new ArrayList<>());
	
	
	
	protected TCPSessionBasedServiceProvider (){
		super();
	}
	
	
	
	public final List<T> getSessions (){
		return this.sessions;
	}
	
	public final void manageSessions (final Consumer<T> handler){
		this.manageSessions(handler, () -> {});
	}
	
	public void manageSessions (final Consumer<T> handler, final Runnable callback){
		try {
			for (final var session : this.sessions) {
				handler.accept(session);
			}
		} finally {
			if (null != callback) {
				callback.run();
			}
		}
	}
	
	public final void manageSessions (final OrderedEnumerator<T> handler){
		this.manageSessions(handler, () -> {});
	}
	
	public void manageSessions (final OrderedEnumerator<T> handler, final Runnable callback){
		try {
			final Iterator<T> it;

			it = this.sessions.iterator();

			while (it.hasNext()) {
				final T session;

				session = it.next();

				if (! handler.accept(session)) {
					break;
				}
			}
		} finally {
			if (null != callback) {
				callback.run();
			}
		}
	}
}