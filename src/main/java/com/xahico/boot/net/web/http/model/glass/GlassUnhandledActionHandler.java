/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.net.web.http.HttpServiceExchange;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface GlassUnhandledActionHandler {
	boolean call (final GlassSession session, final HttpServiceExchange exchange);
}