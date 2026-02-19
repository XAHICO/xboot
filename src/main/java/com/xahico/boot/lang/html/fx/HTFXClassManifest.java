/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.fx;

import com.xahico.boot.io.Source;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class HTFXClassManifest {
	public String document = null;
	public long   documentTime = -1;
	
	public String script = null;
	public long   scriptTime = -1;
	
	public String style = null;
	public long   styleTime = -1;
	
	
	
	public HTMLDocument createDocument (){
		try {
			final HTMLDocument documentObject;
			final HTMLParser   documentParser;
			
			documentParser = new HTMLParser();
			documentParser.setSource(Source.wrapString(this.document));
			documentParser.setTolerateGarbage(true);
			
			documentObject = documentParser.parse();
			documentObject.setIncludeDef(false);
			
			return documentObject;
		} catch (final HTMLException | IOException ex) {
			throw new Error(ex);
		}
	}
}