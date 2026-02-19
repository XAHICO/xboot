/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.KeyManagerFactory;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXServiceConfiguration {
	public int               cacheImages = -1;
	public Charset           charset = StandardCharsets.UTF_8;
	public boolean           enableActions = false;
	public boolean           enableEvents = false;
	public int               eventGroupingDispatch = -1;
	public File              interfaceRoot = null;
	public int               port = -1;
	public boolean           retainHandles = false;
	public boolean           retainEvents = false;
	public KeyManagerFactory ssl = null;
	public boolean           useCachedRendering = true;
	public File              webClassRoot = null;
	public File              webInterfaceFile = null;
	public File              webRoot = null;
}