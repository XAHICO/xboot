/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import java.io.File;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface FTSUploadCallbacks {
	void complete (final String key, final File file);
	
	void discard (final String key, final File file);
}