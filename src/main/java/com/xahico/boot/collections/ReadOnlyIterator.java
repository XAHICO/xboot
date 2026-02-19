/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.collections;

import java.util.Iterator;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ReadOnlyIterator <T> implements Iterator<T> {
	public ReadOnlyIterator (){
		super();
	}
	
	
	
	@Override
	public final void remove (){
		throw new UnsupportedOperationException("Not supported.");
	}
}