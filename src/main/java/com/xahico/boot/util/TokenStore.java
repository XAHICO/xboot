/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import com.xahico.boot.synchronicity.ExecutionHandler;
import com.xahico.boot.util.LargeStringSet;
import com.xahico.boot.util.StringGenerator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class TokenStore {
	public static final StringGenerator GENERATOR_CUID = () -> UUID.randomUUID().toString();
	public static final StringGenerator GENERATOR_UUID = () -> UUID.randomUUID().toString();
	public static final double          DEFAULT_PRECOMPUTE_THRESHOLD = 0.15;
	
	
	
	private int                  capacity = 0;
	private boolean              ensureUniquity = false;
	private StringGenerator      generator = null;
	private TokenStore           peer = null;
	private boolean              precomputeEnabled = false;
	private ExecutionHandler     precomputeExecutor = null;
	private double               precomputeThreshold = DEFAULT_PRECOMPUTE_THRESHOLD;
	private final LargeStringSet set = new LargeStringSet();
	private final Set<String>    setlock = Collections.synchronizedSet(this.set);
	
	
	
	public TokenStore (){
		super();
	}
	
	
	
	public boolean accepts (final String token){
		if (this.ensureUniquity && this.set.contains(token)) {
			return false;
		}
		
		if ((null != this.peer) && !this.peer.accepts(token)) {
			return false;
		}
		
		return true;
	}
	
	public int cap (){
		return this.capacity;
	}
	
	public synchronized void fill (){
		final LargeStringSet generated;
		final int            generatedNeed;

		generatedNeed = (this.capacity - this.setlock.size());
		
		if ((generatedNeed > 0) && (this.setlock.size() < generatedNeed)) {
			generated = LargeStringSet.generate(generatedNeed, this.generator, (token) -> {
				if (! this.ensureUniquity) 
					return true;
				else if (null != this.peer) 
					return this.peer.accepts(token);
				else {
					return true;
				}
			});
			
			synchronized (this.setlock) {
				this.setlock.addAll(generated);
				
				if (this.setlock.size() != this.capacity) {
					this.set.clean();
				}
			}
		}
	}
	
	public int max (final boolean self, final boolean chained){
		int count;
		
		count = 0;
		
		if (self) {
			count += this.size();
		}
		
		if (chained && (null != this.peer)) {
			count += this.peer.size();
		}
		
		return count;
	}
	
	public void pair (final TokenStore peer){
		this.peer = peer;
	}
	
	public String pop (){
		String token = null;
		
		if (this.setlock.isEmpty()) {
			this.fill();
		} else if (this.precomputeEnabled && (this.setlock.size() < (this.capacity * this.precomputeThreshold))) {
			if ((null != this.precomputeExecutor) && !this.precomputeExecutor.isCallingThread()) {
				this.precomputeExecutor.call(this::fill);
			}
		}
		
		while (null == token) {
			synchronized (this.setlock) {
				final Iterator<String> it;
				
				if (this.setlock.isEmpty()) {
					continue;
				}
				
				it = this.setlock.iterator();
				
				if (it.hasNext()) {
					token = it.next();
					
					it.remove();
				}
			}
		}
		
		return token;
	}
	
	public String pull (){
		final String token;
		
		token = this.pop();
		
		if (null != this.peer) {
			this.peer.push(token);
		}
		
		return token;
	}
	
	public boolean pull (final String token){
		final boolean removed;
		
		removed = this.setlock.remove(token);
		
		if (! removed) 
			return false;
		
		if (null != this.peer) {
			this.peer.push(token);
		}
		
		return true;
	}
	
	public synchronized boolean push (final String token){
		if ((null != this.peer) && !this.peer.accepts(token)) {
			return false;
		} else {
			return this.setlock.add(token);
		}
	}
	
	public void setEnsureCapacity (final int capacity){
		this.capacity = capacity;
	}
	
	public void setEnsureUniquity (final boolean ensureUniquity){
		this.ensureUniquity = ensureUniquity;
	}
	
	public void setGenerator (final StringGenerator generator){
		this.generator = generator;
	}
	
	public void setPrecompute (final boolean enabled, final double threshold, final ExecutionHandler executor){
		this.precomputeEnabled = enabled;
		this.precomputeThreshold = threshold;
		this.precomputeExecutor = executor;
	}
	
	public int size (){
		return this.setlock.size();
	}
}