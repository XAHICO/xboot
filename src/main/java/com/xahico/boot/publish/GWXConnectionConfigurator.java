/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface GWXConnectionConfigurator {
	void configure (final GWXConnectionConfiguration config);
}