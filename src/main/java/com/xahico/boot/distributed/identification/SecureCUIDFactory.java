/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.distributed.identification;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class SecureCUIDFactory {
	// 16-bit random process ID
	private static final int          processId;
	
	private static final SecureRandom secure;
	
	// atomic counter packed with timestamp
	// high 48 bits = last timestamp
	// low 16 bits = counter
	private static final AtomicLong   timeCounter = new AtomicLong(0);
	
	
	
	static {
		secure = new SecureRandom();
		
		processId = secure.nextInt(1 << 16);
	}
	
	
	
	public static UUID random (){
		long now = System.currentTimeMillis();
		
		while (true) {
			long prev = timeCounter.get();
			long prevTime = prev >>> 16;
			long prevSeq = prev & 0xFFFF;
			
			long nextSeq = (now == prevTime) ? (prevSeq + 1) & 0xFFFF : 0;
			
			long next = (now << 16) | nextSeq;
			
			if (timeCounter.compareAndSet(prev, next)) {
				// timestamp (48 bits) + process (16 bits)
				long mostSig = (now << 16) | (processId & 0xFFFF);
				
				// randomness (48 bits)
				long rand = secure.nextLong() & 0xFFFFFFFFFFFFL;

				// seq (16 bits) + randomness (48 bits)
				long leastSig = ((long) nextSeq << 48) | rand;

				return new UUID(mostSig, leastSig);
			}
		}
	}
	
	
	
	private SecureCUIDFactory (){
		throw new UnsupportedOperationException("Not supported.");
	}
}