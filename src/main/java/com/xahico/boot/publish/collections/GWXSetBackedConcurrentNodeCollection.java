/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXSetBackedConcurrentNodeCollection <T extends GWXObject> extends GWXSetBackedNodeCollection<T> {
	public GWXSetBackedConcurrentNodeCollection (){
		super(new CopyOnWriteArraySet<>());
	}
	
	public GWXSetBackedConcurrentNodeCollection (final GWXObject owner){
		super(owner, new CopyOnWriteArraySet<>());
	}
}