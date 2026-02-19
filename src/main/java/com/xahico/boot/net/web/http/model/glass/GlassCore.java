/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.lang.javascript.JSCode;
import java.io.IOException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassCore {
	public static JSCode load (){
		try (final var stream = GlassCore.class.getResourceAsStream(GlassCore.class.getSimpleName() + ".js")) {
			return new JSCode(new String(stream.readAllBytes()));
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	public static JSCode loadResourcex (final String resourceName){
		try (final var stream = GlassCore.class.getResourceAsStream(resourceName + ".js")) {
			return new JSCode(new String(stream.readAllBytes()));
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private GlassCore (){
		throw new UnsupportedOperationException("Not supported.");
	}
}