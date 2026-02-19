/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.lang.javascript.JSBuilder;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassCoreUtilities {
	public static String isMeta (final String meta){
		final JSBuilder builder;
		
		builder = new JSBuilder();
		builder.addLine("(null !== __document__.lookupMeta(\"%s\"))".formatted(meta));
		
		return builder.buildString();
	}
	
	
	
	private GlassCoreUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}