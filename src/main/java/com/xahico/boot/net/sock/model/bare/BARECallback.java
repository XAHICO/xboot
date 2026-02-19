/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

/**
 * TBD.
 * 
 * @param <TREQ> 
 * TBD.
 * 
 * @param <TRSP> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface BARECallback <TREQ extends BARERequest, TRSP extends BAREResponse> {
	void onRequest (final TREQ request);
	
	void onResponse (final TRSP response);
}