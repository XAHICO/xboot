/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.lang.html.HTMLDocument;
import java.io.File;
import java.io.IOException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GlassDocumentLoader {
	GlassDocumentLoader (){
		super();
	}
	
	
	
	public abstract HTMLDocument load (final File file, final boolean accessRestricted) throws IOException;
	
 //	public abstract HTMLDocument load (final String path, final boolean lookupInRoot, final boolean accessRestricted) throws IOException;
	
	public abstract File lookup (final String path);
	
	public abstract String realize (final HTMLDocument document, final GlassNamespace context);
}