/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.collections;

import java.util.Collection;
import java.util.Iterator;

/**
 * TBD.
 * 
 * @param <T> 
 * The element type.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class UnmodifiableCollection <T> implements Collection<T> {
	public static <T> UnmodifiableCollection<T> wrap (final Collection<T> collection){
		return new UnmodifiableCollection() {
			@Override
			public boolean contains (final Object o){
				return collection.contains(o);
			}

			@Override
			public boolean containsAll (final Collection c){
				return collection.containsAll(c);
			}

			@Override
			public boolean isEmpty() {
				return collection.isEmpty();
			}
			
			@Override
			public ReadOnlyIterator<T> iterator (){
				return new ReadOnlyIterator() {
					final Iterator<T> it = collection.iterator();
					
					
					@Override
					public boolean hasNext (){
						return it.hasNext();
					}
					
					@Override
					public Object next (){
						return it.next();
					}
				};
			}
			
			@Override
			public int size (){
				return collection.size();
			}
			
			@Override
			public Object[] toArray (){
				return collection.toArray();
			}

			@Override
			public Object[] toArray (final Object[] a){
				return collection.toArray(a);
			}
		};
	}
	
	public static <T> UnmodifiableCollection<T> wrapAtomic (final Collection<T> collection){
		return new UnmodifiableCollection() {
			@Override
			public boolean contains (final Object o){
				synchronized (collection) {
					return collection.contains(o);
				}
			}

			@Override
			public boolean containsAll (final Collection c){
				synchronized (collection) {
					return collection.containsAll(c);
				}
			}

			@Override
			public boolean isEmpty() {
				synchronized (collection) {
					return collection.isEmpty();
				}
			}
			
			@Override
			public ReadOnlyIterator<T> iterator (){
				return new ReadOnlyIterator() {
					final Iterator<T> it = collection.iterator();
					
					
					@Override
					public boolean hasNext (){
						return it.hasNext();
					}
					
					@Override
					public Object next (){
						return it.next();
					}
				};
			}
			
			@Override
			public int size (){
				synchronized (collection) {
					return collection.size();
				}
			}
			
			@Override
			public Object[] toArray (){
				synchronized (collection) {
					return collection.toArray();
				}
			}

			@Override
			public Object[] toArray (final Object[] a){
				synchronized (collection) {
					return collection.toArray(a);
				}
			}
		};
	}
	
	
	
	public UnmodifiableCollection (){
		super();
	}
	
	
	
			
	@Override
	public final boolean add (final Object e){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public final boolean addAll (final Collection c){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public final void clear (){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public abstract ReadOnlyIterator<T> iterator ();
	
	@Override
	public final boolean remove (final Object o){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public final boolean removeAll (final Collection c){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public final boolean retainAll (final Collection c){
		throw new UnsupportedOperationException("Not supported.");
	}
}