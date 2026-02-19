/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface GWXTransactionCallbacks {
	void onComplete ();
	
	void onFailure (final String errorMessage);
	
	void onSuccess (final JSOXVariant data, final Set<JSOXVariant> returns);
}