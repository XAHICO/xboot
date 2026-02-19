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
public abstract class GWXClient {
	protected final GWXConnection connection;
	
	
	
	protected GWXClient (final String hostname, final int requestVersion){
		super();
		
		this.connection = GWXConnection.create(hostname, 0, (config) -> {
			config.setExecutor(null);
			config.setRequestVersion(requestVersion);
			config.setUseTLS(false);
		});
	}
}