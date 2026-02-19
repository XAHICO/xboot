/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.cbd;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.io.Source;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLElement;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLNode;
import com.xahico.boot.lang.html.HTMLParser;
import com.xahico.boot.lang.html.HTMLStandardType;
import com.xahico.boot.util.Exceptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * Component-Based Document (CBD) loader.
 * 
 * @author Tuomas Kontiainen
**/
public final class DocumentLoader {
	private static final String FILE_TYPE_DOCUMENT = ".html";
	private static final String FILE_TYPE_SCRIPT = ".js";
	private static final String FILE_TYPE_STYLESHEET = ".css";
	
	private static final String QL_ATTRIBUTE_ARTIFACT = "artifact";
	private static final String QL_TYPE_ELEMENT = "extern";
	
	
	
	private AbstractionLayer abstractionLayer = AbstractionLayer.NONE;
	private File             componentDirectory = null;
	private Source           inputSource = null;
	
	
	
	private DocumentLoader (){
		super();
	}
	
	
	
	public HTMLDocument load () throws HTMLException, IOException{
		final HTMLDocument document;
		final HTMLParser   parser;
		
		parser = new HTMLParser();
		parser.setSource(inputSource);
		
		document = parser.parse();
		
		if (null != componentDirectory) {
			for (final HTMLElement replaceElement : document.lookup(QL_TYPE_ELEMENT, -1)) {
				final String   artifactName;
				final HTMLNode replaceNode;
				final HTMLNode useNode;

				if (replaceElement instanceof HTMLNode) {
					replaceNode = (HTMLNode)(replaceElement);

					// Get artifact name
					artifactName = replaceNode.getAttribute(QL_ATTRIBUTE_ARTIFACT);

					// Remove 'artifact' attribute from attributes; 
					// This is because we copy other attributes to 
					// the replacement node
					replaceNode.removeAttribute(QL_ATTRIBUTE_ARTIFACT);

					// Load the replacement node and copy attributes
					useNode = loadComponent(artifactName);
					useNode.setAttributes(replaceNode.getAttributes());

					// Swap the replacement node in this node's place in 
					// the DOM tree
					replaceNode.swap(useNode);
				}
			}
		}
		
		switch (abstractionLayer) {
			case TOTAL: {
				document.removeComments();
			}
			case NONE: {
				break;
			}
		}
		
		return document;
	}
	
	private HTMLNode loadComponent (final String artifactName) throws HTMLException, IOException{
		final File         fileDocument;
		final File         fileScript;
		final File         fileStylesheet;
		final HTMLDocument document;
		final HTMLNode     documentRoot;
		final HTMLParser   parser;
		final HTMLNode     script;
		final HTMLNode     stylesheet;
		
		// ...
		fileDocument = new File(componentDirectory, (artifactName + FILE_TYPE_DOCUMENT));
		
		parser = new HTMLParser();
		parser.setSource(Source.wrapFile(fileDocument.getPath()));
		
		document = parser.parse();
		document.setIncludeDef(false);
		documentRoot = document.getDocumentRoot();
		
		if (null != documentRoot) {
			// ...
			fileStylesheet = new File(new File(componentDirectory, "css"), (artifactName + FILE_TYPE_STYLESHEET));

			if (fileStylesheet.exists()) try {
				stylesheet = new HTMLNode(HTMLStandardType.STYLE);
				stylesheet.setContent(Files.readString(fileStylesheet.toPath()));

				documentRoot.addChild(stylesheet);
			} catch (final NoSuchFileException ex) {
				Exceptions.ignore(ex);
			}

			// ...
			fileScript = new File(new File(componentDirectory, "js"), (artifactName + FILE_TYPE_SCRIPT));

			if (fileScript.exists()) try {
				script = new HTMLNode(HTMLStandardType.SCRIPT);
				script.setContent(Files.readString(fileScript.toPath()));

				documentRoot.addChild(script);
			} catch (final NoSuchFileException ex) {
				Exceptions.ignore(ex);
			}
		}
		
		return (HTMLNode) document.getDocumentRoot();
	}
	
	public void setAbstractionLayer (final AbstractionLayer abstractionLayer){
		this.abstractionLayer = abstractionLayer;
	}
	
	public void setComponentDirectory (final File directory){
		this.componentDirectory = directory;
	}
	
	@Helper
	public void setComponentDirectory (final String directoryPath){
		this.setComponentDirectory((null != directoryPath) ? new File(directoryPath) : null);
	}
	
	public void setInputSource (final Source source){
		this.inputSource = source;
	}
}