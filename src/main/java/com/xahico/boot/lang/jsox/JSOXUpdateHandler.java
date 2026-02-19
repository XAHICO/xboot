/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.lang.jsox;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface JSOXUpdateHandler <T extends JSOX> {
	public void handleUpdate (final T data);
}