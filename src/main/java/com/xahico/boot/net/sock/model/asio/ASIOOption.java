/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum ASIOOption {
	ENFORCE_TLS(1 << 2),
	KEEP_ALIVE(1 << 1),
	NONE(0);
	
	
	
	public static ASIOOption getOption (final int flag){
		for (final var option : ASIOOption.values()) {
			if (option.flag == flag) {
				return option;
			}
		}
		
		return null;
	}
	
	public static boolean match (final ASIOOption[] serverOptions, final ASIOOption[] clientOptions){
		final int clientOptionsFlag;
		final int serverOptionsFlag;
		
		if (clientOptions.length != serverOptions.length) 
			return false;
		
		serverOptionsFlag = mergeOptions(serverOptions);
		clientOptionsFlag = mergeOptions(clientOptions);
		
		return (clientOptionsFlag == serverOptionsFlag);
	}
	
	public static int mergeOptions (final ASIOOption... options){
		int flag = 0;
		
		for (final var option : options) {
			flag |= option.flag();
		}
		
		return flag;
	}
	
	public static ASIOOption[] splitOptions (final int flag){
		final ASIOOption[] array;
		int                arrayLength;
		int                cursor;
		
		arrayLength = 0;
		
		for (var i = 0; i < Integer.BYTES; i++) {
			if ((flag & (1 << i)) != 0) {
				arrayLength++;
			}
		}
		
		array = new ASIOOption[arrayLength];
		
		cursor = 0;
		
		for (var i = 0; i < Integer.BYTES; i++) {
			final ASIOOption option;
			
			if ((flag & (1 << i)) == 0) {
				continue;
			}
			
			option = getOption(flag & (1 << i));
			
			array[cursor] = option;
			
			cursor++;
		}
		
		return array;
	}
	
	
	
	private final int flag;
	
	
	
	ASIOOption (final int flag){
		this.flag = flag;
	}
	
	
	
	public int flag (){
		return this.flag;
	}
}