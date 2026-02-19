/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface JSOXCastable {
	<T extends JSOXObject> T castTo (final Class<T> jclass);
}