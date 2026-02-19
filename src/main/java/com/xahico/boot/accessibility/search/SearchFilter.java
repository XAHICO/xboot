/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

/**
 * TBD.
 * 
 * @param <CTX> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface SearchFilter <CTX> {
	boolean accept (final Accessible accessible, final CTX context);
}