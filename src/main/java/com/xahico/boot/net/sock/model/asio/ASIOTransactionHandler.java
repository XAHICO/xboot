/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

/**
 * TBD.
 * 
 * @param <REQ> 
 * TBD.
 * 
 * @param <RSP> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface ASIOTransactionHandler <REQ extends ASIORequest, RSP extends ASIOResponse> {
	void onRequest (final REQ request);
	
	void onResponse (final RSP response);
}