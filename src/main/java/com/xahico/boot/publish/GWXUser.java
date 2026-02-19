/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import java.security.MessageDigest;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXUser extends GWXNode {
	protected GWXUser (){
		super();
	}
	
	
	
	public final boolean comparePassword (final String password){
		return MessageDigest.isEqual(this.getPassword().getBytes(), password.getBytes());
	}
	
	public abstract String getPassword ();
	
	public abstract boolean isOwnerOf (final GWXObject resource);
	
	public abstract boolean isPrivileged ();
}