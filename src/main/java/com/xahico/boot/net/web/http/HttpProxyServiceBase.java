/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

import com.xahico.boot.net.InvalidTokenException;
import com.xahico.boot.net.URIA;
import java.net.URL;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class HttpProxyServiceBase {
	protected HttpProxyServiceBase (){
		super();
	}
	
	
	
	public abstract URL redirect (final String token, final URIA uri) throws InvalidTokenException;
}