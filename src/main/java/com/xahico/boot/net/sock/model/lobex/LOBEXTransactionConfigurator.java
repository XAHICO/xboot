/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.lobex;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface LOBEXTransactionConfigurator {
	void configure (final LOBEXTransaction transaction);
}
