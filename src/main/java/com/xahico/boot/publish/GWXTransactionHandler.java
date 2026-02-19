/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface GWXTransactionHandler {
	void onError (final Throwable cause);

	void onFinalize (final boolean status);

	void onRequest (final JSOXVariant request);

	void onResponse (final JSOXVariant response, final boolean streams);

	void onStream (final JSOXVariant object);

	void onStreamClose ();

	void onStreamOpen ();
}