/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Benchmarker {
	private int                 commit = 1;
	private final AtomicInteger counter = new AtomicInteger(0);
	private ExecutorService     executor = null;
	private String              name = null;
	private int                 parallellism = 1;
	private long                whenBegin = -1;
	private long                whenEnd = -1;
	
	
	
	public Benchmarker (){
		super();
	}
	
	
	
	public void setDefaultCommit (final int commit){
		this.commit = commit;
	}
	
	public void setParallellism (final int parallellism){
		this.parallellism = parallellism;
	}
	
	public void start (final String name){
		this.name = name;
		
		System.out.println("[%s] starting, using %d thread(s)".formatted(this, this.parallellism));
		
		this.executor = Executors.newFixedThreadPool(this.parallellism);
		
		this.whenBegin = System.currentTimeMillis();
		
		this.counter.set(0);
	}
	
	public long stop (){
		this.executor.shutdown();
		
		try {
			this.executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		}
		
		this.whenEnd = System.currentTimeMillis();
		
		System.out.println("[%s] finished, %d calls were executed in %d millisecond(s)".formatted(this, this.counter.get(), (this.whenEnd - this.whenBegin)));
		
		return (this.whenEnd - this.whenBegin);
	}
	
	public void submit (final Runnable task){
		this.submit(task, this.commit);
	}
	
	public void submit (final Runnable task, final int count){
		System.out.println("[%s] submitting %d task(s)".formatted(this, count));
		for (var i = 0; i < count; i++) {
			this.counter.incrementAndGet();
			
			this.executor.submit(task);
		}
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("bench");
		sb.append(" ");
		
		if (null != this.name) {
			sb.append(this.name);
		} else {
			sb.append("?");
		}
		
		return sb.toString();
	}
}