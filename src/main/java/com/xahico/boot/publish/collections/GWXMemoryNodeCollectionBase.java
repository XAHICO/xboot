/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXNodeCollection;
import com.xahico.boot.publish.GWXObject;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXMemoryNodeCollectionBase <T extends GWXObject> extends GWXNodeCollection<T> {
	protected GWXMemoryNodeCollectionBase (){
		super();
	}
	
	protected GWXMemoryNodeCollectionBase (final GWXObject owner){
		super(owner);
	}
}