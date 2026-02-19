/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock.model.trax;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface TRAXConnectionErrorHandler {
	void call (final Throwable throwable);
}