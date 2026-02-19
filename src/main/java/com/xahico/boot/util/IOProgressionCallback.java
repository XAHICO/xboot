/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface IOProgressionCallback {
	IOProgressionCallback EMPTY_HANDLER = (position, size) -> {};
	
	
	
	void invoke (final long position, final long size);
}