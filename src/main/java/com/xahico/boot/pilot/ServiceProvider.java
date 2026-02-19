/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.logging.Logger;
import com.xahico.boot.synchronicity.SynchronizationHandler;
import com.xahico.boot.util.Exceptions;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ServiceProvider extends SynchronizationHandler {
	private static final int DEFAULT_HEARTBEAT_REST_INTERVAL = 1000;
	
	
	
	private int          heartbeatRestInterval = DEFAULT_HEARTBEAT_REST_INTERVAL;
	private Logger       logger = Logger.getLogger(this.getClass()).setStream(System.out);
	private final Object mutex = new Object();
	private boolean      shutdown = false;
	private boolean      started = false;
	
	
	
	protected ServiceProvider (){
		super();
	}
	
	
	
	@Override
	protected Executor createDefaultExecutor (){
		return Executors.newSingleThreadExecutor();
	}
	
	protected abstract void cleanup ();
	
	public final Logger getLogger (){
		return this.logger;
	}
	
	private void heartbeat (){
		this.run();
		
		if (this.isStepper()) try {
			Thread.sleep(0, this.heartbeatRestInterval);
			
			this.call(this::heartbeat);
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	protected abstract void initialize () throws Throwable;
	
	public abstract boolean isIdle ();
	
	public final boolean isStarted (){
		return this.started;
	}
	
	public abstract boolean isStepper ();
	
	public final boolean isStopped (){
		return this.shutdown;
	}
	
	protected abstract void run ();
	
	public final void setHeartbeatRestInterval (final int nanos){
		this.heartbeatRestInterval = nanos;
	}
	
	public final void setLogger (final Logger logger){
		this.logger = logger;
	}
	
	public final void start () throws IllegalStateException {
		final AtomicReference<Throwable> thrownException;
		
		if (this.started == true) {
			throw new IllegalStateException("already started");
		}
		
		try {
			this.initialize();
		} catch (final Throwable ex) {
			throw new Error(new InitializeException(ex));
		}
		
		if (! this.isStepper()) {
			this.run();
		}
		
		this.started = true;
		
		this.logger.log("%s: started".formatted(ServiceProvider.this));

		if (this.isStepper()) {
			this.heartbeat();
		}
	}
	
	public final void stop () throws IllegalStateException {
		if (this.started == false) {
			throw new IllegalStateException("not started");
		}
		
		if (this.shutdown == true) {
			throw new IllegalStateException("already stopped");
		}
		
		this.shutdown = true;
		this.call(() -> {
			try {
				this.cleanup();
			} finally {
				this.logger.log("%s: has shut down".formatted(this.getClass().getSimpleName()));
			}
		});
	}
}