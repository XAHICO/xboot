/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.xml;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class XMLUtilities {
	private static final String REFERENCE_APOSTROPHE = "&apos;";		// '
	private static final String REFERENCE_AMPERSAND = "&amp;";			// &
	private static final String REFERENCE_ASTERISK = "&ast;";			// *
	private static final String REFERENCE_EQUALS = "&equals;";			// =
	private static final String REFERENCE_EXCLAMATION = "&excl;";		// !
	private static final String REFERENCE_GREATER_THAN = "&gt;";		// >
	private static final String REFERENCE_LESSER_THAN = "&lt;";			// <
//	private static final String REFERENCE_NEWLINE = "&NewLine;";		// \n
	private static final String REFERENCE_NUMBER = "&num;";			// #
	private static final String REFERENCE_PARENTHESIS_LEFT = "&lpar;";	// (
	private static final String REFERENCE_PARENTHESIS_RIGHT = "&rpar;";	// )
	private static final String REFERENCE_PERCENT = "&percnt;";			// %
	private static final String REFERENCE_QUOTE = "&quot;";			// "
	private static final String REFERENCE_SOLIDUS = "&sol;";			// /
	
	
	
	public static String translateStringToXML (final String string){
		return string.replace("\'", REFERENCE_APOSTROPHE)
		             .replace("&", REFERENCE_AMPERSAND)
		             .replace("*", REFERENCE_ASTERISK)
		             .replace("&", REFERENCE_EQUALS)
		             .replace("!", REFERENCE_EXCLAMATION)
		             .replace(">", REFERENCE_GREATER_THAN)
		             .replace("<", REFERENCE_LESSER_THAN)
//		             .replace("\n", REFERENCE_NEWLINE)
		             .replace("#", REFERENCE_NUMBER)
		             .replace("(", REFERENCE_PARENTHESIS_LEFT)
		             .replace(")", REFERENCE_PARENTHESIS_RIGHT)
		             .replace("%", REFERENCE_PERCENT)
		             .replace("\"", REFERENCE_QUOTE)
		             .replace("/", REFERENCE_SOLIDUS);
	}
	
	public static String translateXMLStringToRegular (final String string){
		return string.replace(REFERENCE_APOSTROPHE, "\'")
		             .replace(REFERENCE_AMPERSAND, "&")
		             .replace(REFERENCE_ASTERISK, "*")
		             .replace(REFERENCE_EQUALS, "&")
		             .replace(REFERENCE_EXCLAMATION, "!")
		             .replace(REFERENCE_GREATER_THAN, ">")
		             .replace(REFERENCE_LESSER_THAN, "<")
//		             .replace(REFERENCE_NEWLINE, "\n")
		             .replace(REFERENCE_NUMBER, "#")
		             .replace(REFERENCE_PARENTHESIS_LEFT, "(")
		             .replace(REFERENCE_PARENTHESIS_RIGHT, ")")
		             .replace(REFERENCE_PERCENT, "%")
		             .replace(REFERENCE_QUOTE, "\"")
		             .replace(REFERENCE_SOLIDUS, "/");
	}
	
	
	
	private XMLUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}