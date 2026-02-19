/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.util.transformer;

/**
 * TBD.
 * 
 * @param <F> 
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface ObjectTransformer <F, T> {
	T call (final F object);
}