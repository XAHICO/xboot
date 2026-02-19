/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GWXNodeTree {
	private final Map<String, GWXObject> root;
	private final GWXNode                rootNode;
	
	
	
	GWXNodeTree (){
		this(new HashMap<>());
	}
	
	GWXNodeTree (final Map<String, GWXObject> root){
		super();
		
		this.root = root;
		this.rootNode = new GWXNode(GWXProperties.createReflection(null, root)) {
			@Override
			public JSOXVariant snapshot (){
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}
	
	
	
	public void destroy (){
		this.rootNode.destroy();
	}
	
	public GWXObject lookupRoot (final GWXPath path){
		return this.lookupRoot(path.toString());
	}
	
	public GWXObject lookupRoot (final String name){
		return root.get(name);
	}
	
	public Map<String, GWXObject> root (){
		return this.root;
	}
	
	public GWXNode rootNode (){
		return this.rootNode;
	}
}