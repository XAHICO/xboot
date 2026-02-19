/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util.transformer;

/**
 * ...
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface DataTransformer {
	DataTransformer NOTRAN = (bytes) -> bytes;
	
	
	
	byte[] transform (final byte[] bytes);
}