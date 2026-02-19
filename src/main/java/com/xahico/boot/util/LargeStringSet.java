/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class LargeStringSet implements Set<String> {
	private static int computeHash (final String string){
		int blockSize;
		int computed;
		int cursor;
		
		blockSize = (int) Math.sqrt(string.length());
		
		computed = 0;
		
		cursor = 0;
		
		while (cursor < string.length()) {
			int sum = 0;
			
			for (var i = 0; i < blockSize; i++) {
				final int b;
				final int p;
				
				p = (cursor + i);
				
				b = string.charAt(p);
				
				sum += b;
			}
			
			sum *= (cursor / blockSize);
			
			computed += sum;
			
			cursor += blockSize;
		}
		
		return computed;
	}
	
	public static LargeStringSet generate (final int count, final StringGenerator generator){
		return generate(count, generator, (string) -> true);
	}
	
	public static LargeStringSet generate (final int count, final StringGenerator generator, final Filter<String> filter){
		final LargeStringSet generated;
		
		generated = new LargeStringSet();
		
		while (generated.size() != count) {
			final String string;
			
			string = generator.generate();
			
			if (! filter.accept(string)) 
				continue;
			
			if (! generated.contains(string)) {
				generated.add(string);
			}
		}
		
		return generated;
	}
	
	
	
	protected int                             available = 0;
	protected final Map<Integer, List<String>> buckets = new HashMap<>();
	
	
	
	public LargeStringSet (){
		super();
	}
	
	
	
	@Override
	public boolean add (final String string){
		List<String> stringBucket;
		final int    stringHash;
		
		stringHash = computeHash(string);
		
		stringBucket = this.buckets.get(stringHash);

		if (null == stringBucket) {
			stringBucket = new LinkedList<>();

			this.buckets.put(stringHash, stringBucket);
		} else if (stringBucket.contains(string)) {
			return false;
		}

		stringBucket.add(string);

		this.available++;
		
		return true;
	}
	
	@Override
	public boolean addAll (final Collection<? extends String> collection){
		boolean changed = false;
		
		if (collection instanceof LargeStringSet) {
			return this.addAll((LargeStringSet) collection);
		}
		
		for (final var element : collection) {
			if (this.add(element)) {
				changed = true;
			}
		}
		
		return changed;
	}
	
	public boolean addAll (final LargeStringSet other){
		boolean changed = false;
		
		for (final var tokenHash : other.buckets.keySet()) {
			final List<String> storeBucket;
			final List<String> tokenBucket;

			tokenBucket = other.buckets.get(tokenHash);

			if (! this.buckets.containsKey(tokenHash)) {
				this.buckets.put(tokenHash, tokenBucket);

				this.available += tokenBucket.size();
				
				changed = true;

				continue;
			}

			storeBucket = this.buckets.get(tokenHash);

			for (final var token : tokenBucket) {
				if (storeBucket.contains(token)) 
					continue;

				storeBucket.add(token);
				
				this.available++;
				
				changed = true;
			}
		}
		
		return changed;
	}
	
	public boolean clean (){
		boolean                                          changed = false;
		final Iterator<Map.Entry<Integer, List<String>>> it;
		
		it = this.buckets.entrySet().iterator();
		
		while (it.hasNext()) {
			final Map.Entry<Integer, List<String>> entry;

			entry = it.next();

			if (entry.getValue().isEmpty()) {
				it.remove();
				
				changed = true;
			}
		}
		
		return changed;
	}
	
	@Override
	public void clear (){
		synchronized (this.buckets) {
			this.buckets.clear();

			this.available = 0;
		}
	}
	
	@Override
	public boolean contains (final Object object){
		if (object instanceof String) 
			return this.contains((String) object);
		else {
			return false;
		}
	}
	
	public boolean contains (final String string){
		final List<String> stringBucket;
		final int          stringHash;
		
		stringHash = computeHash(string);
		
		stringBucket = this.buckets.get(stringHash);

		if (null == stringBucket) {
			return false;
		}

		return stringBucket.contains(string);
	}
	
	@Override
	public boolean containsAll (final Collection<?> collection){
		if (collection.isEmpty()) 
			return false;
		
		for (final var element : collection) {
			if (!(element instanceof String)) 
				return false;
			
			if (! this.contains((String) element)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isEmpty (){
		return (this.available == 0);
	}
	
	@Override
	public Iterator<String> iterator (){
		return new Iterator<>() {
			Map.Entry<Integer, List<String>>           ent = null;
			Iterator<String>                           itl = null;
			Iterator<Map.Entry<Integer, List<String>>> itu = buckets.entrySet().iterator();
			
			
			@Override
			public boolean hasNext (){
				if ((null != itl) && itl.hasNext()) {
					return true;
				} else {
					return itu.hasNext();
				}
			}
			
			@Override
			public String next (){
				if ((null == ent) || !itl.hasNext()) {
					ent = itu.next();
					
					itl = ent.getValue().iterator();
				}
				
				return itl.next();
			}
			
			@Override
			public void remove (){
				if (null != itl) {
					final List<String> bucket;
					
					itl.remove();
					
					bucket = this.ent.getValue();
					
					if (bucket.isEmpty()) {
						itu.remove();
						
						itl = null;
					}
					
					if (available > 0) {
						available--;
					}
				}
			}
		};
	}
	
	@Override
	public boolean remove (final Object object){
		if (object instanceof String) 
			return this.remove((String) object);
		else {
			return false;
		}
	}
	
	public boolean remove (final String string){
		final List<String> stringBucket;
		final int          stringHash;
		
		stringHash = computeHash(string);
		
		stringBucket = this.buckets.get(stringHash);
		
		if (null == stringBucket) {
			return false;
		}
		
		if (! stringBucket.remove(string)) {
			return false;
		}
		
		if (stringBucket.isEmpty()) {
			this.buckets.remove(stringHash);
		}
		
		this.available--;
		
		return true;
	}
	
	@Override
	public boolean removeAll (final Collection<?> collection){
		boolean changed = false;
		
		for (final var element : collection) {
			if (!(element instanceof String)) 
				continue;
			
			if (this.remove((String) element)) {
				changed = true;
			}
		}
		
		return changed;
	}
	
	@Override
	public boolean retainAll (final Collection<?> collection){
		boolean                changed = false;
		final Iterator<String> it;
		
		it = this.iterator();
		
		while (it.hasNext()) {
			final String element;
			
			element = it.next();
			
			if (! collection.contains(element)) {
				it.remove();
				
				changed = true;
			}
		}
		
		return changed;
	}
	
	@Override
	public int size (){
		return this.available;
	}
	
	@Override
	public Object[] toArray (){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public <T> T[] toArray (final T[] array){
		throw new UnsupportedOperationException("Not supported.");
	}
}