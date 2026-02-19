/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXQueueBackedConcurrentNodeCollection <T extends GWXObject> extends GWXQueueBackedNodeCollection<T> {
	public GWXQueueBackedConcurrentNodeCollection (){
		super(new ConcurrentLinkedQueue<>());
	}
	
	public GWXQueueBackedConcurrentNodeCollection (final GWXObject owner){
		super(owner, new ConcurrentLinkedQueue<>());
	}
}