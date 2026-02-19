/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
interface GlassInterface {
	String CZ_SITE_REQUIRE_AUTH = "site-require-auth";
	
	String GF_AUTH_CHECK = "isAuth";
	String GF_AUTH_CLEAR = "clearAuth";
	String GF_AUTH_GET = "getAuth";
	String GF_AUTH_INIT = "initAuth";
	String GF_EVENT_INIT = "initEvents";
	String GF_SESSION_TIMEOUT_INIT = "initSessionTimeout";
	String GF_SESSION_TIMEOUT_UPDATE = "updateSessionTimeout";
	String GF_STATE_INIT = "loadState";
	
	String GV_EVENT_STREAM = "eventStream";
	String GV_TIMEOUT_HANDLER = "timeoutHandler";
}