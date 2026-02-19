/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.nio.charset.StandardCharsets;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXNode extends GWXObject implements GWXSerializable {
	protected GWXNode (){
		super();
	}
	
	GWXNode (final GWXProperties properties){
		super(properties);
	}
	
	
	
	@Override
	protected void cleanup (){
		super.cleanup();
	}
	
	@Override
	public byte[] serialize (final boolean internal){
		final JSOXVariant serialized;
		
		serialized = new JSOXVariant();
		
		for (final var propertyKey : this.getProperties()) {
			final Object propertyObject;
			final Object propertySerialized;
			
			propertyObject = this.getProperties().get(this, propertyKey);
			
			if (propertyObject instanceof GWXNode propertyNode) {
				propertySerialized = propertyNode.serialize(internal);
			} else if (propertyObject instanceof GWXNodeCollection propertyNodes) {
				propertySerialized = propertyNodes.serialize(internal);
			} else {
				propertySerialized = propertyObject;
			}
			
			serialized.put(propertyKey, propertySerialized);
		}
		
		return serialized.toJSONStringCompact().getBytes(StandardCharsets.UTF_8);
	}
}