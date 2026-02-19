/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class BARETransactRequest extends JSOXObject {
	public String      control;
	public JSOXVariant request;
	public String      transactionId;
}