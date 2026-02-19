/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.fx;

import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLNode;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HTFXClass {
	private final String            classPath;
	private final HTMLDocument      document;
	private final HTFXClassManifest manifest;
	
	
	
	HTFXClass (final String classPath, final HTFXClassManifest manifest){
		super();
		
		this.classPath = classPath;
		this.manifest = manifest;
		this.document = manifest.createDocument();
	}
	
	
	
	public String getBody (){
		return this.document.toHTMLStringAbstracted();
	}
	
	public String getClassName (){
		return this.classPath.substring(this.classPath.lastIndexOf('.') + 1);
	}
	
	public String getClassPath (){
		return this.classPath;
	}
	
	public String getController (){
		if (null != this.manifest.script) 
			return this.manifest.script;
		else {
			return null;
		}
	}
	
	public HTMLDocument getDocument (){
		return this.document;
	}
	
	public String getStyle (){
		return this.manifest.style;
	}
	
	public boolean hasController (){
		return (null != this.manifest.script);
	}
	
	public HTMLNode newInstance (){
		return (HTMLNode) this.document.getDocumentRoot().duplicate();
	}
}