/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeoutException;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class EventQueue <T extends Event> {
	private final Deque<T> queue = new ArrayDeque<>();
	
	
	
	public EventQueue (){
		super();
	}
	
	
	
	public boolean isEmpty (){
		synchronized (queue) {
			return queue.isEmpty();
		}
	}
	
	public T peek (){
		synchronized (queue) {
			return queue.peek();
		}
	}
	
	public T poll () throws InterruptedException {
		synchronized (queue) {
			if (queue.isEmpty()) queue.wait();
			
			return queue.poll();
		}
	}
	
	public T poll (final long timeoutMillis) throws InterruptedException, TimeoutException {
		synchronized (queue) {
			final T       event;
			final boolean timedOut;
			
			if (! queue.isEmpty()) {
				event = queue.poll();
				
				timedOut = false;
			} else {
				queue.wait(timeoutMillis);
				
				event = queue.poll();
				
				timedOut = true;
			}
			
			if (timedOut && (null == event)) {
				throw new TimeoutException();
			}
			
			return event;
		}
	}
	
	public T pop (){
		synchronized (queue) {
			return queue.poll();
		}
	}
	
	public void post (final T e){
		synchronized (queue) {
			queue.addLast(e);
			
			queue.notify();
		}
	}
	
	public void push (final T e){
		synchronized (queue) {
			queue.addFirst(e);
			
			queue.notify();
		}
	}
	
	public boolean ready (){
		return (null != this.peek());
	}
	
	public int size (){
		synchronized (queue) {
			return queue.size();
		}
	}
	
	/**
	 * 'Trashes' the current head of the queue.
	 * 
	 * The head of the queue is removed but <i>not</i> returned, 
	 * unlike with {@link #poll poll}.
	 * 
	 * @throws InterruptedException 
	 * TBD
	**/
	public void trash () throws InterruptedException {
		synchronized (queue) {
			queue.poll();
		}
	}
}