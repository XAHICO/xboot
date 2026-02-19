/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface AccessibleDataSource <T extends Accessible> {
	void close ();
	
	void forEach (final Consumer<T> consumer);
	
	void open ();
}