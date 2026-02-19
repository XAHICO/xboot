/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.distributed.identification;

import java.util.UUID;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class IDx {
	public static final int LENGTH = 36;
	
	
	
	/**
	 * Generates a highly collision-resistant, 
	 * non-cryptographic, time-sortable 
	 * CUID ("Chronologically Unique Identifier")
	 * <br>
	 * <br>
	 * Suitable for high-throughput ID generation, but not for security.
	 * 
	 * @return 
	 * generated UUID
	**/
	public static UUID fast (){
		return FastCUIDFactory.random();
	}
	
	/**
	 * Generates a cryptographically secure, 
	 * extremely precise, time-sortable  
	 * CUID ("Chronologically Unique Identifier")
	 * with nanosecond precision.
	 * 
	 * @return 
	 * generated UUID
	**/
	public static UUID nano (){
		return NanoCUIDFactory.random();
	}
	
	/**
	 * Generates a random UUID4
	 * 
	 * @return 
	 * generated UUID
	**/
	public static UUID random (){
		return UUID.randomUUID();
	}
	
	/**
	 * Generates a cryptographically secure, time-sortable 
	 * CUID ("Chronologically Unique Identifier")
	 * 
	 * @return 
	 * generated UUID
	**/
	public static UUID secure (){
		return SecureCUIDFactory.random();
	}
	
	
	
	private IDx (){
		throw new UnsupportedOperationException("Not supported.");
	}
}